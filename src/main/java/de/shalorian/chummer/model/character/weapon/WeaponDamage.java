package de.shalorian.chummer.model.character.weapon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Wertet die Schadenscodes der GRW-Waffentabellen für eine konkrete Stärke aus.
 *
 * <p>SR4-Grundregelwerk S. 188: "Der Schaden der meisten Nahkampfwaffen beträgt Stärke ÷ 2
 * (aufgerundet), modifiziert durch die Waffe." Wurfwaffen (Shuriken, Wurfmesser) nutzen dieselbe
 * Schreibweise (S. 361). Beim Bogen bestimmt die Mindeststärke den Schaden (S. 361: "Die
 * Mindeststärke der Waffe wird auch für die Bestimmung des Schadens und der Reichweite benutzt").
 *
 * <p>Ausgewertet werden bewusst nur genau diese GRW-Muster, jeweils mit X ∈ {K, G} und optionalem
 * Suffix "(e)" (Elektrizität) bzw. "(f)" (Flechette):
 * <ul>
 *   <li>{@code (STR÷2)X} und {@code (STR÷2+N)X} — Stärke ÷ 2 aufgerundet, plus N</li>
 *   <li>{@code (STR Min + N)X} — Mindeststärke (Bogen-Stufe) plus N</li>
 *   <li>{@code NX} — fester Schaden, unverändert</li>
 * </ul>
 * Alle anderen Codes ("Granate", "Chemikalie", "Speziell", "Droge/Toxin" …) werden unverändert
 * zurückgegeben. Anders als das C#-Original (Zerlegung über {@code Contains}/{@code Replace}) gibt es
 * damit keine Teilersetzungen in unbekannten Codes.
 */
public final class WeaponDamage {

  private static final Pattern HALF_STRENGTH =
      Pattern.compile("\\(STR÷2(?:\\+(\\d+))?\\)([KG](?:\\([ef]\\))?)");
  private static final Pattern MIN_STRENGTH =
      Pattern.compile("\\(STR Min \\+ (\\d+)\\)([KG](?:\\([ef]\\))?)");

  private WeaponDamage() {
  }

  /**
   * Schaden als Anzeigetext (z.B. "6K", "5G(e)").
   *
   * @param code     Schadenscode aus {@link WeaponData#damage()}
   * @param strength Stärke des Charakters, beim Bogen dessen Mindeststärke (Stufe)
   */
  public static @NonNull String evaluate(@NonNull String code, int strength) {
    Matcher half = HALF_STRENGTH.matcher(code);
    if (half.matches()) {
      return (halfRoundedUp(strength) + modifier(half.group(1))) + half.group(2);
    }
    Matcher min = MIN_STRENGTH.matcher(code);
    if (min.matches()) {
      return (strength + Integer.parseInt(min.group(1))) + min.group(2);
    }
    return code;
  }

  /** GRW S. 188: "Stärke ÷ 2 (aufgerundet)" — einzige Rundungsstelle. */
  private static int halfRoundedUp(int strength) {
    return (strength + 1) / 2;
  }

  private static int modifier(@Nullable String group) {
    return group == null ? 0 : Integer.parseInt(group);
  }
}
