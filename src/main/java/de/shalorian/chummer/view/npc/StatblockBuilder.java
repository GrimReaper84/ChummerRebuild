package de.shalorian.chummer.view.npc;

import static de.shalorian.chummer.view.grid.GridSupport.styled;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.critter.CritterMetatype;
import de.shalorian.chummer.model.critter.CritterPowerData;
import de.shalorian.chummer.model.critter.CritterPowerDataLoader;
import de.shalorian.chummer.model.critter.CritterTraits;
import de.shalorian.chummer.model.critter.CritterWeaknessData;
import de.shalorian.chummer.model.critter.CritterWeaknessDataLoader;
import de.shalorian.chummer.model.critter.NaturalWeapon;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.converter.MetatypeStringConverter;
import de.shalorian.chummer.view.dialog.AdeptPowerSelectDialogController;
import de.shalorian.chummer.view.dialog.ArmorSelectDialogController;
import de.shalorian.chummer.view.dialog.CyberwareSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.QualitySelectDialogController;
import de.shalorian.chummer.view.dialog.SpellSelectDialogController;
import de.shalorian.chummer.view.dialog.WeaponSelectDialogController;
import de.shalorian.chummer.view.grid.IntegerSpinner;
import de.shalorian.chummer.view.tab.TabServices;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein Werteblock auf dem Schergen-Bogen im Stil des GRW (S. 325–328): Attributzeile K G R S C I L W (M) ESS INI ID ZM,
 * darunter Fertigkeiten mit Würfelpool, Gaben, Implantate, Zauber, Adeptenkräfte, Waffen mit Pool und Panzerung sowie
 * die sonstige Ausrüstung als Text. Im Bearbeiten-Modus lassen sich alle Teile ändern; Katalogeinträge kommen über die
 * vorhandenen Auswahldialoge. Der Werteblock steht im Spielleiter-Modus, nichts kostet BP, Karma oder Nuyen. Bei
 * Crittern (ADR-064) zeigt er statt Ausrüstung Bewegung, Kräfte und Schwächen mit Tooltip, natürliche Waffen und
 * Panzer; Attribute bearbeitet man in der Spanne ±3 des Katalogs (GRW S. 337).
 */
final class StatblockBuilder {

  private static final List<AttributeType> ATTRIBUTES = List.of(AttributeType.values());

  private final @NonNull NpcStatblock statblock;
  private final @NonNull Character c;
  private final @NonNull TabServices services;
  private final @NonNull Runnable onChanged;
  private final boolean editing;
  /** Merkmale, wenn der Werteblock ein Critter ist (GRW S. 337–343), sonst {@code null}. */
  private final @Nullable CritterTraits critter;

  private StatblockBuilder(@NonNull NpcStatblock statblock, @NonNull TabServices services, @NonNull Runnable onChanged,
      boolean editing) {
    this.statblock = statblock;
    this.c = statblock.getCharacter();
    this.services = services;
    this.onChanged = onChanged;
    this.editing = editing;
    this.critter = c.getCritterTraits();
  }

  /**
   * Baut die Karte eines Werteblocks.
   *
   * @param title       Überschrift, z.B. „SCHERGEN“
   * @param hint        Hinweis unter der Überschrift (Anführer-Richtwert), sonst {@code null}
   * @param editing     ob die Karte im Bearbeiten-Modus steht
   * @param onEditToggle schaltet zwischen Anzeige und Bearbeiten um
   * @param onChanged   nach jeder Änderung der Werte (baut den Bogen neu)
   */
  static @NonNull Node build(@NonNull NpcStatblock statblock, @NonNull String title, @Nullable String hint,
      boolean editing, @NonNull Runnable onEditToggle, @NonNull TabServices services, @NonNull Runnable onChanged) {
    return new StatblockBuilder(statblock, services, onChanged, editing).card(title, hint, onEditToggle);
  }

