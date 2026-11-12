package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekauftes Rüstungsteil eines Charakters: referenziert einen {@link ArmorData}-Katalogeintrag
 * und merkt sich, ob es gerade angelegt ist. Nur angelegte Teile zählen für Schutzwert und
 * Panzerungsbehinderung (siehe {@code Equipment#getArmorBallistic()}). Dasselbe Teil darf mehrfach
 * gekauft werden, daher arbeitet {@code Equipment#removeArmor} über die Objektreferenz.
 */
public class Armor {

  private final @NonNull ArmorData data;
  private final @Nullable Integer chosenPrice;
  private boolean equipped;

  /** Neu gekaufte Rüstung mit festem Preis ist angelegt (wie im C#-Original). */
  public Armor(@NonNull ArmorData data) {
    this(data, true, null);
  }

  public Armor(@NonNull ArmorData data, boolean equipped) {
    this(data, equipped, null);
  }

  /**
   * @param chosenPrice vom Spieler gewählter Preisanteil bei Kleidung (siehe {@link ArmorData#hasVariablePrice()}),
   *                    sonst {@code null}
   * @throws IllegalArgumentException wenn der Preis fehlt, nicht erlaubt ist oder außerhalb der Spanne liegt
   */
  public Armor(@NonNull ArmorData data, boolean equipped, @Nullable Integer chosenPrice) {
    if (data.hasVariablePrice() ? chosenPrice == null || !data.isValidPrice(chosenPrice) : chosenPrice != null) {
      throw new IllegalArgumentException("%s: Preis %s ungültig".formatted(data.name(), chosenPrice));
    }
    this.data = data;
    this.equipped = equipped;
    this.chosenPrice = chosenPrice;
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

  /** Gewählter Preisanteil bei Kleidung, sonst {@code null}. */
  public @Nullable Integer getChosenPrice() {
    return chosenPrice;
  }

  /** Kaufpreis: fester Preis bzw. Aufschlag plus der gewählte Kleidungspreis. */
  public int getNuyenCost() {
    return data.cost() + (chosenPrice == null ? 0 : chosenPrice);
  }
}
