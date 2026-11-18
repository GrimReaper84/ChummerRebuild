package de.shalorian.chummer.model.template;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.ArmorModDataLoader;
import de.shalorian.chummer.model.character.adept.AdeptPower;
import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.LimbAttributes;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.KnowledgeSkill;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Bauanleitung einer Vorlage (Schergen, Archetypen): Metatyp, natürliche Attribute, Magie und Einträge aus den
 * Katalogen. {@link #build(String, BuildMode, List)} erzeugt daraus einen {@link Character}, im {@link BuildMode#GM}
 * als fertigen Werteblock der Spielleitung, im {@link BuildMode#CREATION} in der normalen Erschaffung, in der alle
 * Käufe BP bzw. Ressourcen-Nuyen kosten wie von Hand. Was das Modell nicht abbildet
 * (z.B. eine Glied-Verbesserung über Stufe 3 ohne Cybertorso, S. 390), wird übersprungen und als Problem gemeldet,
 * statt den Werteblock abzubrechen; ein unbekannter Katalogname ist dagegen ein Fehler der Vorlage.
 *
 * @param metatype   Name des Metatyps
 * @param attributes natürliche Werte der acht Grundattribute
 * @param magic      Magiewert (nur mit Gabe Adept, Zauberer oder Magieradept), sonst 0
 * @param entries    Katalogeinträge in Dateireihenfolge
 * @param gearText   sonstige Ausrüstung als Text (Schergen-Bogen bzw. Notiz)
 */
public record CharacterSpec(@NonNull String metatype, @NonNull Map<AttributeType, Integer> attributes, int magic,
    @NonNull List<TemplateEntry> entries, @NonNull String gearText) {

  private static final String PREFIX = "Vorlage:";
  private static final String BOUND = "gebunden";
  private static final String NATIVE = "muttersprache";

  public CharacterSpec {
    attributes = Map.copyOf(attributes);
    entries = List.copyOf(entries);
  }

  /**
   * Baut den Charakter.
   *
   * @param mode     Spielleiter-Werteblock oder normale Erschaffung
   * @param problems nimmt Einträge auf, die das Modell nicht übernehmen konnte, je mit Grund
   * @throws IllegalStateException bei einem Namen, den kein Katalog kennt
   */
  public @NonNull Character build(@NonNull String name, @NonNull BuildMode mode, @NonNull List<String> problems) {
    Character c = new Character(name, MetatypeDataLoader.find(metatype));
    if (mode == BuildMode.GM) {
      c.setCreated(true);
      c.setGmMode(true);
    }
    attributes.forEach((type, value) -> c.getAttribute(type).setValue(value));
    // Gaben zuerst: Adept/Zauberer schalten Magie frei, Talente heben Maxima an.
    entries.stream().filter(e -> e.kind().equals("quality")).forEach(e -> addQuality(c, e));
    SpecialAttribute magicAttribute = c.getMagicOrResonance();
    if (magic > 0 && magicAttribute != null) {
      magicAttribute.setValue(magic);
    }
    for (TemplateEntry entry : entries) {
      try {
        apply(c, entry, mode, problems);
      } catch (IllegalArgumentException e) {
        problems.add(name + ": " + entry.name() + " – " + e.getMessage());
      }
    }
    return c;
  }

  private static void addQuality(@NonNull Character c, @NonNull TemplateEntry e) {
    c.addQuality(required(QualityDataLoader.findByName(e.name()), e), false, e.detail());
  }

  private static void apply(@NonNull Character c, @NonNull TemplateEntry e, @NonNull BuildMode mode,
      @NonNull List<String> problems) {
    switch (e.kind()) {
      case "quality" -> { } // schon vorab
      case "skill" -> {
        Skill skill = required(c.getSkill(e.name()), e);
        // Einzeln gekaufte Fertigkeit einer Gruppe in der Erschaffung: die Gruppe aufbrechen, damit die Stufe einzeln
        // bezahlt wird; Schergen (GM) bleiben unverändert, ihr Bogen bietet nur nicht aufgebrochene Gruppen an
        String group = skill.getGroupName();
        if (mode == BuildMode.CREATION && group != null) {
          c.decoupleGroup(group);
        }
        skill.setRating(e.rating());
        if (!e.detail().isEmpty()) {
          skill.setSpecialization(e.detail());
        }
      }
      case "group" -> {
        while (c.getGroupRating(e.name()) < e.rating()) {
          c.raiseGroup(e.name());
        }
      }
      case "knowledge" -> {
        c.addKnowledgeSkill(e.name(), KnowledgeSkillCategory.valueOf(e.detail()));
        KnowledgeSkill knowledge = c.getKnowledgeSkills().getLast();
        knowledge.setRating(e.rating());
      }
      case "language" -> addLanguage(c, e);
      case "cyberware" -> c.getImplants().add(implant(e, problems, c.getName()));
      case "limb" -> addLimb(c, e, problems);
      case "weapon" -> c.getEquipment().addWeapon(weapon(e));
      case "armor" -> {
        ArmorData data = required(ArmorDataLoader.findByName(e.name()), e);
        // Kleidung mit Preisspanne kostet den Katalogpreis
        Armor armor = new Armor(data, true, data.hasVariablePrice() ? Integer.valueOf(data.cost()) : null);
        for (TemplateEntry part : e.parts()) {
          ArmorModData mod = required(ArmorModDataLoader.findByName(part.name()), part);
          armor.install(new ArmorMod(mod, part.rating()));
        }
        c.getEquipment().addArmor(armor);
      }
      case "spell" -> c.addSpell(required(SpellDataLoader.findByName(e.name()), e));
      case "power" -> {
        AdeptPowerData data = required(AdeptPowerDataLoader.findByName(e.name()), e);
        AdeptPower power = c.getAdeptPowers().add(data, e.detail());
        if (e.rating() > 1) {
          power.setLevel(e.rating());
        }
      }
      case "complexform" -> {
        c.addComplexForm(required(ComplexFormDataLoader.findByName(e.name()), e));
        ComplexForm form = c.getComplexForms().getLast();
        if (e.rating() > 0) {
          form.setRating(e.rating());
        }
      }
      case "gear", "focus" -> addGear(c, e, problems);
      case "vehicle" -> addVehicle(c, e, problems);
      case "spirit" -> c.getSpirits().add(new Spirit(required(SpiritDataLoader.findByName(e.name()), e), e.rating(),
          e.amount(), e.detail().equals(BOUND)));
      case "contact" -> {
        c.addContact(e.name(), e.detail().isEmpty() ? null : e.detail());
        Contact contact = c.getContacts().getLast();
        contact.setConnection(e.rating());
        contact.setLoyalty(e.amount());
      }
      case "lifestyle" -> {
        Lifestyle lifestyle = c.getEquipment().getLifestyle();
        lifestyle.setLevel(LifestyleLevel.valueOf(e.name()));
        lifestyle.setMonths(Math.max(1, e.amount()));
      }
      case "resources" -> c.setNuyenBP(e.rating());
      case "edge" -> c.getEdge().setValue(e.rating());
      default -> throw new IllegalStateException(PREFIX + " Eintragsart " + e.kind());
    }
  }

  /** Muttersprache ({@code detail="muttersprache"}) oder Sprachfertigkeit mit Stufe und optional Spezialisierung. */
  private static void addLanguage(@NonNull Character c, @NonNull TemplateEntry e) {
    if (e.detail().equals(NATIVE)) {
      c.setNativeLanguage(e.name());
      return;
    }
    c.addKnowledgeSkill(e.name(), KnowledgeSkillCategory.LANGUAGE);
    KnowledgeSkill language = c.getKnowledgeSkills().getLast();
    language.setRating(e.rating());
    if (!e.detail().isEmpty()) {
      language.setSpecialization(e.detail());
    }
  }

  /** Waffe (mit Stufe, z.B. Bogen) samt Zubehör; {@code detail} eines Zubehörs ist die Halterung, leer für Zubehör ohne Halterung. */
  private static @NonNull Weapon weapon(@NonNull TemplateEntry e) {
    WeaponData data = required(WeaponDataLoader.findByName(e.name()), e);
    Weapon weapon = e.rating() > 0 ? new Weapon(data, e.rating()) : new Weapon(data);
    for (TemplateEntry part : e.parts()) {
      Mount mount = part.detail().isEmpty() ? null : Mount.valueOf(part.detail());
      weapon.addAccessory(new WeaponAccessory(required(AccessoryDataLoader.findByName(part.name()), part), mount));
    }
    return weapon;
  }

  /** Ausrüstung samt Einbauten (z.B. Programme im Kommlink); ein Eintrag {@code focus} wird gebunden. */
  private static void addGear(@NonNull Character c, @NonNull TemplateEntry e, @NonNull List<String> problems) {
    Gear gear = gear(e);
    for (TemplateEntry part : e.parts()) {
      try {
        gear.install(gear(part));
      } catch (IllegalArgumentException ex) {
        problems.add(c.getName() + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
    c.getEquipment().addGear(gear);
    if (e.kind().equals("focus")) {
      c.getFoci().bind(gear);
    }
  }

  private static @NonNull Gear gear(@NonNull TemplateEntry e) {
    GearData data = required(GearDataLoader.findByName(e.name()), e);
    // detail ist die Auswahl (z.B. Geisterart eines Bindungsfokus), amount die Menge; Gegenstände mit frei gewähltem
    // Preis kosten den Katalog-Grundpreis.
    Integer price = data.variableCost() ? Integer.valueOf(data.cost().trim()) : null;
    return new Gear(data, e.rating(), e.amount() > 0 ? e.amount() : data.packSize(), e.detail(), price);
  }

  /** Fahrzeug samt Ausrüstung und Waffen; jede Waffe kommt in die erste freie bzw. eine neue Halterung. */
  private static void addVehicle(@NonNull Character c, @NonNull TemplateEntry e, @NonNull List<String> problems) {
    Vehicle vehicle = new Vehicle(required(VehicleDataLoader.findByName(e.name()), e));
    c.getEquipment().addVehicle(vehicle);
    for (TemplateEntry part : e.parts()) {
      try {
        if (part.kind().equals("weapon")) {
          WeaponMount mount = vehicle.firstFreeMount();
          vehicle.mountWeapon(mount != null ? mount : vehicle.addMount(), weapon(part));
        } else {
          vehicle.addGear(gear(part));
        }
      } catch (IllegalArgumentException ex) {
        problems.add(c.getName() + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
  }

  /** Implantat samt Einbauten (z.B. Zubehör der Cyberaugen); ein Einbau, der nicht passt, wird gemeldet. */
  private static @NonNull Cyberware implant(@NonNull TemplateEntry e, @NonNull List<String> problems, @NonNull String owner) {
    CyberwareGrade grade = e.detail().isEmpty() ? CyberwareGrade.STANDARD : CyberwareGrade.valueOf(e.detail());
    Cyberware implant = new Cyberware(cyberware(e), grade, e.rating());
    for (TemplateEntry part : e.parts()) {
      try {
        implant.install(new Cyberware(cyberware(part), CyberwareGrade.STANDARD, part.rating()));
      } catch (IllegalArgumentException ex) {
        problems.add(owner + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
    return implant;
  }

  /**
   * Cyberglied an der Körperstelle aus {@code detail}; {@code rating} ist die maßgefertigte Geschicklichkeit,
   * {@code amount} die maßgefertigte Stärke (je 0 für den Standardwert).
   */
  private static void addLimb(@NonNull Character c, @NonNull TemplateEntry e, @NonNull List<String> problems) {
    LimbAttributes standard = LimbAttributes.STANDARD;
    LimbAttributes attributes = new LimbAttributes(standard.body(),
        e.amount() > 0 ? e.amount() : standard.strength(), e.rating() > 0 ? e.rating() : standard.agility());
    Cyberware limb = new Cyberware(cyberware(e), CyberwareGrade.STANDARD, BodyLocation.valueOf(e.detail()), attributes);
    c.getImplants().add(limb);
    for (TemplateEntry part : e.parts()) {
      try {
        c.getImplants().installInLimb(limb, new Cyberware(cyberware(part), CyberwareGrade.STANDARD, part.rating()));
      } catch (IllegalArgumentException ex) {
        problems.add(c.getName() + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
  }

  private static @NonNull CyberwareData cyberware(@NonNull TemplateEntry e) {
    return required(CyberwareDataLoader.findByName(e.name()), e);
  }

  private static <T> @NonNull T required(@Nullable T found, @NonNull TemplateEntry e) {
    if (found == null) {
      throw new IllegalStateException(PREFIX + " " + e.kind() + " „" + e.name() + "“ steht in keinem Katalog");
    }
    return found;
  }
}
