package de.shalorian.chummer.model.character;

import java.util.function.IntConsumer;
import org.jspecify.annotations.NonNull;

/**
 * Ein Eintrag, dessen Kosten der Spielleiter-Modus bei der Erschaffung übernimmt (ADR-070): Schlüssel (das Objekt
 * selbst, verglichen nach Identität), aktuelle Kosten in Nuyen bzw. BP und wohin ein Zuwachs als Gratis-Anteil geht.
 *
 * @param key     der Eintrag; Fokusbindung und Fokuspreis haben getrennte Listen, deshalb genügt das Objekt
 * @param cost    aktuelle Kosten ohne Gratis-Anteil
 * @param addFree schreibt einen Zuwachs dem Gratis-Anteil gut
 */
record GmCostEntry(@NonNull Object key, int cost, @NonNull IntConsumer addFree) {
}