  private @NonNull Node card(@NonNull String title, @Nullable String hint, @NonNull Runnable onEditToggle) {
    VBox box = new VBox(8);
    box.getStyleClass().add("card");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Button edit = new Button(editing ? "Fertig" : "Bearbeiten");
    edit.getStyleClass().add(STYLE_BTN_SECONDARY);
    edit.setOnAction(_ -> onEditToggle.run());
    edit.setMinWidth(Region.USE_PREF_SIZE);
    HBox head = new HBox(8, fixed(styled(new Label(title), "card-title")), metatypeNode(), spacer, edit);
    head.setAlignment(Pos.CENTER_LEFT);
    box.getChildren().add(head);
    if (hint != null) {
      Label hintLabel = styled(new Label(hint), "warn-text");
      hintLabel.setWrapText(true);
      box.getChildren().add(hintLabel);
    }
    if (critter != null) {
      // Critter tragen keine Ausrüstung (Plan-Annahme); Waffen und Panzer kommen aus ihren Kräften
      box.getChildren().addAll(attributeGrid(), movement(critter), skills(), powers(critter), weaknesses(critter),
          qualities(), naturalWeapons(critter));
      if (!c.getQualities().isEmpty()) {
        box.getChildren().add(magic());
      }
      return box;
    }
    box.getChildren().addAll(attributeGrid(), skills(), qualities(), implants(), magic(), weapons(), armor(), gear());
    return box;
  }

  // --- Kopf und Attribute ---

  private @NonNull Node metatypeNode() {
    if (critter != null) {
      return styled(new Label(critter.getData().name() + " · " + critter.getData().category().getDisplayName()),
          "key-caption");
    }
    if (!editing) {
      return styled(new Label(c.getMetatype().name()), "key-caption");
    }
    ComboBox<MetatypeData> box = new ComboBox<>();
    box.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    box.setConverter(new MetatypeStringConverter());
    box.setValue(c.getMetatype());
    box.valueProperty().addListener((_, _, value) -> {
      if (value != null && !value.equals(c.getMetatype())) {
        List<CharacterQuality> removed = c.setMetatype(value);
        services.warn("Metatyp gewechselt", "Attribute stehen wieder auf den Mindestwerten von " + value.name()
            + (removed.isEmpty() ? "." : "; entfernt: " + String.join(", ",
                removed.stream().map(CharacterQuality::displayName).toList()) + "."));
        onChanged.run();
      }
    });
    return box;
  }

  /** Kürzel über Wert, umbrechend: In einer schmalen Karte rutschen die letzten Werte in eine zweite Reihe. */
  private @NonNull Node attributeGrid() {
    FlowPane cells = new FlowPane(10, 6);
    for (AttributeType type : ATTRIBUTES) {
      cells.getChildren().add(cell(type.getAbbreviation(), editing ? attributeSpinner(type) : value(attributeText(type))));
    }
    if (critter != null) {
      cells.getChildren().add(cell("EDG",
          editing ? magicSpinner(c.getEdge()) : value(String.valueOf(c.getEdge().getValue()))));
    }
    SpecialAttribute magic = c.getMagicOrResonance();
    if (magic != null) {
      cells.getChildren().add(cell(c.hasMagic() ? "M" : "RES",
          editing ? magicSpinner(magic) : value(String.valueOf(c.getEffectiveMagicOrResonanceValue()))));
    }
    cells.getChildren().addAll(
        cell("ESS", value(critter != null ? critter.getData().essence() : Numbers.essence(c.getEssence()))),
        cell("INI", value(String.valueOf(c.getInitiative()))),
        cell("ID", value(String.valueOf(c.getInitiativePasses()))),
        cell("ZM", value(String.valueOf(statblock.getMonitorBoxes()))));
    return cells;
  }

  private static @NonNull Node cell(@NonNull String abbreviation, @NonNull Node content) {
    return new VBox(2, header(abbreviation), content);
  }

