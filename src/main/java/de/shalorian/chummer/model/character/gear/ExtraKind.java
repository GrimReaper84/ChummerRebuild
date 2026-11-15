package de.shalorian.chummer.model.character.gear;

import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.KnowledgeSuggestionLoader;
import de.shalorian.chummer.model.character.skill.SkillDataLoader;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.NonNull;

/**
 * Art des Zusatztexts, den ein Gegenstand beim Kauf braucht (Fertigkeit eines Werkzeugs, Thema einer
 * Datasoft, Name einer gefälschten SIN …). Die Feldbezeichnung steht je Eintrag in {@link
 * GearData#extraLabel()}. Ist ein Zusatztext vorgesehen, ist er Pflicht. Arten mit Vorschlägen
 * bieten eine Auswahl; {@link #LICENSE}, {@link #LANGUAGE} und {@link #KNOWLEDGE_SKILL} erlauben darüber hinaus
 * Freitext.
 */
public enum ExtraKind {
  /** Kein Zusatztext. */
  NONE,
  /** Freitext. */
  TEXT,
  /**
   * Lizenz einer gefälschten Lizenz: Vorauswahl der GRW-Beispiele plus Freitext. SR4-Grundregelwerk
   * S. 377: Gefälschte Lizenzen decken lizenzpflichtige Waren oder Tätigkeiten ab (z.B. Jagd, verdecktes
   * Waffentragen, Zauberei); S. 131 nennt Detektiv-Lizenz, Führerschein und Waffenlizenz. Nicht
   * modelliert: Laut S. 359 hängt jede gefälschte Lizenz an einer falschen SIN.
   */
  LICENSE,
  /**
   * Waffenklasse, für die Munition hergestellt ist. SR4-Grundregelwerk S. 368/369: Munition passt
   * vereinfacht in alle Waffen derselben Klasse. Auswahl der
   * Waffenklassen mit Munition ({@link WeaponCategory#usesAmmunition()}), kein Freitext.
   */
  WEAPON_CLASS,
  /**
   * Standardgranate oder Minigranate. SR4-Grundregelwerk S. 370: Minigranaten kosten und wirken wie
   * normale Granaten. Auswahl, kein Freitext.
   */
  GRENADE_FORM,
  /**
   * Art einer Autosoft. SR4-Grundregelwerk S. 287 beschreibt die grundlegenden Autosofts Abwehr,
   * Clearsight, Elektronische Kriegsführung, "Steuerung (Fahrzeugtyp)" und "Zielerfassung
   * (Waffentyp)". Auswahl, kein Freitext; den Fahrzeug- bzw. Waffentyp erfasst die Auswahl nicht
   * (Nutzerentscheidung 2026-10-09).
   */
  AUTOSOFT,
  /** Zauberkategorie eines Zauberfokus (GRW S. 234): Heilung, Illusion, Kampf, Manipulation, Wahrnehmung. */
  SPELL_CATEGORY,
  /** Geisterart eines Geisterfokus (GRW S. 234), eine der sechs Geisterarten aus {@code spirits.xml}. */
  SPIRIT_TYPE,
  /** Nahkampffertigkeit eines Waffenfokus (GRW S. 234; Zuordnung über die Fertigkeit, Plan-Annahme A2). */
  MELEE_SKILL,
  /**
   * Fertigkeit einer Aktionssoft (GRW S. 377): Kampf-, Körperliche, Soziale, Technische und Fahrzeugfertigkeiten, nicht
   * Magie oder Resonanz; ohne die exotischen Grundfertigkeiten (Plan-Annahme A6). Auswahl, kein Freitext.
   */
  ACTIVE_SKILL,
  /** Sprache einer Linguasoft (S. 377): Vorschläge aus der Sprachliste, Freitext erlaubt. */
  LANGUAGE,
  /** Wissensfertigkeit einer Wissenssoft (S. 377): Vorschläge aus der Wissensliste, Freitext erlaubt. */
  KNOWLEDGE_SKILL;

  private static final List<String> LICENSE_SUGGESTIONS = List.of("Waffenlizenz",
      "Verdecktes Tragen", "Jagdlizenz", "Zauberlizenz", "Detektivlizenz", "Führerschein");
  private static final List<String> GRENADE_FORMS = List.of("Standard", "Minigranate");
  private static final List<String> AUTOSOFTS = List.of("Abwehr", "Clearsight",
      "Elektronische Kriegsführung", "Steuerung", "Zielerfassung");
  private static final List<String> SPELL_CATEGORIES = List.of("Heilung", "Illusion", "Kampf", "Manipulation",
      "Wahrnehmung");
  private static final List<String> MELEE_SKILLS = List.of("Klingenwaffen", "Knüppel", "Exotische Nahkampfwaffe");

  /** Vorschläge bzw. Auswahl für den Zusatztext; leer, wenn nur Freitext vorgesehen ist. */
  public @NonNull List<String> suggestions() {
    return switch (this) {
      case LICENSE -> LICENSE_SUGGESTIONS;
      case WEAPON_CLASS -> Arrays.stream(WeaponCategory.values())
          .filter(WeaponCategory::usesAmmunition).map(WeaponCategory::getDisplayName).toList();
      case GRENADE_FORM -> GRENADE_FORMS;
      case AUTOSOFT -> AUTOSOFTS;
      case SPELL_CATEGORY -> SPELL_CATEGORIES;
      case SPIRIT_TYPE -> SpiritDataLoader.getAll().values().stream()
          .filter(d -> d.kind() == SpiritKind.SPIRIT).map(SpiritData::name).toList();
      case MELEE_SKILL -> MELEE_SKILLS;
      case ACTIVE_SKILL -> SkillDataLoader.skillsoftSkillNames();
      case LANGUAGE -> KnowledgeSuggestionLoader.names(KnowledgeSkillCategory.LANGUAGE);
      case KNOWLEDGE_SKILL -> Arrays.stream(KnowledgeSkillCategory.values())
          .filter(c -> c != KnowledgeSkillCategory.LANGUAGE)
          .flatMap(c -> KnowledgeSuggestionLoader.names(c).stream())
          .distinct()
          .sorted(Collator.getInstance(Locale.GERMAN))
          .toList();
      default -> List.of();
    };
  }

  /** Ob der Zusatztext eine der {@link #suggestions()} sein muss (Auswahl statt Freitext). */
  public boolean isChoice() {
    return this != NONE && this != TEXT && this != LICENSE && this != LANGUAGE && this != KNOWLEDGE_SKILL;
  }
}
