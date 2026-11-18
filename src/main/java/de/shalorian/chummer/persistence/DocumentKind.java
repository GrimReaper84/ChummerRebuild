package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.jspecify.annotations.NonNull;

/**
 * Art einer Speicherdatei (ADR-062). Schergengruppen tragen das Feld {@code kind}; Charakterdateien haben es nicht,
 * auch alle Dateien von vor dieser Unterscheidung.
 */
public enum DocumentKind {
  CHARACTER,
  NPC_GROUP;

  static final String KIND_FIELD = "kind";
  static final String NPC_GROUP_VALUE = "npcGroup";

  /** Liest nur das Feld {@code kind} der Datei. */
  public static @NonNull DocumentKind detect(@NonNull File file) throws IOException {
    JsonNode kind = new ObjectMapper().readTree(file).get(KIND_FIELD);
    return kind != null && NPC_GROUP_VALUE.equals(kind.asText()) ? NPC_GROUP : CHARACTER;
  }
}