  /** „3“ bzw. „5 (7)“, wenn Implantate oder Kräfte den Wert verändern. */
  private @NonNull String attributeText(@NonNull AttributeType type) {
    int natural = c.getAttribute(type).getValue();
    int augmented = c.getAugmentedAttributeValue(type);
    return augmented == natural ? String.valueOf(natural) : natural + " (" + augmented + ")";
  }

  private @NonNull Node attributeSpinner(@NonNull AttributeType type) {
    var attribute = c.getAttribute(type);
    Spinner<Integer> spinner = IntegerSpinner.create(attribute.getMin(), attribute.getMax(), attribute.getValue());
    spinner.valueProperty().addListener((_, _, value) -> {
      attribute.setValue(value);
      onChanged.run();
    });
    return spinner;
  }

  private @NonNull Node magicSpinner(@NonNull SpecialAttribute magic) {
    Spinner<Integer> spinner = IntegerSpinner.create(magic.getMin(), magic.getMax(), magic.getValue());
    spinner.valueProperty().addListener((_, _, value) -> {
      magic.setValue(value);
      onChanged.run();
    });
    return spinner;
  }

  // --- Fertigkeiten ---

  private @NonNull Node skills() {
    VBox lines = new VBox(2);
    for (String group : c.getCoupledGroupNames()) {
      int rating = c.getGroupRating(group);
      if (rating > 0) {
        lines.getChildren().add(line(group + "-Fertigkeitsgruppe " + rating, () -> setGroupRating(group, 0)));
      }
    }
    for (Skill skill : c.getSkills()) {
      if (skill.getRating() == 0) {
        continue;
      }
      String spec = skill.getSpecialization();
      Optional<DicePool> pool = c.getSkillDicePool(skill);
      String text = skill.getName() + " " + skill.getRating() + (spec == null ? "" : " (" + spec + ")")
          + pool.map(p -> " → " + p.dice()).orElse("");
      boolean individual = c.getIndividualSkills().contains(skill);
      lines.getChildren().add(individual ? line(text, () -> skill.setRating(0)) : value(text));
    }
    for (KnowledgeSkill knowledge : c.getKnowledgeSkills()) {
      Optional<DicePool> pool = c.getKnowledgeSkillDicePool(knowledge);
      String text = "%s (%s) %d".formatted(knowledge.getName(), knowledge.getCategory().getDisplayName(),
          knowledge.getRating()) + pool.map(p -> " → " + p.dice()).orElse("");
      lines.getChildren().add(line(text, () -> c.removeKnowledgeSkill(knowledge.getName())));
    }
    if (critter != null) {
      for (var special : critter.getSpecialSkills().entrySet()) {
        lines.getChildren().add(editing ? row(value(special.getKey()), specialSkillSpinner(critter, special.getKey(),
            special.getValue())) : value(special.getKey() + " " + special.getValue()));
      }
    }
    if (editing) {
      lines.getChildren().addAll(skillAdder(), groupAdder(), knowledgeAdder());
    }
    return section("Fertigkeiten", lines);
  }

  private void setGroupRating(@NonNull String group, int rating) {
    while (c.getGroupRating(group) > rating) {
      c.lowerGroup(group);
    }
    while (c.getGroupRating(group) < rating) {
      c.raiseGroup(group);
    }
  }

  private @NonNull Node skillAdder() {
    ComboBox<String> names = new ComboBox<>();
    names.getItems().setAll(c.getIndividualSkills().stream().map(Skill::getName).toList());
    names.setPromptText("Fertigkeit");
    // bis zum Höchstwert mit Talentiert; beim Hinzufügen auf das Maximum der Fertigkeit begrenzt
    Spinner<Integer> rating = IntegerSpinner.create(1, Skill.MAX_RATING + 1, 3);
    TextField spec = new TextField();
    spec.setPromptText("Spezialisierung");
    spec.setPrefWidth(130);
    Button add = addButton(() -> {
      Skill skill = names.getValue() == null ? null : c.getSkill(names.getValue());
      if (skill == null) {
        return;
      }
      skill.setRating(Math.min(rating.getValue(), skill.getMaxRating()));
      String text = spec.getText().trim();
      skill.setSpecialization(text.isEmpty() ? null : text);
    });
    return row(names, rating, spec, add);
  }

