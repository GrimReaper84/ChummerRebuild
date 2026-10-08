package de.shalorian.chummer.model.character.gear;

import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Art des Zusatztexts, den ein Gegenstand beim Kauf braucht (Fertigkeit eines Werkzeugs, Thema einer
 * Datasoft, Name einer gefälschten SIN …). Die Feldbezeichnung steht je Eintrag in {@link
 * GearData#extraLabel()}. Ist ein Zusatztext vorgesehen, ist er Pflicht. Arten mit Vorschlägen
 * bieten eine Auswahl; nur {@link #LICENSE} erlaubt darüber hinaus Freitext.
 */
public enum ExtraKind {
  /** Kein Zusatztext. */
  NONE,
  /** Freitext. */
  TEXT,
  /**
   * Lizenz einer gefälschten Lizenz: Vorauswahl der GRW-Beispiele plus Freitext. SR4-Grundregelwerk
   * S. 377: Gefälschte Lizenzen gibt es "für den Kauf lizenzierter Waren … oder bestimmte Aktivitäten
   * (Jagd, verdecktes Tragen einer Waffe, Zauberei usw.)"; S. 131 nennt Detektiv-Lizenz, Führerschein
   * und Waffenlizenz. Nicht modelliert: S. 359 "Eine gefälschte Lizenz ist immer mit einer falschen
   * SIN verbunden".
   */
  LICENSE,
  /**
   * Waffenklasse, für die Munition hergestellt ist. SR4-Grundregelwerk S. 368/369: "Der Einfachheit
   * halber können Waffen einer Klasse die Munition untereinander austauschen". Auswahl der
   * Waffenklassen mit Munition ({@link WeaponCategory#usesAmmunition()}), kein Freitext.
   */
  WEAPON_CLASS,
  /**
   * Standardgranate oder Minigranate. SR4-Grundregelwerk S. 370: "Minigranaten haben den gleichen Preis und
   * die gleiche Wirkung wie normale Granaten." Auswahl, kein Freitext.
   */
  GRENADE_FORM;

  private static final List<String> LICENSE_SUGGESTIONS = List.of("Waffenlizenz",
      "Verdecktes Tragen", "Jagdlizenz", "Zauberlizenz", "Detektivlizenz", "Führerschein");
  private static final List<String> GRENADE_FORMS = List.of("Standard", "Minigranate");

  /** Vorschläge bzw. Auswahl für den Zusatztext; leer, wenn nur Freitext vorgesehen ist. */
  public @NonNull List<String> suggestions() {
    return switch (this) {
      case LICENSE -> LICENSE_SUGGESTIONS;
      case WEAPON_CLASS -> Arrays.stream(WeaponCategory.values())
          .filter(WeaponCategory::usesAmmunition).map(WeaponCategory::getDisplayName).toList();
      case GRENADE_FORM -> GRENADE_FORMS;
      default -> List.of();
    };
  }

  /** Ob der Zusatztext eine der {@link #suggestions()} sein muss (Auswahl statt Freitext). */
  public boolean isChoice() {
    return this == WEAPON_CLASS || this == GRENADE_FORM;
  }
}
