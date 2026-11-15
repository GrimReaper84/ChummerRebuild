package de.shalorian.chummer.model.character.equipment;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.adept.AdeptPowerEffect;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.ArmorProtection;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.GearCombatStats;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.model.character.skill.ExoticSkills;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.weapon.AmmoEffect;
import de.shalorian.chummer.model.character.weapon.RangeBands;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponCategory;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Ausrüstung eines Charakters: alle Kaufkategorien (Cyber- und Bioware, Rüstung, Waffen, übrige
 * Ausrüstung, Fahrzeuge, Lebensstil) samt Kaufprüfung, Schutzwerten, Waffenwerten und der Summe der ausgegebenen
 * Nuyen. Komponente des Aggregats {@link Character}, erreichbar über {@link Character#getEquipment()};
 * künftige Kaufkategorien gehören hierher. Ausgelagert, damit {@code Character} nicht mit jeder
 * Kaufkategorie wächst (SonarQube S6539, {@code docs/plaene/archiv/character_aufteilung_plan.md}).
 *
 * <p>Die Rückreferenz {@code owner} liefert, was die Ausrüstungsregeln vom Charakter brauchen:
 * Erstellungs- oder Karrieremodus ({@link Character#isCreated()}), Konstitution für die
 * Panzerungsbehinderung, wirksame Stärke und Fertigkeiten für die Waffenwerte. Regeln, die mehrere
 * Bereiche verbinden, bleiben in {@code Character}: Essenz-Kombinationsregel und Immunabstoßung,
 * Wirkung der Behinderung auf GES/REA, Ressourcengeld und Abschluss der Erschaffung.
 */
public class Equipment {

  private static final int ARMOR_ENCUMBRANCE_BOD_MULTIPLIER = 2;
  private static final int ARMOR_ENCUMBRANCE_POINTS_PER_PENALTY = 2;
  /** Herkunft der Waffen aus Rüstung im Waffenbereich. */
  private static final String ARMOR_WEAPON_SOURCE = "aus Rüstung";
  /** Herkunft der Waffen aus Implantaten im Waffenbereich. */
  private static final String IMPLANT_WEAPON_SOURCE = "Implantat";
  /** Katalogname des waffenlosen Grundangriffs (GRW S. 188). */
  private static final String UNARMED_ATTACK = "Waffenloser Angriff";

  /** Munition, die nur in vollautomatischen Waffen geladen wird (S. 369). */
  private static final String TRACER_ROUNDS = "Leuchtspurgeschosse";
  private static final String FULL_AUTO = "AM";
  /** Panzerung gegen Standardmunition (S. 369). */
  private static final String STANDARD_ARMOR_TYPE = "Ballistisch";
  /** Grundpreis beim Verkauf in Prozent des Originalpreises (GRW S. 358). */
  public static final int SALE_PERCENT = 30;
  /** Smartlink mit Smartgun-Waffe (GRW S. 180). */
  private static final int SMARTLINK_BONUS = 2;
  /** Lasermarkierer (GRW S. 180). */
  private static final int LASER_SIGHT_BONUS = 1;
  /** Katalogname des Smartlinks, als Cyberware und als Sichtverstärkung gleich (S. 379/388). */
  private static final String SMARTLINK = "Smartlink";
  /** Katalogname der Bildverbindung (S. 379). */
  private static final String IMAGE_LINK = "Bildverbindung";

  /** Schadenswert am Anfang eines Schadenscodes, z.B. „4“ in „4K“. */
  private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+)(.*)");

  private final @NonNull Character owner;

  private final List<Cyberware> cyberware = new ArrayList<>();
  private final List<Armor> armor = new ArrayList<>();
  private final List<Weapon> weapons = new ArrayList<>();
  private final List<Gear> gear = new ArrayList<>();
  private final List<Vehicle> vehicles = new ArrayList<>();
  private final @NonNull Lifestyle lifestyle = new Lifestyle();
  private @Nullable Gear activeCommlink;

  /** Leere Ausrüstung für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Equipment(@NonNull Character owner) {
    this.owner = owner;
  }

  // --- Cyberware & Bioware ---

  /** Alle eingebauten Implantate (Cyberware und Bioware gemeinsam, siehe {@link Cyberware#getKind()}). */
  public @NonNull List<Cyberware> getCyberware() {
    return Collections.unmodifiableList(cyberware);
  }

  /**
   * Baut ein Implantat ein. Dasselbe Implantat darf mehrfach eingebaut werden (z.B. mehrere
   * Schmuggelbehälter), daher keine Duplikatprüfung. Keine Prüfung der Grade-Verfügbarkeit oder der
   * Mindest-Essenz an dieser Stelle — beides sind Erschaffungsregeln, die die UI über {@link
   * #isCyberwareGradeAvailable(CyberwareGrade)} bzw. {@link Character#isEssenceBelowCreationMinimum()} prüft
   * (analog {@link Character#canAddSpellAtCreation()}); der Persistenz-Pfad lädt so auch im Karrieremodus
   * erworbene Beta-/Deltaware ohne Sonderbehandlung.
   */
  public void addCyberware(@NonNull Cyberware implant) {
    cyberware.add(implant);
  }

  /** Entfernt genau dieses Implantat (Objektreferenz, da Duplikate erlaubt sind — analog {@link Character#removeContact(Contact)}). */
  public void removeCyberware(@NonNull Cyberware implant) {
    cyberware.remove(implant);
  }

  /**
   * Ob Implantate dieser Kategorie aktuell gekauft werden dürfen: bei der Charaktererschaffung nur
   * Standard- und Alphaware (SR4-Grundregelwerk S. 359), im Karrieremodus alle Kategorien.
   */
  public boolean isCyberwareGradeAvailable(@NonNull CyberwareGrade grade) {
    return owner.isCreated() || grade.isAvailableAtCreation();
  }

  /** Gesamtpreis aller eingebauten Implantate in Nuyen (inkl. Grade-Multiplikatoren). */
  public int getCyberwareNuyenCost() {
    return cyberware.stream().mapToInt(Cyberware::getNuyenCost).sum();
  }

  /**
   * Summe der Essenzkosten aller Cyberware, ohne Verdopplung durch Immunabstoßung und ohne
   * Kombinationsregel — beides wendet {@link Character#getEssence()} an.
   */
  public @NonNull BigDecimal getCyberwareEssenceCost() {
    return sumEssenceCost(EquipmentKind.CYBERWARE);
  }

  /** Summe der Essenzkosten aller Bioware, Gegenstück zu {@link #getCyberwareEssenceCost()}. */
  public @NonNull BigDecimal getBiowareEssenceCost() {
    return sumEssenceCost(EquipmentKind.BIOWARE);
  }

  /**
   * Essenz einer Art: alle Implantate außer Gliedern, dazu je Körperstelle das Glied mit den höchsten Essenzkosten,
   * egal ob angeschlossen (Nutzerentscheidung: Ersatzglieder kosten keine zusätzliche Essenz, Wechseln spart aber auch
   * keine).
   */
  private @NonNull BigDecimal sumEssenceCost(@NonNull EquipmentKind kind) {
    BigDecimal implants = cyberware.stream()
        .filter(c -> c.getKind() == kind && c.getLocation() == null)
        .map(Cyberware::getEssenceCost)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<BodyLocation, BigDecimal> limbs = new EnumMap<>(BodyLocation.class);
    cyberware.stream()
        .filter(c -> c.getKind() == kind && c.getLocation() != null)
        .forEach(c -> limbs.merge(Objects.requireNonNull(c.getLocation()), c.getEssenceCost(), BigDecimal::max));
    return limbs.values().stream().reduce(implants, BigDecimal::add);
  }

  /**
   * Ob ein Gegenstand mit dieser Verfügbarkeit in dieser Stufe aktuell gekauft werden darf: bei der
   * Charaktererschaffung nur bis Stufe {@value Availability#MAX_RATING_AT_CREATION} und
   * Verfügbarkeit {@value Availability#MAX_AVAILABILITY_AT_CREATION} (SR4-Grundregelwerk S. 111), im
   * Karrieremodus immer. Gilt für alle
   * Kaufkategorien mit Stufe; Gegenstände ohne Stufe übergeben {@code 0}. Wie bei {@link
   * #isCyberwareGradeAvailable(CyberwareGrade)} prüft die UI diese Regel, nicht {@code add…()}, damit
   * der Persistenz-Pfad unverändert laden kann.
   */
  public boolean isAvailableForPurchase(@NonNull Availability availability, int rating) {
    return owner.isCreated()
        || (rating <= Availability.MAX_RATING_AT_CREATION
            && availability.isAllowedAtCreation(rating));
  }

  /**
   * Ob {@code child} in Stufe {@code rating} in {@code device} eingebaut (und damit gekauft) werden
   * darf: wie {@link #isAvailableForPurchase(Availability, int)}, wobei ein Verfügbarkeitszuschlag
   * ("+4") mit der Verfügbarkeit des Geräts samt bisheriger Einbauten zählt — eine Brille mit
   * Ultraschall (+8) und Infrarot (+6) läge bei 14 und ist bei der Erschaffung gesperrt (GRW S. 111,
   * 379).
   */
  public boolean isAvailableForInstall(@NonNull Gear device, @NonNull GearData child, int rating) {
    Availability availability = child.availabilitySurcharge()
        ? device.availabilityWith(child, rating) : child.availability();
    return isAvailableForPurchase(availability, rating);
  }

  /** Wie {@link #isAvailableForPurchase(Availability, int)} für Rüstung, die keine Stufe hat. */
  public boolean isAvailableForPurchase(@NonNull ArmorData armorData) {
    return owner.isCreated() || armorData.isAllowedAtCreation();
  }

  /**
   * Ob {@code mod} in Stufe {@code rating} in {@code piece} eingebaut (und damit gekauft) werden darf: bei der
   * Erschaffung Verfügbarkeit höchstens {@value Availability#MAX_AVAILABILITY_AT_CREATION}, ein Zuschlag ("+6") zählt
   * mit der Verfügbarkeit des Teils (GRW S. 111, 373).
   */
  public boolean isAvailableForInstall(@NonNull Armor piece, @NonNull ArmorModData mod, int rating) {
    return owner.isCreated() || piece.availabilityWith(mod, rating) <= Availability.MAX_AVAILABILITY_AT_CREATION;
  }

  // --- Rüstung ---

  /** Alle gekauften Rüstungsteile, angelegt oder nicht. */
  public @NonNull List<Armor> getArmor() {
    return Collections.unmodifiableList(armor);
  }

  /** Kauft ein Rüstungsteil. Dasselbe Teil darf mehrfach gekauft werden (keine Duplikatprüfung). */
  public void addArmor(@NonNull Armor piece) {
    armor.add(piece);
  }

  /** Entfernt genau dieses Rüstungsteil (Objektreferenz, da Duplikate erlaubt sind). */
  public void removeArmor(@NonNull Armor piece) {
    armor.remove(piece);
  }

  /** Gesamtpreis aller gekauften Rüstungsteile in Nuyen (angelegt oder nicht). */
  public int getArmorNuyenCost() {
    return armor.stream().mapToInt(Armor::getNuyenCost).sum();
  }

  /**
   * Ballistik-Schutzwert der angelegten Rüstung. SR4-Grundregelwerk S. 190: Bei mehreren Panzerungen
   * zählt je Ballistik und Stoß nur der höchste Wert; Helme und Schilde geben einen Bonus, der laut
   * S. 372 ihrer Stufe entspricht. Also höchstes angelegtes Panzerungsteil + Summe aller angelegten Helm-/Schild-Boni.
   */
  public int getArmorBallistic() {
    return protectionValue(Armor::getBallistic);
  }

  /** Stoß-Schutzwert der angelegten Rüstung, Regel wie {@link #getArmorBallistic()}. */
  public int getArmorImpact() {
    return protectionValue(Armor::getImpact);
  }

  private int protectionValue(@NonNull ToIntFunction<Armor> value) {
    int highest = equippedArmor()
        .filter(a -> !a.getData().category().isBonus())
        .mapToInt(value)
        .max().orElse(0);
    int bonus = equippedArmor()
        .filter(a -> a.getData().category().isBonus())
        .mapToInt(value)
        .sum();
    return highest + bonus;
  }

  /**
   * Zusatzschutz der angelegten Rüstung durch Modifikationen (GRW S. 372): die höchste Stufe dieser Art unter allen
   * angelegten Teilen. Sind Panzerung und Schild modifiziert, zählt nur eine (Annahme: die höhere). Bei Elektrizität,
   * Feuer, Kälte und Chemikalien kommt sie zur Panzerung hinzu, bei Wärmedämpfung ist sie ein Würfelbonus.
   */
  public int getSpecialProtection(@NonNull ArmorProtection protection) {
    return equippedArmor().mapToInt(a -> a.getProtection(protection)).max().orElse(0);
  }

  /**
   * Sonderschutz der angelegten Rüstung als Anzeigezeilen (GRW S. 372): je Schutzart die höchste Stufe eines angelegten
   * Teils, Panzerungswerte mit dem Gesamtschutz des Charakters (S. 190).
   */
  public @NonNull List<SpecialProtection> getSpecialProtections() {
    List<SpecialProtection> lines = new ArrayList<>();
    for (ArmorProtection protection : ArmorProtection.values()) {
      if (protection == ArmorProtection.NONE) {
        continue;
      }
      equippedArmor()
          .filter(a -> a.getProtection(protection) > 0)
          .max(Comparator.comparingInt(a -> a.getProtection(protection)))
          .ifPresent(best -> lines.add(protection.describe(best.getProtection(protection), owner.getArmorBallistic(),
              owner.getArmorImpact(), "", best.isFullBody())));
    }
    return lines;
  }

  /** Ob ein Schild angelegt ist (−1 auf körperliche Proben, GRW S. 372). */
  public boolean hasShieldEquipped() {
    return equippedArmor().anyMatch(a -> a.getData().shield());
  }

  /**
   * Waffen, die angelegte Rüstung stellt (GRW S. 373): Schutz- und Taserschild als exotische Nahkampfwaffe,
   * Schockstreifen als waffenloser Angriff. Sie werden nicht gekauft und erscheinen nur, solange das Teil angelegt ist;
   * jeder Aufruf liefert neue Instanzen.
   */
  public @NonNull List<Weapon> getArmorWeapons() {
    List<String> names = new ArrayList<>();
    equippedArmor().forEach(a -> {
      names.add(a.getData().weapon());
      a.getMods().forEach(m -> names.add(m.getData().weapon()));
    });
    return names.stream()
        .filter(name -> !name.isEmpty())
        .map(WeaponDataLoader::findByName)
        .filter(Objects::nonNull)
        .map(data -> new Weapon(data, ARMOR_WEAPON_SOURCE, null))
        .toList();
  }

  /**
   * Waffenloser Grundangriff, den jeder Charakter hat (GRW S. 188: halbe Stärke als Geistiger Schaden); zeigt im
   * Waffen-Tab u.a. Kritischer Schlag (S. 231). Jeder Aufruf liefert eine neue Instanz.
   */
  public @NonNull Weapon getUnarmedAttack() {
    return new Weapon(Objects.requireNonNull(WeaponDataLoader.findByName(UNARMED_ATTACK), UNARMED_ATTACK),
        "immer vorhanden", null);
  }

  /**
   * Waffen aus Implantaten (Cyber-Nahkampfwaffen, S. 391/392): natürlich implantierte mit der Stärke des Charakters,
   * eingebaute in angeschlossenen Gliedern mit der Stärke des Glieds (S. 389/390). Jeder Aufruf liefert neue Instanzen.
   */
  public @NonNull List<Weapon> getImplantWeapons() {
    List<Weapon> weapons = new ArrayList<>();
    for (Cyberware implant : cyberware) {
      BodyLocation location = implant.getLocation();
      if (location == null) {
        addImplantWeapon(weapons, implant, IMPLANT_WEAPON_SOURCE, null);
      } else if (implant.isAttached()) {
        int strength = implant.getLimbAttribute(AttributeType.STR);
        implant.getInstalled().forEach(child -> addImplantWeapon(weapons, child,
            IMPLANT_WEAPON_SOURCE + ", " + location.getDisplayName(), strength));
      }
    }
    return weapons;
  }

  private static void addImplantWeapon(@NonNull List<Weapon> weapons, @NonNull Cyberware implant,
      @NonNull String source, @Nullable Integer strength) {
    String name = implant.getData().weapon();
    WeaponData data = name.isEmpty() ? null : WeaponDataLoader.findByName(name);
    if (data != null) {
      weapons.add(new Weapon(data, source, strength));
    }
  }

  /**
   * Panzerungsbehinderung als (nicht positiver) Modifikator auf Geschicklichkeit und Reaktion.
   * SR4-Grundregelwerk S. 190: Übersteigt die Summe der getragenen Panzerung (je Ballistik und Stoß)
   * die doppelte Konstitution, gibt es je angefangene 2 Punkte darüber −1 auf GES und REA; die
   * Modifikatoren aus Ballistik und Stoß addieren sich. GRW-Beispiel: KON 3, Actioneer-
   * Geschäftskleidung 5/3 + Gefütterter Mantel 6/4 → Ballistik 11 (5 über 6 → −3), Stoß 7 (1 über 6 →
   * −1), zusammen −4.
   *
   * <p>Helme und Schilde zählen in die Summe mit (Nutzerentscheidung, wie im C#-Original; GRW S. 372
   * verweist bei ihnen ausdrücklich auf die Panzerungsbehinderung). Grundlage ist die verstärkte
   * Konstitution ({@link Character#getAugmentedAttributeValue}); Mystischer Panzer zählt nicht mit.
   */
  public int getArmorEncumbrancePenalty() {
    int limit = owner.getAugmentedAttributeValue(AttributeType.BOD) * ARMOR_ENCUMBRANCE_BOD_MULTIPLIER;
    return -(encumbranceSteps(Armor::getBallistic, limit)
        + encumbranceSteps(Armor::getImpact, limit));
  }

  private int encumbranceSteps(@NonNull ToIntFunction<Armor> value, int limit) {
    int total = equippedArmor().mapToInt(value).sum();
    int excess = Math.max(0, total - limit);
    return (excess + ARMOR_ENCUMBRANCE_POINTS_PER_PENALTY - 1) / ARMOR_ENCUMBRANCE_POINTS_PER_PENALTY;
  }

  private @NonNull Stream<Armor> equippedArmor() {
    return armor.stream().filter(Armor::isEquipped);
  }

  // --- Waffen ---

  /** Alle gekauften Waffen. */
  public @NonNull List<Weapon> getWeapons() {
    return Collections.unmodifiableList(weapons);
  }

  /**
   * Kauft eine Waffe. Dieselbe Waffe darf mehrfach gekauft werden (keine Duplikatprüfung). Die
   * Verfügbarkeit prüft die UI über {@link #isAvailableForPurchase(Availability, int)} — wie bei
   * Rüstung, damit der Persistenz-Pfad unverändert laden kann.
   */
  public void addWeapon(@NonNull Weapon weapon) {
    weapons.add(weapon);
  }

  /** Entfernt genau diese Waffe (Objektreferenz, da Duplikate erlaubt sind). */
  public void removeWeapon(@NonNull Weapon weapon) {
    weapons.remove(weapon);
  }

  /** Gesamtpreis aller gekauften Waffen in Nuyen. */
  public int getWeaponNuyenCost() {
    return weapons.stream().mapToInt(Weapon::getTotalNuyenCost).sum();
  }

  /**
   * Aktive Fertigkeit, mit der die Waffe geführt bzw. abgefeuert wird ({@link
   * WeaponCategory#skillName()}, GRW S. 147/148, 360–366). Der Wert einer noch gekoppelten
   * Fertigkeitsgruppe steht bereits auf jeder Einzelfertigkeit, z.B. Gruppe Feuerwaffen 3 → Gewehre 3. Exotische
   * Waffen nutzen die für genau diese Waffe gelernte Instanz (GRW S. 148, 365), ohne sie die Grundfertigkeit mit Wert 0.
   */
  public @NonNull Skill getWeaponSkill(@NonNull Weapon weapon) {
    String skillName = weapon.getData().category().skillName();
    Skill exotic = ExoticSkills.isBase(skillName) ? owner.getExoticSkill(skillName, weapon.getName()) : null;
    if (exotic != null) {
      return exotic;
    }
    return Objects.requireNonNull(owner.getSkill(skillName), () -> "Kampffertigkeit fehlt: " + skillName);
  }

  /**
   * Würfelpool für Angriffe mit der Waffe: Pool der Waffenfertigkeit (Fertigkeit + Geschicklichkeit, ohne Wert nach den
   * Regeln von S. 146; Verletzung, Implantate und Adeptenkräfte schon eingerechnet, GRW S. 177/186), bei Fernkampfwaffen
   * dazu Smartlink +2 mit Smartgunsystem, sonst Lasermarkierer +1; beide Boni zählen nicht zusammen (S. 180). Leer, wenn
   * die Fertigkeit nicht genutzt werden kann.
   */
  public @NonNull Optional<DicePool> getWeaponDicePool(@NonNull Weapon weapon) {
    Optional<DicePool> base = owner.getSkillDicePool(getWeaponSkill(weapon));
    if (base.isEmpty() || weapon.getData().category().isMelee()) {
      return base;
    }
    DicePool pool = base.get();
    if (weapon.hasSmartgun() && hasSmartlink()) {
      return Optional.of(new DicePool(pool.dice() + SMARTLINK_BONUS,
          pool.breakdown() + " + Smartlink " + SMARTLINK_BONUS));
    }
    if (weapon.hasLaserSight()) {
      return Optional.of(new DicePool(pool.dice() + LASER_SIGHT_BONUS,
          pool.breakdown() + " + Lasermarkierer " + LASER_SIGHT_BONUS));
    }
    return base;
  }

  /**
   * Ob die Spezialisierung der Waffenfertigkeit auf diese Waffe passt (dann +2 Würfel, GRW S. 147): gleich dem
   * Waffennamen oder der Waffenkategorie, ohne Groß-/Kleinschreibung.
   */
  public boolean isWeaponSpecialized(@NonNull Weapon weapon) {
    String specialization = getWeaponSkill(weapon).getSpecialization();
    return specialization != null && (specialization.equalsIgnoreCase(weapon.getName())
        || specialization.equalsIgnoreCase(weapon.getData().category().getDisplayName()));
  }

  /**
   * Ob der Charakter einen nutzbaren Smartlink hat (GRW S. 180): als Cyberware (S. 388) oder in einem Gerät wie Brille
   * oder Kontaktlinsen, das zugleich eine Bildverbindung enthält (S. 380). Gegenstände in Fahrzeugen zählen nicht.
   */
  public boolean hasSmartlink() {
    boolean implant = cyberware.stream()
        .flatMap(c -> Stream.concat(Stream.of(c), c.getInstalled().stream()))
        .anyMatch(c -> c.getData().name().equals(SMARTLINK));
    return implant || gear.stream().anyMatch(g -> containsInstalled(g, SMARTLINK) && containsInstalled(g, IMAGE_LINK));
  }

  private static boolean containsInstalled(@NonNull Gear device, @NonNull String name) {
    return device.getInstalled().stream().anyMatch(child -> child.getName().equals(name));
  }

  /**
   * Schaden der Waffe für diesen Charakter (GRW S. 188: halbe Stärke aufgerundet plus Waffenmodifikator;
   * beim Bogen nach Mindeststärke). Grundlage ist die wirksame Stärke ({@link
   * Character#getAugmentedAttributeValue(AttributeType)}), die derzeit der investierten entspricht,
   * da es noch keine Stärkemodifikatoren gibt.
   */
  public @NonNull String getWeaponDamageText(@NonNull Weapon weapon) {
    String damage = baseDamage(weapon);
    AmmoEffect effect = loadedAmmoEffect(weapon);
    if (effect != null) {
      damage = effect.applyToDamage(damage);
    }
    int strike = owner.getAdeptPowers().level(AdeptPowerEffect.CRITICAL_STRIKE);
    if (strike == 0 || weapon.getData().category() != WeaponCategory.UNARMED) {
      return damage;
    }
    // Kritischer Schlag (GRW S. 231): je Stufe +1 Schaden waffenlos
    Matcher m = LEADING_NUMBER.matcher(damage);
    return m.matches() ? (Integer.parseInt(m.group(1)) + strike) + m.group(2) : damage + " +" + strike;
  }

  /**
   * PB der Waffe für diesen Charakter: Tabellenwert, mit geladener Munition nach S. 369 verändert ({@link
   * AmmoEffect}).
   */
  public @NonNull String getWeaponArmorPiercingText(@NonNull Weapon weapon) {
    String ap = weapon.getData().armorPiercing();
    AmmoEffect effect = loadedAmmoEffect(weapon);
    return effect == null ? ap : effect.applyToArmorPiercing(ap, baseDamage(weapon));
  }

  /** Schaden nach Stärke bzw. Mindeststärke, ohne Munition und Kritischen Schlag. */
  private @NonNull String baseDamage(@NonNull Weapon weapon) {
    return weapon.getDamageText(owner.getAugmentedAttributeValue(AttributeType.STR));
  }

  /**
   * Hinweise zu Boni, die nicht im Pool stehen: Smartgunsystem ohne nutzbaren Smartlink (S. 180, 380) und der
   * Salvenbonus von Leuchtspur (S. 180, 369).
   */
  public @NonNull List<String> getWeaponHints(@NonNull Weapon weapon) {
    List<String> hints = new ArrayList<>();
    if (!weapon.getData().category().isMelee() && weapon.hasSmartgun() && !hasSmartlink()) {
      hints.add("Smartgunsystem ohne Smartlink (Cyberware oder mit Bildverbindung im Gerät): kein Bonus (GRW S. 180)");
    }
    Gear ammo = getLoadedAmmo(weapon);
    if (ammo != null && ammo.getName().equals(TRACER_ROUNDS)) {
      hints.add("Leuchtspur: Salven +1/+2/+3, nicht zusammen mit Smartlink oder Lasermarkierer (GRW S. 180, 369)");
    }
    return hints;
  }

  /**
   * Munitionssorten der Ausrüstung, die in die Waffe passen (GRW S. 368/369): Munition mit eigener Wirkung, deren
   * Waffenklasse der Kategorie der Waffe entspricht; Leuchtspurgeschosse nur in vollautomatischen Waffen (S. 369).
   */
  public @NonNull List<Gear> getAmmoCandidates(@NonNull Weapon weapon) {
    WeaponData data = weapon.getData();
    if (!data.category().usesAmmunition()) {
      return List.of();
    }
    return gear.stream()
        .filter(g -> g.getData().category() == GearCategory.AMMUNITION && ammoEffect(g) != null)
        .filter(g -> g.getExtra().equals(data.category().getDisplayName()))
        .filter(g -> !g.getName().equals(TRACER_ROUNDS) || List.of(data.mode().split("/")).contains(FULL_AUTO))
        .toList();
  }

  /**
   * Lädt {@code ammo} in die Waffe bzw. mit {@code null} wieder Standardmunition (Nutzerentscheidung 2026-11-15: eine
   * Munitionssorte je Waffe).
   *
   * @throws IllegalArgumentException wenn die Munition nicht zu {@link #getAmmoCandidates(Weapon)} gehört
   */
  public void loadAmmo(@NonNull Weapon weapon, @Nullable Gear ammo) {
    if (ammo != null && !getAmmoCandidates(weapon).contains(ammo)) {
      throw new IllegalArgumentException(ammo.getDisplayName() + " passt nicht in " + weapon.getName());
    }
    weapon.setLoadedAmmoStack(ammo);
  }

  /** Geladene Munition, oder {@code null} für Standardmunition, auch wenn der Stapel nicht mehr passt oder fehlt. */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau der geladene Stapel
  public @Nullable Gear getLoadedAmmo(@NonNull Weapon weapon) {
    Gear stack = weapon.getLoadedAmmoStack();
    return stack != null && getAmmoCandidates(weapon).stream().anyMatch(g -> g == stack) ? stack : null;
  }

  /** Panzerung, gegen die die Waffe mit ihrer Munition geht (S. 369), leer bei Waffen ohne Munition. */
  public @NonNull String getWeaponArmorType(@NonNull Weapon weapon) {
    if (!weapon.getData().category().usesAmmunition()) {
      return "";
    }
    AmmoEffect effect = loadedAmmoEffect(weapon);
    return effect == null ? STANDARD_ARMOR_TYPE : effect.armorType();
  }

  private @Nullable AmmoEffect loadedAmmoEffect(@NonNull Weapon weapon) {
    Gear ammo = getLoadedAmmo(weapon);
    return ammo == null ? null : ammoEffect(ammo);
  }

  private static @Nullable AmmoEffect ammoEffect(@NonNull Gear ammo) {
    GearCombatStats stats = ammo.getData().combat();
    return stats == null ? null
        : AmmoEffect.fromTable(ammo.getName(), stats.damage(), stats.armorPiercing(), stats.armorType());
  }

  /**
   * Reichweitenstufen der Waffe für diesen Charakter (GRW S. 179; Wurfwaffen nach Stärke, Bögen nach
   * Mindeststärke), oder {@code null} bei Nahkampfwaffen.
   */
  public @Nullable RangeBands getWeaponRangeBands(@NonNull Weapon weapon) {
    return weapon.getRangeBands(owner.getAugmentedAttributeValue(AttributeType.STR));
  }

  /**
   * Grundpreis beim Verkauf: {@value #SALE_PERCENT} % des Originalpreises, abgerundet (GRW S. 358). Den tatsächlichen
   * Erlös bestimmt die Verhandlungsprobe; die App belegt nur vor.
   */
  public static int saleBasePrice(int originalPrice) {
    return originalPrice * SALE_PERCENT / 100;
  }

  // --- Übrige Ausrüstung (Gear) ---

  /** Alle gekauften Ausrüstungsgegenstände in Kaufreihenfolge. */
  public @NonNull List<Gear> getGear() {
    return Collections.unmodifiableList(gear);
  }

  /**
   * Kauft einen Ausrüstungsgegenstand. Gibt es schon einen Gegenstand derselben Art ({@link
   * Gear#isSameKind(Gear)}: Eintrag, Stufe, Zusatztext, gewählter Preis), wird nur dessen Menge erhöht
   * (Nutzerentscheidung 2026-10-04), sonst wird {@code item} angelegt. Verfügbarkeit und Stufengrenze
   * prüft die UI über {@link #isAvailableForPurchase(Availability, int)}.
   *
   * @return der Gegenstand, der die Menge jetzt enthält ({@code item} oder der vorhandene)
   */
  public @NonNull Gear addGear(@NonNull Gear item) {
    return Gear.addOrMerge(gear, item);
  }

  /**
   * Entfernt genau diesen Gegenstand samt seiner ganzen Menge und seiner Einbauten; ist er selbst
   * eingebaut (Betriebssystem), wird er aus seinem Gerät ausgebaut.
   */
  public void removeGear(@NonNull Gear item) {
    if (!gear.remove(item)) {
      gear.forEach(device -> device.uninstall(item));
    }
  }

  // --- Kommlinks ---

  /** Kommlinks des Charakters: gekaufte Kommlink-Modelle und die Kommlinks in Kommlink-Headware (S. 374, 386). */
  public @NonNull List<Gear> getCommlinks() {
    List<Gear> commlinks = new ArrayList<>(gear.stream()
        .filter(g -> g.getData().category() == GearCategory.COMMLINKS).toList());
    commlinks.addAll(owner.getImplants().getCommlinks());
    return commlinks;
  }

  /**
   * Das Kommlink, über das der Charakter online geht (Matrixinitiative): das gewählte, solange es noch vorhanden ist,
   * sonst das einzige Kommlink; {@code null} ohne Kommlink oder bei mehreren ohne Wahl.
   */
  public @Nullable Gear getActiveCommlink() {
    Gear chosen = getChosenCommlink();
    if (chosen != null) {
      return chosen;
    }
    List<Gear> commlinks = getCommlinks();
    return commlinks.size() == 1 ? commlinks.getFirst() : null;
  }

  /** Das ausdrücklich gewählte Kommlink, solange es noch vorhanden ist, sonst {@code null} (Speichern). */
  public @Nullable Gear getChosenCommlink() {
    return activeCommlink != null && getCommlinks().contains(activeCommlink) ? activeCommlink : null;
  }

  /**
   * Wählt das aktive Kommlink.
   *
   * @throws IllegalArgumentException wenn {@code commlink} keins der Kommlinks des Charakters ist
   */
  public void setActiveCommlink(@NonNull Gear commlink) {
    if (!getCommlinks().contains(commlink)) {
      throw new IllegalArgumentException(commlink.getName() + " ist kein Kommlink des Charakters");
    }
    activeCommlink = commlink;
  }

  /** Gesamtpreis aller gekauften Ausrüstungsgegenstände in Nuyen. */
  public int getGearNuyenCost() {
    return gear.stream().mapToInt(Gear::getNuyenCost).sum();
  }

  // --- Fahrzeuge & Drohnen ---

  /**
   * Alle gekauften Fahrzeuge und Drohnen. Ihre Waffen stecken in den Halterungen ({@link
   * Vehicle#getMounts()}) und gehören nicht zu {@link #getWeapons()}.
   */
  public @NonNull List<Vehicle> getVehicles() {
    return Collections.unmodifiableList(vehicles);
  }

  /**
   * Kauft ein Fahrzeug. Dasselbe Fahrzeug darf mehrfach gekauft werden (keine Duplikatprüfung); die
   * Verfügbarkeit prüft die UI über {@link #isAvailableForPurchase(Availability, int)}.
   */
  public void addVehicle(@NonNull Vehicle vehicle) {
    vehicles.add(vehicle);
  }

  /** Entfernt genau dieses Fahrzeug samt Ausstattung (Objektreferenz, da Duplikate erlaubt sind). */
  public void removeVehicle(@NonNull Vehicle vehicle) {
    vehicles.remove(vehicle);
  }

  /** Gesamtpreis aller Fahrzeuge samt Ausstattung in Nuyen ({@link Vehicle#getNuyenCost()}). */
  public int getVehicleNuyenCost() {
    return vehicles.stream().mapToInt(Vehicle::getNuyenCost).sum();
  }

  // --- Lebensstil & Nuyen-Summe ---

  /**
   * Der Lebensstil des Charakters — immer vorhanden, Default Straße/1 Monat. Aufrufer ändern Stufe
   * und Monate direkt am zurückgegebenen Objekt (analog {@link Character#getBiography()}).
   */
  public @NonNull Lifestyle getLifestyle() {
    return lifestyle;
  }

  /**
   * Summe aller Kaufpreise, die gegen das Ressourcengeld zählen: Cyber-/Bioware, Rüstung, Waffen,
   * übrige Ausrüstung, Fahrzeuge und Lebensstil (Monatskosten × Monate). Zentrale Summenstelle — künftige
   * Kaufkategorien werden hier ergänzt. GRW S. 111: Bei der Erschaffung wird jeder Gegenstand zum vollen
   * Preis bezahlt.
   */
  public int getSpentNuyen() {
    return getCyberwareNuyenCost() + getArmorNuyenCost() + getWeaponNuyenCost()
        + getGearNuyenCost() + getVehicleNuyenCost() + lifestyle.getNuyenCost();
  }
}
