package de.shalorian.chummer.model.npc;

import de.shalorian.chummer.model.character.rulebook.Sourced;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Eine Beispiel-Schergengruppe des GRW (S. 325–328) aus {@code npcs.xml}.
 *
 * @param description ein Satz in eigenen Worten
 * @param source      Kürzel des Regelwerks aus {@code books.xml}
 * @param page        gedruckte Seite der Gruppe
 * @param members     vorgeschlagene Mitgliederzahl
 */
public record NpcTemplate(@NonNull String name, int rating, @NonNull String description, @NonNull String source,
    int page, int members, @NonNull NpcStatblockSpec grunt, @Nullable NpcStatblockSpec leader) implements Sourced {

  /** Baut eine neue Gruppe; Einträge, die das Modell nicht übernehmen konnte, landen in {@code problems}. */
  public @NonNull NpcGroup create(@NonNull List<String> problems) {
    NpcGroup group = new NpcGroup(name, rating, members, grunt.build("Scherge", problems));
    if (leader != null) {
      group.setLeader(leader.build("Anführer", problems));
    }
    return group;
  }

  /** Wie {@link #create(List)}, ohne die Probleme auszuwerten. */
  public @NonNull NpcGroup create() {
    return create(new ArrayList<>());
  }
}
