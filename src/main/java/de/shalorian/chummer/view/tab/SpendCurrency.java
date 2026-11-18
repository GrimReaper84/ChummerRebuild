package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.utils.Numbers;
import org.jspecify.annotations.NonNull;

/**
 * Währung eines Karrieremodus-Kaufs mit den Textbausteinen für Hinweis und Bestätigungsdialog.
 * Karma und Nuyen folgen demselben Ablauf (Guthabenprüfung, Bestätigung, negativer Log-Eintrag),
 * unterscheiden sich aber in Einheit und Grammatik ("Verbleibendes Karma" / "Verbleibende Nuyen").
 */
enum SpendCurrency {
  KARMA("Karma", " Karma", "Verbleibendes Karma danach: ", ""),
  NUYEN("Nuyen", " ¥", "Verbleibende Nuyen danach: ", " ¥");

  private final @NonNull String displayName;
  private final @NonNull String amountSuffix;
  private final @NonNull String remainingPrefix;
  private final @NonNull String remainingSuffix;

  SpendCurrency(@NonNull String displayName, @NonNull String amountSuffix,
      @NonNull String remainingPrefix, @NonNull String remainingSuffix) {
    this.displayName = displayName;
    this.amountSuffix = amountSuffix;
    this.remainingPrefix = remainingPrefix;
    this.remainingSuffix = remainingSuffix;
  }

  @NonNull String getDisplayName() {
    return displayName;
  }

  /** Betrag mit Einheit, z.B. "5 Karma" oder "900 ¥". */
  @NonNull String formatAmount(int amount) {
    return Numbers.integer(amount) + amountSuffix;
  }

  /** Zeile mit dem Guthaben nach dem Kauf, z.B. "Verbleibende Nuyen danach: 100 ¥". */
  @NonNull String formatRemaining(int remaining) {
    return remainingPrefix + Numbers.integer(remaining) + remainingSuffix;
  }
}
