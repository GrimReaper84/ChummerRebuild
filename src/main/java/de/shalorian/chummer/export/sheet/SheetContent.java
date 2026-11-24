package de.shalorian.chummer.export.sheet;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterBiography;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.MatrixAccess;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.MatrixStats;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.model.character.quality.QualityType;
import de.shalorian.chummer.model.character.situation.SituationalPool;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.tab.ArmorDetails;
import de.shalorian.chummer.view.tab.VehicleDetails;
import de.shalorian.chummer.view.tab.WeaponDetails;
import java.util.ArrayList;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Alle Texte des Charakterbogens (ADR-067), ohne PDF und damit testbar. Die Werte stammen von einer Kopie des
 * Charakters ohne Schaden, damit Pools und Initiativen den unverletzten Stand zeigen. Bleistiftfelder bleiben leer
 * (Nutzerentscheidung 2026-11-19): Nuyen, Karma aktuell, aktuelles Edge und die Kästchen des Zustandsmonitors. Auf dem
 * Bogen stehen nur Fertigkeiten mit einem Wert, wie auf einem handgeschriebenen Bogen.
 *
 * @param portrait        Portrait als Bilddatei, {@code null} ohne
 * @param physicalMarkers je Kästchen der körperlichen Spalte der Verletzungsmodifikator, der ab ihm gilt („−1“), sonst
 *                        leer; die Länge ist die Zahl der Kästchen
 * @param overflowBoxes   Überzahl-Kästchen hinter der körperlichen Spalte
 * @param situationalPools situative Pools, nur auf Fortsetzungsseiten (ebenso {@code foci} und {@code initiation})
 * @param notes           Konzept, Hintergrund und Notizen der Biografie, auf der letzten Fortsetzungsseite
 */
