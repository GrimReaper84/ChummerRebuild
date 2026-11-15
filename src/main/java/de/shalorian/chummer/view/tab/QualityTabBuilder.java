package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.preventShrink;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.QualitySelectDialogController;
import de.shalorian.chummer.view.dialog.QualitySelectResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Tab Gaben &amp; Handicaps: beide Listen mit BP-/Karma-Kosten, Hinzufügen über den Auswahldialog und Entfernen
 * (mit Rückfrage, wenn dadurch Fertigkeiten gesperrt würden). Jede Änderung meldet er über
 * {@link CharacterTabHost#onQualitiesChanged()}, weil Attribute, Fertigkeiten, Cyberware und die Sichtbarkeit der
 * Magie-/Technomancer-Tabs davon abhängen.
 */
public final class QualityTabBuilder {

  private final @NonNull VBox qualitiesContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull List<Runnable> qualityRefreshers = new ArrayList<>();
  private @Nullable Character character;

  public QualityTabBuilder(@NonNull VBox qualitiesContainer, @NonNull CharacterTabHost host) {
    this.qualitiesContainer = qualitiesContainer;
    this.host = host;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Bewertet die +/−-Zeilen dieses Tabs neu (Restpunkte, Karma, Caps). */
  public void refresh() {
    qualityRefreshers.forEach(Runnable::run);
  }

  public void build() {
    if (character == null) {
      return;
    }
    qualitiesContainer.getChildren().clear();
    qualityRefreshers.clear();

    boolean career = character.isCreated();

    buildQualitySection("Gaben", QualityType.POSITIVE, career);

    Separator sep = new Separator();
    VBox.setMargin(sep, new Insets(16, 0, 16, 0));
    qualitiesContainer.getChildren().add(sep);

    buildQualitySection("Handicaps", QualityType.NEGATIVE, career);
  }

  private void buildQualitySection(
      @NonNull String title, @NonNull QualityType type, boolean career) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }

    qualitiesContainer.getChildren().add(buildQualitySectionHeader(title, type, career, ch));

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(4);

    // Keine festen Breiten und kein eigenes Spaltenwachstum: alle vier Spalten bleiben bei ihrer
    // Inhaltsbreite (siehe preventShrink in addQualityRow); ungenutzter Platz wird außen über
    // withRightFiller verteilt.
    grid.getColumnConstraints()
        .addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(),
            new ColumnConstraints());

    List<CharacterQuality> section = ch.getQualities().stream()
        .filter(q -> q.data().type() == type)
        .toList();

    int row = 0;
    for (CharacterQuality q : section) {
      addQualityRow(grid, row++, q);
    }

    if (row == 0) {
      Label empty = new Label("Keine " + title + " gewählt");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      grid.add(empty, 0, 0);
    }

    qualitiesContainer.getChildren().add(withRightFiller(grid));
  }

  private @NonNull HBox buildQualitySectionHeader(@NonNull String title,
      @NonNull QualityType type, boolean career, @NonNull Character ch) {
    Label sectionLabel = new Label(title);
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    Label limitLabel = new Label();
    limitLabel.getStyleClass().add(STYLE_BP_LABEL);

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddQuality(type));

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(sectionLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    if (!career) {
      header.getChildren().add(limitLabel);
    }
    header.getChildren().add(addBtn);
    VBox.setMargin(header, new Insets(0, 0, 6, 0));

    if (!career) {
      Runnable limitRefresh = () -> {
        int used = type == QualityType.POSITIVE
            ? ch.getPositiveQualityBP()
            : ch.getNegativeQualityBP();
        limitLabel.setText(used + " / " + Character.MAX_POSITIVE_QUALITY_BP + " BP");
      };
      qualityRefreshers.add(limitRefresh);
      limitRefresh.run();
    }

    return header;
  }

  private void addQualityRow(@NonNull GridPane grid, int row, @NonNull CharacterQuality q) {
    boolean isMetatype = q.source() == QualitySource.METATYPE;

    Label qualityNameLabel = new Label(q.displayName());
    qualityNameLabel.getStyleClass().add(isMetatype ? "quality-metatype" : STYLE_ATTR_NAME);
    if (!q.data().note().isEmpty()) {
      String page = q.data().page() > 0 ? "GRW S. %d: ".formatted(q.data().page()) : "";
      qualityNameLabel.setTooltip(new Tooltip(page + q.data().note()));
    }

    Label costLabel = new Label();
    costLabel.getStyleClass().add(STYLE_ATTR_BP);
    if (q.gratis()) {
      costLabel.setText("Gratis");
    } else {
      costLabel.setText(Math.abs(q.data().bp()) + " BP");
    }

    Label sourceLabel = new Label(isMetatype ? "(Metatyp)" : "");
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);

    preventShrink(qualityNameLabel);
    preventShrink(costLabel);
    preventShrink(sourceLabel);
    grid.add(qualityNameLabel, 0, row);
    grid.add(costLabel, 1, row);
    grid.add(sourceLabel, 2, row);

    if (!isMetatype) {
      Button removeBtn = new Button("Entfernen");
      removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
      useIcon(removeBtn, FontAwesomeSolid.TRASH);
      removeBtn.setOnAction(_ -> onRemoveQuality(q));
      preventShrink(removeBtn);
      grid.add(removeBtn, 3, row);
    }
  }

  /**
   * Entfernt eine gewählte Quality. Handicaps im Karrieremodus loswerden kostet die doppelten GP-Kosten in Karma (siehe
   * {@link Character#getQualityKarmaCost}); alle anderen Fälle (Erstellungsmodus, Gaben, Gratis-Qualities) bleiben unverändert kostenlos.
   */
  private void onRemoveQuality(@NonNull CharacterQuality q) {
    Character c = this.character;
    if (c == null) {
      return;
    }
    String qualityName = q.displayName();
    if (!confirmRemovalConsequences(c, q)) {
      return;
    }
    if (c.isCreated() && q.data().type() == QualityType.NEGATIVE && !q.gratis()) {
      int karmaCost = c.getQualityKarmaCost(q.data());
      if (host.services().karmaChargeFailed(c, LogCategory.QUALITY, "%s loswerden".formatted(qualityName),
          karmaCost)) {
        return;
      }
    }
    c.removeQuality(q);
    host.onQualitiesChanged();
  }

  /**
   * Fragt per Ja/Nein-Dialog nach, wenn das Entfernen mehr bewirkt als den Wegfall der Quality: mit entfernte Gaben,
   * nicht mehr erlernbare Fertigkeiten und gesunkene Maxima (siehe {@link
   * Character#getQualityRemovalConsequences(CharacterQuality)}). Ohne solche Folgen keine Abfrage.
   *
   * @return true, wenn entfernt werden darf
   */
  private boolean confirmRemovalConsequences(@NonNull Character c, @NonNull CharacterQuality q) {
    List<String> consequences = c.getQualityRemovalConsequences(q);
    if (consequences.isEmpty()) {
      return true;
    }
    String list = consequences.stream().map(line -> "• " + line).collect(Collectors.joining("\n"));
    return host.services().confirmYesNo("Quality entfernen", "\"%s\" wirklich entfernen?".formatted(q.displayName()),
        "Dabei ändert sich außerdem:\n" + list);
  }

  private void onAddQuality(@NonNull QualityType filter) {
    openQualitySelectDialog(filter);
  }

  private void openQualitySelectDialog(@NonNull QualityType filter) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean again = true;
    while (again) {
      again = openQualitySelectDialogOnce(ch, filter);
    }
  }

  /**
   * Öffnet den Quality-Auswahl-Dialog einmal und wertet das Ergebnis aus.
   *
   * @return ob der Dialog erneut geöffnet werden soll ({@code r.addAnother()})
   */
  private boolean openQualitySelectDialogOnce(@NonNull Character ch,
      @NonNull QualityType filter) {
    try {
      DialogHandle dialog = host.services().openDialog("QualitySelectDialog.fxml", "Quality hinzufügen");
      QualitySelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      ctrl.setFilter(filter);
      dialog.stage().showAndWait();

      Optional<QualitySelectResult> result = ctrl.getResult();
      if (result.isEmpty()) {
        return false;
      }
      QualitySelectResult r = result.get();
      tryAddQuality(ch, r);
      host.onQualitiesChanged();
      return r.addAnother();
    } catch (IOException _) {
      host.services().showDialogLoadError();
      return false;
    }
  }

  private void tryAddQuality(@NonNull Character ch, @NonNull QualitySelectResult r) {
    // Erst prüfen, dann Karma abbuchen, damit bei einer Ablehnung kein Karma verloren geht.
    String problem = ch.getQualityAddProblem(r.data(), r.gratis(), r.choice());
    if (problem != null) {
      host.services().warn("Nicht wählbar", problem);
      return;
    }
    // Eine Gabe im Karrieremodus erwerben kostet die doppelten GP-Kosten in Karma.
    if (ch.isCreated() && r.data().type() == QualityType.POSITIVE && !r.gratis()) {
      int karmaCost = ch.getQualityKarmaCost(r.data());
      if (host.services().karmaChargeFailed(ch, LogCategory.QUALITY, "%s erwerben".formatted(r.data().name()),
          karmaCost)) {
        return;
      }
    }
    ch.addQuality(r.data(), r.gratis(), r.choice());
  }
}