  private @NonNull Node groupAdder() {
    ComboBox<String> groups = new ComboBox<>();
    groups.getItems().setAll(c.getCoupledGroupNames());
    groups.setPromptText("Fertigkeitsgruppe");
    Spinner<Integer> rating = IntegerSpinner.create(1, Skill.MAX_RATING, 3);
    Button add = addButton(() -> {
      if (groups.getValue() != null) {
        setGroupRating(groups.getValue(), rating.getValue());
      }
    });
    return row(groups, rating, add);
  }

  private @NonNull Node knowledgeAdder() {
    TextField name = new TextField();
    name.setPromptText("Wissensfertigkeit");
    ComboBox<KnowledgeSkillCategory> category = new ComboBox<>();
    category.getItems().setAll(KnowledgeSkillCategory.values());
    category.setValue(KnowledgeSkillCategory.STREET);
    Spinner<Integer> rating = IntegerSpinner.create(1, KnowledgeSkill.MAX_RATING, 2);
    Button add = addButton(() -> {
      String text = name.getText().trim();
      if (text.isEmpty() || category.getValue() == null
          || c.getKnowledgeSkills().stream().anyMatch(k -> k.getName().equals(text))) {
        return;
      }
      c.addKnowledgeSkill(text, category.getValue());
      c.getKnowledgeSkills().getLast().setRating(rating.getValue());
    });
    return row(name, category, rating, add);
  }

  // --- Critter (GRW S. 337–343) ---

  private static @NonNull Node movement(@NonNull CritterTraits critter) {
    return section("Bewegung", value(critter.getData().movement()));
  }

  /** Sonderfertigkeit ohne Eintrag in skills.xml (Fliegen) in der Spanne ±3 um den Katalogwert, nicht unter 0. */
  private @NonNull Node specialSkillSpinner(@NonNull CritterTraits traits, @NonNull String name, int rating) {
    int base = traits.getData().skills().stream().filter(s -> s.name().equals(name)).mapToInt(s -> s.rating())
        .findFirst().orElse(rating);
    AttributeRange range = CritterMetatype.range(base);
    Spinner<Integer> spinner = IntegerSpinner.create(range.min(), range.max(), rating);
    spinner.valueProperty().addListener((_, _, value) -> {
      traits.setSpecialSkill(name, value);
      onChanged.run();
    });
    return spinner;
  }

  private @NonNull Node powers(@NonNull CritterTraits traits) {
    VBox lines = new VBox(2);
    for (String power : traits.getPowers()) {
      CritterPowerData data = CritterPowerDataLoader.findForEntry(power);
      lines.getChildren().add(line(power, data == null ? null : data.describe(), () -> traits.removePower(power)));
    }
    SpecialAttribute magic = c.getMagicOrResonance();
    if (magic != null && !traits.getPowers().isEmpty()) {
      lines.getChildren().add(styled(new Label("Hält bis zu %d Kräfte zugleich aufrecht (Magie, GRW S. 337)"
          .formatted(c.getEffectiveMagicOrResonanceValue())), "key-caption"));
    }
    if (magic != null && !c.isMagician()) {
      lines.getChildren().add(styled(new Label("Ohne die Gabe Zauberer keine Zauber, Beschwörung oder Projektion"
          + " (GRW S. 337)"), "key-caption"));
    }
    if (editing) {
      lines.getChildren().add(traitAdder(CritterPowerDataLoader.getAll().values().stream()
          .filter(p -> !p.sprite()).map(CritterPowerData::name).toList(), "Kraft", traits::addPower));
    }
    return section("Kräfte", lines);
  }

