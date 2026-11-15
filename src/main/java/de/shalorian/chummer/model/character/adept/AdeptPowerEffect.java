package de.shalorian.chummer.model.character.adept;

import de.shalorian.chummer.model.character.situation.SituationKind;
import org.jspecify.annotations.Nullable;

/**
 * Rechnerische Wirkung einer Adeptenkraft, die der Charakter auswertet (Tag {@code <effect>} in
 * {@code adept_powers.xml}). Wirkungen mit {@link #getSituation()} geben je Stufe einen Würfel in einem situativen
 * Pool; Kräfte ohne Rechnung tragen {@link #NONE} und nur einen Hinweis.
 */
public enum AdeptPowerEffect {
  /** Keine Rechnung, Wirkung nur als Hinweis. */
  NONE,
  /** Gesteigerte Reflexe (GRW S. 230): je Stufe Reaktion +1 und ein Initiativedurchgang mehr. */
  IMPROVED_REFLEXES,
  /** Gesteigertes körperliches Attribut (GRW S. 230): je Stufe das gewählte Attribut +1. */
  IMPROVED_ATTRIBUTE,
  /** Verbesserte Fertigkeit (GRW S. 232): je Stufe +1 Würfel auf die gewählte Fertigkeit. */
  IMPROVED_SKILL,
  /** Verbesserte Wahrnehmung (GRW S. 232): je Stufe +1 Würfel auf Wahrnehmung und Askennen. */
  IMPROVED_PERCEPTION,
  /** Schmerzresistenz (GRW S. 231): je Stufe ein Kästchen je Spalte ohne Verletzungsmodifikator. */
  PAIN_RESISTANCE,
  /** Mystischer Panzer (GRW S. 231): je Stufe +1 Ballistik und Stoß. */
  MYSTIC_ARMOR,
  /** Astrale Wahrnehmung (GRW S. 230): Zugang zu Askennen und Astralkampf. */
  ASTRAL_PERCEPTION,
  /** Kampfsinn (GRW S. 230): je Stufe ein Würfel mehr auf die Verteidigung (situativer Pool). */
  COMBAT_SENSE(SituationKind.DEFENSE),
  /** Magieresistenz (GRW S. 231): je Stufe ein Würfel mehr gegen Zauber (situativer Pool). */
  SPELL_RESISTANCE(SituationKind.SPELL_RESISTANCE),
  /** Natürliche Immunität (GRW S. 231): je Stufe ein Würfel mehr gegen Toxine und Krankheiten (situativer Pool). */
  NATURAL_IMMUNITY(SituationKind.TOXIN_RESISTANCE),
  /** Beschleunigte Heilung (GRW S. 230): je Stufe ein Würfel mehr auf Heilungsproben (situativer Pool). */
  RAPID_HEALING(SituationKind.HEALING),
  /** Großer Sprung (GRW S. 230): je Stufe ein Würfel mehr auf Sprungproben (situativer Pool). */
  GREAT_LEAP(SituationKind.JUMP),
  /** Körpersprache (GRW S. 231): je Stufe ein Würfel mehr auf soziale Proben (situativer Bonus). */
  BODY_LANGUAGE(SituationKind.SOCIAL),
  /** Kritischer Schlag (GRW S. 231): je Stufe +1 Schaden waffenlos. */
  CRITICAL_STRIKE;

  private final @Nullable SituationKind situation;

  AdeptPowerEffect() {
    this(null);
  }

  AdeptPowerEffect(@Nullable SituationKind situation) {
    this.situation = situation;
  }

  /** Anlass des situativen Pools, in dem die Kraft je Stufe einen Würfel gibt, oder {@code null}. */
  public @Nullable SituationKind getSituation() {
    return situation;
  }
}
