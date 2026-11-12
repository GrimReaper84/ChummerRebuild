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
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.cyberware.ImplantEffect;
import de.shalorian.chummer.model.character.gear.CommlinkStats;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.dialog.CyberwareSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.GearSelectDialogController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Cyberware-Tab: Essenz-Übersicht, eingebaute Cyber- und Bioware samt Wirkung, Einbau über den Auswahldialog und
 * Ausbau. Unter einem Basissystem (Cyberaugen, Cyberohren) steht sein Zubehör eingerückt, unter der Kommlink-Headware
 * das eingebaute Kommlink.
 * Änderungen an Implantaten meldet er über {@link CharacterTabHost#onImplantsChanged()}, weil Essenz, MAG/RES und
 * Restgeld davon abhängen.
 */
public final class CyberwareTabBuilder {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  /** Einrückung für eingebautes Zubehör und Kommlink unter ihrem Implantat. */
  private static final String CHILD_PREFIX = "   ↳ ";

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
      for (Cyberware accessory : implant.getInstalled()) {
        addAccessoryRow(grid, row++, implant, accessory);
      }
      Gear commlink = implant.getCommlink();
      if (commlink != null) {
        addCommlinkRow(grid, row++, implant, commlink);
      }
    }
    cyberwareContainer.getChildren().add(withRightFiller(grid));
  }

  /**
   * Implantat-Grid inkl. Kopfzeile (Zeile 0), inhaltsbasierte Spalten analog {@code MagicTabBuilder.buildSpellGrid()}.
   */
  private @NonNull MigPane buildCyberwareGrid() {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][][center][right][right][][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));

    String[] headers = {"Implantat", "Bereich", "Kategorie", "Stufe", "Essenz", "Preis", "Wirkung", "Quelle", ""};
    addHeaderRow(grid, headers);
    return grid;
  }

  private void addCyberwareRow(@NonNull MigPane grid, int row, @NonNull Cyberware implant,
      @NonNull Character character) {
    Label implantNameLabel = styled(new Label(implant.getDisplayName()), STYLE_ATTR_NAME);
    String note = implant.getData().note();
    if (!note.isEmpty()) {
      implantNameLabel.setTooltip(new Tooltip(note));
    }
    Label categoryLabel = styled(new Label(implant.getData().category()), STYLE_ATTR_RANGE);
    Label gradeLabel = styled(new Label(implant.getGrade().getDisplayName()), STYLE_ATTR_ABBR);
    Label ratingLabel = styled(new Label(implant.getData().hasRating()
        ? String.valueOf(implant.getRating())
        : "–"), STYLE_ATTR_VALUE);
    Label essenceLabel = styled(new Label(ESSENCE_FORMAT.toString(implant.getEssenceCost().doubleValue())),
        STYLE_ATTR_VALUE);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(implant.getOwnNuyenCost())),
        STYLE_ATTR_BP);
    Label effectLabel = styled(new Label(effectText(implant)), STYLE_ATTR_RANGE);
    Label sourceLabel = styled(new Label(sourceText(implant)), STYLE_ATTR_RANGE);

    HBox actions = new HBox(6);
    actions.setAlignment(Pos.CENTER_LEFT);
    if (implant.getData().hasCapacity()) {
      Button installBtn = actionButton("Zubehör", FontAwesomeSolid.PLUS);
      installBtn.setDisable(implant.getUsedCapacity() >= implant.getCapacity());
      installBtn.setOnAction(_ -> onInstallAccessory(implant));
      actions.getChildren().add(installBtn);
    }
    if (implant.getData().acceptedGear() != null && implant.getCommlink() == null) {
      Button commlinkBtn = actionButton("Kommlink", FontAwesomeSolid.PLUS);
      commlinkBtn.setOnAction(_ -> onInstallCommlink(implant));
      actions.getChildren().add(commlinkBtn);
    }
    Button removeBtn = actionButton("Entfernen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      character.getEquipment().removeCyberware(implant);
      onCyberwareChanged();
    });
    actions.getChildren().add(removeBtn);

    addRowCells(grid, row, implantNameLabel, categoryLabel, gradeLabel, ratingLabel, essenceLabel,
        priceLabel, effectLabel, sourceLabel, actions);
  }

  /** Eingebautes Zubehör eines Basissystems: eingerückt, Kapazität statt Essenz, Ausbau. */
  private void addAccessoryRow(@NonNull MigPane grid, int row, @NonNull Cyberware base,
      @NonNull Cyberware accessory) {
    Label nameLabel = styled(new Label(CHILD_PREFIX + accessory.getDisplayName()), STYLE_ATTR_NAME);
    String note = accessory.getData().note();
    if (!note.isEmpty()) {
      nameLabel.setTooltip(new Tooltip(note));
    }
    Label ratingLabel = styled(new Label(accessory.getData().hasRating()
        ? String.valueOf(accessory.getRating())
        : "–"), STYLE_ATTR_VALUE);
    Label capacityLabel = styled(new Label("Kap. " + accessory.getData().capacityCostFor(accessory.getRating())),
        STYLE_ATTR_VALUE);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(accessory.getNuyenCost())), STYLE_ATTR_BP);
    Button removeBtn = actionButton("Ausbauen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      base.uninstall(accessory);
      onCyberwareChanged();
    });
    addRowCells(grid, row, nameLabel, new Label(), new Label(), ratingLabel, capacityLabel, priceLabel,
        new Label(), styled(new Label(sourceText(accessory)), STYLE_ATTR_RANGE), removeBtn);
  }

  /** Eingebautes Kommlink der Kommlink-Headware samt Betriebssystem. */
  private void addCommlinkRow(@NonNull MigPane grid, int row, @NonNull Cyberware headware, @NonNull Gear commlink) {
    String os = commlink.getInstalled().isEmpty() ? "ohne Betriebssystem"
        : commlink.getInstalled().getFirst().getName();
    Label nameLabel = styled(new Label(CHILD_PREFIX + commlink.getName() + " (" + os + ")"), STYLE_ATTR_NAME);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(commlink.getNuyenCost())), STYLE_ATTR_BP);
    Label statsLabel = styled(new Label(commlinkStatsText(commlink)), STYLE_ATTR_RANGE);
    Label sourceLabel = styled(new Label(commlink.getData().source() + " S. " + commlink.getData().page()),
        STYLE_ATTR_RANGE);

    HBox actions = new HBox(6);
    actions.setAlignment(Pos.CENTER_LEFT);
    if (commlink.getInstalled().isEmpty()) {
      Button osBtn = actionButton("Betriebssystem", FontAwesomeSolid.PLUS);
      osBtn.setOnAction(_ -> onInstallOperatingSystem(commlink));
      actions.getChildren().add(osBtn);
    }
    Button removeBtn = actionButton("Ausbauen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      headware.removeCommlink();
      onCyberwareChanged();
    });
    actions.getChildren().add(removeBtn);
    addRowCells(grid, row, nameLabel, new Label(), new Label(), new Label(), new Label(), priceLabel,
        statsLabel, sourceLabel, actions);
  }

  /** Matrixwerte des eingebauten Kommlinks wie im Ausrüstungs-Detail (GRW S. 260, 374); ohne Betriebssystem „–“. */
  private static @NonNull String commlinkStatsText(@NonNull Gear commlink) {
    CommlinkStats stats = commlink.getCommlinkStats();
    if (stats == null) {
      return "";
    }
    return "Prozessor %d, Signal %d, Firewall %s, System %s".formatted(stats.processor(), stats.signal(),
        orNoValue(stats.firewall()), orNoValue(stats.system()));
  }

  private static @NonNull String orNoValue(int value) {
    return value == 0 ? "–" : String.valueOf(value);
  }

  private static @NonNull Label styled(@NonNull Label label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }

  private static @NonNull Button actionButton(@NonNull String text, @NonNull FontAwesomeSolid icon) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    return button;
  }

  private static @NonNull String sourceText(@NonNull Cyberware implant) {
    return implant.getData().source() + " S. " + implant.getData().page();
  }

  /**
   * Kurzanzeige der rechnenden Wirkungen, z.B. {@code "STR +2, GES +2"} oder {@code "Durchgänge +1"}; bei
   * Basissystemen die belegte Kapazität, bei Implantaten ohne Wirkung leer.
   */
  private static @NonNull String effectText(@NonNull Cyberware implant) {
    List<String> parts = new ArrayList<>();
    if (implant.getData().hasCapacity()) {
      parts.add("Kapazität " + implant.getUsedCapacity() + "/" + implant.getCapacity());
    }
    if (implant.getData().acceptedGear() != null && implant.getCommlink() == null) {
      parts.add("kein Kommlink eingebaut");
    }
    for (ImplantEffect effect : implant.getData().effects()) {
      int value = effect.value(implant.getRating());
      parts.add(switch (effect.type()) {
        case ATTRIBUTE -> AttributeType.valueOf(effect.target()).getAbbreviation() + " +" + value;
        case INITIATIVE_PASSES -> "Durchgänge +" + value;
        case ARMOR_BALLISTIC -> "Ballistik +" + value;
        case ARMOR_IMPACT -> "Stoß +" + value;
        case SKILL_GROUP_DICE -> effect.target() + " +" + value;
        case SOCIAL_SKILL_DICE -> "soziale Fertigkeiten +" + value;
        case PHYSICAL_SKILL_DICE -> "körperliche Fertigkeiten +" + value;
        case KNOWLEDGE_SKILL_DICE -> "Wissensfertigkeiten +" + value;
        case SELECTED_SKILL_DICE -> implant.getChoice() + " +" + value;
        case WOUND_BOXES_IGNORED -> value + " Kästchen ignoriert";
      });
    }
    return String.join(", ", parts);
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
        if (chargeFailed(ch, cyberwareLabel(implant), implant.getNuyenCost())) {
          return;
        }
        ch.getImplants().add(implant);
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

  /** Wählt Zubehör für ein Basissystem (Cyberaugen, Cyberohren) und baut es ein; kostet Kapazität, keine Essenz. */
  private void onInstallAccessory(@NonNull Cyberware base) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("CyberwareSelectDialog.fxml",
          "Zubehör für " + base.getName());
      CyberwareSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      ctrl.setInstallTarget(base);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(accessory -> {
        if (chargeFailed(ch, cyberwareLabel(accessory) + " in " + base.getName(), accessory.getNuyenCost())) {
          return;
        }
        base.install(accessory);
        onCyberwareChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Wählt ein Kommlink-Modell aus dem Ausrüstungskatalog und baut es in die Kommlink-Headware ein (GRW S. 386). */
  private void onInstallCommlink(@NonNull Cyberware headware) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml", "Kommlink einbauen");
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCommlinkTarget(ch, headware);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(commlink -> {
        if (chargeFailed(ch, commlink.getName() + " in " + headware.getName(), commlink.getNuyenCost())) {
          return;
        }
        headware.setCommlink(commlink);
        onCyberwareChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Baut ein Betriebssystem in das Kommlink der Kommlink-Headware ein, wie im Ausrüstungs-Tab. */
  private void onInstallOperatingSystem(@NonNull Gear commlink) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml", "Betriebssystem einbauen");
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setInstallTarget(ch, commlink);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(os -> {
        if (chargeFailed(ch, os.getName() + " in " + commlink.getName(), os.getNuyenCost())) {
          return;
        }
        commlink.install(os);
        onCyberwareChanged();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Im Karrieremodus wird der Preis nach Bestätigung abgebucht; {@code true}, wenn der Kauf abgebrochen wurde. */
  private boolean chargeFailed(@NonNull Character ch, @NonNull String label, int nuyenCost) {
    return ch.isCreated() && host.services().nuyenChargeFailed(ch, "Kauf: " + label, nuyenCost);
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
    return implant.getDisplayName() + " (" + details + ")";
  }
}
