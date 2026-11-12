package de.shalorian.chummer.model.character.cyberware;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Kaufbeschränkungen eines Implantats; geprüft in {@link Implants#getBlockReason}.
 *
 * @param multiple           ob das Implantat mehrfach eingebaut werden darf (Behälter, Cortexbomben, Reflexrekorder)
 * @param excludes           Namen von Implantaten, die nicht zusammen mit diesem eingebaut werden dürfen; die Prüfung
 *                           läuft in beide Richtungen
 * @param excludedCategories Kategorien, deren Implantate dieses ausschließt (Katzenaugen ↔ Augen-Cyberware, S. 393)
 * @param requires           Name eines Implantats, das vorhanden sein muss (Zweitmuster ↔ Stimmenmodulator), sonst leer
 * @param initiativeBoost    ob es eine technische Initiativesteigerung ist und damit nicht mit der Adeptenkraft
 *                           Gesteigerte Reflexe kombiniert werden darf (S. 230)
 * @param installOnly        ob es nur in ein Basissystem eingebaut, nicht einzeln implantiert werden kann
 *                           (Okular-Drohne, S. 386: ersetzt ein Auge innerhalb eines Cyberauges)
 */
public record ImplantRestrictions(
    boolean multiple,
    @NonNull List<String> excludes,
    @NonNull List<String> excludedCategories,
    @NonNull String requires,
    boolean initiativeBoost,
    boolean installOnly
) {

  /** Keine Beschränkung außer „nur einmal“. */
  public static final ImplantRestrictions NONE = new ImplantRestrictions(false, List.of(), List.of(), "", false, false);

  public ImplantRestrictions {
    excludes = List.copyOf(excludes);
    excludedCategories = List.copyOf(excludedCategories);
  }
}
