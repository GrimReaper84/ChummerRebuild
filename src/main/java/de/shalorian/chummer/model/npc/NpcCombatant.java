package de.shalorian.chummer.model.npc;

/**
 * Kampfzustand eines Schergen oder Anführers: Schaden auf dem einen gemeinsamen Zustandsmonitor (S. 324), körperlich
 * und geistig zusammen. Überschüssiger Schaden wird nicht eingetragen.
 */
public class NpcCombatant {

  private int damage;

  public int getDamage() {
    return damage;
  }

  /** Setzt den Schaden, begrenzt auf 0 bis {@code boxes}. */
  public void setDamage(int damage, int boxes) {
    this.damage = Math.clamp(damage, 0, boxes);
  }

  /** Ausgeschaltet, sobald der Monitor voll ist. */
  public boolean isKnockedOut(int boxes) {
    return damage >= boxes;
  }
}
