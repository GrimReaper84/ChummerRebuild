package de.shalorian.chummer.model.character.option;

import de.shalorian.chummer.model.character.rulebook.RulebookDataLoader;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.jspecify.annotations.NonNull;

/**
 * Regel-Einstellungen eines Charakters: Auslegungen, wo das GRW mehrdeutig ist, und Hausregeln, die eine Runde am
 * Tisch zuschaltet. Sie stehen in der Charakterdatei, damit ein weitergegebener Charakter überall dieselben Werte
 * zeigt; alle Schalter sind anfangs aus, dann gilt das Standardverhalten. Anders als der Spielleiter-Modus
 * ({@code Character#isGmMode}) sind sie dauerhaft und heben keine Kosten auf.
 */
public class RuleOptions {

  private boolean limbArmorAverage;
  private boolean lateAwakening;
  private boolean missedLifestylePayments;
  /** Zugeschaltete Zusatzbände (Kürzel); das Grundregelwerk ist immer aktiv und steht nicht darin. */
  private final Set<String> extraBooks = new TreeSet<>();

  /**
   * Gliedpanzerung als Durchschnitt über die fünf Körperteile (zwei Arme, zwei Beine, Torso) statt als Summe aller
   * angeschlossenen Glieder. Das GRW sagt auf S. 390 nur, dass sie zur getragenen Panzerung zählt; die Summe
   * entspricht dem C#-Original, der Durchschnitt rechnet wie die Attribute der Glieder. Auslegung, kein GRW-Beleg.
   */
  public boolean isLimbArmorAverage() {
    return limbArmorAverage;
  }

  public void setLimbArmorAverage(boolean limbArmorAverage) {
    this.limbArmorAverage = limbArmorAverage;
  }

  /**
   * Spätes Erwachen: Adept, Zauberer, Magieradept und Technomancer auch nach der Erschaffung wählbar, für latente
   * Charaktere. Hebt die Sperre aus GRW S. 313 auf; Hausregel.
   */
  public boolean isLateAwakening() {
    return lateAwakening;
  }

  public void setLateAwakening(boolean lateAwakening) {
    this.lateAwakening = lateAwakening;
  }

  /** Im Karrieremodus darf ein Monat Lebensstil ausgesetzt werden, mit Würfelwurf, Abstieg und Schulden (S. 312). */
  public boolean isMissedLifestylePayments() {
    return missedLifestylePayments;
  }

  public void setMissedLifestylePayments(boolean missedLifestylePayments) {
    this.missedLifestylePayments = missedLifestylePayments;
  }

  /**
   * Ob das Regelwerk {@code code} aktiv ist. Das Grundregelwerk und ein leeres Kürzel sind es immer; Zusatzbände nur,
   * wenn zugeschaltet. Neue Charaktere haben nur das Grundregelwerk (Nutzerentscheidung 2026-11-18).
   */
  public boolean isBookEnabled(@NonNull String code) {
    return code.isBlank() || code.equals(RulebookDataLoader.getCore().code()) || extraBooks.contains(code);
  }

  /**
   * Schaltet einen Zusatzband zu oder ab; das Grundregelwerk lässt sich nicht abschalten. Unbekannte Kürzel (z.B. aus
   * einer Datei, deren Regelwerk dieses Programm nicht kennt) werden behalten, wirken aber nicht.
   */
  public void setBookEnabled(@NonNull String code, boolean enabled) {
    if (code.isBlank() || code.equals(RulebookDataLoader.getCore().code())) {
      return;
    }
    if (enabled) {
      extraBooks.add(code);
    } else {
      extraBooks.remove(code);
    }
  }

  /** Übernimmt alle Einstellungen von {@code other}, z.B. nach Rückgängig (Einstellungen sind keine Buchung). */
  public void copyFrom(@NonNull RuleOptions other) {
    limbArmorAverage = other.limbArmorAverage;
    lateAwakening = other.lateAwakening;
    missedLifestylePayments = other.missedLifestylePayments;
    extraBooks.clear();
    extraBooks.addAll(other.extraBooks);
  }

  /** Die zugeschalteten Zusatzbände, ohne das Grundregelwerk. */
  public @NonNull Set<String> getExtraBooks() {
    return Collections.unmodifiableSet(extraBooks);
  }

  /** Ob ein Katalogeintrag angeboten werden darf: ohne Quellenangabe immer, sonst nur aus aktiven Regelwerken. */
  public boolean allows(@NonNull Object entry) {
    return !(entry instanceof Sourced sourced) || isBookEnabled(sourced.source());
  }
}
