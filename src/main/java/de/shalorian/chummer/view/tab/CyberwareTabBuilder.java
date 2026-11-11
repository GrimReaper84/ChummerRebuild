package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.dialog.CyberwareSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Cyberware-Tab: Essenz-Übersicht, eingebaute Cyber- und Bioware, Einbau über den Auswahldialog und Ausbau.
 * Änderungen an Implantaten meldet er über {@link CharacterTabHost#onImplantsChanged()}, weil Essenz, MAG/RES und
 * Restgeld davon abhängen.
 */
public final class CyberwareTabBuilder {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();

  private final @NonNull VBox cyberwareContainer;
  private final @NonNull CharacterTabHost host;
  private @Nullable Character character;

  public CyberwareTabBuilder(@NonNull VBox cyberwareContainer, @NonNull CharacterTabHost host) {
    this.cyberwareContainer = cyberwareContainer;
    this.host = host;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /**
   * Baut den (immer sichtbaren) Cyberware-&amp;-Bioware-Tab: Kopfzeile mit Essenz-Übersicht (Gesamt-Essenz sowie
   * getrennte Cyberware-/Bioware-Verluste, wie vom GRW zum Notieren empfohlen) und zwei Sektionen mit je eigenem
   * Hinzufügen-Button — analog {@code QualityTabBuilder.build()}. Wird bei jeder Implantat-Änderung komplett neu
   * aufgebaut (kein In-Place-Refresh einzelner Zellen).
   */
  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    cyberwareContainer.getChildren().clear();

    Label sectionLabel = new Label("Cyberware & Bioware");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);

    Label essenceLabel = new Label("Essenz: " + ESSENCE_FORMAT.toString(ch.getEssence()));
    essenceLabel.getStyleClass().add(STYLE_BP_VALUE);
    Label lossLabel = new Label("(Cyberware " + ESSENCE_FORMAT.toString(ch.getCyberwareEssenceLoss())
        + " / Bioware " + ESSENCE_FORMAT.toString(ch.getBiowareEssenceLoss()) + ")");
    lossLabel.getStyleClass().add(STYLE_BP_LABEL);
    Label costLabel =
        new Label("Kosten: " + SpendCurrency.NUYEN.formatAmount(ch.getEquipment().getCyberwareNuyenCost()));
    costLabel.getStyleClass().add(STYLE_BP_LABEL);

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().addAll(sectionLabel, essenceLabel, lossLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(spacer, costLabel);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    cyberwareContainer.getChildren().add(header);

    buildCyberwareSection(ch, EquipmentKind.CYBERWARE);

    Separator sep = new Separator();
    VBox.setMargin(sep, new Insets(16, 0, 16, 0));
    cyberwareContainer.getChildren().add(sep);

    buildCyberwareSection(ch, EquipmentKind.BIOWARE);
  }

  private void buildCyberwareSection(@NonNull Character ch, @NonNull EquipmentKind kind) {
    Label title = new Label(kind.getDisplayName());
    title.getStyleClass().add(STYLE_SECTION_TITLE);

    Button addBtn = new Button(kind.getDisplayName() + " hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddCyberware(kind));

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(title, spacer, addBtn);
    cyberwareContainer.getChildren().add(header);

    List<Cyberware> implants = ch.getEquipment().getCyberware().stream()
        .filter(c -> c.getKind() == kind)
        .toList();
    if (implants.isEmpty()) {
      Label empty = new Label("Keine " + kind.getDisplayName() + " eingebaut");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      cyberwareContainer.getChildren().add(empty);
      return;
    }

    MigPane grid = buildCyberwareGrid();
    int row = 1;
    for (Cyberware implant : implants) {
      addCyberwareRow(grid, row++, implant, ch);
    }
    cyberwareContainer.getChildren().add(withRightFiller(grid));
  }

  /**
   * Implantat-Grid inkl. Kopfzeile (Zeile 0), inhaltsbasierte Spalten analog {@code MagicTabBuilder.buildSpellGrid()}.
   */
  private @NonNull MigPane buildCyberwareGrid() {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][][center][right][right][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));

    String[] headers = {"Implantat", "Bereich", "Kategorie", "Stufe", "Essenz", "Preis", "Quelle",
        ""};
    addHeaderRow(grid, headers);
    return grid;
  }

  private void addCyberwareRow(@NonNull MigPane grid, int row, @NonNull Cyberware implant,
      @NonNull Character character) {
    Label implantNameLabel = new Label(implant.getName());
    Label categoryLabel = new Label(implant.getData().category());
    Label gradeLabel = new Label(implant.getGrade().getDisplayName());
    Label ratingLabel = new Label(implant.getData().hasRating()
        ? String.valueOf(implant.getRating())
        : "–");
    Label essenceLabel = new Label(ESSENCE_FORMAT.toString(implant.getEssenceCost().doubleValue()));
    Label priceLabel = new Label(SpendCurrency.NUYEN.formatAmount(implant.getNuyenCost()));
    Label sourceLabel = new Label(implant.getData().source() + " S. " + implant.getData().page());
    Button removeBtn = new Button("Entfernen");

    implantNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    categoryLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    gradeLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    ratingLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    essenceLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    priceLabel.getStyleClass().add(STYLE_ATTR_BP);
    sourceLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(removeBtn, FontAwesomeSolid.TRASH);

    removeBtn.setOnAction(_ -> {
      character.getEquipment().removeCyberware(implant);
      onCyberwareChanged();
    });

    addRowCells(grid, row, implantNameLabel, categoryLabel, gradeLabel, ratingLabel, essenceLabel,
        priceLabel, sourceLabel, removeBtn);
  }

  /**
   * Öffnet den Implantat-Auswahl-Dialog für Cyberware bzw. Bioware und baut die Auswahl ein. Fällt die Essenz während der Charaktererschaffung
   * dadurch auf 0 oder darunter, erscheint nur eine Warnung (keine Sperre, siehe {@link Character#isEssenceBelowCreationMinimum()}). Bei der
   * Erschaffung zählt der Preis gegen das Ressourcengeld ({@link Character#getRemainingNuyen()}), im Karrieremodus wird er nach Bestätigung vom
   * Nuyen-Guthaben abgebucht ({@link TabServices#nuyenChargeFailed}).
   */
  private void onAddCyberware(@NonNull EquipmentKind kind) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("CyberwareSelectDialog.fxml",
          kind.getDisplayName() + " hinzufügen");
      CyberwareSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setKind(kind);
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(implant -> {
        if (ch.isCreated()
            && host.services().nuyenChargeFailed(ch, "Kauf: " + cyberwareLabel(implant),
                implant.getNuyenCost())) {
          return;
        }
        ch.getEquipment().addCyberware(implant);
        onCyberwareChanged();
        if (ch.isEssenceBelowCreationMinimum()) {
          host.services().warn("Essenz zu niedrig",
              "Bei der Charaktererschaffung darf die Essenz nicht auf 0 oder darunter fallen");
        }
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /**
   * Zieht alle essenzabhängigen Anzeigen nach einer Implantat-Änderung nach: den Cyberware-Tab selbst, das Attribut-Grid (Essenz-Zeile, wirksame
   * Magie/Resonanz inkl. Maximum im Bereichs-Label) und die übrigen Zeilen (z.B. Komplexe Formen, deren Maximum am wirksamen Resonanzwert hängt).
   */
  private void onCyberwareChanged() {
    build();
    host.onImplantsChanged();
  }

  /**
   * Bezeichnung eines Implantats für Log-Einträge, z.B. "Dermalpanzerung (Stufe 2, Alphaware)".
   */
  private static @NonNull String cyberwareLabel(@NonNull Cyberware implant) {
    String details = implant.getGrade().getDisplayName();
    if (implant.getData().hasRating()) {
      details = "Stufe " + implant.getRating() + ", " + details;
    }
    return implant.getName() + " (" + details + ")";
  }
}
