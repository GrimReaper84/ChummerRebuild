package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.equipment.Availability;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.weapon.WeaponDamage;
import de.shalorian.chummer.utils.Numbers;
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
 *
 * <p><b>Cybergliedmaßen</b> ({@link LimbSpec#isLimb()}, GRW S. 389–391) sitzen an einer {@link BodyLocation} und sind
 * angeschlossen oder abgenommen (Ersatzglieder). Sie haben eigene KON/STR/GES: Grundwert 3 bzw. beim Kauf
 * maßgefertigt (je Punkt über 3 Verfügbarkeit +1 und 1.500 ¥ auf den Endpreis, S. 390), dazu die Stufe einer
 * eingebauten Verbesserung. Eingebaut werden Verbesserungen, Zubehör, Cyber-Nahkampfwaffen sowie Head- und Bodyware mit
 * Gliedkapazität; sie kosten Kapazität statt Essenz. Die Regeln, die den Charakter brauchen (Torso, Maxima, zweites
 * Bein), prüft {@link Implants#getLimbInstallProblem}.
 */
public class Cyberware {

  /** Aufpreis je Punkt Maßanfertigung über 3 (S. 390). */
  private static final int CUSTOM_POINT_PRICE = 1500;
  /** Waffenloser Schaden mit einem Cyberglied (S. 390). */
  private static final String UNARMED_DAMAGE = "(STR÷2)K";

  /** Gratis-Anteil (Nuyen) aus dem Spielleiter-Modus bei der Erschaffung, ADR-070; zählt nicht ins Restgeld. */
  private int gmFreeNuyen;
  private final @NonNull CyberwareData data;
  private @NonNull CyberwareGrade grade;
  private final int rating;
  private final @NonNull String choice;
  private final @Nullable Integer chosenPrice;
  private final List<Cyberware> installed = new ArrayList<>();
  private @Nullable Gear commlink;
  private final @Nullable BodyLocation location;
  private final @NonNull LimbAttributes baseAttributes;
  private boolean attached = true;
  /** Zweiter Teil einer Sprunghydraulik: belegt Kapazität im anderen Bein, der Preis steckt im ersten Teil. */
  private final boolean pairedCopy;
  /** Das Gegenstück einer Sprunghydraulik im anderen Bein (Original bzw. zweiter Teil), sonst {@code null}. */
  private @Nullable Cyberware pairedPart;

  /**
   * @param rating      gewählte Stufe; muss bei stufenbehafteten Einträgen in [{@value CyberwareData#MIN_RATING},
   *                    {@link CyberwareData#maxRating()}] liegen, bei Einträgen ohne Stufe {@code 0} sein
   * @param choice      gewählte Fertigkeit bzw. Fertigkeitsgruppe bei {@link CyberwareData#selection()}, sonst leer
   * @param chosenPrice gewählter Preis bei {@link CyberwareData#hasVariablePrice()}, sonst {@code null}
   * @throws IllegalArgumentException bei unzulässiger Stufe, fehlender Auswahl oder unzulässigem Preis
   */
  public Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, int rating, @NonNull String choice,
      @Nullable Integer chosenPrice) {
    this(data, grade, rating, choice, chosenPrice, null, LimbAttributes.STANDARD, false);
  }

  /**
   * Cybergliedmaße an {@code location} mit den Grundwerten {@code baseAttributes} (Standard 3/3/3 oder maßgefertigt).
   *
   * @throws IllegalArgumentException wenn {@code data} kein Glied ist, die Stelle nicht zur Gliedart passt oder ein
   *                                  Grundwert unter 3 liegt bzw. der Schädel maßgefertigt werden soll
   */
  public Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, @NonNull BodyLocation location,
      @NonNull LimbAttributes baseAttributes) {
    this(data, grade, 0, "", null, location, baseAttributes, false);
  }

  private Cyberware(@NonNull CyberwareData data, @NonNull CyberwareGrade grade, int rating, @NonNull String choice,
      @Nullable Integer chosenPrice, @Nullable BodyLocation location, @NonNull LimbAttributes baseAttributes,
      boolean pairedCopy) {
    this.data = data;
    this.grade = grade;
    this.rating = rating;
    this.choice = choice.trim();
    this.chosenPrice = chosenPrice;
    this.location = location;
    this.baseAttributes = baseAttributes;
    this.pairedCopy = pairedCopy;
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
      return "Stufe %d ist nicht zulässig (max. %d).".formatted(rating, data.maxRating());
    }
    if ((data.selection() == ImplantSelection.NONE) != choice.isEmpty()) {
      return data.selection() == ImplantSelection.NONE ? "Auswahl nicht vorgesehen" : "Auswahl fehlt";
    }
    if (data.hasVariablePrice() != (chosenPrice != null)) {
      return data.hasVariablePrice() ? "Preis fehlt" : "Preis nicht wählbar";
    }
    if (chosenPrice != null && !data.isValidPrice(chosenPrice)) {
      return "Preis %s liegt außerhalb von %s–%s.".formatted(Numbers.nuyen(chosenPrice), Numbers.integer(data.priceMin()), Numbers.nuyen(data.priceMax()));
    }
    return limbProblem();
  }

  private @Nullable String limbProblem() {
    LimbPart part = data.limb().part();
    if (part == null) {
      return location == null && baseAttributes.equals(LimbAttributes.STANDARD) ? null : "kein Cyberglied";
    }
    if (location == null || !part.locations().contains(location)) {
      return "Körperstelle " + (location == null ? "fehlt" : location.getDisplayName() + " passt nicht");
    }
    int lowest = Math.min(baseAttributes.body(), Math.min(baseAttributes.strength(), baseAttributes.agility()));
    if (lowest < LimbAttributes.STANDARD_VALUE) {
      return "Grundwerte beginnen bei " + LimbAttributes.STANDARD_VALUE;
    }
    if (!part.hasAttributes() && baseAttributes.pointsAboveStandard() > 0) {
      return "hat keine eigenen Attribute";
    }
    return null;
  }

  /**
   * Zweiter Teil einer Sprunghydraulik für das andere Bein (S. 391): gleiche Stufe, ohne eigenen Preis, mit diesem
   * Original verknüpft ({@link #getPairedPart()}).
   *
   * @throws IllegalStateException wenn dieses Implantat nicht zwei Beine braucht
   */
  public @NonNull Cyberware pairedCopy() {
    if (!data.limb().pairedLegs()) {
      throw new IllegalStateException(data.name() + " gehört nicht in zwei Beine");
    }
    Cyberware copy = new Cyberware(data, grade, rating, choice, chosenPrice, null, LimbAttributes.STANDARD, true);
    linkPair(copy);
    return copy;
  }

  /**
   * Verknüpft dieses Original mit seinem zweiten Teil (beim Laden einer Speicherdatei).
   *
   * @throws IllegalArgumentException wenn {@code copy} kein zweiter Teil desselben Eintrags ist
   */
  public void linkPair(@NonNull Cyberware copy) {
    if (pairedCopy || !copy.pairedCopy || !copy.data.equals(data)) {
      throw new IllegalArgumentException(copy.getName() + " ist kein zweiter Teil von " + data.name());
    }
    this.pairedPart = copy;
    copy.pairedPart = this;
  }

  /** Ob dies der zweite Teil einer Sprunghydraulik ist (Preis im ersten Teil). */
  public boolean isPairedCopy() {
    return pairedCopy;
  }

  /** Gegenstück im anderen Bein (Original bzw. zweiter Teil), sonst {@code null}. */
  public @Nullable Cyberware getPairedPart() {
    return pairedPart;
  }

  public @NonNull CyberwareData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  /** Name mit Auswahl bzw. Körperstelle, z.B. {@code "Reflexrekorder (Fertigkeit): Pistolen"}. */
  public @NonNull String getDisplayName() {
    if (location != null) {
      return data.name() + ", " + location.getDisplayName();
    }
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

  /**
   * Preis nur dieses Implantats ohne Einbauten (für die Anzeige je Zeile); bei Gliedern samt Maßanfertigung, die als
   * Endkosten ohne Grade-Multiplikator dazukommt. Der zweite Teil einer Sprunghydraulik kostet nichts.
   */
  public int getOwnNuyenCost() {
    if (pairedCopy) {
      return 0;
    }
    return gradedListPrice() + baseAttributes.pointsAboveStandard() * CUSTOM_POINT_PRICE;
  }

  /**
   * Verfügbarkeit in der gewählten Stufe; bei maßgefertigten Gliedern je Punkt über 3 um 1 höher (S. 390).
   */
  public @NonNull Availability getAvailability() {
    int points = baseAttributes.pointsAboveStandard();
    return points == 0 ? data.availability()
        : new Availability(String.valueOf(data.availability().evaluate(rating) + points),
            data.availability().legality());
  }

  private int gradedListPrice() {
    BigDecimal listPrice = RatingFormula.evaluate(data.cost(), rating)
        .add(BigDecimal.valueOf(chosenPrice == null ? 0 : chosenPrice));
    return listPrice
        .multiply(BigDecimal.valueOf(grade.getPriceMultiplier()))
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }

  // --- Cybergliedmaßen ---

  /** Körperstelle eines Glieds, sonst {@code null}. */
  public @Nullable BodyLocation getLocation() {
    return location;
  }

  /** Ob das Glied angeschlossen ist; nur angeschlossene Glieder wirken. Andere Implantate sind immer „angeschlossen“. */
  public boolean isAttached() {
    return attached;
  }

  public void setAttached(boolean attached) {
    this.attached = attached;
  }

  /** Grundwerte des Glieds (Standard 3/3/3 oder maßgefertigt), ohne Verbesserungen. */
  public @NonNull LimbAttributes getBaseAttributes() {
    return baseAttributes;
  }

  /**
   * KON, STR oder GES des Glieds: Grundwert plus Stufe der Verbesserung (S. 390); {@code 0} bei anderen Implantaten
   * und beim Schädel.
   */
  public int getLimbAttribute(@NonNull AttributeType type) {
    LimbPart part = data.limb().part();
    if (part == null || !part.hasAttributes()) {
      return 0;
    }
    return baseAttributes.get(type) + enhancementLevel(LimbEnhancement.forAttribute(type));
  }

  /** KON, STR und GES des Glieds samt Verbesserungen; beim Schädel und anderen Implantaten alle {@code 0}. */
  public @NonNull LimbAttributes getLimbAttributes() {
    return new LimbAttributes(getLimbAttribute(AttributeType.BOD), getLimbAttribute(AttributeType.STR),
        getLimbAttribute(AttributeType.AGI));
  }

  /** Panzerung des Glieds aus seiner Panzerungsverbesserung (ballistisch und Stoß gleich), {@code 0} ohne. */
  public int getLimbArmor() {
    return enhancementLevel(LimbEnhancement.ARMOR);
  }

  /** Schaden eines waffenlosen Angriffs mit diesem Glied, z.B. „4K“ (S. 390); {@code null} ohne eigene Stärke. */
  public @Nullable String getUnarmedDamageText() {
    int strength = getLimbAttribute(AttributeType.STR);
    return strength == 0 ? null : WeaponDamage.evaluate(UNARMED_DAMAGE, strength);
  }

  private int enhancementLevel(@NonNull LimbEnhancement enhancement) {
    return installed.stream()
        .filter(c -> c.data.limb().enhancement() == enhancement)
        .mapToInt(Cyberware::getRating)
        .max()
        .orElse(0);
  }

  // --- Einbauten in Basissysteme und Glieder ---

  /** Eingebautes Zubehör in Einbaureihenfolge. */
  public @NonNull List<Cyberware> getInstalled() {
    return Collections.unmodifiableList(installed);
  }

  /** Kapazität des Basissystems bzw. Glieds in seiner Stufe, {@code 0} bei anderen Implantaten. */
  public int getCapacity() {
    return data.capacityFor(rating);
  }

  /** Vom eingebauten Zubehör belegte Kapazität. */
  public int getUsedCapacity() {
    return installed.stream().mapToInt(c -> capacityNeed(c.data, c.rating)).sum();
  }

  /** Kapazität, die {@code child} hier belegt: in Gliedern der eckige Gliedwert, in Basissystemen der Zubehörwert. */
  public int capacityNeed(@NonNull CyberwareData child, int childRating) {
    return data.limb().isLimb() ? child.limbCapacityCostFor(childRating) : child.capacityCostFor(childRating);
  }

  /**
   * Warum {@code child} in Stufe {@code childRating} nicht eingebaut werden kann, oder {@code null}: Es muss passen
   * ({@link #fitProblem}), darf noch nicht eingebaut sein (außer {@link ImplantRestrictions#multiple()}) und muss in die
   * freie Kapazität passen.
   */
  public @Nullable String installProblem(@NonNull CyberwareData child, int childRating) {
    String fit = fitProblem(child);
    if (fit != null) {
      return fit;
    }
    if (!child.restrictions().multiple() && installed.stream().anyMatch(c -> c.data.equals(child))) {
      return child.name() + " ist bereits eingebaut";
    }
    int needed = capacityNeed(child, childRating);
    int free = getCapacity() - getUsedCapacity();
    if (needed > free) {
      return "Kapazität reicht nicht: %s braucht %d, frei sind %d von %d.".formatted(child.name(), needed, free,
          getCapacity());
    }
    return null;
  }

  /**
   * Warum {@code child} grundsätzlich nicht hierher passt, oder {@code null}. Basissystem: Zubehör derselben Kategorie,
   * das einbaubar und nicht schon enthalten ist (S. 386/388). Glied: Eintrag mit Gliedkapazität (S. 385, 388, 391), der
   * zur Gliedart passt; Attributverbesserungen nicht im Schädel.
   */
  private @Nullable String fitProblem(@NonNull CyberwareData child) {
    LimbPart part = data.limb().part();
    if (part != null) {
      if (!child.limb().isInstallableInLimb()) {
        return child.name() + " lässt sich nicht in Cybergliedmaßen einbauen";
      }
      if (!child.limb().fits(part)) {
        return child.name() + " passt nicht in " + data.name();
      }
      LimbEnhancement enhancement = child.limb().enhancement();
      if (enhancement != null && enhancement.attribute() != null && !part.hasAttributes()) {
        return data.name() + " hat keine eigenen Attribute";
      }
      return null;
    }
    if (!data.hasCapacity() || !child.category().equals(data.category())) {
      return child.name() + " lässt sich nicht in " + data.name() + " einbauen";
    }
    if (CyberwareData.INCLUDED_IN_BASE.equals(child.capacityCost())) {
      return child.name() + " ist im " + data.name() + " schon enthalten";
    }
    return child.isInstallable() ? null : child.name() + " lässt sich nicht in " + data.name() + " einbauen";
  }

  /**
   * Baut {@code child} ein; es übernimmt die Grade dieses Basissystems bzw. Glieds. Der zweite Teil einer
   * Sprunghydraulik ({@link #isPairedCopy()}) belegt nur Kapazität.
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

  /** Gratis-Anteil (Nuyen) aus dem Spielleiter-Modus bei der Erschaffung (ADR-070). */
  public int getGmFreeNuyen() {
    return gmFreeNuyen;
  }

  public void setGmFreeNuyen(int gmFreeNuyen) {
    this.gmFreeNuyen = Math.max(0, gmFreeNuyen);
  }
}
