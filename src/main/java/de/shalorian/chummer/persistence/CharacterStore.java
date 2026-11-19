package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterBiography;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorData;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.ArmorModDataLoader;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.LimbAttributes;
import de.shalorian.chummer.model.character.cyberware.LimbPart;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearCategory;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.initiation.TechniqueData;
import de.shalorian.chummer.model.character.initiation.TechniqueDataLoader;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.magic.Tradition;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.critter.CritterData;
import de.shalorian.chummer.model.critter.CritterDataLoader;
import de.shalorian.chummer.model.critter.CritterMetatype;
import de.shalorian.chummer.model.critter.CritterTraits;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualitySource;
import de.shalorian.chummer.model.character.reputation.Reputation;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritData;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexFormData;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.character.vehicle.VehicleData;
import de.shalorian.chummer.model.character.vehicle.VehicleDataLoader;
import de.shalorian.chummer.model.character.vehicle.WeaponMount;
import de.shalorian.chummer.model.character.weapon.AccessoryData;
import de.shalorian.chummer.model.character.weapon.AccessoryDataLoader;
import de.shalorian.chummer.model.character.weapon.Mount;
import de.shalorian.chummer.model.character.weapon.Weapon;
import de.shalorian.chummer.model.character.weapon.WeaponAccessory;
import de.shalorian.chummer.model.character.weapon.WeaponData;
import de.shalorian.chummer.model.character.weapon.WeaponDataLoader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CharacterStore {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private CharacterStore() {
  }

  public static void save(@NonNull Character character, @NonNull File file) throws IOException {
    MAPPER.writeValue(file, toData(character));
  }

  public static @NonNull Character load(@NonNull File file) throws IOException {
    return fromData(MAPPER.readValue(file, CharacterData.class));
  }

  /** Merkt den aktuellen Stand im Speicherformat (für Rückgängig, nur im Speicher). */
  public static @NonNull CharacterSnapshot snapshot(@NonNull Character character) {
    return new CharacterSnapshot(serialize(character));
  }

  /**
   * Der aktuelle Stand im Speicherformat, wie {@link #save} ihn schreiben würde; Grundlage für die Erkennung
   * ungespeicherter Änderungen (ADR-066).
   */
  public static byte @NonNull [] serialize(@NonNull Character character) {
    try {
      return MAPPER.writeValueAsBytes(toData(character));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Baut aus einem gemerkten Stand einen neuen Charakter, wie beim Laden einer Datei. */
  public static @NonNull Character restore(@NonNull CharacterSnapshot snapshot) {
    try {
      return fromData(MAPPER.readValue(snapshot.json(), CharacterData.class));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static @NonNull CharacterData toData(@NonNull Character character) {
    character.coverGmChanges(); // Käufe im Spielleiter-Modus vor dem Speichern gutschreiben (ADR-070)
    CharacterData data = new CharacterData();
    data.name = character.getName();
    data.metatype = character.getMetatype().name(); // MetatypeData.name() Accessor
    CritterTraits critter = character.getCritterTraits();
    if (critter != null) {
      data.critter = critter.getData().name();
      data.critterPowers = new ArrayList<>(critter.getPowers());
      data.critterWeaknesses = new ArrayList<>(critter.getWeaknesses());
      data.critterSkills = new HashMap<>(critter.getSpecialSkills());
    }

    for (AttributeType type : AttributeType.values()) {
      data.attributes.put(type.name(), character.getAttribute(type).getValue());
    }
    data.edge = character.getEdge().getValue();
    data.edgeBurned = character.getEdge().getBurned();
    data.edgeSpent = character.getEdgeSpent();
    if (character.hasMagic()) {
      data.magic = Objects.requireNonNull(character.getMagicOrResonance()).getValue();
    } else if (character.hasResonance()) {
      data.resonance = Objects.requireNonNull(character.getMagicOrResonance()).getValue();
    }
    data.skills = character.getSkills().stream()
        .filter(s -> s.getRating() > 0 || s.getSpecialization() != null || s.getExoticTarget() != null)
        .map(s -> new SkillEntry(s.getBaseName(), s.getRating(), s.getSpecialization(), s.getExoticTarget()))
        .toList();
    data.created = character.isCreated();
    data.nuyenBP = character.getNuyenBP();
    data.physicalDamage = character.getPhysicalDamage();
    data.stunDamage = character.getStunDamage();
    data.selectedQualities = character.getQualities().stream()
        .filter(q -> q.source() == QualitySource.SELECTED)
        .map(q -> new SelectedQualityEntry(q.data().name(), q.gratis(), q.choice()))
        .toList();
    data.knowledgeSkills = character.getKnowledgeSkills().stream()
        .map(k -> new KnowledgeSkillEntry(k.getName(), k.getCategory().name(), k.getRating(),
            k.isNativeLanguage(), k.getSpecialization()))
        .toList();
    data.contacts = character.getContacts().stream()
        .map(c -> {
          ContactEntry entry = new ContactEntry(c.getName(), c.getDescription(), c.getConnection(), c.getLoyalty());
          entry.gmFreeBp = c.getGmFreeBp();
          return entry;
        })
        .toList();
    data.decoupledGroups = new ArrayList<>(character.getDecoupledGroups());
    data.karmaLog = character.getKarmaLog().stream()
        .map(e -> new LogEntryData(e.description(), e.amount(), e.date().toString(), e.category().name()))
        .toList();
    data.nuyenLog = character.getNuyenLog().stream()
        .map(e -> new LogEntryData(e.description(), e.amount(), e.date().toString(), e.category().name()))
        .toList();
    data.biography = toBiographyData(character.getBiography());
    data.tradition = character.getTradition().name();
    data.spellNames = character.getSpells().stream().map(SpellData::name).toList();
    data.gmFreeSpells = List.copyOf(character.getGmFreeSpells());
    data.complexForms = character.getComplexForms().stream()
        .map(cf -> {
          ComplexFormEntry entry = new ComplexFormEntry(cf.getName(), cf.getRating());
          entry.gmFreeBp = cf.getGmFreeBp();
          return entry;
        })
        .toList();
    data.adeptPowers = character.getAdeptPowers().getPowers().stream()
        .map(p -> new AdeptPowerEntry(p.getData().name(), p.getChoice(), p.getLevel()))
        .toList();
    data.adeptMagic = character.getAdeptPowers().getAdeptMagic();
    data.spirits = character.getSpirits().getAll().stream()
        .map(s -> {
          SpiritEntry entry = new SpiritEntry(s.getName(), s.getForce(), s.getServices(), s.isBound(), s.getOptions());
          entry.gmFreeBp = s.getGmFreeBp();
          return entry;
        })
        .toList();
    Reputation reputation = character.getReputation();
    data.reputation = new ReputationData(reputation.getStreetCredBonus(), reputation.getNotorietyBonus(),
        reputation.getBurnedStreetCred(), reputation.getAwarenessAdjustment());
    data.initiation = new InitiationEntry(character.getInitiation().getGrade(),
        character.getInitiation().getTechniques().stream().map(TechniqueData::name).toList());
    IdentityHashMap<Cyberware, Integer> pairIds = pairIds(character);
    Gear chosenCommlink = character.getEquipment().getChosenCommlink();
    data.cyberware = character.getEquipment().getCyberware().stream()
        .map(implant -> toCyberwareEntry(implant, pairIds, chosenCommlink))
        .toList();
    data.armor = character.getEquipment().getArmor().stream().map(CharacterStore::toArmorEntry).toList();
    data.weapons = character.getEquipment().getWeapons().stream()
        .map(w -> toWeaponEntry(w, character.getEquipment().getLoadedAmmo(w)))
        .toList();
    data.gear = character.getEquipment().getGear().stream().map(g -> toGearEntry(g, chosenCommlink))
        .toList();
    data.vehicles = character.getEquipment().getVehicles().stream()
        .map(CharacterStore::toVehicleEntry)
        .toList();
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    data.lifestyle = new LifestyleEntry(lifestyle.getLevel().name(), lifestyle.getMonths(),
        lifestyle.getMissedMonths(), lifestyle.getDebt());
    data.lifestyle.gmFreeNuyen = lifestyle.getGmFreeNuyen();
    RuleOptions rules = character.getRuleOptions();
    data.rules = new RuleOptionsEntry(rules.isLimbArmorAverage(), rules.isLateAwakening(),
        rules.isMissedLifestylePayments(), rules.isDecoupleAtCreation(), List.copyOf(rules.getExtraBooks()));

    return data;
  }

  /** Waffe in der Speicherdatei; {@code ammo} ist die gültig geladene Munition (sonst {@code null}). */
  private static @NonNull WeaponEntry toWeaponEntry(@NonNull Weapon weapon, @Nullable Gear ammo) {
    WeaponEntry entry = new WeaponEntry(weapon.getName(), weapon.getRating(),
        weapon.getAccessories().stream()
            .map(a -> new AccessoryEntry(a.getName(), a.mount() == null ? null : a.mount().name()))
            .toList());
    entry.arrows = weapon.getArrows().stream().map(CharacterStore::toGearEntry).toList();
    entry.ammo = ammo == null ? "" : ammo.getName();
    entry.gmFreeNuyen = weapon.getGmFreeNuyen();
    return entry;
  }

  private static @NonNull VehicleEntry toVehicleEntry(@NonNull Vehicle vehicle) {
    VehicleEntry entry = new VehicleEntry(vehicle.getName());
    entry.riggerAdapter = vehicle.isRiggerAdapterPurchased();
    entry.mounts = vehicle.getMounts().stream()
        .map(m -> {
          Weapon weapon = m.getWeapon();
          return new MountEntry(m.isIncluded(), weapon == null ? null : toWeaponEntry(weapon, null));
        })
        .toList();
    entry.includedGear = vehicle.getIncludedGear().stream().map(CharacterStore::toGearEntry).toList();
    entry.gear = vehicle.getGear().stream().map(CharacterStore::toGearEntry).toList();
    entry.matrixUpgrades = vehicle.getMatrixUpgrades().stream().map(CharacterStore::toGearEntry).toList();
    entry.damage = vehicle.getDamage();
    entry.gmFreeNuyen = vehicle.getGmFreeNuyen();
    return entry;
  }

  private static @NonNull ArmorEntry toArmorEntry(@NonNull Armor armor) {
    ArmorEntry entry = new ArmorEntry(armor.getName(), armor.isEquipped(), armor.getChosenPrice());
    entry.mods = armor.getMods().stream().map(m -> new ArmorModEntry(m.getName(), m.getRating())).toList();
    entry.gmFreeNuyen = armor.getGmFreeNuyen();
    return entry;
  }

  /**
   * Kennungen für die Paare einer Sprunghydraulik (Original und zweiter Teil bekommen dieselbe Zahl ab 1), nach
   * Objektidentität, weil gleiche Einbauten in mehreren Beinen stecken können.
   */
  private static @NonNull IdentityHashMap<Cyberware, Integer> pairIds(@NonNull Character character) {
    IdentityHashMap<Cyberware, Integer> ids = new IdentityHashMap<>();
    for (Cyberware limb : character.getImplants().getLimbs()) {
      for (Cyberware child : limb.getInstalled()) {
        Cyberware counterpart = child.getPairedPart();
        if (counterpart != null && !ids.containsKey(child)) {
          int id = ids.size() / 2 + 1;
          ids.put(child, id);
          ids.put(counterpart, id);
        }
      }
    }
    return ids;
  }

  private static @NonNull CyberwareEntry toCyberwareEntry(@NonNull Cyberware implant,
      @NonNull IdentityHashMap<Cyberware, Integer> pairIds, @Nullable Gear chosenCommlink) {
    CyberwareEntry entry = new CyberwareEntry(implant.getName(), implant.getGrade().name(), implant.getRating());
    entry.choice = implant.getChoice();
    entry.price = implant.getChosenPrice();
    entry.installed = implant.getInstalled().stream()
        .map(child -> toCyberwareEntry(child, pairIds, chosenCommlink)).toList();
    Gear commlink = implant.getCommlink();
    entry.commlink = commlink == null ? null : toGearEntry(commlink, chosenCommlink);
    BodyLocation location = implant.getLocation();
    if (location != null) {
      entry.location = location.name();
      entry.attached = implant.isAttached();
      entry.body = implant.getBaseAttributes().body();
      entry.strength = implant.getBaseAttributes().strength();
      entry.agility = implant.getBaseAttributes().agility();
    }
    entry.pairedCopy = implant.isPairedCopy();
    entry.pair = pairIds.getOrDefault(implant, 0);
    entry.gmFreeNuyen = implant.getGmFreeNuyen();
    return entry;
  }

  /** Wie {@link #toGearEntry(Gear)}, dazu die Markierung, ob {@code gear} das gewählte aktive Kommlink ist. */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: genau dieses Kommlink
  private static @NonNull GearEntry toGearEntry(@NonNull Gear gear, @Nullable Gear chosenCommlink) {
    GearEntry entry = toGearEntry(gear);
    entry.active = gear == chosenCommlink;
    return entry;
  }

  private static @NonNull GearEntry toGearEntry(@NonNull Gear gear) {
    GearEntry entry = new GearEntry(gear.getName(), gear.getRating(), gear.getQuantity(),
        gear.getExtra(), gear.getChosenPackPrice());
    entry.installed = gear.getInstalled().stream().map(CharacterStore::toGearEntry).toList();
    entry.running = gear.isRunning();
    entry.bound = gear.isBound();
    entry.focusActive = gear.isFocusActive();
    entry.gmFreeNuyen = gear.getGmFreeNuyen();
    entry.gmFreeBindingBp = gear.getGmFreeBindingBp();
    return entry;
  }

  private static @NonNull BiographyData toBiographyData(@NonNull CharacterBiography biography) {
    BiographyData data = new BiographyData();
    data.alias = biography.getAlias();
    data.sex = biography.getSex();
    data.age = biography.getAge();
    data.eyes = biography.getEyes();
    data.height = biography.getHeight();
    data.weight = biography.getWeight();
    data.skin = biography.getSkin();
    data.hair = biography.getHair();
    data.description = biography.getDescription();
    data.background = biography.getBackground();
    data.concept = biography.getConcept();
    data.notes = biography.getNotes();
    data.playerName = biography.getPlayerName();
    data.gameNotes = biography.getGameNotes();
    data.portrait = biography.getPortrait();
    return data;
  }

  static @NonNull Character fromData(@NonNull CharacterData data) {
    String charName = Objects.requireNonNull(data.name, "Name fehlt in Speicherdatei");
    String metatypeName = Objects.requireNonNull(data.metatype, "Metatyp fehlt in Speicherdatei");
    CritterData critter = data.critter == null ? null
        : Objects.requireNonNull(CritterDataLoader.findByName(data.critter), "Unbekannter Critter: " + data.critter);
    MetatypeData metatype = critter == null ? MetatypeDataLoader.find(metatypeName) : CritterMetatype.of(critter);
    Character character = new Character(charName, metatype);
    if (critter != null) {
      CritterTraits traits = new CritterTraits(critter, data.critterPowers, data.critterWeaknesses);
      data.critterSkills.forEach(traits::setSpecialSkill);
      character.setCritterTraits(traits);
    }
    // Regel-Einstellungen vor allem anderen, damit jede spätere Berechnung schon mit ihnen läuft.
    applyRuleOptions(character, data);

    // Qualities zuerst: sie heben Maxima an (Außergewöhnliches Attribut, Glück, Talentiert).
    applySelectedQualities(character, data);
    applyAttributes(character, data);
    applySkills(character, data);
    character.setCreated(data.created);
    character.setNuyenBP(data.nuyenBP);
    // Initiation vor dem Magiewert: sie hebt das Maximum über 6.
    applyInitiation(character, data);
    applyMagicOrResonance(character, data);
    applyAdeptPowers(character, data);
    applySpirits(character, data);
    applyKnowledgeSkills(character, data);
    applyContacts(character, data);
    data.decoupledGroups.forEach(character::decoupleGroup);
    applyLog(data.karmaLog, character::addKarmaEntry);
    applyLog(data.nuyenLog, character::addNuyenEntry);
    character.getReputation().restore(data.reputation.streetCredBonus, data.reputation.notorietyBonus,
        data.reputation.burnedStreetCred, data.reputation.awarenessAdjustment);
    applyBiography(character, data);
    applyTradition(character, data);
    applySpells(character, data);
    applyComplexForms(character, data);
    Gear chosenCommlink = applyCyberware(character, data);
    applyArmor(character, data);
    Gear chosenGear = applyGear(character, data);
    applyWeapons(character, data); // nach der Ausrüstung: geladene Munition verweist auf ihre Stapel
    chosenCommlink = chosenGear != null ? chosenGear : chosenCommlink;
    if (chosenCommlink != null) {
      character.getEquipment().setActiveCommlink(chosenCommlink);
    }
    applyVehicles(character, data);
    applyLifestyle(character, data);
    // Schaden zuletzt: die Spaltengröße hängt von verstärkter KON und WIL ab (Qualities, Attribute, Adeptenkräfte,
    // Implantate) und von Überlebenswille.
    character.setPhysicalDamage(data.physicalDamage);
    character.setStunDamage(data.stunDamage);

    return character;
  }

  private static void applyTradition(@NonNull Character character, @NonNull CharacterData data) {
    for (Tradition t : Tradition.values()) {
      if (t.name().equals(data.tradition)) {
        character.setTradition(t);
        return;
      }
    }
    // Unbekannter/fehlender Tradition-Name (z.B. Altdatei) — Default aus dem Character-Konstruktor
    // bleibt unangetastet, analog zur Nachsicht bei applySelectedQualities().
  }

  private static void applySpells(@NonNull Character character, @NonNull CharacterData data) {
    for (String name : data.spellNames) {
      SpellData spellData = SpellDataLoader.findByName(name);
      if (spellData != null) {
        character.addSpell(spellData);
      }
    }
    character.restoreGmFreeSpells(data.gmFreeSpells);
  }

  /**
   * Lädt Adeptenkräfte ohne Regelprüfung (wie Qualities): nach Qualities, Attributen, Fertigkeiten und Magie, weil
   * Kosten und Höchststufen davon abhängen. Unbekannte Kräfte werden übersprungen; ohne Adeptengabe gibt es keine.
   */
  private static void applyAdeptPowers(@NonNull Character character, @NonNull CharacterData data) {
    if (!character.hasAdeptPowers()) {
      return;
    }
    character.getAdeptPowers().loadAdeptMagic(data.adeptMagic);
    for (AdeptPowerEntry entry : data.adeptPowers) {
      AdeptPowerData powerData = AdeptPowerDataLoader.findByName(entry.name);
      if (powerData != null) {
        character.getAdeptPowers().load(powerData, entry.choice, entry.level);
      }
    }
  }

  private static void applyComplexForms(@NonNull Character character,
      @NonNull CharacterData data) {
    for (ComplexFormEntry entry : data.complexForms) {
      ComplexFormData complexFormData = ComplexFormDataLoader.findByName(entry.name);
      if (complexFormData == null) {
        continue;
      }
      character.addComplexForm(complexFormData);
      character.getComplexForms().stream()
          .filter(cf -> cf.getName().equals(entry.name))
          .findFirst()
          .ifPresent(cf -> {
            cf.setRating(entry.rating);
            cf.setGmFreeBp(entry.gmFreeBp);
          });
    }
  }

  /**
   * Stellt die eingebauten Implantate wieder her. Unbekannte Katalognamen und Werte, die nicht (mehr) zum Katalog
   * passen, werden — analog {@link #applyComplexForms} — übersprungen; eine unbekannte Grade fällt auf {@link
   * CyberwareGrade#STANDARD} zurück (analog zur Nachsicht bei {@link #applyTradition}). Zubehör und Kommlink, die
   * nicht mehr passen, werden ebenfalls übersprungen.
   *
   * @return das als aktiv gespeicherte Headware-Kommlink, oder {@code null}
   */
  private static @Nullable Gear applyCyberware(@NonNull Character character, @NonNull CharacterData data) {
    Map<Integer, List<Cyberware>> pairs = new HashMap<>();
    List<Gear> chosen = new ArrayList<>();
    for (CyberwareEntry entry : data.cyberware) {
      Cyberware implant = toCyberware(entry, pairs, chosen);
      if (implant != null) {
        character.getEquipment().addCyberware(implant);
      }
    }
    linkPairs(character, pairs);
    // nur ein Kommlink, das tatsächlich eingebaut ist, kommt in Frage
    List<Gear> commlinks = character.getImplants().getCommlinks();
    return chosen.stream().filter(commlinks::contains).findFirst().orElse(null);
  }

  /**
   * Verknüpft Original und zweiten Teil jeder Sprunghydraulik wieder; ein zweiter Teil ohne Original (z.B. weil das
   * Original nicht mehr passte) wird ausgebaut, damit er nicht kostenlos Kapazität belegt.
   */
  private static void linkPairs(@NonNull Character character, @NonNull Map<Integer, List<Cyberware>> pairs) {
    for (List<Cyberware> parts : pairs.values()) {
      Cyberware original = parts.stream().filter(c -> !c.isPairedCopy()).findFirst().orElse(null);
      Cyberware copy = parts.stream().filter(Cyberware::isPairedCopy).findFirst().orElse(null);
      if (original != null && copy != null) {
        original.linkPair(copy);
      } else if (copy != null) {
        character.getImplants().getLimbs().forEach(limb -> limb.uninstall(copy));
      }
    }
  }

  /**
   * Implantat aus {@code entry}; Teile einer Sprunghydraulik sammelt {@code pairs} nach ihrer Kennung, ein als aktiv
   * gespeichertes Kommlink {@code chosen}.
   */
  private static @Nullable Cyberware toCyberware(@NonNull CyberwareEntry entry,
      @NonNull Map<Integer, List<Cyberware>> pairs, @NonNull List<Gear> chosen) {
    CyberwareData cyberwareData = CyberwareDataLoader.findByName(entry.name);
    if (cyberwareData == null) {
      return null;
    }
    // Preisspanne ohne gültigen Preis (z.B. von Hand bearbeitet) fällt wie bei Kleidung auf die Untergrenze zurück.
    Integer price = null;
    if (cyberwareData.hasVariablePrice()) {
      price = entry.price != null && cyberwareData.isValidPrice(entry.price) ? entry.price : cyberwareData.priceMin();
    }
    Cyberware implant;
    try {
      LimbPart part = cyberwareData.limb().part();
      if (part != null) {
        implant = new Cyberware(cyberwareData, parseGrade(entry.grade), parseLocation(entry.location, part),
            new LimbAttributes(baseValue(entry.body), baseValue(entry.strength), baseValue(entry.agility)));
        implant.setAttached(entry.attached);
        implant.setGmFreeNuyen(entry.gmFreeNuyen);
      } else {
        implant = new Cyberware(cyberwareData, parseGrade(entry.grade), entry.rating, entry.choice, price);
        if (entry.pairedCopy && cyberwareData.limb().pairedLegs()) {
          implant = implant.pairedCopy();
        }
        implant.setGmFreeNuyen(entry.gmFreeNuyen);
      }
    } catch (IllegalArgumentException _) {
      return null;
    }
    for (CyberwareEntry childEntry : entry.installed) {
      if (childEntry.pairedCopy && childEntry.pair == 0) {
        continue; // zweiter Teil ohne Kennung: sein Original ist nicht zuzuordnen
      }
      Cyberware child = toCyberware(childEntry, pairs, chosen);
      if (child != null && implant.installProblem(child.getData(), child.getRating()) == null) {
        implant.install(child);
        if (childEntry.pair > 0) {
          pairs.computeIfAbsent(childEntry.pair, _ -> new ArrayList<>()).add(child);
        }
      }
    }
    Gear commlink = entry.commlink == null ? null : toGear(entry.commlink);
    if (commlink != null && implant.commlinkProblem(commlink.getData()) == null) {
      implant.setCommlink(commlink);
      if (Objects.requireNonNull(entry.commlink).active) {
        chosen.add(commlink);
      }
    }
    return implant;
  }

  /** Stellt die gekauften Rüstungsteile wieder her; unbekannte Katalognamen werden übersprungen. */
  private static void applyArmor(@NonNull Character character, @NonNull CharacterData data) {
    for (ArmorEntry entry : data.armor) {
      ArmorData armorData = ArmorDataLoader.findByName(entry.name);
      if (armorData == null) {
        continue;
      }
      // Kleidung ohne gültigen Preis (z.B. von Hand bearbeitet) fällt auf die Untergrenze zurück.
      Integer price = null;
      if (armorData.hasVariablePrice()) {
        price = entry.price != null && armorData.isValidPrice(entry.price) ? entry.price : armorData.priceMin();
      }
      Armor armor = new Armor(armorData, entry.equipped, price);
      armor.setGmFreeNuyen(entry.gmFreeNuyen);
      installArmorMods(armor, entry.mods);
      character.getEquipment().addArmor(armor);
    }
  }

  /** Baut die gespeicherten Einbauten ein; unbekannte oder nicht (mehr) passende werden übersprungen. */
  private static void installArmorMods(@NonNull Armor armor, @NonNull List<ArmorModEntry> entries) {
    for (ArmorModEntry modEntry : entries) {
      ArmorModData modData = ArmorModDataLoader.findByName(modEntry.name);
      if (modData != null && armor.installProblem(modData, modEntry.rating) == null) {
        armor.install(new ArmorMod(modData, modEntry.rating));
      }
    }
  }

  /**
   * Stellt die gekauften Waffen samt Zubehör und Pfeilen wieder her; unbekannte Katalognamen,
   * unzulässige Stufen, Zubehör, das laut Montageregeln nicht (mehr) passt, und Pfeile an einer Waffe,
   * die keine führt, werden übersprungen statt die ganze Datei abzulehnen.
   */
  private static void applyWeapons(@NonNull Character character, @NonNull CharacterData data) {
    for (WeaponEntry entry : data.weapons) {
      Weapon weapon = toWeapon(entry);
      if (weapon != null) {
        character.getEquipment().addWeapon(weapon);
        // unbekannte oder nicht mehr passende Munition: Standardmunition
        character.getEquipment().getAmmoCandidates(weapon).stream()
            .filter(g -> g.getName().equals(entry.ammo)).findFirst()
            .ifPresent(ammo -> character.getEquipment().loadAmmo(weapon, ammo));
      }
    }
  }

  /** Baut eine Waffe samt Zubehör und Pfeilen; {@code null} bei unbekanntem Namen oder Stufe. */
  private static @Nullable Weapon toWeapon(@NonNull WeaponEntry entry) {
    WeaponData weaponData = WeaponDataLoader.findByName(entry.name);
    if (weaponData == null || !weaponData.isValidRating(entry.rating)) {
      return null;
    }
    Weapon weapon = new Weapon(weaponData, entry.rating);
    weapon.setGmFreeNuyen(entry.gmFreeNuyen);
    entry.accessories.forEach(a -> applyAccessory(weapon, a));
    for (GearEntry arrowEntry : entry.arrows) {
      Gear arrows = toGear(arrowEntry);
      if (arrows == null) {
        continue;
      }
      try {
        weapon.addArrows(arrows);
      } catch (IllegalArgumentException _) {
        // kein Bogen bzw. keine Pfeile: überspringen
      }
    }
    return weapon;
  }

  /**
   * Stellt die gekaufte Ausrüstung wieder her; unbekannte Katalognamen, Einträge mit unzulässiger
   * Stufe, Menge, Zusatztext oder Preis und Einbauten ohne Gerät (Betriebssystem ohne Kommlink)
   * werden übersprungen statt die ganze Datei abzulehnen.
   *
   * @return das als aktiv gespeicherte Kommlink der Ausrüstung, oder {@code null}
   */
  private static @Nullable Gear applyGear(@NonNull Character character, @NonNull CharacterData data) {
    Gear chosen = null;
    for (GearEntry entry : data.gear) {
      Gear gear = toGear(entry);
      if (gear == null) {
        continue;
      }
      // Einbauten ohne Gerät und Pfeile ohne Bogen gehören nicht in die Ausrüstungsliste
      GearCategory category = gear.getData().category();
      if (!category.isInstalledOnly() && category != GearCategory.ARROWS) {
        Gear added = character.getEquipment().addGear(gear);
        if (entry.active && category == GearCategory.COMMLINKS) {
          chosen = added;
        }
      }
    }
    return chosen;
  }

  /**
   * Stellt die Fahrzeuge wieder her. Mitgelieferte Halterungen und Ausrüstung entstehen aus dem
   * Katalog; aus der Datei kommen nur ihre Waffen bzw. Einbauten (positionsweise). Unbekannte
   * Fahrzeuge, Waffen und Gegenstände, Waffen, die nicht (mehr) in eine Halterung passen, und
   * Halterungen über der Grenze Rumpf ÷ 3 werden übersprungen statt die ganze Datei abzulehnen.
   */
  private static void applyVehicles(@NonNull Character character, @NonNull CharacterData data) {
    for (VehicleEntry entry : data.vehicles) {
      VehicleData vehicleData = VehicleDataLoader.findByName(entry.name);
      if (vehicleData == null) {
        continue;
      }
      Vehicle vehicle = new Vehicle(vehicleData);
      vehicle.setGmFreeNuyen(entry.gmFreeNuyen);
      if (entry.riggerAdapter && vehicle.riggerAdapterProblem() == null) {
        vehicle.installRiggerAdapter();
      }
      applyMounts(vehicle, entry.mounts);
      applyIncludedGear(vehicle, entry.includedGear);
      applyVehicleGear(vehicle, entry.gear);
      applyMatrixUpgrades(vehicle, entry.matrixUpgrades);
      vehicle.setDamage(entry.damage);
      character.getEquipment().addVehicle(vehicle);
    }
  }

  /**
   * Stellt die Einbauten der mitgelieferten Geräte positionsweise wieder her; ein Eintrag, dessen Name
   * nicht zum Katalog-Gerät an derselben Position passt, wird übersprungen.
   */
  private static void applyIncludedGear(@NonNull Vehicle vehicle,
      @NonNull List<GearEntry> entries) {
    List<Gear> included = vehicle.getIncludedGear();
    for (int i = 0; i < Math.min(included.size(), entries.size()); i++) {
      Gear device = included.get(i);
      GearEntry savedDevice = entries.get(i);
      if (device.getName().equals(savedDevice.name)) {
        installChildren(device, savedDevice.installed);
      }
    }
  }

  /** Stellt die Matrix-Aufrüstungen eines Fahrzeugs wieder her; unpassende werden übersprungen. */
  private static void applyMatrixUpgrades(@NonNull Vehicle vehicle, @NonNull List<GearEntry> entries) {
    for (GearEntry upgradeEntry : entries) {
      Gear upgrade = toGear(upgradeEntry);
      if (upgrade != null && vehicle.matrixUpgradeProblem(upgrade.getData(), upgrade.getRating()) == null) {
        vehicle.addMatrixUpgrade(upgrade);
      }
    }
  }

  /** Stellt die ins Fahrzeug gekaufte Ausrüstung wieder her; reine Einbauten werden übersprungen. */
  private static void applyVehicleGear(@NonNull Vehicle vehicle, @NonNull List<GearEntry> entries) {
    for (GearEntry gearEntry : entries) {
      Gear gear = toGear(gearEntry);
      if (gear != null && !gear.getData().category().isInstalledOnly()) {
        vehicle.addGear(gear);
      }
    }
  }

  private static void applyMounts(@NonNull Vehicle vehicle, @NonNull List<MountEntry> entries) {
    int includedIndex = 0;
    for (MountEntry entry : entries) {
      WeaponMount mount;
      if (entry.included) {
        List<WeaponMount> mounts = vehicle.getMounts();
        if (includedIndex >= mounts.size() || !mounts.get(includedIndex).isIncluded()) {
          continue;
        }
        mount = mounts.get(includedIndex++);
      } else if (vehicle.mountProblem() == null) {
        mount = vehicle.addMount();
      } else {
        continue;
      }
      WeaponEntry weaponEntry = entry.weapon;
      Weapon weapon = weaponEntry == null ? null : toWeapon(weaponEntry);
      if (weapon != null && vehicle.weaponProblem(mount, weapon.getData()) == null) {
        vehicle.mountWeapon(mount, weapon);
      }
    }
  }

  /**
   * Baut einen Gegenstand samt Einbauten aus der Speicherdatei; {@code null} bei unbekanntem Namen
   * oder Werten, die nicht (mehr) zum Katalog passen. Unpassende Einbauten werden übersprungen.
   */
  private static @Nullable Gear toGear(@NonNull GearEntry entry) {
    GearData gearData = GearDataLoader.findByName(entry.name);
    if (gearData == null) {
      return null;
    }
    Gear gear;
    try {
      gear = new Gear(gearData, entry.rating, entry.quantity, entry.extra, entry.packPrice);
    } catch (IllegalArgumentException _) {
      return null;
    }
    installChildren(gear, entry.installed);
    gear.setGmFreeNuyen(entry.gmFreeNuyen);
    gear.setGmFreeBindingBp(entry.gmFreeBindingBp);
    if (entry.running && gear.isProgram()) {
      gear.setRunning(true);
    }
    if (entry.bound && gear.isFocus()) {
      gear.setFocusState(true, entry.focusActive); // ohne Grenzprüfung wie beim Laden der Geister; Hinweis über Foci
    }
    return gear;
  }

  /** Baut die gespeicherten Einbauten in {@code target} ein; unpassende werden übersprungen. */
  private static void installChildren(@NonNull Gear target, @NonNull List<GearEntry> entries) {
    for (GearEntry childEntry : entries) {
      Gear child = toGear(childEntry);
      if (child != null && target.installProblem(child.getData(), child.getRating()) == null) {
        target.install(child);
      }
    }
  }

  private static void applyAccessory(@NonNull Weapon weapon, @NonNull AccessoryEntry entry) {
    AccessoryData accessoryData = AccessoryDataLoader.findByName(entry.name);
    if (accessoryData == null) {
      return;
    }
    try {
      Mount mount = entry.mount == null ? null : Mount.valueOf(entry.mount);
      weapon.addAccessory(new WeaponAccessory(accessoryData, mount));
    } catch (IllegalArgumentException _) {
      // unbekannte Halterung oder Teil passt nicht: überspringen
    }
  }

  /**
   * Stellt den Lebensstil wieder her. Eine unbekannte Stufe bzw. ein fehlender Knoten (Altdatei)
   * lässt den Default Straße stehen; weniger als ein Monat wird auf das Minimum angehoben.
   */
  private static void applyLifestyle(@NonNull Character character, @NonNull CharacterData data) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    for (LifestyleLevel level : LifestyleLevel.values()) {
      if (level.name().equals(data.lifestyle.level)) {
        lifestyle.setLevel(level);
      }
    }
    lifestyle.setMonths(Math.max(Lifestyle.MIN_MONTHS, data.lifestyle.months));
    lifestyle.restoreArrears(data.lifestyle.missedMonths, data.lifestyle.debt);
    lifestyle.setGmFreeNuyen(data.lifestyle.gmFreeNuyen);
  }

  private static void applyRuleOptions(@NonNull Character character, @NonNull CharacterData data) {
    RuleOptions rules = character.getRuleOptions();
    rules.setLimbArmorAverage(data.rules.limbArmorAverage);
    rules.setLateAwakening(data.rules.lateAwakening);
    rules.setMissedLifestylePayments(data.rules.missedLifestylePayments);
    rules.setDecoupleAtCreation(data.rules.decoupleAtCreation);
    data.rules.books.forEach(code -> rules.setBookEnabled(code, true));
  }

  /** Gespeicherte Körperstelle; fehlt sie oder passt sie nicht, die erste Stelle der Gliedart. */
  private static @NonNull BodyLocation parseLocation(@Nullable String name, @NonNull LimbPart part) {
    for (BodyLocation location : part.locations()) {
      if (location.name().equals(name)) {
        return location;
      }
    }
    return part.locations().getFirst();
  }

  /** Gespeicherter Grundwert eines Glieds; {@code 0} (Feld fehlt) steht für den Standardwert. */
  private static int baseValue(int stored) {
    return stored == 0 ? LimbAttributes.STANDARD_VALUE : stored;
  }

  private static @NonNull CyberwareGrade parseGrade(@NonNull String name) {
    for (CyberwareGrade grade : CyberwareGrade.values()) {
      if (grade.name().equals(name)) {
        return grade;
      }
    }
    return CyberwareGrade.STANDARD;
  }

  private static void applyBiography(@NonNull Character character, @NonNull CharacterData data) {
    CharacterBiography biography = character.getBiography();
    biography.setAlias(data.biography.alias);
    biography.setSex(data.biography.sex);
    biography.setAge(data.biography.age);
    biography.setEyes(data.biography.eyes);
    biography.setHeight(data.biography.height);
    biography.setWeight(data.biography.weight);
    biography.setSkin(data.biography.skin);
    biography.setHair(data.biography.hair);
    biography.setDescription(data.biography.description);
    biography.setBackground(data.biography.background);
    biography.setConcept(data.biography.concept);
    biography.setNotes(data.biography.notes);
    biography.setPlayerName(data.biography.playerName);
    biography.setGameNotes(data.biography.gameNotes);
    biography.setPortrait(data.biography.portrait);
  }

  private static void applyAttributes(@NonNull Character character, @NonNull CharacterData data) {
    for (Map.Entry<String, Integer> e : data.attributes.entrySet()) {
      character.getAttribute(AttributeType.valueOf(e.getKey())).setValue(e.getValue());
    }
    if (data.edge > 0 || data.edgeBurned > 0) { // 0 nur, wenn alles verheizt ist
      character.getEdge().load(data.edge, data.edgeBurned);
    }
    character.loadEdgeSpent(data.edgeSpent);
  }

  private static void applySkills(@NonNull Character character, @NonNull CharacterData data) {
    for (SkillEntry entry : data.skills) {
      Skill skill = entry.exoticTarget == null ? character.getSkill(entry.name)
          : character.loadExoticSkill(entry.name, entry.exoticTarget);
      if (skill != null) {
        skill.setRating(entry.rating);
        skill.setSpecialization(entry.specialization);
      }
    }
  }

  private static void applySelectedQualities(@NonNull Character character,
      @NonNull CharacterData data) {
    for (SelectedQualityEntry entry : data.selectedQualities) {
      QualityData qualityData = QualityDataLoader.findByName(
          QualityNameMigration.currentName(entry.name));
      if (qualityData != null) {
        character.loadSelectedQuality(qualityData, entry.gratis, entry.choice);
      }
    }
  }

  /** Stellt Grad und Techniken wieder her; unbekannte Techniken werden übersprungen. */
  private static void applyInitiation(@NonNull Character character, @NonNull CharacterData data) {
    List<TechniqueData> techniques = data.initiation.techniques.stream()
        .map(TechniqueDataLoader::findByName)
        .filter(Objects::nonNull)
        .toList();
    character.getInitiation().restore(data.initiation.grade, techniques);
  }

  /**
   * Stellt Geister und Sprites ohne Grenzprüfung wieder her; unbekannte Arten und ungültige Werte überspringen, ebenso
   * Optionen, die nicht (mehr) zur Art gehören.
   */
  private static void applySpirits(@NonNull Character character, @NonNull CharacterData data) {
    List<Spirit> spirits = new ArrayList<>();
    for (SpiritEntry entry : data.spirits) {
      SpiritData spiritData = SpiritDataLoader.findByName(entry.name);
      if (spiritData != null && entry.force >= 1 && entry.services >= 0) {
        List<String> options = entry.options.stream().filter(spiritData.optional()::contains).distinct().toList();
        Spirit spirit = new Spirit(spiritData, entry.force, entry.services, entry.bound, options);
        spirit.setGmFreeBp(entry.gmFreeBp);
        spirits.add(spirit);
      }
    }
    character.getSpirits().restore(spirits);
  }

  private static void applyMagicOrResonance(@NonNull Character character,
      @NonNull CharacterData data) {
    if (data.magic != null && character.hasMagic()) {
      Objects.requireNonNull(character.getMagicOrResonance()).setValue(data.magic);
    } else if (data.resonance != null && character.hasResonance()) {
      Objects.requireNonNull(character.getMagicOrResonance()).setValue(data.resonance);
    }
  }

  private static void applyKnowledgeSkills(@NonNull Character character,
      @NonNull CharacterData data) {
    for (KnowledgeSkillEntry entry : data.knowledgeSkills) {
      if (entry.nativeLanguage) {
        character.setNativeLanguage(entry.name);
        continue;
      }
      character.addKnowledgeSkill(entry.name, KnowledgeSkillCategory.valueOf(entry.category));
      character.getKnowledgeSkills().stream()
          .filter(k -> k.getName().equals(entry.name))
          .findFirst()
          .ifPresent(k -> {
            k.setRating(entry.rating);
            k.setSpecialization(entry.specialization);
          });
    }
  }

  private static void applyContacts(@NonNull Character character, @NonNull CharacterData data) {
    for (ContactEntry entry : data.contacts) {
      character.addContact(entry.name, entry.description);
      Contact added = character.getContacts().getLast();
      added.setConnection(entry.connection);
      added.setLoyalty(entry.loyalty);
      added.setGmFreeBp(entry.gmFreeBp);
    }
  }

  /** Art aus der Datei; unbekannt oder fehlend → {@link LogCategory#MANUAL}. */
  private static @NonNull LogCategory category(@NonNull String name) {
    try {
      return LogCategory.valueOf(name);
    } catch (IllegalArgumentException _) {
      return LogCategory.MANUAL;
    }
  }

  private static void applyLog(@NonNull List<LogEntryData> entries,
      @NonNull Consumer<LogEntry> sink) {
    for (LogEntryData e : entries) {
      if (!e.date.isEmpty()) {
        sink.accept(new LogEntry(e.description, e.amount, LocalDate.parse(e.date), category(e.category)));
      }
    }
  }
}