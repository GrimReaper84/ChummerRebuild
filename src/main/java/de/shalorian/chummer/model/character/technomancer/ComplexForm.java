package de.shalorian.chummer.model.character.technomancer;

import de.shalorian.chummer.model.character.skill.Skill;
import org.jspecify.annotations.NonNull;

/**
 * Eine von einem Charakter gelernte Komplexe Form (Rating-Instanz eines {@link ComplexFormData}-
 * Katalogeintrags). Wird laut SR4-Grundregelwerk immer mit Stufe 1 gelernt ({@code min == 1} — anders
 * als bei Wissensfertigkeiten gibt es kein "gekanntes Rating 0"). Die Kosten-Formel entspricht laut GRW
 * exakt der einer Wissensfertigkeit (2 Karma für eine neue Komplexe Form, Karma in Höhe der neuen Stufe für jede weitere
 * Steigerung) — eigene Konstanten statt Wiederverwendung von {@code Character}s
 * Wissensfertigkeiten-Konstanten, um eine versehentliche Kopplung zwischen beiden Feature-Bereichen zu
 * vermeiden, siehe {@code docs/plaene/archiv/technomancer_plan.md} Abschnitt 13.
 */
public class ComplexForm {

  public static final int MIN_RATING = 1;
  /** BP-Kosten pro Stufenpunkt bei Erstellung — fixer Preis, keine Staffelung (GRW S. 112: 1 GP je Stufe). */
  public static final int BP_PER_RATING = 1;
  /** Karma-Kosten, um eine neue Komplexe Form (Stufe 1) zu erlernen (GRW: 2 Karma). */
  public static final int KARMA_NEW = 2;

  private static final int KARMA_PER_RATING = 1;

  private final @NonNull ComplexFormData data;
  private int rating = MIN_RATING;

  public ComplexForm(@NonNull ComplexFormData data) {
    this.data = data;
  }

  public @NonNull ComplexFormData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    if (rating < MIN_RATING || rating > Skill.MAX_RATING) {
      throw new IllegalArgumentException(
          "%s: Rating %d liegt außerhalb [%d/%d]".formatted(data.name(), rating, MIN_RATING,
              Skill.MAX_RATING));
    }
    this.rating = rating;
  }

  public int getBpCostPerRating() {
    return BP_PER_RATING;
  }

  /**
   * Karma-Kosten, um von {@code currentRating} auf {@code currentRating + 1} zu steigern (bzw. um die
   * Komplexe Form neu zu erlernen, wenn {@code currentRating == 0}). Entspricht exakt {@link
   * de.shalorian.chummer.model.character.Character#getKnowledgeSkillKarmaCost}, siehe
   * Klassen-Javadoc.
   */
  public int getKarmaCost(int currentRating) {
    return currentRating == 0 ? KARMA_NEW : (currentRating + 1) * KARMA_PER_RATING;
  }
}
