package de.shalorian.chummer.model.character.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekaufter Ausrüstungsgegenstand: Katalogeintrag, beim Kauf festgelegte Stufe, Zusatztext und —
 * bei Preisspannen — der gewählte Packungspreis sowie die veränderliche Menge. Stufe, Zusatztext und
 * Preis stehen nach dem Kauf fest (Nutzerentscheidung 2026-10-04), nur die Menge bleibt änderbar.
 *
 * <p>Die Menge zählt Einheiten ({@link GearData#unit()}), nicht Packungen: 40 Standard-RFID-Marker
 * sind zwei Packungen "pro 20 Stück" (GRW S. 374). Preis = Packungspreis × Menge ÷ Packungsgröße; die
 * Menge muss ein Vielfaches der Packungsgröße sein, daher wird nicht gerundet.
 */
public class Gear {

  /**
   * Obergrenze für einen frei gewählten Packungspreis (Preisspanne). Das GRW nennt keine Obergrenze
   * ("200+ ¥"); die Grenze verhindert nur unsinnige Eingaben.
   */
  public static final int MAX_CHOSEN_PACK_PRICE = 1_000_000;

  private final @NonNull GearData data;
  private final int rating;
  private final @NonNull String extra;
  private final @Nullable Integer chosenPackPrice;
  private int quantity;

  /**
   * @param rating          Stufe in [{@link GearData#minRating()}, {@link GearData#maxRating()}],
   *                        bzw. {@code 0} ohne Stufe
   * @param quantity        Menge in Einheiten, siehe {@link GearData#isValidQuantity(int)}
   * @param extra           Zusatztext; Pflicht, wenn {@link GearData#needsExtra()}, sonst leer
   * @param chosenPackPrice bei Preisspanne der gewählte Packungspreis (GRW-Mindestpreis bis {@value
   *                        #MAX_CHOSEN_PACK_PRICE} ¥), sonst {@code null}
   * @throws IllegalArgumentException wenn einer der Werte nicht zum Katalogeintrag passt
   */
  public Gear(@NonNull GearData data, int rating, int quantity, @NonNull String extra,
      @Nullable Integer chosenPackPrice) {
    this.data = data;
    this.rating = rating;
    this.extra = extra.trim();
    this.chosenPackPrice = chosenPackPrice;
    String problem = problem(quantity);
    if (problem != null) {
      throw new IllegalArgumentException(data.name() + ": " + problem);
    }
    this.quantity = quantity;
  }

  /** Gegenstand ohne Zusatztext und Preisspanne, Menge eine Packung. */
  public Gear(@NonNull GearData data, int rating) {
    this(data, rating, data.packSize(), "", null);
  }

  private @Nullable String problem(int quantity) {
    if (!data.isValidRating(rating)) {
      return "Stufe " + rating + " ist nicht zulässig";
    }
    if (data.needsExtra() == extra.isEmpty()) {
      return data.needsExtra() ? data.extraLabel() + " fehlt" : "Zusatztext nicht vorgesehen";
    }
    if (data.variableCost() != (chosenPackPrice != null)) {
      return data.variableCost() ? "Preis fehlt" : "Preis ist fest";
    }
    if (chosenPackPrice != null && chosenPackPrice < data.packCostFor(rating)) {
      return "Preis liegt unter dem Mindestpreis " + data.packCostFor(rating) + " ¥";
    }
    if (chosenPackPrice != null && chosenPackPrice > MAX_CHOSEN_PACK_PRICE) {
      return "Preis liegt über " + MAX_CHOSEN_PACK_PRICE + " ¥";
    }
    return quantityProblem(quantity);
  }

  /**
   * Warum {@code quantity} keine zulässige Menge ist (keine ganze Zahl Packungen ≥ 1 oder Gesamtpreis
   * nicht mehr darstellbar), oder {@code null}.
   */
  private @Nullable String quantityProblem(int quantity) {
    if (!data.isValidQuantity(quantity)) {
      return "Menge " + quantity + " ist kein Vielfaches von " + data.packSize();
    }
    if ((long) getPackPrice() * quantity / data.packSize() > Integer.MAX_VALUE) {
      return "Menge " + quantity + " ist zu groß";
    }
    return null;
  }

  public @NonNull GearData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Gewählte Stufe, bzw. {@code 0} ohne Stufe. */
  public int getRating() {
    return rating;
  }

  /** Zusatztext (Fertigkeit, Thema, Lizenz …), bzw. leer. */
  public @NonNull String getExtra() {
    return extra;
  }

  /** Bei Preisspanne der gewählte Packungspreis, sonst {@code null}. */
  public @Nullable Integer getChosenPackPrice() {
    return chosenPackPrice;
  }

  /** Menge in Einheiten. */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Ändert die Menge (Nachkauf oder Verbrauch).
   *
   * @throws IllegalArgumentException wenn {@code quantity} keine ganze Zahl Packungen (≥ 1) ist
   *                                  oder der Gesamtpreis zu groß würde
   */
  public void setQuantity(int quantity) {
    String problem = quantityProblem(quantity);
    if (problem != null) {
      throw new IllegalArgumentException(data.name() + ": " + problem);
    }
    this.quantity = quantity;
  }

  /** Preis einer Packung: gewählter Preis bei Preisspanne, sonst laut Stufenformel. */
  public int getPackPrice() {
    return chosenPackPrice != null ? chosenPackPrice : data.packCostFor(rating);
  }

  /** Preis der aktuellen Menge. */
  public int getNuyenCost() {
    return getNuyenCostFor(quantity);
  }

  /** Preis für {@code units} Einheiten dieses Gegenstands (Vielfaches der Packungsgröße). */
  public int getNuyenCostFor(int units) {
    return getPackPrice() * units / data.packSize();
  }

  /** Verfügbarkeit in der gewählten Stufe, wie im GRW angezeigt. */
  public @NonNull String getAvailabilityText() {
    return data.availability().displayText(rating);
  }

  /**
   * Ob {@code other} derselbe Gegenstand ist und beim Kauf mit diesem zusammengeführt wird: gleicher
   * Katalogeintrag, gleiche Stufe, gleicher Zusatztext und gleicher gewählter Preis.
   */
  public boolean isSameKind(@NonNull Gear other) {
    return data.equals(other.data) && rating == other.rating && extra.equals(other.extra)
        && Objects.equals(chosenPackPrice, other.chosenPackPrice);
  }

  /**
   * Anzeigename im Stil der GRW-Ausrüstungslisten (S. 125ff.), z.B. "Automatischer Dietrich (Stufe 4)",
   * "Gefälschte Lizenz (Stufe 3, Waffenlizenz)", "Cram (3 Dosen)", "DocWagon-Vertrag (Gold, 1 Jahr)",
   * "Standard-RFID-Marker ×40". Stückzahlen erscheinen nur ab 2 als "×n".
   */
  public @NonNull String getDisplayName() {
    List<String> details = new ArrayList<>();
    if (data.hasRating()) {
      details.add("Stufe " + rating);
    }
    if (!extra.isEmpty()) {
      details.add(extra);
    }
    boolean pieces = data.unit() == GearUnit.PIECE;
    if (!pieces) {
      details.add(data.unit().format(quantity));
    }
    String name = data.name();
    if (!details.isEmpty()) {
      String joined = String.join(", ", details);
      name = name.endsWith(")")
          ? name.substring(0, name.length() - 1) + ", " + joined + ")"
          : name + " (" + joined + ")";
    }
    return pieces && quantity > 1 ? name + " ×" + quantity : name;
  }
}
