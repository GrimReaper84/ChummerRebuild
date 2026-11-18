package de.shalorian.chummer.model.character.magic;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import de.shalorian.chummer.model.character.situation.SituationKind;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Schutzpatron aus {@code mentors.xml} (GRW S. 234–237).
 *
 * @param bonuses       feste Vorteile und rechnende Nachteile
 * @param choiceKind    Anlass des Vorteils nach Wahl, {@code null} ohne Wahl (z.B. Katze: Akrobatik oder Infiltration)
 * @param choiceDice    Würfel des gewählten Vorteils
 * @param choiceOptions wählbare Fertigkeiten
 * @param drawback      Nachteil ohne Rechnung in eigenen Worten, sonst leer
 */
public record MentorData(@NonNull String name, @NonNull List<MentorBonus> bonuses, @Nullable SituationKind choiceKind,
                         int choiceDice, @NonNull List<String> choiceOptions, @NonNull String drawback,
                         @NonNull String source, @NonNull String page) implements CatalogEntry, Sourced {

  /** Trennt in der gespeicherten Auswahl Patron und gewählten Vorteil: „Katze (Infiltration)“. */
  private static final String OPEN = " (";

  public MentorData {
    bonuses = List.copyOf(bonuses);
    choiceOptions = List.copyOf(choiceOptions);
  }

  /** Auswahltexte für die Gabe: der Name, bei einem Vorteil nach Wahl je Möglichkeit „Name (Fertigkeit)“. */
  public @NonNull List<String> choiceLabels() {
    return choiceOptions.isEmpty() ? List.of(name)
        : choiceOptions.stream().map(o -> name + OPEN + o + ")").toList();
  }

  /** Alle rechnenden Boni bei der Auswahl {@code label} (siehe {@link #choiceLabels()}). */
  public @NonNull List<MentorBonus> bonusesFor(@NonNull String label) {
    if (choiceKind == null || !label.startsWith(name + OPEN)) {
      return bonuses;
    }
    String option = label.substring(name.length() + OPEN.length(), label.length() - 1);
    return Stream.concat(bonuses.stream(), Stream.of(
        new MentorBonus(choiceKind, option, choiceDice))).toList();
  }

  /**
   * Vorteile und Nachteile bei der Auswahl {@code label} zum Lesen, z.B. für den Auswahldialog: „Vorteile: +2
   * Spruch- und Ritualzauberei (Wahrnehmung), … Nachteile: …“.
   */
  public @NonNull String describe(@NonNull String label) {
    List<MentorBonus> all = bonusesFor(label);
    String penalties = bonusText(all.stream().filter(b -> b.dice() < 0));
    String drawbacks = Stream.of(penalties, drawback).filter(t -> !t.isEmpty()).collect(Collectors.joining("; "));
    return "Vorteile: " + bonusText(all.stream().filter(b -> b.dice() > 0))
        + (drawbacks.isEmpty() ? "" : "\nNachteile: " + drawbacks);
  }

  /** „+2 Kampfsinn, −1 Verhandlung“. */
  private static @NonNull String bonusText(@NonNull Stream<MentorBonus> bonuses) {
    return bonuses.map(b -> (b.dice() < 0 ? "−" : "+") + Math.abs(b.dice()) + " " + b.kind().describe(b.detail()))
        .collect(Collectors.joining(", "));
  }

  /** Ob {@code label} eine Auswahl dieses Patrons ist. */
  public boolean matches(@NonNull String label) {
    return choiceLabels().contains(label);
  }
}
