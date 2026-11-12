package de.shalorian.chummer.model.character.armor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ein gekauftes Rüstungsteil eines Charakters: referenziert einen {@link ArmorData}-Katalogeintrag
 * und merkt sich, ob es gerade angelegt ist. Nur angelegte Teile zählen für Schutzwert und
 * Panzerungsbehinderung (siehe {@code Equipment#getArmorBallistic()}). Dasselbe Teil darf mehrfach
 * gekauft werden, daher arbeitet {@code Equipment#removeArmor} über die Objektreferenz.
 *
 * <p><b>Einbauten</b> (GRW S. 372/373): Zubehör gehört zu genau einem Teil (Helm, Chemische Versiegelung,
 * Umweltanpassung der Ganzkörperpanzerung; Helm des Urban Explorer Overalls) und erhöht dessen Werte.
 * Panzerungsmodifikationen passen an jedes Teil mit {@link ArmorData#acceptsMods()}, an Schilde nur die dafür
 * erlaubten; jede Modifikation einmal je Teil, eine Kapazität kennt das GRW nicht. Einbauten gehen mit ihrem Teil:
 * Angelegt, abgelegt und entfernt werden sie mit ihm.
 */
public class Armor {

  /** Name der Ganzkörperpanzerung in {@code armor.xml}. */
  public static final String FULL_BODY_ARMOR = "Ganzkörperpanzerung";

  private final @NonNull ArmorData data;
  private final @Nullable Integer chosenPrice;
  private boolean equipped;
  private final List<ArmorMod> mods = new ArrayList<>();

  /** Neu gekaufte Rüstung mit festem Preis ist angelegt (wie im C#-Original). */
  public Armor(@NonNull ArmorData data) {
    this(data, true, null);
  }

  public Armor(@NonNull ArmorData data, boolean equipped) {
    this(data, equipped, null);
  }

  /**
   * @param chosenPrice vom Spieler gewählter Preisanteil bei Kleidung (siehe {@link ArmorData#hasVariablePrice()}),
   *                    sonst {@code null}
   * @throws IllegalArgumentException wenn der Preis fehlt, nicht erlaubt ist oder außerhalb der Spanne liegt
   */
  public Armor(@NonNull ArmorData data, boolean equipped, @Nullable Integer chosenPrice) {
    if (data.hasVariablePrice() ? chosenPrice == null || !data.isValidPrice(chosenPrice) : chosenPrice != null) {
      throw new IllegalArgumentException("%s: Preis %s ungültig".formatted(data.name(), chosenPrice));
    }
    this.data = data;
    this.equipped = equipped;
    this.chosenPrice = chosenPrice;
  }

  public @NonNull ArmorData getData() {
    return data;
  }

  public @NonNull String getName() {
    return data.name();
  }

  public boolean isEquipped() {
    return equipped;
  }

  public void setEquipped(boolean equipped) {
    this.equipped = equipped;
  }

  /** Gewählter Preisanteil bei Kleidung, sonst {@code null}. */
  public @Nullable Integer getChosenPrice() {
    return chosenPrice;
  }

  /** Kaufpreis: fester Preis bzw. Aufschlag plus der gewählte Kleidungspreis, plus alle Einbauten. */
  public int getNuyenCost() {
    return data.cost() + (chosenPrice == null ? 0 : chosenPrice)
        + mods.stream().mapToInt(ArmorMod::getNuyenCost).sum();
  }

  // --- Einbauten ---

  public @NonNull List<ArmorMod> getMods() {
    return Collections.unmodifiableList(mods);
  }

  /** Ballistik dieses Teils samt Zubehör (Helm der Ganzkörperpanzerung +2). */
  public int getBallistic() {
    return data.ballistic() + mods.stream().mapToInt(m -> m.getData().ballistic()).sum();
  }

  /** Stoßwert dieses Teils samt Zubehör. */
  public int getImpact() {
    return data.impact() + mods.stream().mapToInt(m -> m.getData().impact()).sum();
  }

  /** Ob das Teil eine Ganzkörperpanzerung ist (Chemische Isolierung schützt dann auch gegen Inhalation, S. 372). */
  public boolean isFullBody() {
    return data.name().equals(FULL_BODY_ARMOR);
  }

  /** Sonderschutz dieses Teils je Modifikation, Werte samt Zubehör (bei Helmen und Schilden als Bonus „+“). */
  public @NonNull List<SpecialProtection> getSpecialProtections() {
    String prefix = data.category().isBonus() ? "+" : "";
    return Arrays.stream(ArmorProtection.values())
        .filter(p -> p != ArmorProtection.NONE && getProtection(p) > 0)
        .map(p -> p.describe(getProtection(p), getBallistic(), getImpact(), prefix, isFullBody()))
        .toList();
  }

  /** Stufe der Modifikation mit diesem Zusatzschutz, {@code 0} ohne (S. 372). */
  public int getProtection(@NonNull ArmorProtection protection) {
    return mods.stream()
        .filter(m -> m.getData().protection() == protection)
        .mapToInt(ArmorMod::getRating)
        .max()
        .orElse(0);
  }

  /**
   * Warum {@code mod} grundsätzlich nicht an dieses Teil passt, oder {@code null}: Zubehör nur an seinem Teil,
   * Modifikationen nur an Teilen, die sie aufnehmen, an Schilden nur die dafür erlaubten ({@code <shield/>}).
   */
  public @Nullable String fitProblem(@NonNull ArmorModData mod) {
    if (mod.kind() == ArmorModKind.ACCESSORY) {
      return mod.forArmor().equals(data.name()) ? null : mod.name() + " gehört zu " + mod.forArmor();
    }
    if (!data.acceptsMods()) {
      return data.name() + " nimmt keine Modifikationen auf";
    }
    if (data.shield() && !mod.shieldAllowed()) {
      return mod.name() + " passt nicht an Schilde";
    }
    return null;
  }

  /** Ob {@code mod} grundsätzlich an dieses Teil passt ({@link #fitProblem} liefert keinen Grund). */
  public boolean fits(@NonNull ArmorModData mod) {
    return fitProblem(mod) == null;
  }

  /**
   * Warum {@code mod} in Stufe {@code rating} nicht eingebaut werden kann, oder {@code null}: {@link #fitProblem};
   * jeder Einbau einmal; Voraussetzung (Schockstreifen braucht Elektrische Isolierung) am selben Teil; zulässige
   * Stufe.
   */
  public @Nullable String installProblem(@NonNull ArmorModData mod, int rating) {
    String fit = fitProblem(mod);
    if (fit != null) {
      return fit;
    }
    if (mods.stream().anyMatch(m -> m.getData().equals(mod))) {
      return mod.name() + " ist bereits eingebaut";
    }
    if (!mod.requires().isEmpty() && mods.stream().noneMatch(m -> m.getName().equals(mod.requires()))) {
      return "Nur zusammen mit " + mod.requires();
    }
    if (!mod.isValidRating(rating)) {
      return "Stufe " + rating + " ist nicht zulässig";
    }
    return null;
  }

  /**
   * Baut {@code mod} ein.
   *
   * @throws IllegalArgumentException wenn {@link #installProblem} einen Grund liefert
   */
  public void install(@NonNull ArmorMod mod) {
    String problem = installProblem(mod.getData(), mod.getRating());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    mods.add(mod);
  }

  /** Warum {@code mod} nicht ausgebaut werden kann (ein anderer Einbau braucht ihn), oder {@code null}. */
  public @Nullable String uninstallProblem(@NonNull ArmorMod mod) {
    return mods.stream()
        .filter(m -> m.getData().requires().equals(mod.getName()))
        .findFirst()
        .map(m -> m.getName() + " braucht " + mod.getName())
        .orElse(null);
  }

  /** Baut {@code mod} aus, sofern nichts ihn braucht; {@code false}, wenn er nicht ausgebaut wurde. */
  public boolean uninstall(@NonNull ArmorMod mod) {
    return uninstallProblem(mod) == null && mods.remove(mod);
  }

  /**
   * Verfügbarkeit von {@code mod} beim Einbau: ein Zuschlag ("+6") zählt auf die des Teils (Ganzkörperpanzerung 14E +
   * 6 = 20), sonst die eigene.
   */
  public int availabilityWith(@NonNull ArmorModData mod, int rating) {
    int own = mod.availability().evaluate(rating);
    return mod.availabilitySurcharge() ? data.availability().evaluate(0) + own : own;
  }

  /** Verfügbarkeit von {@code mod} zur Anzeige: mit Zuschlag „+6 (mit Ganzkörperpanzerung: 20)“, sonst die eigene. */
  public @NonNull String availabilityText(@NonNull ArmorModData mod, int rating) {
    return mod.availabilitySurcharge()
        ? "+%d (mit %s: %d)".formatted(mod.availability().evaluate(rating), data.name(), availabilityWith(mod, rating))
        : mod.availability().displayText(rating);
  }
}
