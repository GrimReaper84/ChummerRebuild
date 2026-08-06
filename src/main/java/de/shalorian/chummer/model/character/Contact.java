package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein Kontakt (NSC) eines Charakters mit Connection- und Loyalitätsstufe (SR4-Grundregelwerk S.
 * 329f., jeweils 1–6). Bewusst reduzierter Kern-Scope gegenüber dem C#-Original: kein
 * Enemy-/Pet-Unterschied, keine Gruppenkontakt-Modifikatoren, keine Datei-Verknüpfung — siehe
 * {@code docs/kontakte_analyse.md}. Namensduplikate sind zulässig (siehe {@link
 * Character#addContact(String, String)}).
 */
public class Contact {

  public static final int MIN_RATING = 1;
  public static final int MAX_RATING = 6;

  private @NonNull String name;
  private @Nullable String description;
  private int connection = MIN_RATING;
  private int loyalty = MIN_RATING;

  /**
   * Legt einen neuen Kontakt an. Connection und Loyalty starten auf dem Minimum ({@link
   * #MIN_RATING}) und werden danach über {@link #setConnection(int)}/{@link #setLoyalty(int)}
   * angepasst.
   */
  public Contact(@NonNull String name, @Nullable String description) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("Name eines Kontakts darf nicht leer sein");
    }
    this.name = name;
    setDescription(description);
  }

  public @NonNull String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("Name eines Kontakts darf nicht leer sein");
    }
    this.name = name;
  }

  /**
   * Freitext-Kurzbeschreibung des Kontakts, oder {@code null} wenn keine gesetzt ist. Wird in der
   * UI als Tooltip auf dem Namen angezeigt.
   */
  public @Nullable String getDescription() {
    return description;
  }

  /**
   * Setzt die Kurzbeschreibung. Ein leerer oder nur aus Leerzeichen bestehender Wert wird als
   * "keine Beschreibung" ({@code null}) normalisiert.
   */
  public void setDescription(@Nullable String description) {
    this.description = (description == null || description.isBlank())
        ? null
        : description.trim();
  }

  public int getConnection() {
    return connection;
  }

  public void setConnection(int connection) {
    if (connection < MIN_RATING || connection > MAX_RATING) {
      throw new IllegalArgumentException(
          "%s: Connection %d liegt außerhalb [%d/%d]"
              .formatted(name, connection, MIN_RATING, MAX_RATING));
    }
    this.connection = connection;
  }

  public int getLoyalty() {
    return loyalty;
  }

  public void setLoyalty(int loyalty) {
    if (loyalty < MIN_RATING || loyalty > MAX_RATING) {
      throw new IllegalArgumentException(
          "%s: Loyalty %d liegt außerhalb [%d/%d]"
              .formatted(name, loyalty, MIN_RATING, MAX_RATING));
    }
    this.loyalty = loyalty;
  }
}
