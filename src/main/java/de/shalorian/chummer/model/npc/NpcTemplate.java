package de.shalorian.chummer.model.npc;

import de.shalorian.chummer.model.template.BuildMode;
import de.shalorian.chummer.model.template.CharacterSpec;
import de.shalorian.chummer.model.template.DocumentTemplate;
import de.shalorian.chummer.model.template.TemplateKind;
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
    int page, int members, @NonNull CharacterSpec grunt, @Nullable CharacterSpec leader) implements DocumentTemplate {

  @Override
  public @NonNull TemplateKind kind() {
    return TemplateKind.NPC_GROUP;
  }

  /** Baut eine neue Gruppe; Einträge, die das Modell nicht übernehmen konnte, landen in {@code problems}. */
  public @NonNull NpcGroup create(@NonNull List<String> problems) {
    NpcGroup group = new NpcGroup(name, rating, members, statblock(grunt, "Scherge", problems));
    if (leader != null) {
      group.setLeader(statblock(leader, "Anführer", problems));
    }
    return group;
  }

  private static @NonNull NpcStatblock statblock(@NonNull CharacterSpec spec, @NonNull String name,
      @NonNull List<String> problems) {
    return new NpcStatblock(spec.build(name, BuildMode.GM, problems), spec.gearText());
  }

  /** Wie {@link #create(List)}, ohne die Probleme auszuwerten. */
  public @NonNull NpcGroup create() {
    return create(new ArrayList<>());
  }
}
