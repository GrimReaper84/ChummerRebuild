package de.shalorian.chummer.model.template;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Ein Eintrag einer Vorlage ({@code npcs.xml}, {@code archetypes.xml}): eine Gabe, Fertigkeit, Fertigkeitsgruppe,
 * Wissens- oder Sprachfertigkeit, ein Implantat (samt Einbauten), Cyberglied, eine Waffe (samt Zubehör), Panzerung
 * (samt Zubehör), ein Zauber, eine Adeptenkraft, Komplexe Form, Ausrüstung (samt Einbauten), ein Fahrzeug, Geist,
 * Kontakt, Lebensstil, Ressourcen-BP oder Edge.
 *
 * @param kind   XML-Element, z.B. {@code skill} oder {@code cyberware}
 * @param name   Katalogname bzw. Name des Kontakts oder der Sprache
 * @param rating Stufe (Kontakt: Connection, Geist: Kraftstufe, Ressourcen: BP), 0 ohne Angabe
 * @param amount Menge (Ausrüstung: Stück, Kontakt: Loyalität, Geist: Dienste, Lebensstil: Monate), 0 ohne Angabe
 * @param detail Spezialisierung, Auswahl, Wissenskategorie, Körperstelle, Grad, Halterung oder Merker wie
 *               {@code gebunden}/{@code muttersprache}; leer ohne Angabe
 * @param parts  Einbauten (Augen-Zubehör, Glied-Verbesserungen, Panzerungs- und Waffenzubehör, Programme)
 */
public record TemplateEntry(@NonNull String kind, @NonNull String name, int rating, int amount,
    @NonNull String detail, @NonNull List<TemplateEntry> parts) {

  public TemplateEntry {
    parts = List.copyOf(parts);
  }
}
