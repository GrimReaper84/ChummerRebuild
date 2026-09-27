package de.shalorian.chummer.model.character.weapon;

import org.jspecify.annotations.NonNull;

/**
 * Eine gekaufte Waffe eines Charakters: referenziert einen {@link WeaponData}-Katalogeintrag und —
 * nur beim Bogen — die gewählte Stufe (Mindeststärke). Dieselbe Waffe darf mehrfach gekauft werden,
 * daher arbeitet {@code Character#removeWeapon} über die Objektreferenz.
 */
public class Weapon {

  private final @NonNull WeaponData data;
  private final int rating;

  /**
   * Waffe mit Stufe.
   *
   * @param rating bei Waffen mit Stufe in [{@value WeaponData#MIN_RATING}, {@link
   *               WeaponData#maxRating()}], sonst {@code 0}
   * @throws IllegalArgumentException bei unzulässiger Stufe
   */
  public Weapon(@NonNull WeaponData data, int rating) {
    if (!data.isValidRating(rating)) {
      throw new IllegalArgumentException(
          "%s: Stufe %d ist nicht zulässig (max. %d)".formatted(data.name(), rating,
              data.maxRating()));
    }
    this.data = data;
    this.rating = rating;
  }

  /** Waffe ohne Stufe. */
  public Weapon(@NonNull WeaponData data) {
    this(data, 0);
  }

  public @NonNull WeaponData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Gewählte Stufe, bzw. {@code 0} bei Waffen ohne Stufe. */
  public int getRating() {
    return rating;
  }

  /** Anzeigename inkl. Stufe, z.B. "Bogen (Stufe 4)". */
  public @NonNull String getDisplayName() {
    return data.hasRating() ? data.name() + " (Stufe " + rating + ")" : data.name();
  }

  public int getNuyenCost() {
    return data.costFor(rating);
  }

  /** Verfügbarkeit dieser Waffe in ihrer Stufe, wie im GRW angezeigt (z.B. "8" beim Bogen Stufe 4). */
  public @NonNull String getAvailabilityText() {
    return data.availability().displayText(rating);
  }
}
