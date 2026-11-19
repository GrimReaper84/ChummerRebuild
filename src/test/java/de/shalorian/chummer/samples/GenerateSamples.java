package de.shalorian.chummer.samples;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.initiation.TechniqueDataLoader;
import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.spirit.SpiritDataLoader;
import de.shalorian.chummer.model.character.vehicle.Vehicle;
import de.shalorian.chummer.model.npc.NpcCombatant;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import de.shalorian.chummer.model.template.ArchetypeLoader;
import de.shalorian.chummer.model.template.ArchetypeTemplate;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.persistence.NpcGroupStore;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Erzeugt die Testfälle des Beta-Menüs (ADR-063) nach {@code src/test/resources/samples}: Charaktere und eine
 * Schergengruppe in Zuständen, die die Archetypen nicht abdecken (Karriere mit Log, Initiation, Wandlung, Rigger-Ausbau,
 * überzogene Erschaffung, alle Regel-Einstellungen, Kampfzustand). Läuft von Hand über {@code ./gradlew generateSamples},
 * nicht im Build; die Dateien werden eingecheckt. Adept und Cyberglieder deckt das Beta-Menü über die Archetypen
 * Pistolero-Adeptin und Straßensamurai ab.
 */
public final class GenerateSamples {

  private static final LocalDate DATE = LocalDate.of(2072, 3, 1);

  private GenerateSamples() {
  }

  /** Schreibt alle Testfälle in den Ordner aus dem ersten Argument. */
  public static void main(String[] args) throws IOException {
    File dir = new File(args[0]);
    if (!dir.isDirectory() && !dir.mkdirs()) {
      throw new IOException("Ordner nicht anlegbar: " + dir);
    }
    for (Map.Entry<String, Character> sample : characters().entrySet()) {
      CharacterStore.save(sample.getValue(), new File(dir, sample.getKey() + ".json"));
    }
    NpcGroupStore.save(groupInCombat(), new File(dir, "schergengruppe-im-kampf.json"));
    System.out.println("Testfälle geschrieben nach " + dir.getAbsolutePath());
  }

  /** Die Charakter-Testfälle nach Dateiname (ohne Endung). */
  static Map<String, Character> characters() {
    Map<String, Character> samples = new LinkedHashMap<>();
    samples.put("karriere-mit-log", careerWithLog());
    samples.put("initiierter-magier", initiate());
    samples.put("technomancer-mit-wandlung", submerged());
    samples.put("rigger-ausbau", rigger());
    samples.put("ueberzogene-erschaffung", overspent());
    samples.put("alle-regel-einstellungen", allRules());
    return samples;
  }

  private static Character archetype(String name) {
    ArchetypeTemplate template = Objects.requireNonNull(ArchetypeLoader.findByName(name), name);
    return template.create();
  }

  private static void karma(Character c, LogCategory category, String text, int amount) {
    c.addKarmaEntry(new LogEntry(text, amount, DATE, category));
  }

