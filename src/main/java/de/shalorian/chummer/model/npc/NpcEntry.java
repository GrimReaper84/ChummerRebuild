package de.shalorian.chummer.model.npc;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Ein Eintrag eines Vorlagen-Werteblocks aus {@code npcs.xml}: eine Gabe, Fertigkeit, Fertigkeitsgruppe,
 * Wissensfertigkeit, ein Implantat (samt Einbauten), Cyberglied, Waffe, Panzerung, Zauber oder eine Adeptenkraft.
 *
 * @param kind   XML-Element, z.B. {@code skill} oder {@code cyberware}
 * @param name   Katalogname
 * @param rating Stufe, 0 ohne Angabe
 * @param detail Spezialisierung, Auswahl, Wissenskategorie bzw. Körperstelle; leer ohne Angabe
 * @param parts  Einbauten (Augen-Zubehör, Glied-Verbesserungen, Panzerungszubehör)
 */
public record NpcEntry(@NonNull String kind, @NonNull String name, int rating, @NonNull String detail,
    @NonNull List<NpcEntry> parts) {

  public NpcEntry {
    parts = List.copyOf(parts);
  }
}
