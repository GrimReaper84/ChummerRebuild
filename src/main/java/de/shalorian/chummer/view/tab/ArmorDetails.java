package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import de.shalorian.chummer.utils.Numbers;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Anzeigewerte eines Rüstungsteils im Detailbereich, ohne JavaFX und damit testbar.
 *
 * @param protection Schutz samt Zubehör, bei Helmen und Schilden als Bonus („+1/+2“)
 * @param special    Sonderschutz durch Modifikationen ({@link Armor#getSpecialProtections()}, GRW S. 372)
 */
record ArmorDetails(@NonNull String name, @NonNull String category, @NonNull String protection,
                    @NonNull String availability, @NonNull String price, @NonNull String source,
                    @NonNull List<SpecialProtection> special, @NonNull String state) {

  static @NonNull ArmorDetails of(@NonNull Armor armor) {
    ArmorData data = armor.getData();
    String prefix = data.category().isBonus() ? "+" : "";
    return new ArmorDetails(
        armor.getName(),
        data.category().getDisplayName() + (data.shield() ? " (Schild: −1 auf körperliche Proben)" : ""),
        prefix + armor.getBallistic() + "/" + prefix + armor.getImpact(),
        data.availabilityText(),
        Numbers.nuyen(armor.getNuyenCost()),
        data.source() + " S. " + data.page(),
        armor.getSpecialProtections(),
        armor.isEquipped() ? "angelegt" : "abgelegt");
  }
}