  /**
   * Straßensamurai nach drei Runs: Karma und Nuyen aus Aufträgen, je eine Buchung für Attribut, Fertigkeit, Gabe und
   * Kontakt, Kauf, Verkauf und Lebensstil (Magie/Resonanz buchen die Testfälle Initiierter Magier und Technomancer).
   */
  private static Character careerWithLog() {
    Character c = archetype("Straßensamurai");
    c.setName("Karriere mit Log");
    c.finalizeCreation(10, DATE);
    karma(c, LogCategory.MANUAL, "Run: Hafenauftrag", 20);
    karma(c, LogCategory.MANUAL, "Run: Konzernextraktion", 30);
    c.getAttribute(AttributeType.INT).setValue(4);
    karma(c, LogCategory.ATTRIBUTE, "Intuition 3 → 4", -20);
    c.getSkill("Pistolen").setRating(5);
    karma(c, LogCategory.SKILL, "Pistolen 4 → 5", -10);
    c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName("Fotografisches Gedächtnis")), false);
    karma(c, LogCategory.QUALITY, "Fotografisches Gedächtnis", -20);
    c.addContact("Waffenhändler", null);
    karma(c, LogCategory.CONTACT, "Kontakt Waffenhändler", -2);
    c.addNuyenEntry(new LogEntry("Run: Hafenauftrag", 6000, DATE, LogCategory.MANUAL));
    Gear medkit = new Gear(Objects.requireNonNull(GearDataLoader.findByName("Medkit")), 4);
    c.getEquipment().addGear(medkit);
    c.addNuyenEntry(new LogEntry("Medkit (Stufe 4)", -400, DATE, LogCategory.PURCHASE));
    Gear sold = c.getEquipment().getGear().stream().filter(g -> g.getData().name().equals("AR-Handschuhe"))
        .findFirst().orElseThrow();
    c.getEquipment().removeGear(sold);
    c.addNuyenEntry(new LogEntry("AR-Handschuhe verkauft", 75, DATE, LogCategory.SALE));
    c.addNuyenEntry(new LogEntry("Lebensstil, 1 Monat", -2000, DATE, LogCategory.LIFESTYLE));
    return c;
  }

  /** Kampfmagier mit Initiatengrad 1, Metamagie, gebundenem Kraftfokus und gebundenem Geist. */
  private static Character initiate() {
    Character c = archetype("Kampfmagier");
    c.setName("Initiierter Magier");
    c.finalizeCreation(6, DATE);
    karma(c, LogCategory.MANUAL, "Karma aus Runs", 40);
    int initiationCost = c.getInitiation().getNextGradeKarmaCost();
    c.getInitiation().raiseGrade();
    karma(c, LogCategory.MAGIC_RESONANCE, "Initiation Grad 1", -initiationCost);
    c.getInitiation().learn(Objects.requireNonNull(TechniqueDataLoader.findByName("Zentrierung")));
    Gear focus = new Gear(Objects.requireNonNull(GearDataLoader.findByName("Kraftfokus")), 1);
    c.getEquipment().addGear(focus);
    c.getFoci().bind(focus);
    karma(c, LogCategory.MAGIC_RESONANCE, "Kraftfokus binden", -c.getFoci().getBindingKarma(focus));
    c.getSpirits().add(new Spirit(Objects.requireNonNull(SpiritDataLoader.findByName("Feuergeist")), 4, 2, true));
    return c;
  }

  /** Technomancer mit Wandlung, Echo und registriertem Sprite. */
  private static Character submerged() {
    Character c = archetype("Technomancer");
    c.setName("Technomancer mit Wandlung");
    c.finalizeCreation(8, DATE);
    karma(c, LogCategory.MANUAL, "Karma aus Runs", 30);
    int submersionCost = c.getInitiation().getNextGradeKarmaCost();
    c.getInitiation().raiseGrade();
    karma(c, LogCategory.MAGIC_RESONANCE, "Wandlung Grad 1", -submersionCost);
    c.getInitiation().learn(Objects.requireNonNull(TechniqueDataLoader.findByName("Firewall-Upgrade")));
    c.getSpirits().add(new Spirit(Objects.requireNonNull(SpiritDataLoader.findByName("Kurier-Sprite")), 3, 2, true));
    return c;
  }

  /** Drohnenrigger mit Riggeradaption im Step-Van und Matrix-Modulen in einer Drohne. */
  private static Character rigger() {
    Character c = archetype("Drohnenrigger");
    c.setName("Rigger-Ausbau");
    Vehicle van = c.getEquipment().getVehicles().getFirst();
    van.installRiggerAdapter();
    Vehicle doberman = c.getEquipment().getVehicles().stream()
        .filter(v -> v.getData().name().startsWith("GM-Nissan Doberman")).findFirst().orElseThrow();
    doberman.addMatrixUpgrade(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Prozessor-Modul")), 4));
    doberman.addMatrixUpgrade(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Signal-Modul")), 4));
    return c;
  }

  /** Chamäleon mit zu vielen BP, zu teurer Ausrüstung und Essenz unter 0: alle drei Warnungen der Erschaffung. */
  private static Character overspent() {
    Character c = archetype("Chamäleon");
    c.setName("Überzogene Erschaffung");
    c.setNuyenBP(15);
    c.getImplants().add(new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName("Reflexbooster")),
        CyberwareGrade.STANDARD, 3));
    c.getImplants().add(new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName("Kunstmuskeln")),
        CyberwareGrade.STANDARD, 2));
    Gear sins = new Gear(Objects.requireNonNull(GearDataLoader.findByName("Gefälschte SIN")), 6, 10, "Tarnidentität",
        null);
    c.getEquipment().addGear(sins);
    c.getSkill("Pistolen").setRating(4);
    return c;
  }

  /** Geheimagent mit allen Regel-Einstellungen an und einem Zusatzband-Kürzel. */
  private static Character allRules() {
    Character c = archetype("Geheimagent");
    c.setName("Alle Regel-Einstellungen");
    RuleOptions options = c.getRuleOptions();
    options.setLimbArmorAverage(true);
    options.setLateAwakening(true);
    options.setMissedLifestylePayments(true);
    options.setDecoupleAtCreation(true);
    options.setBookEnabled("SM", true);
    return c;
  }

  /** Lone-Star-Einheit mitten im Kampf: verletzte und ausgeschaltete Schergen, angeschlagener Anführer, Edge verbraucht. */
  static NpcGroup groupInCombat() {
    NpcGroup group = Objects.requireNonNull(NpcTemplateLoader.findByName("Lone Star Polizeieinheit")).create();
    int boxes = group.getGrunt().getMonitorBoxes();
    NpcCombatant first = group.getMembers().getFirst();
    first.setDamage(3, boxes);
    group.getMembers().get(1).setDamage(boxes, boxes);
    group.getLeaderState().setDamage(2, group.getLeader() == null ? boxes : group.getLeader().getMonitorBoxes());
    group.setEdgeSpent(1);
    return group;
  }
}
