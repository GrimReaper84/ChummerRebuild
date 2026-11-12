package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/**
 * Wogegen eine Panzerungsmodifikation zusätzlich schützt (GRW S. 372). Elektrizität, Feuer und Kälte addieren die
 * Stufe zur Panzerung gegen diese Schadensart. Chemische Isolierung gibt die Stufe als Würfel auf die
 * Toxinwiderstandsprobe gegen Kontakt-Toxine, an der Ganzkörperpanzerung auch gegen Inhalation (Tabelle „Schutz vor
 * Chemikalien“), und zählt gegen Säure voll zur halben Stoßpanzerung (S. 194). Wärmedämpfung gibt die Stufe als Würfel
 * beim Infiltrieren gegen IR.
 */
public enum ArmorProtection {
  NONE(""),
  ELECTRICITY("gegen Elektrizität"),
  FIRE("gegen Feuer"),
  COLD("gegen Kälte"),
  CHEMICAL("Toxinwiderstand (Kontakt)"),
  IR_DAMPING("Infiltration gegen IR");

  private final @NonNull String label;

  ArmorProtection(@NonNull String label) {
    this.label = label;
  }

  /** Beschriftung wie „gegen Elektrizität“. */
  public @NonNull String getLabel() {
    return label;
  }

  /** Ob die Stufe zum Panzerungswert addiert wird (statt Würfel bzw. nichts). */
  public boolean addsToArmor() {
    return this == ELECTRICITY || this == FIRE || this == COLD;
  }

  /**
   * Anzeigezeile für Stufe {@code rating} bei Schutz {@code ballistic}/{@code impact}.
   *
   * @param prefix   "+" bei Helmen und Schilden, deren Werte Boni sind, sonst leer
   * @param fullBody ob das Teil eine Ganzkörperpanzerung ist (Chemische Isolierung auch gegen Inhalation)
   */
  public @NonNull SpecialProtection describe(int rating, int ballistic, int impact, @NonNull String prefix,
      boolean fullBody) {
    if (addsToArmor()) {
      return new SpecialProtection(label, "%s%d/%s%d".formatted(prefix, ballistic + rating, prefix, impact + rating));
    }
    if (this == CHEMICAL) {
      return new SpecialProtection(fullBody ? "Toxinwiderstand (Kontakt, Inhalation)" : label,
          "+%d Würfel, gegen Säure +%d Panzerung".formatted(rating, rating));
    }
    return new SpecialProtection(label, "+" + rating + " Würfel");
  }

  /**
   * Liest den Wert von {@code <protection>} in {@code armor_mods.xml}; leer = keiner.
   *
   * @throws IllegalArgumentException bei unbekanntem Code
   */
  public static @NonNull ArmorProtection fromCode(@NonNull String code) {
    return switch (code) {
      case "" -> NONE;
      case "electricity" -> ELECTRICITY;
      case "fire" -> FIRE;
      case "cold" -> COLD;
      case "chemical" -> CHEMICAL;
      case "irdamping" -> IR_DAMPING;
      default -> throw new IllegalArgumentException("Unbekannter Schutz: " + code);
    };
  }
}
