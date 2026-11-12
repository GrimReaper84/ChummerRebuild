package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class ArmorEntry {

  public @NonNull String name = "";
  public boolean equipped = true;
  /** Gewählter Kleidungspreis, sonst {@code null}; fehlt in Dateien vor 2026-11-12. */
  public @Nullable Integer price;
  /** Zubehör und Modifikationen in Einbaureihenfolge; fehlt in Dateien vor 2026-11-12. */
  public @NonNull List<ArmorModEntry> mods = new ArrayList<>();

  public ArmorEntry() {
  }

  public ArmorEntry(@NonNull String name, boolean equipped, @Nullable Integer price) {
    this.name = name;
    this.equipped = equipped;
    this.price = price;
  }
}
