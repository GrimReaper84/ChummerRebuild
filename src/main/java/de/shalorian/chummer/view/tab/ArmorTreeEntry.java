package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorCategory;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Knotenwert im Rüstungsbaum: eine Kategorie ({@code armor == null}), ein Rüstungsteil oder — als Kind des Teils —
 * ein eingebautes Zubehör bzw. eine Modifikation.
 *
 * @param armor das Teil des Knotens bzw. das Teil, in dem der Einbau sitzt; {@code null} bei einer Kategorie
 * @param mod   der Einbau, sonst {@code null}
 */
record ArmorTreeEntry(@NonNull String label, @Nullable Armor armor, @Nullable ArmorMod mod) {

  static @NonNull ArmorTreeEntry ofCategory(@NonNull ArmorCategory category) {
    return new ArmorTreeEntry(category.getDisplayName(), null, null);
  }

  /** Teil; abgelegte tragen den Zusatz „(abgelegt)“. */
  static @NonNull ArmorTreeEntry ofArmor(@NonNull Armor armor) {
    return new ArmorTreeEntry(armor.isEquipped() ? armor.getName() : armor.getName() + " (abgelegt)", armor, null);
  }

  static @NonNull ArmorTreeEntry ofMod(@NonNull Armor armor, @NonNull ArmorMod mod) {
    return new ArmorTreeEntry(mod.getDisplayName(), armor, mod);
  }

  @Override
  public @NonNull String toString() {
    return label;
  }
}
