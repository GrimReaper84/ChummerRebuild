package de.shalorian.chummer.model.character.gear;

import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.equipment.Legality;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 *
 * <p><b>Einbauten:</b> Geräte nehmen Gegenstände der Kategorie {@link GearData#accepts()} als Kinder
 * auf: ein Kommlink höchstens ein Betriebssystem (Nutzerentscheidung 2026-10-04), Sicht- und
 * Audiosensoren Verstärkungen und Sensorpakete Sensor-Funktionen bis zu ihrer Kapazität, ein
 * Magschloss seine Zusätze (GRW S. 379–382). Der Preis der Einbauten zählt zum Gerät, Zuschläge auf
 * die Verfügbarkeit erhöhen dessen Verfügbarkeit ({@link #getAvailabilityText()}). Matrixwerte siehe
 * {@link #getCommlinkStats()}.
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
  private final List<Gear> installed = new ArrayList<>();

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
    if (data.extraKind().isChoice() && !data.extraKind().suggestions().contains(extra)) {
      return "\"" + extra + "\" ist keine zulässige Angabe für " + data.extraLabel();
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

  /** Ob sich eine Packung abziehen lässt; mindestens eine Packung bleibt immer. */
  public boolean canRemovePack() {
    return quantity > data.packSize();
  }

  /**
   * Erhöht die Menge um eine Packung (Nachkauf; kostet {@link #getPackPrice()}).
   *
   * @throws IllegalArgumentException wenn der Gesamtpreis zu groß würde
   */
  public void addPack() {
    setQuantity(quantity + data.packSize());
  }

  /**
   * Verringert die Menge um eine Packung (Verbrauch, ohne Erstattung — Nutzerentscheidung
   * 2026-10-04).
   *
   * @throws IllegalStateException wenn nur noch eine Packung übrig ist ({@link #canRemovePack()})
   */
  public void removePack() {
    if (!canRemovePack()) {
      throw new IllegalStateException(data.name() + ": mindestens eine Packung bleibt");
    }
    quantity -= data.packSize();
  }

  /** Preis einer Packung: gewählter Preis bei Preisspanne, sonst laut Stufenformel. */
  public int getPackPrice() {
    return chosenPackPrice != null ? chosenPackPrice : data.packCostFor(rating);
  }

  /** Preis der aktuellen Menge samt Einbauten (Betriebssystem, Verstärkungen, Sensoren, Zusätze). */
  public int getNuyenCost() {
    return getPackPrice() * quantity / data.packSize()
        + installed.stream().mapToInt(Gear::getNuyenCost).sum();
  }

  // --- Einbauten ---

  /** Eingebaute Gegenstände in Einbaureihenfolge. */
  public @NonNull List<Gear> getInstalled() {
    return Collections.unmodifiableList(installed);
  }

  /** Kapazität des Geräts in seiner Stufe, {@code 0} ohne Kapazität. */
  public int getCapacity() {
    return data.capacityFor(rating);
  }

  /** Von den Einbauten belegte Kapazität. */
  public int getUsedCapacity() {
    return installed.stream().mapToInt(g -> g.data.capacityCostFor(g.rating)).sum();
  }

  /**
   * Warum {@code child} in Stufe {@code childRating} nicht in diesen Gegenstand eingebaut werden kann,
   * oder {@code null}. Das Gerät muss die Kategorie aufnehmen ({@link GearData#accepts()}); ein
   * Kommlink nimmt nur ein Betriebssystem auf; derselbe Einbau steckt höchstens einmal im Gerät, außer
   * er ist "pro Stück" erhältlich (Biometrischer Scanner, GRW S. 382); die Einbauten dürfen die
   * Kapazität nicht übersteigen (GRW S. 379/380).
   */
  public @Nullable String installProblem(@NonNull GearData child, int childRating) {
    if (child.category() != data.accepts()) {
      return child.name() + " lässt sich nicht in " + data.name() + " einbauen";
    }
    if (child.category() == GearCategory.OPERATING_SYSTEMS && !installed.isEmpty()) {
      return data.name() + " hat bereits ein Betriebssystem";
    }
    if (!child.repeatable() && installed.stream().anyMatch(g -> g.data.equals(child))) {
      return child.name() + " ist bereits eingebaut";
    }
    int needed = child.capacityCostFor(childRating);
    int free = getCapacity() - getUsedCapacity();
    if (data.hasCapacity() && needed > free) {
      return "Kapazität reicht nicht: " + child.name() + " braucht " + needed + ", frei sind " + free
          + " von " + getCapacity();
    }
    return null;
  }

  /**
   * Baut {@code child} ein.
   *
   * @throws IllegalArgumentException wenn {@link #installProblem(GearData, int)} einen Grund liefert
   */
  public void install(@NonNull Gear child) {
    String problem = installProblem(child.data, child.rating);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    installed.add(child);
  }

  /** Baut genau diesen Gegenstand aus; {@code false}, wenn er hier nicht eingebaut ist. */
  public boolean uninstall(@NonNull Gear child) {
    return installed.remove(child);
  }

  /**
   * Matrixwerte eines Kommlinks mit seinem Betriebssystem (GRW S. 260, 374), oder {@code null} bei
   * anderen Gegenständen.
   */
  public @Nullable CommlinkStats getCommlinkStats() {
    if (data.category() != GearCategory.COMMLINKS) {
      return null;
    }
    return CommlinkStats.of(data, installed.isEmpty() ? null : installed.get(0).data);
  }

  // --- Verfügbarkeit ---

  /**
   * Verfügbarkeit des Geräts in seiner Stufe samt der Zuschläge seiner Einbauten ("+4", "+4E",
   * GRW S. 379/382), als fester Wert; die höchste Legalitätsstufe gilt. Beispiel: Brille (–) mit
   * Restlichtverstärkung (+4) und Infrarot (+6) → 10. Einbauten mit eigener Verfügbarkeit
   * (Sensor-Funktionen) ändern die Verfügbarkeit des Geräts nicht.
   */
  private @NonNull Availability deviceAvailability() {
    Availability sum = new Availability(String.valueOf(data.availability().evaluate(rating)),
        data.availability().legality());
    for (Gear child : installed) {
      sum = plusSurcharge(sum, child.data, child.rating);
    }
    return sum;
  }

  /**
   * Verfügbarkeit des Geräts samt Zuschlägen, nachdem zusätzlich {@code child} in Stufe {@code
   * childRating} eingebaut wäre.
   */
  public @NonNull Availability availabilityWith(@NonNull GearData child, int childRating) {
    return plusSurcharge(deviceAvailability(), child, childRating);
  }

  /** {@code sum} plus den Zuschlag von {@code child}; unverändert, wenn es kein Zuschlag ist. */
  private static @NonNull Availability plusSurcharge(@NonNull Availability sum,
      @NonNull GearData child, int childRating) {
    if (!child.availabilitySurcharge()) {
      return sum;
    }
    Availability surcharge = child.availability();
    Legality legality = surcharge.legality().compareTo(sum.legality()) > 0
        ? surcharge.legality() : sum.legality();
    return new Availability(String.valueOf(sum.evaluate(0) + surcharge.evaluate(childRating)),
        legality);
  }

  /**
   * Verfügbarkeit wie im GRW angezeigt: beim Gerät inklusive Zuschlägen der Einbauten, bei einem
   * Zuschlag selbst mit "+" ("+4E").
   */
  public @NonNull String getAvailabilityText() {
    return data.availabilitySurcharge()
        ? data.availabilityText(rating) : deviceAvailability().displayText(0);
  }

  /**
   * Ob noch einer der {@code candidates} in dieses Gerät passt (in seiner niedrigsten Stufe, ohne
   * Verfügbarkeitsprüfung); {@code false} auch bei Gegenständen ohne Einbauten.
   */
  public boolean canInstallAnyOf(@NonNull Collection<GearData> candidates) {
    return candidates.stream()
        .filter(candidate -> candidate.category() == data.accepts())
        .anyMatch(candidate -> installProblem(candidate, candidate.lowestRating()) == null);
  }

  /**
   * Ob {@code other} derselbe Gegenstand ist und beim Kauf mit diesem zusammengeführt wird: gleicher
   * Katalogeintrag, gleiche Stufe, gleicher Zusatztext und gleicher gewählter Preis. Einzelstücke
   * ({@link GearData#isSingleItem()}, z.B. Kommlinks) werden nie zusammengeführt.
   */
  public boolean isSameKind(@NonNull Gear other) {
    return !data.isSingleItem() && data.equals(other.data) && rating == other.rating && extra.equals(other.extra)
        && Objects.equals(chosenPackPrice, other.chosenPackPrice);
  }

  /**
   * Nimmt {@code item} in {@code items} auf: Gibt es dort schon einen Gegenstand derselben Art ({@link
   * #isSameKind(Gear)}), wird nur dessen Menge erhöht (Nutzerentscheidung 2026-10-04), sonst wird
   * {@code item} angehängt. Gemeinsame Regel für die Ausrüstung des Charakters und der Fahrzeuge.
   *
   * @return der Gegenstand, der die Menge jetzt enthält ({@code item} oder der vorhandene)
   */
  public static @NonNull Gear addOrMerge(@NonNull List<Gear> items, @NonNull Gear item) {
    for (Gear existing : items) {
      if (existing.isSameKind(item)) {
        existing.setQuantity(existing.getQuantity() + item.getQuantity());
        return existing;
      }
    }
    items.add(item);
    return item;
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