// Das Portrait ist eine Bilddatei, die nur an PDFBox weitergereicht wird; Gleichheit des Records spielt keine Rolle.
@SuppressWarnings("ArrayRecordComponent")
public record SheetContent(
    @NonNull String name,
    byte @Nullable [] portrait,
    @NonNull List<SheetField> identity,
    @NonNull List<SheetField> resources,
    @NonNull List<SheetField> reputation,
    @NonNull List<SheetBlock> attributes,
    @NonNull List<List<String>> activeSkills,
    @NonNull List<List<String>> knowledgeSkills,
    @NonNull List<List<String>> qualities,
    @NonNull List<SheetField> appearance,
    @NonNull String description,
    @NonNull List<String> physicalMarkers,
    int overflowBoxes,
    @NonNull List<String> stunMarkers,
    @NonNull List<List<String>> attributeTests,
    @NonNull List<List<String>> contacts,
    @NonNull List<List<String>> implants,
    @NonNull List<List<String>> rangedWeapons,
    @NonNull List<List<String>> meleeWeapons,
    @NonNull List<List<String>> armor,
    @NonNull List<List<String>> vehicles,
    @NonNull List<SheetField> commlink,
    @NonNull List<List<String>> programs,
    @NonNull List<List<String>> magic,
    @NonNull List<List<String>> spirits,
    @NonNull List<List<String>> gear,
    @NonNull List<List<String>> situationalPools,
    @NonNull List<List<String>> foci,
    @NonNull List<List<String>> initiation,
    @NonNull List<String> notes
) {
  /** Deutsche Sortierung (Umlaute an ihrer Stelle im Alphabet), wie in den Listen der App. */
  private static final Collator GERMAN = Collator.getInstance(Locale.GERMAN);

  /** Liest alle Texte aus {@code original}; der Charakter selbst bleibt unverändert. */
  public static @NonNull SheetContent of(@NonNull Character original) {
    Character c = CharacterStore.restore(CharacterStore.snapshot(original));
    c.setPhysicalDamage(0);
    c.setStunDamage(0);
    CharacterBiography bio = c.getBiography();
    return new SheetContent(
        c.getName(),
        bio.getPortrait(),
        List.of(new SheetField("Metatyp", c.getMetatype().name()), new SheetField("Alter", bio.getAge()),
            new SheetField("Geschlecht", bio.getSex()),
            new SheetField("Lebensstil", c.getEquipment().getLifestyle().getLevel().getDisplayName())),
        List.of(new SheetField("Karma gesamt", Numbers.integer(c.getKarmaTotal())),
            new SheetField("Karma aktuell", ""), new SheetField("Nuyen", "")),
        List.of(new SheetField("Straßenruf", Numbers.integer(c.getStreetCred().value())),
            new SheetField("Schlechter Ruf", Numbers.integer(c.getNotoriety().value())),
            new SheetField("Prominenz", Numbers.integer(c.getPublicAwareness().value()))),
        attributes(c),
        activeSkills(c),
        knowledgeSkills(c),
        c.getQualities().stream().map(SheetContent::qualityRow).toList(),
        List.of(new SheetField("Größe", bio.getHeight()), new SheetField("Gewicht", bio.getWeight()),
            new SheetField("Augen", bio.getEyes()), new SheetField("Haare", bio.getHair()),
            new SheetField("Haut", bio.getSkin())),
        bio.getDescription(),
        markers(c, c.getPhysicalBoxes()),
        c.getPhysicalOverflowBoxes(),
        markers(c, c.getStunBoxes()),
        attributeTests(c),
        c.getContacts().stream().map(SheetContent::contactRow).toList(),
        implants(c),
        weapons(c, false),
        weapons(c, true),
        c.getEquipment().getArmor().stream().filter(Armor::isEquipped).map(SheetContent::armorRow).toList(),
        c.getEquipment().getVehicles().stream().map(v -> vehicleRow(VehicleDetails.of(v))).toList(),
        commlink(c),
        programs(c),
        magic(c),
        c.getSpirits().getAll().stream().map(SheetContent::spiritRow).toList(),
        gear(c),
        c.getSituationalPools().stream().map(SheetContent::situationalRow).toList(),
        c.getFoci().getAll().stream().map(SheetContent::focusRow).toList(),
        initiation(c),
        notes(bio));
  }

  /** Vier Blöcke wie im GRW: körperlich, geistig, Besondere (Edge …), Besondere (Initiativen). */
  private static @NonNull List<SheetBlock> attributes(@NonNull Character c) {
    SpecialAttribute magicOrResonance = c.getMagicOrResonance();
    MatrixAccess access = c.getMatrixAccess();
    boolean online = access != MatrixAccess.NONE;
    String passes = c.getInitiativePasses() + (online ? " / " + c.getMatrixInitiativePasses() : " / –")
        + (c.hasAstralAccess() ? " / " + Character.ASTRAL_INITIATIVE_PASSES : " / –");
    return List.of(
        new SheetBlock("Körperliche Attribute", attributeFields(c, AttributeType.BOD, AttributeType.AGI,
            AttributeType.REA, AttributeType.STR)),
        new SheetBlock("Geistige Attribute", attributeFields(c, AttributeType.CHA, AttributeType.INT,
            AttributeType.LOG, AttributeType.WIL)),
        new SheetBlock("Besondere Attribute", List.of(
            new SheetField("Edge", Numbers.integer(c.getEdge().getValue())),
            new SheetField("Essenz", Numbers.essence(c.getEssence())),
            new SheetField("Initiative", Numbers.integer(c.getInitiativeAttribute())),
            new SheetField(magicOrResonance == null ? "Magie/Resonanz" : magicOrResonance.getType().getDisplayName(),
                magicOrResonance == null ? Numbers.NO_VALUE : Numbers.integer(c.getEffectiveMagicOrResonanceValue())))),
        new SheetBlock("Kampf und Matrix", List.of(
            new SheetField("Aktuelles Edge", ""),
            new SheetField("Astrale INI", c.hasAstralAccess() ? Numbers.integer(c.getAstralInitiative()) : Numbers.NO_VALUE),
            new SheetField("Matrix INI", online ? Numbers.integer(c.getMatrixInitiative()) : Numbers.NO_VALUE),
            new SheetField("INI-Durchgänge", passes))));
  }

  /** „natürlich (wirksam)“, wenn Implantate, Kräfte o.Ä. den Wert ändern, sonst nur der Wert. */
  private static @NonNull List<SheetField> attributeFields(@NonNull Character c, @NonNull AttributeType... types) {
    List<SheetField> fields = new ArrayList<>();
    for (AttributeType type : types) {
      int natural = c.getAttribute(type).getValue();
      int augmented = c.getAugmentedAttributeValue(type);
      fields.add(new SheetField(type.getDisplayName(),
          natural == augmented ? Numbers.integer(natural) : Numbers.integer(natural) + " (" + augmented + ")"));
    }
    return fields;
  }

  /** Aktive Fertigkeiten mit Wert, nach Name: Bezeichnung (mit Spezialisierung), Wert, Attribut, Pool. */
  private static @NonNull List<List<String>> activeSkills(@NonNull Character c) {
    return c.getSkills().stream()
        .filter(skill -> skill.getRating() > 0)
        .sorted(Comparator.comparing(Skill::getName, GERMAN))
        .map(skill -> List.of(withSpecialization(skill.getName(), skill.getSpecialization()),
            Numbers.integer(skill.getRating()), AttributeType.abbreviationOf(skill.getLinkedAttribute()),
            poolText(c.getSkillDicePool(skill), skill.getSpecialization() != null)))
        .toList();
  }

  /** Wissens- und Sprachfertigkeiten: Muttersprache mit „M“ statt Wert und ohne Pool. */
  private static @NonNull List<List<String>> knowledgeSkills(@NonNull Character c) {
    return c.getKnowledgeSkills().stream()
        .filter(skill -> skill.isNativeLanguage() || skill.getRating() > 0)
        .sorted(Comparator.comparing(KnowledgeSkill::getName, GERMAN))
        .map(skill -> List.of(withSpecialization(skill.getName(), skill.getSpecialization()),
            skill.isNativeLanguage() ? "M" : Numbers.integer(skill.getRating()),
            skill.getCategory().getLinkedAttribute().getAbbreviation(),
            skill.isNativeLanguage() ? "" : poolText(c.getKnowledgeSkillDicePool(skill),
                skill.getSpecialization() != null)))
        .toList();
  }

  private static @NonNull String withSpecialization(@NonNull String name, @Nullable String specialization) {
    return specialization == null ? name : name + " (" + specialization + ")";
  }

  /** Würfel, mit Spezialisierung zusätzlich der Pool im Spezialgebiet: „7 (9)“; „–“ ohne mögliche Probe. */
  private static @NonNull String poolText(@NonNull Optional<DicePool> pool, boolean specialized) {
    return pool.map(p -> p.dice() + (specialized ? " (" + Character.withSpecialization(p) + ")" : ""))
        .orElse(Numbers.NO_VALUE);
  }

  private static @NonNull List<String> qualityRow(@NonNull CharacterQuality quality) {
    return List.of(quality.displayName(), quality.data().type() == QualityType.POSITIVE ? "Gabe" : "Handicap");
  }

  /** Je Kästchen der Verletzungsmodifikator, der mit ihm beginnt (S. 193, mit Schmerztoleranz u.Ä.), sonst leer. */
  private static @NonNull List<String> markers(@NonNull Character c, int boxes) {
    List<String> markers = new ArrayList<>();
    for (int box = 1; box <= boxes; box++) {
      int modifier = c.getColumnWoundModifier(box);
      markers.add(modifier != c.getColumnWoundModifier(box - 1) ? "−" + -modifier : "");
    }
    return markers;
  }

  private static @NonNull List<List<String>> attributeTests(@NonNull Character c) {
    List<List<String>> rows = new ArrayList<>();
    for (AttributeTest test : AttributeTest.values()) {
      rows.add(List.of(test.getDisplayName(), test.getFirst().getAbbreviation() + " + "
          + test.getSecond().getAbbreviation(), Numbers.integer(c.getAttributeTestPool(test).dice())));
    }
    return rows;
  }

  private static @NonNull List<String> contactRow(@NonNull Contact contact) {
    return List.of(contact.getName(), Numbers.integer(contact.getLoyalty()), Numbers.integer(contact.getConnection()));
  }

  /** Implantate samt eingebauter Teile (in Cybergliedmaßen), eingerückt mit „ – “. */
  private static @NonNull List<List<String>> implants(@NonNull Character c) {
    List<List<String>> rows = new ArrayList<>();
    for (Cyberware implant : c.getEquipment().getCyberware()) {
      rows.add(implantRow(implant, ""));
      implant.getInstalled().forEach(child -> rows.add(implantRow(child, "– ")));
    }
    return rows;
  }

  private static @NonNull List<String> implantRow(@NonNull Cyberware implant, @NonNull String prefix) {
    return List.of(prefix + implant.getDisplayName(), implant.getRating() > 0 ? Numbers.integer(implant.getRating()) : "",
        Numbers.essence(implant.getEssenceCost().doubleValue()),
        implant.getKind().getDisplayName() + ", " + implant.getGrade().getDisplayName());
  }

  /**
   * Fern- bzw. Nahkampfwaffen mit den Werten des Waffen-Tabs ({@link WeaponDetails}), dazu Waffen aus Rüstung und
   * Implantaten und, bei Nahkampf, der waffenlose Angriff.
   */
  private static @NonNull List<List<String>> weapons(@NonNull Character c, boolean melee) {
    List<Weapon> all = new ArrayList<>();
    if (melee) {
      all.add(c.getEquipment().getUnarmedAttack());
    }
    all.addAll(c.getEquipment().getWeapons());
    all.addAll(c.getEquipment().getArmorWeapons());
    all.addAll(c.getEquipment().getImplantWeapons());
    return all.stream()
        .filter(weapon -> weapon.getData().category().isMelee() == melee)
        .map(weapon -> WeaponDetails.of(weapon, c))
        .map(d -> melee
            ? List.of(d.name(), d.reach(), d.damage(), d.armorPiercing(), dice(d))
            : List.of(d.name(), d.damage(), d.armorPiercing(), d.mode(), d.recoil(), d.ammo(), dice(d)))
        .toList();
  }

  private static @NonNull String dice(@NonNull WeaponDetails details) {
    return poolText(Optional.ofNullable(details.dicePool()), details.specialization() != null);
  }

  private static @NonNull List<String> armorRow(@NonNull Armor armor) {
    ArmorDetails details = ArmorDetails.of(armor);
    return List.of(details.name(), details.protection(),
        String.join(", ", details.special().stream().map(Object::toString).toList()));
  }

  private static @NonNull List<String> vehicleRow(@NonNull VehicleDetails d) {
    return List.of(d.name(), d.handling(), d.accel(), d.speed(), d.pilot(), d.body(), d.armor(), d.sensor());
  }

  /** Aktives Kommlink mit Prozessor, System, Firewall, Signal (GRW S. 260); ohne Kommlink leere Felder. */
  private static @NonNull List<SheetField> commlink(@NonNull Character c) {
    Gear commlink = c.getEquipment().getActiveCommlink();
    MatrixStats stats = commlink == null ? null : commlink.getMatrixStats();
    if (commlink == null || stats == null) {
      String name = commlink == null ? "" : commlink.getDisplayName();
      return List.of(new SheetField("Kommlink", name), new SheetField("Prozessor", ""), new SheetField("System", ""),
          new SheetField("Firewall", ""), new SheetField("Signal", ""));
    }
    return List.of(
        new SheetField("Kommlink", commlink.getDisplayName()),
        new SheetField("Prozessor", Numbers.integer(stats.processor())),
        new SheetField("System", Numbers.integer(stats.system())),
        new SheetField("Firewall", Numbers.integer(stats.firewall())),
        new SheetField("Signal", Numbers.integer(stats.signal())));
  }

  private static @NonNull List<List<String>> programs(@NonNull Character c) {
    Gear commlink = c.getEquipment().getActiveCommlink();
    if (commlink == null) {
      return List.of();
    }
    return commlink.getInstalled().stream().filter(Gear::isProgram)
        .map(program -> List.of(program.getDisplayName(), program.isRunning() ? "ja" : ""))
        .toList();
  }

  /** Zauber, Adeptenkräfte und Komplexe Formen in einer Tabelle: Name, Art, Reichweite, Dauer, Entzug/Wert. */
  private static @NonNull List<List<String>> magic(@NonNull Character c) {
    List<List<String>> rows = new ArrayList<>();
    for (SpellData spell : c.getSpells()) {
      rows.add(List.of(spell.name(), spell.category() + ", " + spell.type(), spell.range(), spell.duration(),
          spell.dv()));
    }
    for (AdeptPower power : c.getAdeptPowers().getPowers()) {
      rows.add(List.of(power.displayName(), "Adeptenkraft", "", "",
          Numbers.decimal(c.getAdeptPowers().getCost(power)) + " KP"));
    }
    for (ComplexForm form : c.getComplexForms()) {
      rows.add(List.of(form.getName(), "Komplexe Form", "", "", "Stufe " + form.getRating()));
    }
    return rows;
  }

  /** Name mit den gewählten optionalen Kräften bzw. Komplexen Formen in Klammern, z.B. „Feuergeist (Grauen)“. */
  private static @NonNull List<String> spiritRow(@NonNull Spirit spirit) {
    var kind = spirit.getData().kind();
    String name = spirit.getOptions().isEmpty() ? spirit.getName()
        : spirit.getName() + " (" + String.join(", ", spirit.getOptions()) + ")";
    return List.of(name, spirit.isBound() ? kind.getBoundName() : kind.getUnboundName(),
        Numbers.integer(spirit.getForce()), Numbers.integer(spirit.getServices()));
  }

  /**
   * Ausrüstung mit dem Anzeigenamen der App (Stufe, Zusatz und Stückzahl im Stil der GRW-Listen), eingebaute Teile
   * eingerückt mit „– “. Foki stehen in ihrer eigenen Tabelle, die Programme des aktiven Kommlinks in der
   * Kommlink-Tabelle; beide fehlen deshalb hier.
   */
  private static @NonNull List<List<String>> gear(@NonNull Character c) {
    Gear active = c.getEquipment().getActiveCommlink();
    List<List<String>> rows = new ArrayList<>();
    for (Gear item : c.getEquipment().getGear()) {
      if (item.isFocus()) {
        continue;
      }
      rows.add(List.of(item.getDisplayName()));
      boolean activeCommlink = item.equals(active);
      item.getInstalled().stream()
          .filter(child -> !(activeCommlink && child.isProgram()))
          .forEach(child -> rows.add(List.of("– " + child.getDisplayName())));
    }
    return rows;
  }

  private static @NonNull List<String> situationalRow(@NonNull SituationalPool pool) {
    return List.of(pool.label(), pool.diceText(), pool.breakdown());
  }

  private static @NonNull List<String> focusRow(@NonNull Gear focus) {
    return List.of(focus.getDisplayName(),
        focus.isBound() ? (focus.isFocusActive() ? "gebunden, aktiv" : "gebunden") : "nicht gebunden");
  }

  /** Grad und gelernte Metamagien bzw. Echos; leer ohne Initiation oder Wandlung. */
  private static @NonNull List<List<String>> initiation(@NonNull Character c) {
    var initiation = c.getInitiation();
    if (initiation.getKind() == null || initiation.getGrade() == 0) {
      return List.of();
    }
    return Stream.concat(
            Stream.of(List.of("Grad", Numbers.integer(initiation.getGrade()))),
            initiation.getTechniques().stream().map(TechniqueData::name).distinct()
                .map(name -> List.of(name, "")))
        .toList();
  }

  private static @NonNull List<String> notes(@NonNull CharacterBiography bio) {
    return Stream.of(bio.getConcept(), bio.getBackground(), bio.getNotes())
        .filter(text -> !text.isBlank())
        .toList();
  }
}
