package de.shalorian.chummer.model.character.cyberware;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

/**
 * Implantatkategorie ("Grade") einer Cyber- oder Bioware mit ihrem Essenz- und Preis-Multiplikator.
 * Werte laut SR4-Grundregelwerk, Tabelle <i>Implantatkategorien</i> (S. 359, gegen das PDF-Bild
 * verifiziert): Standard ×1/×1, Alphaware ×0,8/×2, Betaware ×0,7/×4, Deltaware ×0,5/×10. Gilt
 * gleichermaßen für Cyberware und Bioware ("Cyberware und Bioware ist in vier Kategorien erhältlich").
 *
 * <p>Bewusst nur die vier GRW-Kern-Kategorien — Adapsin- und Second-Hand-Varianten aus dem
 * C#-Original stammen aus dem Zusatzband Arsenal und sind nicht Teil dieses Feature-Schnitts (siehe
 * {@code docs/plaene/cyberware_plan.md} Abschnitt 13).
 */
public enum CyberwareGrade {
  STANDARD("Standard", "1", 1, true),
  ALPHAWARE("Alphaware", "0.8", 2, true),
  BETAWARE("Betaware", "0.7", 4, false),
  DELTAWARE("Deltaware", "0.5", 10, false);

  private final @NonNull String displayName;
  private final @NonNull BigDecimal essenceMultiplier;
  private final int priceMultiplier;
  private final boolean availableAtCreation;

  CyberwareGrade(@NonNull String displayName, @NonNull String essenceMultiplier,
      int priceMultiplier, boolean availableAtCreation) {
    this.displayName = displayName;
    this.essenceMultiplier = new BigDecimal(essenceMultiplier);
    this.priceMultiplier = priceMultiplier;
    this.availableAtCreation = availableAtCreation;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Faktor, mit dem die Standard-Essenzkosten eines Implantats multipliziert werden. */
  public @NonNull BigDecimal getEssenceMultiplier() {
    return essenceMultiplier;
  }

  /** Faktor, mit dem der Listenpreis (Standardvariante) eines Implantats multipliziert wird. */
  public int getPriceMultiplier() {
    return priceMultiplier;
  }

  /**
   * Ob diese Kategorie bei der Charaktererschaffung gekauft werden darf. SR4-Grundregelwerk S. 359:
   * "Nur Standard- und Alphaware können bei der Charaktererschaffung gekauft werden." Im
   * Karrieremodus sind alle vier Kategorien erhältlich.
   */
  public boolean isAvailableAtCreation() {
    return availableAtCreation;
  }
}
