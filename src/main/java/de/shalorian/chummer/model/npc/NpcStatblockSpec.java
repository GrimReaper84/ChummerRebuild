package de.shalorian.chummer.model.npc;

import de.shalorian.chummer.model.character.Character;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Bauanleitung eines Vorlagen-Werteblocks: Metatyp, natürliche Attribute, Magie und Einträge aus den Katalogen.
 * {@link #build(String, List)} erzeugt daraus einen {@link Character} im Spielleiter-Modus. Was das Modell nicht abbildet
 * (z.B. eine Glied-Verbesserung über Stufe 3 ohne Cybertorso, S. 390), wird übersprungen und als Problem gemeldet,
 * statt den Werteblock abzubrechen; ein unbekannter Katalogname ist dagegen ein Fehler der Vorlage.
 *
 * @param metatype   Name des Metatyps
 * @param attributes natürliche Werte der acht Grundattribute
 * @param magic      Magiewert (nur mit Gabe Adept, Zauberer oder Magieradept), sonst 0
 * @param entries    Katalogeinträge in Dateireihenfolge
 * @param gearText   sonstige Ausrüstung als Text
 */
public record NpcStatblockSpec(@NonNull String metatype, @NonNull Map<AttributeType, Integer> attributes, int magic,
    @NonNull List<NpcEntry> entries, @NonNull String gearText) {

  public NpcStatblockSpec {
    attributes = Map.copyOf(attributes);
    entries = List.copyOf(entries);
  }

  /**
   * Baut den Werteblock.
   *
   * @param problems nimmt Einträge auf, die das Modell nicht übernehmen konnte, je mit Grund
   * @throws IllegalStateException bei einem Namen, den kein Katalog kennt
   */
  public @NonNull NpcStatblock build(@NonNull String name, @NonNull List<String> problems) {
    Character c = new Character(name, MetatypeDataLoader.find(metatype));
    NpcStatblock statblock = new NpcStatblock(c, gearText);
    attributes.forEach((type, value) -> c.getAttribute(type).setValue(value));
    // Gaben zuerst: Adept/Zauberer schalten Magie frei, Talente heben Maxima an.
    entries.stream().filter(e -> e.kind().equals("quality")).forEach(e -> addQuality(c, e));
    SpecialAttribute magicAttribute = c.getMagicOrResonance();
    if (magic > 0 && magicAttribute != null) {
      magicAttribute.setValue(magic);
    }
    for (NpcEntry entry : entries) {
      try {
        apply(c, entry, problems);
      } catch (IllegalArgumentException e) {
        problems.add(name + ": " + entry.name() + " – " + e.getMessage());
      }
    }
    return statblock;
  }

  private static void addQuality(@NonNull Character c, @NonNull NpcEntry e) {
    c.addQuality(required(QualityDataLoader.findByName(e.name()), e), false, e.detail());
  }

  private static void apply(@NonNull Character c, @NonNull NpcEntry e, @NonNull List<String> problems) {
    switch (e.kind()) {
      case "quality" -> { } // schon vorab
      case "skill" -> {
        Skill skill = required(c.getSkill(e.name()), e);
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
      case "cyberware" -> c.getImplants().add(implant(e, problems, c.getName()));
      case "limb" -> addLimb(c, e, problems);
      case "weapon" -> c.getEquipment().addWeapon(new Weapon(required(WeaponDataLoader.findByName(e.name()), e)));
      case "armor" -> {
        Armor armor = new Armor(required(ArmorDataLoader.findByName(e.name()), e), true);
        for (NpcEntry part : e.parts()) {
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
      default -> throw new IllegalStateException("Unbekannter Eintrag in npcs.xml: " + e.kind());
    }
  }

  /** Implantat samt Einbauten (z.B. Zubehör der Cyberaugen); ein Einbau, der nicht passt, wird gemeldet. */
  private static @NonNull Cyberware implant(@NonNull NpcEntry e, @NonNull List<String> problems, @NonNull String owner) {
    Cyberware implant = new Cyberware(cyberware(e), CyberwareGrade.STANDARD, e.rating());
    for (NpcEntry part : e.parts()) {
      try {
        implant.install(new Cyberware(cyberware(part), CyberwareGrade.STANDARD, part.rating()));
      } catch (IllegalArgumentException ex) {
        problems.add(owner + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
    return implant;
  }

  /** Cyberglied an der Körperstelle aus {@code detail}; {@code rating} ist hier die maßgefertigte Geschicklichkeit. */
  private static void addLimb(@NonNull Character c, @NonNull NpcEntry e, @NonNull List<String> problems) {
    int base = LimbAttributes.STANDARD.agility();
    LimbAttributes attributes = new LimbAttributes(LimbAttributes.STANDARD.body(), LimbAttributes.STANDARD.strength(),
        e.rating() > 0 ? e.rating() : base);
    Cyberware limb = new Cyberware(cyberware(e), CyberwareGrade.STANDARD, BodyLocation.valueOf(e.detail()), attributes);
    c.getImplants().add(limb);
    for (NpcEntry part : e.parts()) {
      try {
        c.getImplants().installInLimb(limb, new Cyberware(cyberware(part), CyberwareGrade.STANDARD, part.rating()));
      } catch (IllegalArgumentException ex) {
        problems.add(c.getName() + ": " + part.name() + " in " + e.name() + " – " + ex.getMessage());
      }
    }
  }

  private static @NonNull CyberwareData cyberware(@NonNull NpcEntry e) {
    return required(CyberwareDataLoader.findByName(e.name()), e);
  }

  private static <T> @NonNull T required(@Nullable T found, @NonNull NpcEntry e) {
    if (found == null) {
      throw new IllegalStateException("npcs.xml: " + e.kind() + " „" + e.name() + "“ steht in keinem Katalog");
    }
    return found;
  }
}
