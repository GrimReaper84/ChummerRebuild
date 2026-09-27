package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/**
 * Ein gekauftes Rüstungsteil eines Charakters: referenziert einen {@link ArmorData}-Katalogeintrag
 * und merkt sich, ob es gerade angelegt ist. Nur angelegte Teile zählen für Schutzwert und
 * Panzerungsbehinderung (siehe {@code Character#getArmorBallistic()}). Dasselbe Teil darf mehrfach
 * gekauft werden, daher arbeitet {@code Character#removeArmor} über die Objektreferenz.
 */
public class Armor {

  private final @NonNull ArmorData data;
  private boolean equipped;

  /** Neu gekaufte Rüstung ist angelegt (wie im C#-Original). */
  public Armor(@NonNull ArmorData data) {
    this(data, true);
  }

  public Armor(@NonNull ArmorData data, boolean equipped) {
    this.data = data;
    this.equipped = equipped;
  }

  public @NonNull ArmorData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  public boolean isEquipped() {
    return equipped;
  }

  public void setEquipped(boolean equipped) {
    this.equipped = equipped;
  }

  public int getNuyenCost() {
    return data.cost();
  }
}
