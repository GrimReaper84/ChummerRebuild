package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import de.shalorian.chummer.model.template.TemplateEntry;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Ein Critter aus {@code critters.xml} (GRW S. 343–351): Werteblock wie im GRW. Die Spielleitung darf Attribute und
 * Fertigkeiten um bis zu 3 ändern, nicht unter 0 (S. 337, {@link CritterMetatype}).
 *
 * @param attributes natürliche Werte der acht Grundattribute
 * @param magic      Magie, 0 bei nicht erwachten Crittern
 * @param essence    Essenz wie im GRW, z.B. „6“ oder „2W6 − 1“
 * @param passes     Initiativedurchgänge
 * @param movement   Bewegung wie im GRW, z.B. „15/50“ oder „25/40 (fliegend)“
 * @param skills     Fertigkeiten als Einträge der Arten Aktionsfertigkeit, Gruppe, Wissen oder Sonderfertigkeit
 * @param powers     Kräfte mit Zusatz in Klammern, Namen aus {@code critter_powers.xml}
 * @param weaknesses Schwächen mit Zusatz, Namen aus {@code critter_weaknesses.xml}
 * @param qualities  Gaben, die der Critter laut GRW immer hat (z.B. Zauberer bei Dracoformen)
 * @param note       Anmerkung in eigenen Worten
 */
public record CritterData(@NonNull String name, @NonNull CritterCategory category,
    @NonNull Map<AttributeType, Integer> attributes, int edge, int magic, @NonNull String essence, int passes,
    @NonNull String movement, @NonNull List<TemplateEntry> skills, @NonNull List<String> powers,
    @NonNull List<String> weaknesses, @NonNull List<String> qualities, @NonNull String note, @NonNull String source, int page)
    implements CatalogEntry, Sourced {

  public CritterData {
    attributes = Map.copyOf(attributes);
    skills = List.copyOf(skills);
    powers = List.copyOf(powers);
    weaknesses = List.copyOf(weaknesses);
    qualities = List.copyOf(qualities);
  }

  public int attribute(@NonNull AttributeType type) {
    Integer value = attributes.get(type);
    return value == null ? 0 : value;
  }
}
