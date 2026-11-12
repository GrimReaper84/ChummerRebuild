package de.shalorian.chummer.view.tab;

import org.jspecify.annotations.NonNull;

/**
 * Rückmeldungen der Tab-Builder an den Charakterbogen ({@code CharacterViewController}). Die Tabs melden nur,
 * <em>was</em> sich fachlich geändert hat; welche anderen Anzeigen deshalb neu gebaut werden, entscheidet der
 * Charakterbogen an einer Stelle (Nutzerentscheidung 2026-11-11,
 * {@code docs/plaene/archiv/character_view_aufteilung_plan.md}).
 */
public interface CharacterTabHost {

  /** Dialoge, Hinweise und Karrieremodus-Abbuchung. */
  @NonNull TabServices services();

  /**
   * Ein Wert hat sich geändert, der die +/−-Zeilen anderer Bereiche beeinflussen kann (Restpunkte, Karma, Caps):
   * alle Zeilen neu bewerten.
   */
  void refreshRows();

  /** Der Name des Charakters wurde geändert (Kopfzeile nachziehen). */
  void onNameChanged();

  /** Die verbleibenden BP haben sich geändert. */
  void onBpChanged();

  /** Karma- oder Nuyen-Log hat sich geändert (z.B. Abbuchung). */
  void onLogChanged();

  /** Ein Kaufpreis hat sich geändert: Restgeld-Anzeige nachziehen. */
  void onNuyenSpentChanged();

  /**
   * Gaben oder Handicaps wurden hinzugefügt bzw. entfernt: davon abhängige Tabs (Attribute, Fertigkeiten,
   * Cyberware, Sichtbarkeit von Magie/Technomancer) und BP nachziehen.
   */
  void onQualitiesChanged();

  /** Implantate wurden ein- oder ausgebaut: Essenz, MAG/RES (Attribute) und Restgeld nachziehen. */
  void onImplantsChanged();

  /**
   * Gekaufte oder angelegte Rüstung hat sich geändert: Restgeld und alle Anzeigen, die von der
   * Panzerungsbehinderung abhängen (Aug-Spalte GES/REA), nachziehen.
   */
  void onArmorChanged();

  /**
   * Adeptenkräfte oder ihre Stufen haben sich geändert: Attribute, Würfelpools, Initiative, Zustandsmonitor und Schutz
   * nachziehen.
   */
  void onAdeptPowersChanged();
}
