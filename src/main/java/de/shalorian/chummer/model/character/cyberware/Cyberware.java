package de.shalorian.chummer.model.character.cyberware;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jspecify.annotations.NonNull;

/**
 * Ein in einen Charakter eingebautes Cyber- oder Bioware-Implantat: referenziert einen {@link
 * CyberwareData}-Katalogeintrag und hält die gewählte {@link CyberwareGrade} sowie — nur bei
 * stufenbehafteten Einträgen ({@link CyberwareData#hasRating()}) — die gewählte Stufe.
 *
 * <p>Essenzkosten = Katalog-Essenzformel (für die Stufe ausgewertet) × Essenz-Multiplikator der
 * Grade; Preis = Katalog-Preisformel × Preis-Multiplikator der Grade (GRW S. 359: Katalogpreise sind
 * Standardware-Preise). Die Essenzkosten werden hier bewusst
 * <b>nicht</b> gerundet — gerundet wird einheitlich erst bei der Gesamt-Essenz auf Charakterebene.
 * Anders als Zauber/Komplexe Formen darf dasselbe Implantat mehrfach eingebaut werden, daher arbeitet
 * {@code Equipment#removeCyberware} über die Objektreferenz.
 */
public class Cyberware {

  private final @NonNull CyberwareData data;
  private @NonNull CyberwareGrade grade;
  private int rating;

  /**
   * @param rating gewählte Stufe; muss bei stufenbehafteten Einträgen in
   *               [{@value CyberwareData#MIN_RATING}, {@link CyberwareData#maxRating()}] liegen, bei
   *               Einträgen ohne Stufe {@code 0} sein
   * @throws IllegalArgumentException bei unzulässiger Stufe
   */
  public Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, int rating) {
    this.data = data;
    this.grade = grade;
    setRating(rating);
  }

  public @NonNull CyberwareData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  public @NonNull EquipmentKind getKind() {
    return data.kind();
  }

  public @NonNull CyberwareGrade getGrade() {
    return grade;
  }

  public void setGrade(@NonNull CyberwareGrade grade) {
    this.grade = grade;
  }

  /** Gewählte Stufe, bzw. {@code 0} bei Implantaten ohne Stufe. */
  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    boolean valid = data.hasRating()
        ? rating >= CyberwareData.MIN_RATING && rating <= data.maxRating()
        : rating == 0;
    if (!valid) {
      throw new IllegalArgumentException(
          "%s: Stufe %d ist nicht zulässig (max. %d)".formatted(data.name(), rating,
              data.maxRating()));
    }
    this.rating = rating;
  }

  /** Exakte (ungerundete) Essenzkosten dieses Implantats inkl. Grade-Multiplikator. */
  public @NonNull BigDecimal getEssenceCost() {
    return RatingFormula.evaluate(data.essence(), rating).multiply(grade.getEssenceMultiplier());
  }

  /** Preis in Nuyen inkl. Grade-Multiplikator (kaufmännisch auf ganze Nuyen gerundet). */
  public int getNuyenCost() {
    return RatingFormula.evaluate(data.cost(), rating)
        .multiply(BigDecimal.valueOf(grade.getPriceMultiplier()))
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }
}
