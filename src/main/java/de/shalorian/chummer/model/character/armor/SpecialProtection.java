package de.shalorian.chummer.model.character.armor;

import org.jspecify.annotations.NonNull;

/**
 * Eine Zeile Sonderschutz durch Panzerungsmodifikationen (GRW S. 372), fertig zur Anzeige, z.B. „gegen Elektrizität“ /
 * „11/9“ oder „Toxinwiderstand (Kontakt)“ / „+3 Würfel“. Erzeugt von {@link ArmorProtection#describe}.
 *
 * @param label wogegen der Schutz wirkt
 * @param value Panzerungswert samt Stufe bzw. Würfelbonus
 */
public record SpecialProtection(@NonNull String label, @NonNull String value) {

  @Override
  public @NonNull String toString() {
    return label + " " + value;
  }
}
