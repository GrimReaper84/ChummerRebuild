package de.shalorian.chummer.model.character.spirit;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.magic.Tradition;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Katalogeintrag einer Geisterart (GRW S. 347/348) oder Sprite-Art (S. 282) aus {@code spirits.xml}.
 *
 * @param categories      Magiekategorie, für die eine Tradition diesen Geist ruft (S. 213); leer bei Sprites
 * @param stats           Werteblock in Tabellenreihenfolge
 * @param movement        Bewegung als Text (Geister), sonst leer
 * @param skills          Fertigkeiten, jeweils mit Stufe = Kraft bzw. Stufe
 * @param complexForms    Komplexe Formen eines Sprites, sonst leer
 * @param optional        optionale Kräfte bzw. optionale Komplexe Formen; je 3 volle Punkte eine (S. 282, 347)
 */
public record SpiritData(@NonNull String name, @NonNull SpiritKind kind, @NonNull Map<Tradition, String> categories,
                         @NonNull List<SpiritStat> stats, @NonNull String movement, @NonNull List<String> skills,
                         @NonNull List<String> complexForms, @NonNull List<String> powers,
                         @NonNull List<String> optional, @NonNull List<String> weaknesses, @NonNull String source,
                         @NonNull String page) implements CatalogEntry {

  /** Punkte Kraft bzw. Stufe je optionaler Kraft bzw. Komplexer Form. */
  private static final int POINTS_PER_OPTION = 3;

  public SpiritData {
    categories = Map.copyOf(categories);
    stats = List.copyOf(stats);
    skills = List.copyOf(skills);
    complexForms = List.copyOf(complexForms);
    powers = List.copyOf(powers);
    optional = List.copyOf(optional);
    weaknesses = List.copyOf(weaknesses);
  }

  /** Magiekategorie dieses Geistes in {@code tradition} (z.B. „Kampf“), oder {@code null}, wenn sie ihn nicht ruft. */
  public @Nullable String categoryIn(@NonNull Tradition tradition) {
    return categories.get(tradition);
  }

  /** Wie viele optionale Kräfte bzw. Komplexe Formen bei Kraft bzw. Stufe {@code force} wählbar sind. */
  public int optionalCount(int force) {
    return force / POINTS_PER_OPTION;
  }

  /** Werteblock für {@code force}, z.B. „K 7 · G 2 · …“. */
  public @NonNull String statsText(int force) {
    return String.join(" · ", stats.stream().map(s -> s.label() + " " + s.valueFor(force)).toList());
  }
}
