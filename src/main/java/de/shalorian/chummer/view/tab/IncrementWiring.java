package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

/**
 * Verdrahtet die +/−-Zeilen der Tabs (Attribute, Fertigkeiten, Wissensfertigkeiten, Kontakte, Komplexe Formen)
 * einheitlich: Anzeige-Refresher, Klick-Handler, BP- bzw. Karma-Prüfung samt Bestätigung im Karrieremodus. Nach
 * jeder Änderung meldet sie über den {@link CharacterTabHost}, dass Zeilen, BP oder Log neu zu bewerten sind.
 */
public final class IncrementWiring {

  private static final String KARMA_SUFFIX = " Karma";

  private final @NonNull CharacterTabHost host;

  public IncrementWiring(@NonNull CharacterTabHost host) {
    this.host = host;
  }

  /**
   * Verdrahtet eine +/−-Zeile (Refresh-Runnable, Klick-Handler, BP- bzw. Karma-Kostenprüfung inkl. Bestätigungsdialog im Karrieremodus) für
   * {@code AttributeTabBuilder.addAttributeRow}, {@code SkillTabBuilder.addGroupHeaderRow} und
   * {@code SkillTabBuilder.addSkillRow} einheitlich. Das Ausblenden von {@code minus} im Karrieremodus verlässt sich
   * auf {@code hidemode 3} des umgebenden {@code MigPane} — ein zusätzliches {@code setManaged(false)} ist dafür
   * nicht nötig.
   */
  public void wire(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, boolean career, @NonNull RowModel model,
      @NonNull Character character, @NonNull LogCategory category) {
    if (career) {
      wireCareerIncrementable(refresherList, controls, model, character, category);
    } else {
      wireCreationIncrementable(refresherList, controls, model, character);
    }
  }

  private void wireCareerIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model, @NonNull Character character,
      @NonNull LogCategory category) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    minus.setVisible(false);
    Runnable refresh = () -> {
      int val = model.value();
      int karmaCost = model.karmaStepCost();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(val >= model.max() ? "–" : karmaCost + KARMA_SUFFIX);
      plus.setDisable(val >= model.max() || character.getKarmaTotal() < karmaCost
          || !model.canIncrement());
    };
    refresherList.add(refresh);

    plus.setOnAction(_ -> {
      int val = model.value();
      int karmaCost = model.karmaStepCost();
      String label = model.actionLabel(val, val + 1);
      if (val < model.max() && character.getKarmaTotal() >= karmaCost && model.canIncrement()
          && host.services().confirmKarmaSpend(label, karmaCost, character)) {
        host.services().bookKarma(character, category, label, -karmaCost);
        model.increment();
        host.refreshRows();
      }
    });

    refresh.run();
  }

  private void wireCreationIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model, @NonNull Character character) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    Runnable refresh = () -> {
      int val = model.value();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(model.bpDisplayText());
      minus.setDisable(val <= model.min());
      plus.setDisable(val >= model.max() || character.getRemainingBP() < model.bpStepCost()
          || !model.canIncrement());
    };
    refresherList.add(refresh);

    minus.setOnAction(_ -> {
      if (model.value() > model.min()) {
        model.decrement();
        host.refreshRows();
        host.onBpChanged();
      }
    });
    plus.setOnAction(_ -> {
      if (model.value() < model.max() && character.getRemainingBP() >= model.bpStepCost()
          && model.canIncrement()) {
        model.increment();
        host.refreshRows();
        host.onBpChanged();
      }
    });

    refresh.run();
  }

  /**
   * Verdrahtet eine +/−-Zeile ohne jegliche BP-/Karma-Kosten und ohne Bestätigung — für Werte, die in beiden Richtungen jederzeit frei änderbar
   * bleiben sollen. Aktuell nur für Kontakt-Ratings im Karrieremodus genutzt (siehe {@code docs/kontakte_plan.md}: das SR4-Grundregelwerk kennt
   * keinen Karma-Kauf für Connections). Bewusst getrennt von {@link #wireCareerIncrementable}, das strukturell einen Karma-Preis voraussetzt und das
   * Minus-Control ausblendet — beides passt hier nicht.
   */
  public void wireFree(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();

    Runnable refresh = () -> {
      int val = model.value();
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText("");
      minus.setDisable(val <= model.min());
      plus.setDisable(val >= model.max());
    };
    refresherList.add(refresh);

    minus.setOnAction(_ -> {
      if (model.value() > model.min()) {
        model.decrement();
        host.refreshRows();
      }
    });
    plus.setOnAction(_ -> {
      if (model.value() < model.max()) {
        model.increment();
        host.refreshRows();
      }
    });

    refresh.run();
  }
}
