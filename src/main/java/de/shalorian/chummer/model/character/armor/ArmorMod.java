package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/** Ein in ein Rüstungsteil eingebautes Zubehör bzw. eine Panzerungsmodifikation mit gewählter Stufe. */
public final class ArmorMod {

  private final @NonNull ArmorModData data;
  private final int rating;

  /**
   * @param rating Stufe 1 bis Höchststufe, bzw. {@code 0} ohne Stufe
   * @throws IllegalArgumentException bei unzulässiger Stufe
   */
  public ArmorMod(@NonNull ArmorModData data, int rating) {
    if (!data.isValidRating(rating)) {
      throw new IllegalArgumentException("%s: Stufe %d ist nicht zulässig".formatted(data.name(), rating));
    }
    this.data = data;
    this.rating = rating;
  }

  public @NonNull ArmorModData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  public int getRating() {
    return rating;
  }

  /** Name mit Stufe, z.B. {@code "Elektrische Isolierung (Stufe 3)"}. */
  public @NonNull String getDisplayName() {
    return data.hasRating() ? data.name() + " (Stufe " + rating + ")" : data.name();
  }

  public int getNuyenCost() {
    return data.costFor(rating);
  }
}