  private @NonNull Node weaknesses(@NonNull CritterTraits traits) {
    VBox lines = new VBox(2);
    for (String weakness : traits.getWeaknesses()) {
      CritterWeaknessData data = CritterWeaknessDataLoader.findForEntry(weakness);
      lines.getChildren().add(line(weakness, data == null ? null : data.describe(),
          () -> traits.removeWeakness(weakness)));
    }
    if (editing) {
      lines.getChildren().add(traitAdder(CritterWeaknessDataLoader.getAll().values().stream()
          .map(CritterWeaknessData::name).toList(), "Schwäche", traits::addWeakness));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Schwächen", lines);
  }

  /** Auswahl aus dem Katalog plus Zusatztext, z.B. „Allergie“ + „Sonnenlicht, Leicht“. */
  private @NonNull Node traitAdder(@NonNull List<String> names, @NonNull String prompt,
      @NonNull Consumer<String> add) {
    ComboBox<String> box = new ComboBox<>();
    box.getItems().setAll(names);
    box.setPromptText(prompt);
    TextField extra = new TextField();
    extra.setPromptText("Zusatz");
    extra.setPrefWidth(200);
    return row(box, extra, addButton(() -> {
      if (box.getValue() != null) {
        String text = extra.getText().trim();
        add.accept(CritterTraits.entry(box.getValue(), text));
      }
    }));
  }

  /**
   * Natürliche Waffen mit Pool aus Waffenloser Kampf (S. 340; Fernangriffe wie Elementarer Angriff nutzen Exotische
   * Fernkampfwaffe und stehen als Kraft da) und der Panzer aus den Kräften.
   */
  private @NonNull Node naturalWeapons(@NonNull CritterTraits traits) {
    VBox lines = new VBox(2);
    Skill unarmed = c.getSkill("Waffenloser Kampf");
    String pool = unarmed == null ? "" : c.getSkillDicePool(unarmed).map(p -> " → " + p.dice()).orElse("");
    for (NaturalWeapon weapon : traits.getNaturalWeapons()) {
      lines.getChildren().add(value("%s: %s, PB %d%s%s".formatted(weapon.name(), weapon.damage(), weapon.ap(),
          optional(", ", weapon.extra()), pool)));
    }
    if (c.getArmorBallistic() > 0 || c.getArmorImpact() > 0) {
      lines.getChildren().add(value("Panzer %d/%d%s".formatted(c.getArmorBallistic(), c.getArmorImpact(),
          traits.isHardened() ? " (verstärkt: Schaden bis zur Panzerung prallt ab)" : "")));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Kampf", lines);
  }

  // --- Gaben, Implantate, Magie ---

  private @NonNull Node qualities() {
    VBox lines = new VBox(2);
    for (CharacterQuality quality : c.getQualities()) {
      lines.getChildren().add(quality.source() == QualitySource.SELECTED
          ? line(quality.displayName(), () -> c.removeQuality(quality))
          : value(quality.displayName()));
    }
    if (editing) {
      lines.getChildren().add(row(dialogButton("Gabe", () -> addQuality(QualityType.POSITIVE)),
          dialogButton("Handicap", () -> addQuality(QualityType.NEGATIVE))));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Gaben und Handicaps", lines);
  }

  private @NonNull Node implants() {
    VBox lines = new VBox(2);
    for (Cyberware implant : c.getEquipment().getCyberware()) {
      lines.getChildren().add(line(implantText(implant), () -> c.getImplants().remove(implant)));
    }
    if (editing) {
      lines.getChildren().add(row(dialogButton("Cyberware", () -> addImplant(EquipmentKind.CYBERWARE)),
          dialogButton("Bioware", () -> addImplant(EquipmentKind.BIOWARE))));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Bodytech", lines);
  }

  private static @NonNull String implantText(@NonNull Cyberware implant) {
    List<Cyberware> parts = implant.getInstalled();
    return parts.isEmpty() ? implant.getDisplayName()
        : implant.getDisplayName() + " (" + String.join(", ", parts.stream().map(Cyberware::getDisplayName).toList()) + ")";
  }

  private @NonNull Node magic() {
    VBox lines = new VBox(2);
    for (SpellData spell : c.getSpells()) {
      lines.getChildren().add(line("Zauber: " + spell.name(), () -> c.removeSpell(spell.name())));
    }
    for (AdeptPower power : c.getAdeptPowers().getPowers()) {
      lines.getChildren().add(line("Adeptenkraft: " + power.displayName()
          + (power.getData().leveled() ? " " + power.getLevel() : ""), () -> c.removeAdeptPower(power)));
    }
    if (editing && c.hasMagic()) {
      lines.getChildren().add(row(dialogButton("Zauber", this::addSpell),
          dialogButton("Adeptenkraft", this::addAdeptPower)));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Magie", lines);
  }

  // --- Waffen, Panzerung, Ausrüstung ---

  private @NonNull Node weapons() {
    Equipment equipment = c.getEquipment();
    VBox lines = new VBox(2);
    for (Weapon weapon : equipment.getWeapons()) {
      String text = "%s: %s, PB %s%s%s%s%s".formatted(weapon.getDisplayName(), equipment.getWeaponDamageText(weapon),
          equipment.getWeaponArmorPiercingText(weapon), optional(", ", weapon.getData().mode()),
          optional(", RK ", weapon.getRecoil().displayText()), optional(", ", weapon.getData().ammo()),
          equipment.getWeaponDicePool(weapon).map(p -> " → " + p.dice()).orElse(""));
      // Waffen aus Panzerung und Implantaten kommen mit ihrem Teil und lassen sich hier nicht entfernen.
      boolean own = !equipment.getArmorWeapons().contains(weapon) && !equipment.getImplantWeapons().contains(weapon);
      lines.getChildren().add(own ? line(text, () -> equipment.removeWeapon(weapon)) : value(text));
    }
    if (editing) {
      lines.getChildren().add(dialogButton("Waffe", this::addWeapon));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Waffen", lines);
  }

  private @NonNull Node armor() {
    Equipment equipment = c.getEquipment();
    VBox lines = new VBox(2);
    for (Armor piece : equipment.getArmor()) {
      lines.getChildren().add(line("%s %d/%d".formatted(piece.getName(), piece.getBallistic(), piece.getImpact()),
          () -> equipment.removeArmor(piece)));
    }
    if (!equipment.getArmor().isEmpty()) {
      int penalty = equipment.getArmorEncumbrancePenalty();
      lines.getChildren().add(styled(new Label("Gesamt %d/%d%s".formatted(c.getArmorBallistic(), c.getArmorImpact(),
          penalty == 0 ? "" : ", Behinderung " + Numbers.integer(penalty) + " auf GES und REA")), "key-caption"));
    }
    if (editing) {
      lines.getChildren().add(dialogButton("Panzerung", this::addArmor));
    }
    return lines.getChildren().isEmpty() ? new VBox() : section("Panzerung", lines);
  }

  private @NonNull Node gear() {
    if (editing) {
      TextArea area = new TextArea(statblock.getGearText());
      area.setWrapText(true);
      area.setPrefRowCount(2);
      area.textProperty().addListener((_, _, text) -> statblock.setGearText(text));
      return section("Sonstige Ausrüstung", area);
    }
    if (statblock.getGearText().isBlank()) {
      return new VBox();
    }
    Label text = value(statblock.getGearText());
    text.setWrapText(true);
    return section("Sonstige Ausrüstung", text);
  }

  // --- Auswahldialoge (alles kostenlos, Spielleiter-Modus) ---

  private void addQuality(@NonNull QualityType type) {
    try {
      DialogHandle dialog = services.openDialog("QualitySelectDialog.fxml", "Gabe oder Handicap hinzufügen");
      QualitySelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(c);
      ctrl.setFilter(type);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(r -> {
        String problem = c.getQualityAddProblem(r.data(), r.gratis(), r.choice());
        if (problem != null) {
          services.warn("Nicht wählbar", problem);
          return;
        }
        c.addQuality(r.data(), r.gratis(), r.choice());
      });
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void addImplant(@NonNull EquipmentKind kind) {
    try {
      DialogHandle dialog = services.openDialog("CyberwareSelectDialog.fxml", kind.getDisplayName() + " hinzufügen");
      CyberwareSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setKind(kind);
      ctrl.setCharacter(c);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(implant -> {
        try {
          c.getImplants().add(implant);
        } catch (IllegalArgumentException e) {
          services.warn("Nicht einbaubar", e.getMessage() == null ? implant.getName() : e.getMessage());
        }
      });
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void addSpell() {
    try {
      DialogHandle dialog = services.openDialog("SpellSelectDialog.fxml", "Zauber hinzufügen");
      SpellSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(c);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(c::addSpell);
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void addAdeptPower() {
    try {
      DialogHandle dialog = services.openDialog("AdeptPowerSelectDialog.fxml", "Adeptenkraft hinzufügen");
      AdeptPowerSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(c);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(r -> c.getAdeptPowers().add(r.data(), r.choice()));
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void addWeapon() {
    try {
      DialogHandle dialog = services.openDialog("WeaponSelectDialog.fxml", "Waffe hinzufügen");
      WeaponSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(c);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(c.getEquipment()::addWeapon);
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void addArmor() {
    try {
      DialogHandle dialog = services.openDialog("ArmorSelectDialog.fxml", "Panzerung hinzufügen");
      ArmorSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(c);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(c.getEquipment()::addArmor);
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  // --- Bausteine ---

  /** Zeile mit Text und, im Bearbeiten-Modus, einem Entfernen-Knopf. */
  private @NonNull Node line(@NonNull String text, @NonNull Runnable remove) {
    return line(text, null, remove);
  }

  /** Wie {@link #line(String, Runnable)} mit Tooltip, z.B. der Wirkung einer Critterkraft. */
  private @NonNull Node line(@NonNull String text, @Nullable String tooltip, @NonNull Runnable remove) {
    Label label = value(text);
    if (tooltip != null) {
      label.setTooltip(new Tooltip(tooltip));
    }
    if (!editing) {
      return label;
    }
    Button button = new Button("×");
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    button.setOnAction(_ -> {
      remove.run();
      onChanged.run();
    });
    // der Text bricht um, statt den Entfernen-Knopf aus der schmalen Karte zu drängen
    HBox.setHgrow(label, Priority.ALWAYS);
    button.setMinWidth(Region.USE_PREF_SIZE);
    return row(label, button);
  }

  private @NonNull Button dialogButton(@NonNull String text, @NonNull Runnable open) {
    return addButton(open, "+ " + text);
  }

  private @NonNull Button addButton(@NonNull Runnable action) {
    return addButton(action, "Hinzufügen");
  }

  private @NonNull Button addButton(@NonNull Runnable action, @NonNull String text) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    button.setOnAction(_ -> {
      action.run();
      onChanged.run();
    });
    return button;
  }

  /** {@code prefix + value}, leer bei leerem Wert oder „0“ (z.B. keine RK). */
  private static @NonNull String optional(@NonNull String prefix, @NonNull String value) {
    return value.isBlank() || value.equals("0") ? "" : prefix + value;
  }

  private static @NonNull Node section(@NonNull String title, @NonNull Node content) {
    return new VBox(2, fixed(styled(new Label(title), "attr-name")), content);
  }

  private static @NonNull HBox row(@NonNull Node... nodes) {
    HBox row = new HBox(6, nodes);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private static @NonNull Label header(@NonNull String text) {
    return fixed(styled(new Label(text), "key-caption"));
  }

  /** Werte und Zeilen brechen in einer schmalen Karte um, statt mit „…“ abgeschnitten zu werden. */
  private static @NonNull Label value(@NonNull String text) {
    Label label = new Label(text);
    label.setWrapText(true);
    return label;
  }

  /** Überschriften und Kürzel schrumpfen nicht unter ihre Textbreite. */
  private static @NonNull Label fixed(@NonNull Label label) {
    label.setMinWidth(Region.USE_PREF_SIZE);
    return label;
  }

}
