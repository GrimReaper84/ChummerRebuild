package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Speicherformat einer Schergengruppe (ADR-062); {@code kind} unterscheidet sie von Charakterdateien. */
class NpcGroupData {

  public @NonNull String kind = DocumentKind.NPC_GROUP_VALUE;
  public @NonNull String name = "";
  public int professionalRating;
  public @NonNull NpcStatblockData grunt = new NpcStatblockData();
  public @Nullable NpcStatblockData leader;
  /** Schaden je Scherge; die Länge ist die Mitgliederzahl. */
  public @NonNull List<Integer> memberDamage = new ArrayList<>();
  public int leaderDamage;
  public int edgeSpent;

  public NpcGroupData() {
  }
}
