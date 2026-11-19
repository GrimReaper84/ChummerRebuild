package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/** Eine Schwäche aus {@code critter_weaknesses.xml} (GRW S. 343), Wirkung in eigenen Worten. */
public record CritterWeaknessData(@NonNull String name, @NonNull String note, @NonNull String source, int page)
    implements CatalogEntry, Sourced {

  public @NonNull String describe() {
    return note + "\n(" + source + " S. " + page + ")";
  }
}
