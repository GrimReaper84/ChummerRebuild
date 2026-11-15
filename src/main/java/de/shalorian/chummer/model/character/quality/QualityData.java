package de.shalorian.chummer.model.character.quality;

import de.shalorian.chummer.model.character.CatalogEntry;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag einer Gabe oder eines Handicaps aus {@code qualities.xml}.
 *
 * @param bp          GP-Kosten; bei Handicaps negativ
 * @param page        gedruckte GRW-Seite, {@code 0} ohne Angabe
 * @param note        Kurzwirkung in eigenen Worten, sonst leer
 * @param selection   was beim Kauf gewählt werden muss
 * @param selectLabel Bezeichnung des Auswahlfelds (z.B. "Substanz"), leer ohne Auswahl
 * @param level       Stufe bei Qualities mit Stufen (z.B. Hohe Schmerztoleranz 2), sonst 1; Stärke der Wirkung
 * @param notoriety   Beitrag zum Schlechten Ruf (GRW S. 308: +1 je genanntem Handicap, −1 je genannter Gabe), sonst 0
 */
public record QualityData(
    @NonNull String name,
    int bp,
    @NonNull QualityType type,
    @NonNull Set<QualityBonusType> bonuses,
    int page,
    @NonNull String note,
    @NonNull QualitySelection selection,
    @NonNull String selectLabel,
    @NonNull QualityRules rules,
    int level,
    int notoriety
) implements CatalogEntry {}
