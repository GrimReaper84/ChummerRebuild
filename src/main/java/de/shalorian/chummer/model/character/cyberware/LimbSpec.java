package de.shalorian.chummer.model.character.cyberware;

import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Gliedbezogene Katalogangaben eines Implantats (GRW S. 385, 388, 390/391).
 *
 * @param part         Gliedart, wenn das Implantat selbst eine Cybergliedmaße ist, sonst {@code null}
 * @param capacityCost Kapazitätsbedarf beim Einbau in ein Glied (eckige Werte der Tabellen, z.B. {@code "3"} oder
 *                     {@code "Rating * 2"}); leer = nicht in Glieder einbaubar
 * @param fitsIn       Gliedarten, in die es passt; leer = alle
 * @param enhancement  was es als Cyberglied-Verbesserung erhöht, sonst {@code null}
 * @param pairedLegs   ob es zwei Cyberbeine braucht und mit einem Kauf in beide eingebaut wird (Sprunghydraulik)
 */
public record LimbSpec(@Nullable LimbPart part, @NonNull String capacityCost, @NonNull Set<LimbPart> fitsIn,
                       @Nullable LimbEnhancement enhancement, boolean pairedLegs) {

  /** Kein Gliedbezug. */
  public static final LimbSpec NONE = new LimbSpec(null, "", Set.of(), null, false);

  public LimbSpec {
    fitsIn = Set.copyOf(fitsIn);
  }

  /** Ob das Implantat eine Cybergliedmaße ist. */
  public boolean isLimb() {
    return part != null;
  }

  /** Ob es gegen Kapazität in ein Glied eingebaut werden kann. */
  public boolean isInstallableInLimb() {
    return !capacityCost.isEmpty();
  }

  /** Ob es in diese Gliedart passt. */
  public boolean fits(@NonNull LimbPart limb) {
    return fitsIn.isEmpty() || fitsIn.contains(limb);
  }
}
