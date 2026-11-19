package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.addSourceTooltip;
import static de.shalorian.chummer.view.grid.GridSupport.installButton;
import static de.shalorian.chummer.view.grid.GridSupport.markGmFree;
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
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.cyberware.ImplantEffect;
import de.shalorian.chummer.model.character.cyberware.LimbAttributes;
import de.shalorian.chummer.model.character.cyberware.LimbPart;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.dialog.CyberwareSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.GearSelectDialogController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * das eingebaute Kommlink. Ein eigener Abschnitt zeigt die Cybergliedmaßen (GRW S. 389–391) mit Stelle, Werten,
 * Kapazität und Einbauten, dazu den Durchschnitt über die fünf Körperteile; Glieder lassen sich an- und abnehmen.
 * Änderungen an Implantaten meldet er über {@link CharacterTabHost#onImplantsChanged()}, weil Essenz, MAG/RES und
 * Restgeld davon abhängen.
 */
public final class CyberwareTabBuilder {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  private static final String[] IMPLANT_HEADERS =
      {"Implantat", "Bereich", "Kategorie", "Stufe", "Essenz", "Preis", "Wirkung", ""};
  private static final String[] LIMB_HEADERS =
      {"Glied", "Werte", "Kategorie", "Stufe", "Essenz", "Preis", "Kapazität", ""};
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
    header.getChildren().addAll(essenceLabel, lossLabel);
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

    Separator limbSep = new Separator();
    VBox.setMargin(limbSep, new Insets(16, 0, 16, 0));
    cyberwareContainer.getChildren().add(limbSep);

    buildLimbSection(ch);
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
        .filter(c -> c.getKind() == kind && c.getLocation() == null)
        .toList();
    if (implants.isEmpty()) {
      Label empty = new Label("Keine " + kind.getDisplayName() + " eingebaut");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      cyberwareContainer.getChildren().add(empty);
      return;
    }

    MigPane grid = buildCyberwareGrid(IMPLANT_HEADERS);
    int row = 1;
    for (Cyberware implant : implants) {
      addCyberwareRow(grid, row++, implant, ch);
      for (Cyberware accessory : implant.getInstalled()) {
        addAccessoryRow(grid, row++, implant, accessory, () -> implant.uninstall(accessory));
        row = addCommlinkRowIfPresent(grid, row, accessory);
      }
      row = addCommlinkRowIfPresent(grid, row, implant);
    }
    cyberwareContainer.getChildren().add(withRightFiller(grid));
  }

  /**
   * Implantat-Grid inkl. Kopfzeile (Zeile 0), inhaltsbasierte Spalten analog {@code MagicTabBuilder.buildSpellGrid()}.
   */
  private static @NonNull MigPane buildCyberwareGrid(@NonNull String[] headers) {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][][center][right][right][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    addHeaderRow(grid, headers);
    return grid;
  }

  // ── Cybergliedmaßen ───────────────────────────────────────

  /**
   * Abschnitt Cybergliedmaßen: Kopf mit Durchschnitt über die fünf Körperteile (S. 389/390), je Glied eine Zeile mit
   * Stelle, Werten, Kapazität und Knöpfen zum An-/Abnehmen, Einbauen und Entfernen, darunter die Einbauten.
   */
  private void buildLimbSection(@NonNull Character ch) {
    Label title = new Label("Cybergliedmaßen");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    Button addBtn = new Button("Cyberglied hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddLimb());

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(title);
    if (!ch.getImplants().getAttachedLimbs().isEmpty()) {
      Label averages = styled(new Label(averagesText(ch.getImplants().getLimbAverages(),
          ch.getImplants().getLimbArmor())), STYLE_ATTR_RANGE);
      averages.setTooltip(new Tooltip("Abgerundeter Durchschnitt über beide Arme, beide Beine und den Torso, für "
          + "Aktionen des ganzen Körpers (GRW S. 389/390); Proben mit einem bestimmten Glied nutzen dessen Werte"));
      header.getChildren().add(averages);
    }
    header.getChildren().addAll(spacer, addBtn);
    cyberwareContainer.getChildren().add(header);

    List<Cyberware> limbs = ch.getImplants().getLimbs();
    if (limbs.isEmpty()) {
      Label empty = new Label("Keine Cybergliedmaßen eingebaut");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      VBox.setMargin(empty, new Insets(8, 0, 0, 0));
      cyberwareContainer.getChildren().add(empty);
      return;
    }
    MigPane grid = buildCyberwareGrid(LIMB_HEADERS);
    int row = 1;
    for (Cyberware limb : limbs) {
      addLimbRow(grid, row++, limb, ch);
      for (Cyberware part : limb.getInstalled()) {
        addAccessoryRow(grid, row++, limb, part, () -> ch.getImplants().uninstallFromLimb(limb, part));
        row = addCommlinkRowIfPresent(grid, row, part);
      }
    }
    cyberwareContainer.getChildren().add(withRightFiller(grid));
  }

  /** „Durchschnitt BOD 4 · STR 4 · AGI 2“, mit der Summe der Gliedpanzerung, wenn vorhanden. */
  private static @NonNull String averagesText(@NonNull LimbAttributes averages, int armor) {
    String text = "Durchschnitt " + limbValuesText(averages);
    return armor == 0 ? text : text + " · Gliedpanzerung +" + armor;
  }

  /** „BOD 8 · STR 9 · AGI 4“; auch für die Status-Übersicht. */
  static @NonNull String limbValuesText(@NonNull LimbAttributes values) {
    return "BOD %d · STR %d · AGI %d".formatted(values.body(), values.strength(), values.agility());
  }

  private void addLimbRow(@NonNull MigPane grid, int row, @NonNull Cyberware limb, @NonNull Character ch) {
    String name = limb.getDisplayName() + (limb.isAttached() ? "" : " (abgenommen)");
    Label nameLabel = styled(new Label(name), STYLE_ATTR_NAME);
    if (!limb.getData().note().isEmpty()) {
      nameLabel.setTooltip(new Tooltip(limb.getData().note()));
    }
    LimbPart part = limb.getData().limb().part();
    boolean hasAttributes = part != null && part.hasAttributes();
    Label valuesLabel = styled(new Label(hasAttributes ? limbValuesText(limb.getLimbAttributes())
        : "keine eigenen Attribute"), STYLE_ATTR_RANGE);
    String unarmed = limb.getUnarmedDamageText();
    if (unarmed != null) {
      valuesLabel.setTooltip(new Tooltip("Waffenlos mit diesem Glied: " + unarmed + " (GRW S. 390)"));
    }
    Label gradeLabel = styled(new Label(limb.getGrade().getDisplayName()), STYLE_ATTR_ABBR);
    Label essenceLabel = styled(new Label(ESSENCE_FORMAT.toString(limb.getEssenceCost().doubleValue())),
        STYLE_ATTR_VALUE);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(limb.getOwnNuyenCost())), STYLE_ATTR_BP);
    String capacity = "Kapazität " + limb.getUsedCapacity() + "/" + limb.getCapacity();
    int armor = limb.getLimbArmor();
    Label effectLabel = styled(new Label(armor == 0 ? capacity : capacity + ", Panzerung " + armor), "effect-text");
    addSourceTooltip(nameLabel, limb.getData().source(), limb.getData().page());
    markGmFree(nameLabel, limb.getGmFreeNuyen() > 0, Numbers.nuyen(limb.getGmFreeNuyen()));

    Button attachBtn = actionButton(limb.isAttached() ? "Abnehmen" : "Anschließen",
        limb.isAttached() ? FontAwesomeSolid.UNLINK : FontAwesomeSolid.LINK);
    attachBtn.setOnAction(_ -> onToggleAttached(ch, limb));
    Button installBtn = installButton("Verbesserung oder Zubehör in das Glied einbauen");
    installBtn.setOnAction(_ -> onInstallAccessory(limb));
    Button removeBtn = actionButton("Entfernen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      ch.getImplants().remove(limb);
      onCyberwareChanged();
    });
    HBox actions = new HBox(6, attachBtn, installBtn, removeBtn);
    actions.setAlignment(Pos.CENTER_LEFT);
    if (!limb.isAttached()) {
      nameLabel.setOpacity(0.6);
    }
    addRowCells(grid, row, nameLabel, valuesLabel, gradeLabel, styled(new Label("–"), STYLE_ATTR_VALUE),
        essenceLabel, priceLabel, effectLabel, actions);
  }

  /** Schließt an bzw. nimmt ab; sind danach Stellen mehrfach belegt, nur ein Hinweis (spätere Regelwerke, Surge). */
  private void onToggleAttached(@NonNull Character ch, @NonNull Cyberware limb) {
    limb.setAttached(!limb.isAttached());
    onCyberwareChanged();
    warnIfOverOccupied(ch);
  }

  private void warnIfOverOccupied(@NonNull Character ch) {
    List<BodyLocation> crowded = ch.getImplants().getOverOccupiedLocations();
    if (!crowded.isEmpty()) {
      host.services().warn("Stelle mehrfach belegt", "Mehr als ein Glied angeschlossen: "
          + String.join(", ", crowded.stream().map(BodyLocation::getDisplayName).toList()));
    }
  }

  /**
   * Kauft ein Glied über den Dialog (Kauf wie bei Implantaten). An einer belegten Stelle kommt es abgenommen dazu; der
   * Dialog weist vorher darauf hin.
   */
  private void onAddLimb() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("CyberwareSelectDialog.fxml", "Cyberglied hinzufügen");
      CyberwareSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      ctrl.setLimbMode();
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(limb -> {
        if (chargeFailed(ch, cyberwareLabel(limb), limb.getNuyenCost())) {
          return;
        }
        ch.getImplants().add(limb);
        onCyberwareChanged();
        if (ch.isEssenceBelowCreationMinimum()) {
          host.services().warn("Essenz zu niedrig",
              "Bei der Charaktererschaffung darf die Essenz nicht auf 0 oder darunter fallen.");
        }
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private void addCyberwareRow(@NonNull MigPane grid, int row, @NonNull Cyberware implant,
      @NonNull Character character) {
    Label implantNameLabel = styled(new Label(implant.getDisplayName()), STYLE_ATTR_NAME);
    String note = implant.getData().note();
    if (!note.isEmpty()) {
      implantNameLabel.setTooltip(new Tooltip(note));
    }
    addSourceTooltip(implantNameLabel, implant.getData().source(), implant.getData().page());
    markGmFree(implantNameLabel, implant.getGmFreeNuyen() > 0, Numbers.nuyen(implant.getGmFreeNuyen()));
    Label categoryLabel = styled(new Label(implant.getData().category()), STYLE_ATTR_RANGE);
    Label gradeLabel = styled(new Label(implant.getGrade().getDisplayName()), STYLE_ATTR_ABBR);
    Label ratingLabel = styled(new Label(implant.getData().hasRating()
        ? String.valueOf(implant.getRating())
        : "–"), STYLE_ATTR_VALUE);
    Label essenceLabel = styled(new Label(ESSENCE_FORMAT.toString(implant.getEssenceCost().doubleValue())),
        STYLE_ATTR_VALUE);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(implant.getOwnNuyenCost())),
        STYLE_ATTR_BP);
    Label effectLabel = styled(new Label(effectText(implant)), "effect-text");

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
      character.getImplants().remove(implant);
      onCyberwareChanged();
    });
    actions.getChildren().add(removeBtn);

    addRowCells(grid, row, implantNameLabel, categoryLabel, gradeLabel, ratingLabel, essenceLabel,
        priceLabel, effectLabel, actions);
  }

  /**
   * Eingebautes Zubehör eines Basissystems bzw. Einbau eines Glieds: eingerückt, Kapazität statt Essenz, Ausbau über
   * {@code uninstall}.
   */
  private void addAccessoryRow(@NonNull MigPane grid, int row, @NonNull Cyberware base,
      @NonNull Cyberware accessory, @NonNull Runnable uninstall) {
    String suffix = accessory.isPairedCopy() ? " (zweites Bein, Preis im ersten)" : "";
    Label nameLabel = styled(new Label(CHILD_PREFIX + accessory.getDisplayName() + suffix), STYLE_ATTR_NAME);
    String note = accessory.getData().note();
    if (!note.isEmpty()) {
      nameLabel.setTooltip(new Tooltip(note));
    }
    addSourceTooltip(nameLabel, accessory.getData().source(), accessory.getData().page());
    Label ratingLabel = styled(new Label(accessory.getData().hasRating()
        ? String.valueOf(accessory.getRating())
        : "–"), STYLE_ATTR_VALUE);
    Label capacityLabel = styled(new Label("Kap. " + base.capacityNeed(accessory.getData(), accessory.getRating())),
        STYLE_ATTR_VALUE);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(accessory.getNuyenCost())), STYLE_ATTR_BP);
    Button removeBtn = actionButton("Ausbauen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      uninstall.run();
      onCyberwareChanged();
    });
    HBox actions = new HBox(6);
    actions.setAlignment(Pos.CENTER_LEFT);
    if (accessory.getData().acceptedGear() != null && accessory.getCommlink() == null) {
      // Kommlink-Headware in einem Glied (S. 385) nimmt wie implantiert ein Kommlink-Modell auf
      Button commlinkBtn = actionButton("Kommlink", FontAwesomeSolid.PLUS);
      commlinkBtn.setOnAction(_ -> onInstallCommlink(accessory));
      actions.getChildren().add(commlinkBtn);
    }
    actions.getChildren().add(removeBtn);
    addRowCells(grid, row, nameLabel, new Label(), new Label(), ratingLabel, capacityLabel, priceLabel,
        new Label(), actions);
  }

  /** Fügt die Zeile des eingebauten Kommlinks von {@code headware} an, falls vorhanden; liefert die nächste Zeile. */
  private int addCommlinkRowIfPresent(@NonNull MigPane grid, int row, @NonNull Cyberware headware) {
    Gear commlink = headware.getCommlink();
    if (commlink == null) {
      return row;
    }
    return addCommlinkRow(grid, row, headware, commlink);
  }

  /**
   * Eingebautes Kommlink der Kommlink-Headware samt Betriebssystem, darunter je Einbau (Module, Pakete, Programme,
   * Sim-Modul) eine Zeile; Programme mit Schalter „läuft“ (Prozessorlimit, GRW S. 261). Liefert die nächste Zeile.
   */
  private int addCommlinkRow(@NonNull MigPane grid, int row, @NonNull Cyberware headware, @NonNull Gear commlink) {
    Gear operatingSystem = commlink.getOperatingSystem();
    String os = operatingSystem == null ? "ohne Betriebssystem" : operatingSystem.getName();
    Label nameLabel = styled(new Label(CHILD_PREFIX + commlink.getName() + " (" + os + ")"), STYLE_ATTR_NAME);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(commlink.getNuyenCost())), STYLE_ATTR_BP);
    Label statsLabel = styled(new Label(commlinkStatsText(commlink)), STYLE_ATTR_RANGE);
    addSourceTooltip(nameLabel, commlink.getData().source(), commlink.getData().page());

    HBox actions = new HBox(6);
    actions.setAlignment(Pos.CENTER_LEFT);
    Button installBtn = installButton("In das Kommlink einbauen");
    installBtn.setOnAction(_ -> onInstallIntoCommlink(commlink));
    actions.getChildren().add(installBtn);
    Button removeBtn = actionButton("Ausbauen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      headware.removeCommlink();
      onCyberwareChanged();
    });
    actions.getChildren().add(removeBtn);
    addRowCells(grid, row, nameLabel, new Label(), new Label(), new Label(), new Label(), priceLabel,
        statsLabel, actions);
    int next = row + 1;
    MatrixStats stats = Objects.requireNonNull(commlink.getMatrixStats());
    for (Gear part : commlink.getInstalled()) {
      if (part.getData().category() == GearCategory.OPERATING_SYSTEMS) {
        continue; // steht schon in der Kommlink-Zeile
      }
      addCommlinkPartRow(grid, next++, commlink, part, stats);
    }
    return next;
  }

  /** Ein Einbau im Headware-Kommlink; Programme zeigen ihre wirksame Stufe (höchstens System, S. 271). */
  private void addCommlinkPartRow(@NonNull MigPane grid, int row, @NonNull Gear commlink, @NonNull Gear part,
      @NonNull MatrixStats stats) {
    Label nameLabel = styled(new Label("      " + CHILD_PREFIX.strip() + " " + part.getDisplayName()), STYLE_ATTR_NAME);
    Label priceLabel = styled(new Label(SpendCurrency.NUYEN.formatAmount(part.getNuyenCost())), STYLE_ATTR_BP);
    Label infoLabel = styled(new Label(part.isProgram()
        ? "läuft mit Stufe " + stats.programRating(part.getRating()) : ""), STYLE_ATTR_RANGE);
    HBox actions = new HBox(6);
    actions.setAlignment(Pos.CENTER_LEFT);
    if (part.isProgram()) {
      CheckBox running = new CheckBox("läuft");
      running.setSelected(part.isRunning());
      running.selectedProperty().addListener((_, _, value) -> {
        part.setRunning(value);
        onCyberwareChanged();
      });
      actions.getChildren().add(running);
    }
    Button removeBtn = actionButton("Ausbauen", FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      commlink.uninstall(part);
      onCyberwareChanged();
    });
    actions.getChildren().add(removeBtn);
    addSourceTooltip(nameLabel, part.getData().source(), part.getData().page());
    addRowCells(grid, row, nameLabel, new Label(), new Label(), new Label(), new Label(), priceLabel, infoLabel,
        actions);
  }

  /** Matrixwerte des eingebauten Kommlinks, dieselben Texte wie im Ausrüstungs-Detail ({@link GearDetails}). */
  private static @NonNull String commlinkStatsText(@NonNull Gear commlink) {
    GearDetails d = GearDetails.of(commlink);
    return "Prozessor %s, Signal %s, Firewall %s, System %s".formatted(d.processor(), d.signal(), d.firewall(),
        d.system());
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
        case DNI -> "Neuralinterface";
        case SIM_MODULE -> value > 1 ? "heißes Sim" : "kaltes Sim";
        case SKILLWIRES -> "Aktionssofts bis Stufe " + value;
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
              "Bei der Charaktererschaffung darf die Essenz nicht auf 0 oder darunter fallen.");
        }
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /**
   * Wählt Zubehör für ein Basissystem (Cyberaugen, Cyberohren) bzw. einen Einbau für ein Glied und baut es ein; kostet
   * Kapazität, keine Essenz. In Gliedern prüft {@code Implants#installInLimb} die Regeln mit Charakterbezug.
   */
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
        if (base.getLocation() != null) {
          ch.getImplants().installInLimb(base, accessory);
        } else {
          base.install(accessory);
        }
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
  private void onInstallIntoCommlink(@NonNull Gear commlink) {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("GearSelectDialog.fxml", "Einbau in " + commlink.getName());
      GearSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setInstallTarget(ch, commlink);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(part -> {
        if (chargeFailed(ch, part.getName() + " in " + commlink.getName(), part.getNuyenCost())) {
          return;
        }
        commlink.install(part);
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
