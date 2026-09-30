package de.shalorian.chummer.model.character.weapon;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Die vier Reichweitenstufen einer Fernkampfwaffe in Metern (SR4-Grundregelwerk S. 179, Tabelle
 * <i>Waffenreichweiten</i>): Kurz (±0), Mittel (−1), Weit (−3), Extrem weit (−6). Jede Stufe beginnt
 * einen Meter hinter dem Ende der vorigen; nur die kurze Stufe hat eine eigene Untergrenze (0, bei
 * Granatwerfern die Minimalreichweite 5, bei Raketenwerfern 20).
 *
 * @param minimum   Beginn der kurzen Reichweite
 * @param shortMax  Ende Kurz
 * @param mediumMax Ende Mittel
 * @param longMax   Ende Weit
 * @param extremeMax Ende Extrem weit (maximale Reichweite)
 */
public record RangeBands(int minimum, int shortMax, int mediumMax, int longMax, int extremeMax) {

  /** Die vier Stufen als Anzeigetexte wie in der GRW-Tabelle: Kurz, Mittel, Weit, Extrem weit. */
  public @NonNull List<String> bandTexts() {
    return List.of(band(minimum, shortMax), band(shortMax + 1, mediumMax),
        band(mediumMax + 1, longMax), band(longMax + 1, extremeMax));
  }

  private static @NonNull String band(int from, int to) {
    return from + "–" + to;
  }
}
