package de.shalorian.chummer.model.critter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

/**
 * Natürliche Waffe eines Critters aus dem Zusatz der Kraft, z.B. „Krallen/Biss: Schaden 5K, PB 0“ (GRW S. 340). Der
 * Angriff nutzt Waffenloser Kampf im Nahkampf bzw. Exotische Fernkampfwaffe im Fernkampf; der Schaden steht fest im
 * Zusatz und hängt nicht von der Stärke ab.
 *
 * @param name   Art des Angriffs, z.B. „Krallen/Biss“
 * @param damage Schadenscode, z.B. „5K“
 * @param ap     Panzerbrechend, z.B. 0 oder −2
 * @param extra  übriger Zusatz, z.B. „−1 Reichweite“; leer ohne Angabe
 */
public record NaturalWeapon(@NonNull String name, @NonNull String damage, int ap, @NonNull String extra) {

  private static final Pattern PATTERN = Pattern.compile(
      "\\s*(?<name>[^:]+):\\s*Schaden\\s+(?<damage>\\d+[KG]),\\s*PB\\s+(?<ap>[−-]?\\d+)\\s*(?:,\\s*(?<extra>.*))?");

  /**
   * Liest den Zusatz einer Natürlichen Waffe.
   *
   * @throws IllegalArgumentException wenn der Text nicht dem Muster „Name: Schaden 5K, PB 0“ folgt
   */
  public static @NonNull NaturalWeapon parse(@NonNull String text) {
    Matcher m = PATTERN.matcher(text);
    if (!m.matches()) {
      throw new IllegalArgumentException("Natürliche Waffe nicht lesbar: „" + text + "“ (erwartet „Name: Schaden 5K, PB 0“)");
    }
    String extra = m.group("extra");
    return new NaturalWeapon(m.group("name").trim(), m.group("damage"),
        Integer.parseInt(m.group("ap").replace('−', '-')), extra == null ? "" : extra.trim());
  }
}
