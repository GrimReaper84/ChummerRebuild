package de.shalorian.chummer.model.character.technomancer;

import org.jspecify.annotations.NonNull;

/**
 * Lebende Persona eines Technomancers (GRW S. 279, Tabelle am Bild geprüft): Firewall = Willenskraft, Prozessor =
 * Intuition (in VR +1), Signal = halbe Resonanz aufgerundet, System = Logik; keines dieser vier Persona-Attribute liegt
 * über der Resonanz (Beispiel S. 279: Resonanz 3, Willenskraft 5 → Firewall 3). Der Biofeedback-Filter entspricht dem
 * Charisma; die Grenze durch die Resonanz gilt laut Tabelle nur für die vier Persona-Attribute.
 *
 * @param processor Prozessor außerhalb von VR; in VR um 1 höher ({@link #vrProcessor()})
 * @param intuition wirksame Intuition, Grundlage der VR-Initiative
 */
public record LivingPersona(int firewall, int processor, int signal, int system, int biofeedbackFilter,
                            int resonance, int intuition) {

  /** Persona aus den wirksamen Attributwerten und der wirksamen Resonanz. */
  public static @NonNull LivingPersona of(int willpower, int intuition, int logic, int charisma, int resonance) {
    return new LivingPersona(Math.min(willpower, resonance), Math.min(intuition, resonance), (resonance + 1) / 2,
        Math.min(logic, resonance), charisma, resonance, intuition);
  }

  /**
   * VR-Matrixinitiative ohne Verletzungsmodifikator: (Intuition × 2) + 1 (S. 279). Die Tabelle nennt sie getrennt von den
   * Persona-Attributen, die Resonanzgrenze gilt für sie deshalb nicht.
   */
  public int matrixInitiative() {
    return intuition * 2 + 1;
  }

  /** Prozessor in VR: Intuition + 1, höchstens Resonanz. */
  public int vrProcessor() {
    return Math.min(processor + 1, resonance);
  }
}
