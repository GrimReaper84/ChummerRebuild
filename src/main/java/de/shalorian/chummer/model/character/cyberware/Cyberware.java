package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein in einen Charakter eingebautes Cyber- oder Bioware-Implantat: referenziert einen {@link
 * CyberwareData}-Katalogeintrag und hält die gewählte {@link CyberwareGrade} sowie — nur bei
 * stufenbehafteten Einträgen ({@link CyberwareData#hasRating()}) — die gewählte Stufe.
 *
 * <p>Essenzkosten = Katalog-Essenzformel (für die Stufe ausgewertet) × Essenz-Multiplikator der
 * Grade; Preis = Katalog-Preisformel × Preis-Multiplikator der Grade (GRW S. 359: Katalogpreise sind
 * Standardware-Preise). Die Essenzkosten werden hier bewusst
 * <b>nicht</b> gerundet — gerundet wird einheitlich erst bei der Gesamt-Essenz auf Charakterebene.
 * Anders als Zauber/Komplexe Formen darf dasselbe Implantat mehrfach eingebaut werden, daher arbeitet
 * {@code Equipment#removeCyberware} über die Objektreferenz.
 *
 * <p><b>Einbauten:</b> Ein Basissystem ({@link CyberwareData#hasCapacity()}, Cyberaugen und Cyberohren) nimmt
 * Zubehör seiner Kategorie auf. Eingebautes Zubehör kostet Kapazität statt Essenz (GRW S. 386, 387) und hat die Grade
 * des Basissystems (wie im C#-Original); seine Essenz zählt deshalb nirgends mit, sein Preis im Preis des
 * Basissystems. Die Kommlink-Headware nimmt ein Kommlink-Modell aus dem Ausrüstungskatalog auf (S. 386); dessen
 * Preis kommt ohne Grade-Multiplikator dazu.
 */
public class Cyberware {

  private final @NonNull CyberwareData data;
  private @NonNull CyberwareGrade grade;
  private final int rating;
  private final @NonNull String choice;
  private final @Nullable Integer chosenPrice;
  private final List<Cyberware> installed = new ArrayList<>();
  private @Nullable Gear commlink;

  /**
   * @param rating      gewählte Stufe; muss bei stufenbehafteten Einträgen in [{@value CyberwareData#MIN_RATING},
   *                    {@link CyberwareData#maxRating()}] liegen, bei Einträgen ohne Stufe {@code 0} sein
   * @param choice      gewählte Fertigkeit bzw. Fertigkeitsgruppe bei {@link CyberwareData#selection()}, sonst leer
   * @param chosenPrice gewählter Preis bei {@link CyberwareData#hasVariablePrice()}, sonst {@code null}
   * @throws IllegalArgumentException bei unzulässiger Stufe, fehlender Auswahl oder unzulässigem Preis
   */
  public Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, int rating, @NonNull String choice,
      @Nullable Integer chosenPrice) {
    this.data = data;
    this.grade = grade;
    this.rating = rating;
    this.choice = choice.trim();
    this.chosenPrice = chosenPrice;
    String problem = problem();
    if (problem != null) {
      throw new IllegalArgumentException(data.name() + ": " + problem);
    }
  }

  /** Implantat ohne Auswahl und mit festem Preis. */
  public Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, int rating) {
    this(data, grade, rating, "", null);
  }

  private @Nullable String problem() {
    boolean validRating = data.hasRating()
        ? rating >= CyberwareData.MIN_RATING && rating <= data.maxRating()
        : rating == 0;
    if (!validRating) {
      return "Stufe %d ist nicht zulässig (max. %d)".formatted(rating, data.maxRating());
    }
    if ((data.selection() == ImplantSelection.NONE) != choice.isEmpty()) {
      return data.selection() == ImplantSelection.NONE ? "Auswahl nicht vorgesehen" : "Auswahl fehlt";
    }
    if (data.hasVariablePrice() != (chosenPrice != null)) {
      return data.hasVariablePrice() ? "Preis fehlt" : "Preis nicht wählbar";
    }
    if (chosenPrice != null && !data.isValidPrice(chosenPrice)) {
      return "Preis %d ¥ liegt außerhalb von %d–%d ¥".formatted(chosenPrice, data.priceMin(), data.priceMax());
    }
    return null;
  }

  public @NonNull CyberwareData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Name mit Auswahl, z.B. {@code "Reflexrekorder (Fertigkeit): Pistolen"}. */
  public @NonNull String getDisplayName() {
    return choice.isEmpty() ? data.name() : data.name() + ": " + choice;
  }

  public @NonNull EquipmentKind getKind() {
    return data.kind();
  }

  public @NonNull CyberwareGrade getGrade() {
    return grade;
  }

  /** Setzt die Grade, auch für alles eingebaute Zubehör (es hat immer die Grade des Basissystems). */
  public void setGrade(@NonNull CyberwareGrade grade) {
    this.grade = grade;
    installed.forEach(child -> child.setGrade(grade));
  }

  /** Gewählte Stufe, bzw. {@code 0} bei Implantaten ohne Stufe. */
  public int getRating() {
    return rating;
  }

  /** Gewählte Fertigkeit bzw. Fertigkeitsgruppe, sonst leer. */
  public @NonNull String getChoice() {
    return choice;
  }

  /** Gewählter Preis bei Preisspanne, sonst {@code null}. */
  public @Nullable Integer getChosenPrice() {
    return chosenPrice;
  }

  /** Exakte (ungerundete) Essenzkosten dieses Implantats inkl. Grade-Multiplikator, ohne Einbauten. */
  public @NonNull BigDecimal getEssenceCost() {
    return RatingFormula.evaluate(data.essence(), rating).multiply(grade.getEssenceMultiplier());
  }

  /**
   * Preis in Nuyen: Katalogpreis (bzw. gewählter Preis plus Aufschlag) × Preis-Multiplikator der Grade, kaufmännisch
   * auf ganze Nuyen gerundet, plus eingebautes Zubehör und eingebautes Kommlink.
   */
  public int getNuyenCost() {
    return getOwnNuyenCost() + installed.stream().mapToInt(Cyberware::getNuyenCost).sum()
        + (commlink == null ? 0 : commlink.getNuyenCost());
  }

  /** Preis nur dieses Implantats ohne Einbauten (für die Anzeige je Zeile). */
  public int getOwnNuyenCost() {
    BigDecimal listPrice = RatingFormula.evaluate(data.cost(), rating)
        .add(BigDecimal.valueOf(chosenPrice == null ? 0 : chosenPrice));
    return listPrice
        .multiply(BigDecimal.valueOf(grade.getPriceMultiplier()))
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }

  // --- Einbauten in Basissysteme ---

  /** Eingebautes Zubehör in Einbaureihenfolge. */
  public @NonNull List<Cyberware> getInstalled() {
    return Collections.unmodifiableList(installed);
  }

  /** Kapazität des Basissystems in seiner Stufe, {@code 0} bei anderen Implantaten. */
  public int getCapacity() {
    return data.capacityFor(rating);
  }

  /** Vom eingebauten Zubehör belegte Kapazität. */
  public int getUsedCapacity() {
    return installed.stream().mapToInt(c -> c.data.capacityCostFor(c.rating)).sum();
  }

  /**
   * Warum {@code child} in Stufe {@code childRating} nicht eingebaut werden kann, oder {@code null}: Dieses
   * Implantat muss ein Basissystem sein, das Zubehör aus derselben Kategorie stammen, einbaubar sein (nicht schon im
   * Basissystem enthalten, S. 386/388), noch nicht eingebaut sein (außer {@link ImplantRestrictions#multiple()}) und in
   * die freie Kapazität passen.
   */
  public @Nullable String installProblem(@NonNull CyberwareData child, int childRating) {
    if (!data.hasCapacity() || !child.category().equals(data.category())) {
      return child.name() + " lässt sich nicht in " + data.name() + " einbauen";
    }
    if (CyberwareData.INCLUDED_IN_BASE.equals(child.capacityCost())) {
      return child.name() + " ist im " + data.name() + " schon enthalten";
    }
    if (!child.isInstallable()) {
      return child.name() + " lässt sich nicht in " + data.name() + " einbauen";
    }
    if (!child.restrictions().multiple() && installed.stream().anyMatch(c -> c.data.equals(child))) {
      return child.name() + " ist bereits eingebaut";
    }
    int needed = child.capacityCostFor(childRating);
    int free = getCapacity() - getUsedCapacity();
    if (needed > free) {
      return "Kapazität reicht nicht: %s braucht %d, frei sind %d von %d".formatted(child.name(), needed, free,
          getCapacity());
    }
    return null;
  }

  /**
   * Baut {@code child} ein; es übernimmt die Grade dieses Basissystems.
   *
   * @throws IllegalArgumentException wenn {@link #installProblem(CyberwareData, int)} einen Grund liefert
   */
  public void install(@NonNull Cyberware child) {
    String problem = installProblem(child.data, child.rating);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    child.setGrade(grade);
    installed.add(child);
  }

  /** Baut genau dieses Zubehör aus; {@code false}, wenn es hier nicht eingebaut ist. */
  public boolean uninstall(@NonNull Cyberware child) {
    return installed.remove(child);
  }

  // --- Eingebautes Kommlink (Kommlink-Headware) ---

  /** Eingebautes Kommlink-Modell samt Betriebssystem, oder {@code null}. */
  public @Nullable Gear getCommlink() {
    return commlink;
  }

  /**
   * Warum {@code gear} nicht als Kommlink eingebaut werden kann, oder {@code null}: Das Implantat muss Geräte dieser
   * Kategorie aufnehmen ({@link CyberwareData#acceptedGear()}) und darf noch keins enthalten.
   */
  public @Nullable String commlinkProblem(@NonNull GearData gear) {
    if (data.acceptedGear() == null || gear.category() != data.acceptedGear()) {
      return gear.name() + " lässt sich nicht in " + data.name() + " einbauen";
    }
    if (commlink != null) {
      return data.name() + " enthält bereits " + commlink.getData().name();
    }
    return null;
  }

  /**
   * Baut ein Kommlink ein.
   *
   * @throws IllegalArgumentException wenn {@link #commlinkProblem(GearData)} einen Grund liefert
   */
  public void setCommlink(@NonNull Gear gear) {
    String problem = commlinkProblem(gear.getData());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    commlink = gear;
  }

  /** Baut das Kommlink aus. */
  public void removeCommlink() {
    commlink = null;
  }
}
