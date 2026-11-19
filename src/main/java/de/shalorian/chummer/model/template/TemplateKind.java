package de.shalorian.chummer.model.template;

import org.jspecify.annotations.NonNull;

/** Art einer Vorlage; die Reihenfolge ist die der Gruppen in der Vorlagen-Auswahl des Dialogs „Neu“. */
public enum TemplateKind {
  ARCHETYPE("Archetypen"),
  NPC_GROUP("Schergengruppen"),
  CRITTER("Critter");

  private final @NonNull String displayName;

  TemplateKind(@NonNull String displayName) {
    this.displayName = displayName;
  }

  /** Überschrift der Gruppe in der Auswahl. */
  public @NonNull String getDisplayName() {
    return displayName;
  }
}
