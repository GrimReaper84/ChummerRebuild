package de.shalorian.chummer.model.character.cyberware;

import java.util.Arrays;
import org.jspecify.annotations.NonNull;

/**
 * Dauerhafte, rechnende Wirkung eines Implantats. Situative Wirkungen (z.B. Toxinwiderstand, Geruchsproben) stehen nur
 * als Hinweis im Katalog. Der Wert kommt aus der Formel von {@link ImplantEffect}.
 */
public enum ImplantEffectType {
  /** Bonus auf ein Attribut ({@link ImplantEffect#target()} = Kürzel), z.B. Kunstmuskeln (GRW S. 389). */
  ATTRIBUTE("attribute"),
  /** Zusätzliche Initiativedurchgänge: Reflexbooster (S. 389), Synapsenbeschleuniger (S. 394). */
  INITIATIVE_PASSES("initiativepasses"),
  /** Ballistische Panzerung: Dermalpanzerung, Titan-Kompositknochen (S. 388), Orthoskin (S. 393). */
  ARMOR_BALLISTIC("armorballistic"),
  /** Stoßpanzerung: Dermalpanzerung, Aluminium-/Titan-Kompositknochen (S. 388), Orthoskin (S. 393). */
  ARMOR_IMPACT("armorimpact"),
  /** Würfel auf alle Fertigkeiten einer Gruppe ({@link ImplantEffect#target()} = Gruppenname): Synthacardium (S. 393). */
  SKILL_GROUP_DICE("skillgroupdice"),
  /** Würfel auf soziale Fertigkeiten: Maßgeschneiderte Pheromone (S. 393). */
  SOCIAL_SKILL_DICE("socialskilldice"),
  /** Würfel auf Fertigkeiten mit körperlichem verbundenem Attribut: Hochleistungsgelenke (S. 393). */
  PHYSICAL_SKILL_DICE("physicalskilldice"),
  /** Würfel auf Wissens- und Sprachfertigkeiten: Mnemoverstärker (S. 393/394). */
  KNOWLEDGE_SKILL_DICE("knowledgeskilldice"),
  /** Würfel auf die beim Kauf gewählte Fertigkeit oder Fertigkeitsgruppe: Reflexrekorder (S. 394). */
  SELECTED_SKILL_DICE("selectedskilldice"),
  /** Kästchen je Spalte, die beim Verletzungsmodifikator nicht zählen: Schadenskompensator (S. 394). */
  WOUND_BOXES_IGNORED("woundboxesignored"),
  /** Direktes Neuralinterface für VR: Datenbuchse, Kommlink, Sim-Modul (S. 386), Simrig (S. 389); siehe GRW S. 258. */
  DNI("dni"),
  /** Implantiertes Sim-Modul: 1 = kaltes Sim, 2 = auch heißes Sim (S. 374, 386). */
  SIM_MODULE("simmodule"),
  /** Talentleitungen: Stufe, bis zu der Aktionssofts genutzt werden können (S. 389). */
  SKILLWIRES("skillwires");

  private final @NonNull String code;

  ImplantEffectType(@NonNull String code) {
    this.code = code;
  }

  /**
   * Typ zum {@code type}-Attribut eines {@code <effect>}-Tags.
   *
   * @throws IllegalArgumentException bei unbekanntem Code (Datenfehler im Katalog)
   */
  public static @NonNull ImplantEffectType fromCode(@NonNull String code) {
    return Arrays.stream(values())
        .filter(t -> t.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unbekannte Implantat-Wirkung: " + code));
  }
}
