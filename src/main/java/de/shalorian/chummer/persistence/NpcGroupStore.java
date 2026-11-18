package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.shalorian.chummer.model.npc.NpcCombatant;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Speichert und lädt Schergengruppen als JSON (ADR-062). Die Werteblöcke nutzen das Charakterformat von
 * {@link CharacterStore}; nach dem Laden stehen sie wieder im Spielleiter-Modus ({@link NpcStatblock}).
 */
public final class NpcGroupStore {

  private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private NpcGroupStore() {
  }

  public static void save(@NonNull NpcGroup group, @NonNull File file) throws IOException {
    MAPPER.writeValue(file, toData(group));
  }

  public static @NonNull NpcGroup load(@NonNull File file) throws IOException {
    return fromData(MAPPER.readValue(file, NpcGroupData.class));
  }

  static @NonNull NpcGroupData toData(@NonNull NpcGroup group) {
    NpcGroupData data = new NpcGroupData();
    data.name = group.getName();
    data.professionalRating = group.getProfessionalRating();
    data.grunt = toData(group.getGrunt());
    NpcStatblock leader = group.getLeader();
    data.leader = leader == null ? null : toData(leader);
    data.memberDamage = group.getMembers().stream().map(NpcCombatant::getDamage).toList();
    data.leaderDamage = group.getLeaderState().getDamage();
    data.edgeSpent = group.getEdgeSpent();
    return data;
  }

  static @NonNull NpcGroup fromData(@NonNull NpcGroupData data) {
    List<Integer> damage = data.memberDamage;
    NpcGroup group = new NpcGroup(data.name, data.professionalRating, Math.clamp(damage.size(), 1, NpcGroup.MAX_MEMBERS),
        fromData(data.grunt));
    NpcStatblockData leaderData = data.leader;
    if (leaderData != null) {
      NpcStatblock leader = fromData(leaderData);
      group.setLeader(leader);
      group.getLeaderState().setDamage(data.leaderDamage, leader.getMonitorBoxes());
    }
    int boxes = group.getGrunt().getMonitorBoxes();
    for (int i = 0; i < group.getMemberCount() && i < damage.size(); i++) {
      group.getMembers().get(i).setDamage(damage.get(i), boxes);
    }
    group.setEdgeSpent(data.edgeSpent);
    return group;
  }

  private static @NonNull NpcStatblockData toData(@NonNull NpcStatblock statblock) {
    return new NpcStatblockData(CharacterStore.toData(statblock.getCharacter()), statblock.getGearText());
  }

  private static @NonNull NpcStatblock fromData(@NonNull NpcStatblockData data) {
    return new NpcStatblock(CharacterStore.fromData(data.character), data.gear);
  }
}
