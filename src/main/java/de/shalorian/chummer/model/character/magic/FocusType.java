package de.shalorian.chummer.model.character.magic;

import de.shalorian.chummer.model.character.situation.SituationKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Fokusart mit Bindungsfaktor (Karma = Kraft × Faktor, Tabelle Fokus-Bindung GRW S. 234, am Bild geprüft) und dem
 * Anlass, für den der Fokus Würfel in Höhe seiner Kraft gibt (S. 233/234). Der Zauberspeicher gibt keine Würfel, er hält
 * einen Zauber aufrecht.
 */
public enum FocusType {
  BINDING(3, SituationKind.SPIRIT_BIND),
  SUMMONING(4, SituationKind.SPIRIT_SUMMON),
  POWER(8, SituationKind.MAGIC),
  COUNTERSPELLING(3, SituationKind.COUNTERSPELL_CATEGORY),
  BANISHING(3, SituationKind.SPIRIT_BANISH),
  WEAPON(3, SituationKind.SKILL),
  SPELL_STORING(2, null),
  SPELLCASTING(4, SituationKind.SPELL_CATEGORY);

  private final int karmaFactor;
  private final @Nullable SituationKind situation;

  FocusType(int karmaFactor, @Nullable SituationKind situation) {
    this.karmaFactor = karmaFactor;
    this.situation = situation;
  }

  /** Karma je Kraftpunkt für die Bindung im Karrieremodus. */
  public int getKarmaFactor() {
    return karmaFactor;
  }

  /** Anlass der Würfel; {@code null} beim Zauberspeicher. */
  public @Nullable SituationKind getSituation() {
    return situation;
  }

  /** Karma für die Bindung eines Fokus der Kraft {@code force}. */
  public int bindingKarma(int force) {
    return force * karmaFactor;
  }
}
