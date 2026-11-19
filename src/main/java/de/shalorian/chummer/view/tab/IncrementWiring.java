package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.util.List;
import java.util.function.Supplier;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Verdrahtet die +/−-Zeilen der Tabs (Attribute, Fertigkeiten, Wissensfertigkeiten, Kontakte, Komplexe Formen)
 * einheitlich: Anzeige-Refresher, Klick-Handler, BP- bzw. Karma-Prüfung samt Bestätigung im Karrieremodus. Nach
 * jeder Änderung meldet sie über den {@link CharacterTabHost}, dass Zeilen, BP oder Log neu zu bewerten sind.
 *
 * <p>Ein gesperrter +-Knopf bleibt bedienbar, trägt aber die Pseudoklasse {@code :blocked} (sieht deaktiviert aus) und
 * den Grund als Tooltip; ein Klick zeigt den Grund als Hinweis. Deaktivierte Knöpfe zeigen in JavaFX keinen Tooltip,
 * deshalb nicht {@code setDisable}. Der −-Knopf wird am Mindestwert weiter deaktiviert, dort braucht es keinen Grund.
 */
public final class IncrementWiring {

  private static final String KARMA_SUFFIX = " Karma";
  private static final PseudoClass BLOCKED = PseudoClass.getPseudoClass("blocked");

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
    if (character.isGmMode()) {
      wireGm(refresherList, controls, model);
    } else if (career) {
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
    Button plus = controls.plus();
    Supplier<@Nullable String> problem = () -> {
      if (model.value() >= model.max()) {
        return maxText(model);
      }
      int karmaCost = model.karmaStepCost();
      return character.getKarmaTotal() < karmaCost
          ? "Nicht genug Karma (%d nötig, %d vorhanden)".formatted(karmaCost, character.getKarmaTotal())
          : model.incrementProblem();
    };

    controls.minus().setVisible(false);
    Runnable refresh = () -> {
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(model.value() >= model.max() ? "–" : model.karmaStepCost() + KARMA_SUFFIX);
      block(plus, problem.get());
    };
    refresherList.add(refresh);

    plus.setOnAction(_ -> {
      if (warnIfBlocked(problem.get())) {
        return;
      }
      int val = model.value();
      if (!host.services().karmaChargeFailed(character, category, model.actionLabel(val, val + 1),
          model.karmaStepCost())) {
        model.increment();
        host.refreshRows();
      }
    });
    refresh.run();
  }

  /**
   * Spielleiter-Modus (ADR-060): Steigern und Senken ohne Kosten, Bestätigung und Buchung, in beiden Modi; es gelten nur
   * Höchstwert und {@link RowModel#incrementProblem()} (die Sperren dort hebt der Charakter im Spielleiter-Modus auf).
   * BP zählen bei der Erschaffung weiter, deshalb auch {@code onBpChanged}.
   */
  private void wireGm(@NonNull List<Runnable> refresherList, @NonNull RowControls controls,
      @NonNull RowModel model) {
    wireWithoutCost(refresherList, controls, model, "GM");
  }

  private void wireCreationIncrementable(@NonNull List<Runnable> refresherList,
      @NonNull RowControls controls, @NonNull RowModel model, @NonNull Character character) {
    Label valueLabel = controls.valueLabel();
    Label costLabel = controls.costLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();
    Supplier<@Nullable String> problem = () -> {
      if (model.value() >= model.max()) {
        return maxText(model);
      }
      String bpProblem = character.getBpShortfallProblem(model.bpStepCost());
      return bpProblem != null ? bpProblem : model.incrementProblem();
    };

    Runnable refresh = () -> {
      valueLabel.setText(model.valueDisplayText());
      costLabel.setText(model.bpDisplayText());
      minus.setDisable(model.value() <= model.min());
      block(plus, problem.get());
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
      if (!warnIfBlocked(problem.get())) {
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
    wireWithoutCost(refresherList, controls, model, "");
  }

  /**
   * Gemeinsamer Baustein für {@link #wireFree} und den Spielleiter-Modus: beide Richtungen ohne Kosten, nur Höchstwert,
   * Mindestwert und {@link RowModel#incrementProblem()} gelten; {@code costText} steht in der Kostenspalte.
   */
  private void wireWithoutCost(@NonNull List<Runnable> refresherList, @NonNull RowControls controls,
      @NonNull RowModel model, @NonNull String costText) {
    Label valueLabel = controls.valueLabel();
    Button minus = controls.minus();
    Button plus = controls.plus();
    Supplier<@Nullable String> problem = () -> model.value() >= model.max() ? maxText(model)
        : model.incrementProblem();

    Runnable refresh = () -> {
      valueLabel.setText(model.valueDisplayText());
      controls.costLabel().setText(costText);
      minus.setVisible(true);
      minus.setDisable(model.value() <= model.min());
      block(plus, problem.get());
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
      if (!warnIfBlocked(problem.get())) {
        model.increment();
        host.refreshRows();
        host.onBpChanged();
      }
    });
    refresh.run();
  }

  /**
   * Sperrt eine Zeile dauerhaft mit {@code reason}, z.B. eine Fertigkeit einer noch gekoppelten Gruppe: + ist gesperrt
   * mit Tooltip und zeigt beim Klick den Grund, − bleibt deaktiviert. Den Wert setzt der Aufrufer.
   */
  public void wireLocked(@NonNull RowControls controls, @NonNull String reason) {
    controls.minus().setDisable(true);
    block(controls.plus(), reason);
    controls.plus().setOnAction(_ -> warnIfBlocked(reason));
  }

  private static @NonNull String maxText(@NonNull RowModel model) {
    return "Höchstwert %d erreicht".formatted(model.max());
  }

  /** Setzt bzw. löst die Sperre: Pseudoklasse {@code :blocked} und der Grund als Tooltip. */
  private static void block(@NonNull Button plus, @Nullable String reason) {
    plus.pseudoClassStateChanged(BLOCKED, reason != null);
    plus.setTooltip(reason == null ? null : new Tooltip(reason));
  }

  /** Zeigt {@code reason} als Hinweis; {@code true}, wenn gesperrt war und nichts passieren soll. */
  private boolean warnIfBlocked(@Nullable String reason) {
    if (reason == null) {
      return false;
    }
    host.services().warn("Nicht möglich", reason);
    return true;
  }
}
