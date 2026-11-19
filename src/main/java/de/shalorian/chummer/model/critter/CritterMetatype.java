package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.attribute.AttributeRange;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.Movement;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

/**
 * Baut aus einem Critter einen Metatyp für den Werteblock: Jedes Attribut und Edge darf die Spielleitung um bis zu 3
 * erhöhen oder senken, keines unter 0 (GRW S. 337). Keine BP-Kosten, keine Gaben.
 */
public final class CritterMetatype {

  /** Spielraum der Spielleitung je Attribut und Fertigkeit (GRW S. 337). */
  public static final int ADJUSTMENT = 3;

  private static final Pattern MOVEMENT = Pattern.compile("(\\d+)/(\\d+).*");
  /** „wie der Metatyp“ (Ghul): Bewegung eines Menschen (GRW S. 177). */
  private static final Movement METAHUMAN_MOVEMENT = new Movement(10, 25);

  private CritterMetatype() {
  }

  public static @NonNull MetatypeData of(@NonNull CritterData critter) {
    Map<AttributeType, AttributeRange> ranges = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      ranges.put(type, range(critter.attribute(type)));
    }
    return new MetatypeData(critter.name(), 0, false, critter.name(), ranges, range(critter.edge()), List.of(),
        List.of(), movement(critter.movement()));
  }

  /** Spanne [Wert − 3, Wert + 3], nicht unter 0. */
  public static @NonNull AttributeRange range(int value) {
    return new AttributeRange(Math.max(0, value - ADJUSTMENT), value + ADJUSTMENT);
  }

  /** Erste Angabe „Gehen/Laufen“ der Bewegung; Sonderbewegungen in Klammern zeigt der Bogen als Text. */
  static @NonNull Movement movement(@NonNull String text) {
    Matcher m = MOVEMENT.matcher(text.trim());
    return m.matches() ? new Movement(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))) : METAHUMAN_MOVEMENT;
  }
}
