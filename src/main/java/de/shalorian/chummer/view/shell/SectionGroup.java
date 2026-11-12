package de.shalorian.chummer.view.shell;

import org.jspecify.annotations.NonNull;

/** Gruppen der Seitenleiste des Charakterbogens, in Anzeigereihenfolge. */
public enum SectionGroup {
  CHARACTER("Charakter"),
  MAGIC_RESONANCE("Magie & Resonanz"),
  EQUIPMENT("Ausrüstung"),
  NETWORK("Netzwerk & Protokoll");

  private final @NonNull String title;

  SectionGroup(@NonNull String title) {
    this.title = title;
  }

  public @NonNull String getTitle() {
    return title;
  }
}
