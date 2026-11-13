package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPowerEffect;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.quality.QualityBonusType;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Kaufregeln und Wirkungen der Implantate eines Charakters (Unterobjekt von {@link Character}, Vorbild
 * {@code AdeptPowers}). Die Implantate selbst liegen weiter in {@code Equipment#getCyberware()}, weil Nuyen,
 * Persistenz und Tab dort ansetzen; hier stehen nur die Prüfungen und die Summen der Wirkungen.
 *
 * <p>Gezählt werden nur direkt implantierte Implantate, nicht eingebautes Zubehör in Basissystemen; dieses Zubehör
 * hat keine rechnenden Wirkungen.
 */
public class Implants {

  private static final List<SkillCategory> REFLEX_RECORDER_CATEGORIES =
      List.of(SkillCategory.COMBAT, SkillCategory.PHYSICAL);

  /** Die fünf Körperteile des Durchschnitts; mit dem Schädel ginge das Rechenbeispiel S. 390 nicht auf. */
  private static final List<BodyLocation> AVERAGE_PARTS = List.of(BodyLocation.ARM_LEFT, BodyLocation.ARM_RIGHT,
      BodyLocation.LEG_LEFT, BodyLocation.LEG_RIGHT, BodyLocation.TORSO);
  private static final List<AttributeType> LIMB_ATTRIBUTES =
      List.of(AttributeType.BOD, AttributeType.STR, AttributeType.AGI);

  /** Höchste Stufe einer Cyberglied-Verbesserung ohne Cybertorso (S. 390). */
  private static final int MAX_ENHANCEMENT_WITHOUT_TORSO = 3;

  private final @NonNull Character owner;

  /** Implantatregeln für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Implants(@NonNull Character owner) {
    this.owner = owner;
  }

  private @NonNull List<Cyberware> implants() {
    return owner.getEquipment().getCyberware();
  }

  /** Implantate samt allem, was in Cybergliedmaßen eingebaut ist (für Doppelkauf und Unverträglichkeiten). */
  private @NonNull List<Cyberware> implantsWithLimbParts() {
    List<Cyberware> all = new ArrayList<>(implants());
    getLimbs().forEach(limb -> all.addAll(limb.getInstalled()));
    return all;
  }

  // --- Kaufprüfung ---

  /**
   * Warum {@code data} grundsätzlich nicht implantiert werden kann, oder {@code null}. Einzige Prüfung für Modell,
   * Dialog und Tab:
   * <ul>
   *   <li>nur als Einbau erhältlich (Okular-Drohne, S. 386; Cyberglied-Verbesserungen und -Zubehör, S. 391)</li>
   *   <li>schon vorhanden und nicht {@link ImplantRestrictions#multiple()}</li>
   *   <li>Voraussetzung fehlt ({@link ImplantRestrictions#requires()})</li>
   *   <li>Unverträglichkeit mit einem vorhandenen Implantat, in beide Richtungen (S. 388, 389, 393, 394)</li>
   *   <li>Basissystem und einzeln implantiertes Zubehör derselben Kategorie schließen sich aus: Zubehör kostet im
   *       Cyberauge/-ohr Kapazität oder im natürlichen Organ Essenz, nicht beides (S. 386, 387)</li>
   *   <li>technische Initiativesteigerung neben der Adeptenkraft Gesteigerte Reflexe (S. 230)</li>
   *   <li>Schmerzeditor oder Schadenskompensator neben der Gabe Hohe Schmerztoleranz (S. 116)</li>
   * </ul>
   * Head- und Bodyware in Cybergliedmaßen zählt dabei mit (S. 385, 388).
   */
  public @Nullable String getBlockReason(@NonNull CyberwareData data) {
    if (data.restrictions().installOnly()) {
      return "Nur als Einbau erhältlich";
    }
    return conflictReason(data);
  }

  /** Alle Prüfungen von {@link #getBlockReason} außer „nur als Einbau“; gilt auch für Einbauten in Glieder. */
  private @Nullable String conflictReason(@NonNull CyberwareData data) {
    List<Cyberware> all = implantsWithLimbParts();
    if (!data.restrictions().multiple() && all.stream().anyMatch(c -> c.getData().equals(data))) {
      return "Bereits eingebaut";
    }
    String requires = data.restrictions().requires();
    if (!requires.isEmpty() && all.stream().noneMatch(c -> c.getName().equals(requires))) {
      return "Nur zusammen mit " + requires;
    }
    for (Cyberware other : all) {
      if (excludes(data, other.getData()) || excludes(other.getData(), data)) {
        return "Nicht zusammen mit " + other.getName();
      }
      if (sameOrganConflict(data, other.getData())) {
        return data.hasCapacity()
            ? "Nicht zusammen mit einzeln implantiertem " + other.getName()
            : "In " + other.getName() + " einbauen statt einzeln implantieren";
      }
    }
    if (data.restrictions().initiativeBoost() && owner.getAdeptPowers().level(AdeptPowerEffect.IMPROVED_REFLEXES) > 0) {
      return "Nicht zusammen mit Gesteigerte Reflexe";
    }
    if (data.restrictions().excludesPainTolerance() && owner.hasQualityBonus(QualityBonusType.HIGH_PAIN_TOLERANCE)) {
      return "Nicht zusammen mit Hohe Schmerztoleranz";
    }
    return null;
  }

  /**
   * Implantat, das die Gabe Hohe Schmerztoleranz ausschließt (Schmerzeditor, Schadenskompensator, S. 116), oder
   * {@code null}; Gegenrichtung zu {@link #getBlockReason}.
   */
  public @Nullable String getPainToleranceConflict() {
    return implants().stream()
        .filter(c -> c.getData().restrictions().excludesPainTolerance())
        .map(Cyberware::getName)
        .findFirst()
        .orElse(null);
  }

  /**
   * Implantiert {@code implant} nach den Kaufregeln. Ein Glied an einer Stelle, an der schon ein angeschlossenes sitzt,
   * kommt abgenommen dazu (Ersatzglied).
   *
   * @throws IllegalArgumentException wenn {@link #getAddProblem} einen Grund liefert
   */
  public void add(@NonNull Cyberware implant) {
    String problem = getAddProblem(implant.getData(), implant.getChoice());
    if (problem == null && implant.getData().limb().isLimb()) {
      problem = getLimbBaseProblem(implant.getBaseAttributes());
    }
    if (problem != null) {
      throw new IllegalArgumentException(implant.getName() + ": " + problem);
    }
    BodyLocation location = implant.getLocation();
    if (location != null && isOccupied(location)) {
      implant.setAttached(false);
    }
    owner.getEquipment().addCyberware(implant);
  }

  /**
   * Warum {@code data} mit dieser Auswahl nicht implantiert werden kann, oder {@code null}: erst {@link
   * #getBlockReason}, dann die Auswahl beim Reflexrekorder (Kampf- oder körperliche Fertigkeit bzw. -gruppe). Je
   * Fertigkeit ist nur ein Reflexrekorder möglich (S. 394), auch nicht einer für die Fertigkeit und einer für ihre
   * Gruppe.
   */
  public @Nullable String getAddProblem(@NonNull CyberwareData data, @NonNull String choice) {
    String reason = getBlockReason(data);
    if (reason != null) {
      return reason;
    }
    String trimmed = choice.trim();
    String choiceProblem = switch (data.selection()) {
      case NONE -> null;
      case SKILL -> isReflexRecorderSkill(trimmed) ? null : "Kampf- oder körperliche Fertigkeit wählen";
      case SKILL_GROUP -> getReflexRecorderGroups().contains(trimmed) ? null : "Fertigkeitsgruppe wählen";
    };
    if (choiceProblem != null) {
      return choiceProblem;
    }
    if (!trimmed.isEmpty()
        && implants().stream().anyMatch(c -> c.getData().equals(data) && c.getChoice().equals(trimmed))) {
      return "Mit dieser Auswahl bereits eingebaut";
    }
    return switch (data.selection()) {
      case NONE -> null;
      case SKILL -> recorderForGroupOf(trimmed);
      case SKILL_GROUP -> recorderForSkillIn(trimmed);
    };
  }

  /** Grund, falls die Gruppe der Fertigkeit {@code skillName} schon einen Reflexrekorder hat, sonst {@code null}. */
  private @Nullable String recorderForGroupOf(@NonNull String skillName) {
    Skill skill = owner.getSkill(skillName);
    String group = skill == null ? null : skill.getGroupName();
    return group != null && hasRecorderChoice(ImplantSelection.SKILL_GROUP, List.of(group))
        ? "Gruppe " + group + " hat bereits einen Reflexrekorder" : null;
  }

  /** Grund, falls eine Fertigkeit der Gruppe {@code group} schon einen Reflexrekorder hat, sonst {@code null}. */
  private @Nullable String recorderForSkillIn(@NonNull String group) {
    List<String> members = owner.getSkills().stream()
        .filter(s -> group.equals(s.getGroupName()))
        .map(Skill::getName)
        .toList();
    return hasRecorderChoice(ImplantSelection.SKILL, members)
        ? "Eine Fertigkeit der Gruppe hat bereits einen Reflexrekorder" : null;
  }

  private boolean hasRecorderChoice(@NonNull ImplantSelection selection, @NonNull List<String> choices) {
    return implants().stream()
        .anyMatch(c -> c.getData().selection() == selection && choices.contains(c.getChoice()));
  }

  /** Fertigkeiten, die ein Reflexrekorder verbessern kann: Kampf- und körperliche Fertigkeiten (S. 394). */
  public @NonNull List<String> getReflexRecorderSkills() {
    return owner.getSkills().stream()
        .filter(s -> REFLEX_RECORDER_CATEGORIES.contains(s.getCategory()))
        .map(Skill::getName)
        .sorted()
        .toList();
  }

  /** Fertigkeitsgruppen aus Kampf- und körperlichen Fertigkeiten (S. 394). */
  public @NonNull List<String> getReflexRecorderGroups() {
    return owner.getSkills().stream()
        .filter(s -> REFLEX_RECORDER_CATEGORIES.contains(s.getCategory()))
        .map(Skill::getGroupName)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .toList();
  }

  private boolean isReflexRecorderSkill(@NonNull String name) {
    return getReflexRecorderSkills().contains(name);
  }

  /**
   * Ob {@code data} das andere Implantat ausschließt: namentlich oder über seine Kategorie. Eine Kategorie schließt nur
   * Organmodifikationen aus, also Basissysteme und ihr Zubehör; Schutzgläser etwa sind keine Augenmodifikation und
   * bleiben neben Katzenaugen erlaubt (S. 388, 393).
   */
  private static boolean excludes(@NonNull CyberwareData data, @NonNull CyberwareData other) {
    return data.restrictions().excludes().contains(other.name())
        || (data.restrictions().excludedCategories().contains(other.category())
            && (other.hasCapacity() || isAccessory(other)));
  }

  /** Basissystem gegen einzeln implantiertes Zubehör derselben Kategorie (Augen bzw. Ohren). */
  private static boolean sameOrganConflict(@NonNull CyberwareData data, @NonNull CyberwareData other) {
    if (!data.category().equals(other.category())) {
      return false;
    }
    return (data.hasCapacity() && isAccessory(other)) || (other.hasCapacity() && isAccessory(data));
  }

  /** Zubehör, das in ein Basissystem gehört (einbaubar oder dort schon enthalten). */
  private static boolean isAccessory(@NonNull CyberwareData data) {
    return !data.capacityCost().isEmpty();
  }

  /** Ob ein Implantat eine technische Initiativesteigerung ist, die Gesteigerte Reflexe ausschließt (S. 230). */
  public boolean hasInitiativeBoost() {
    return implants().stream().anyMatch(c -> c.getData().restrictions().initiativeBoost());
  }

  // --- Cybergliedmaßen (S. 389–391) ---

  /** Alle gekauften Cybergliedmaßen, angeschlossen oder abgenommen. */
  public @NonNull List<Cyberware> getLimbs() {
    return implants().stream().filter(c -> c.getData().limb().isLimb()).toList();
  }

  /** Angeschlossene Cybergliedmaßen; nur sie wirken. */
  public @NonNull List<Cyberware> getAttachedLimbs() {
    return getLimbs().stream().filter(Cyberware::isAttached).toList();
  }

  /**
   * Warum ein Glied mit diesen Grundwerten nicht gekauft werden kann, oder {@code null}: Maßanfertigung höchstens bis
   * zum natürlichen Maximum des Charakters; darüber nur mit Verbesserungen (S. 390).
   */
  public @Nullable String getLimbBaseProblem(@NonNull LimbAttributes base) {
    for (AttributeType type : LIMB_ATTRIBUTES) {
      int max = owner.getAttribute(type).getMax();
      if (base.get(type) > max) {
        return "%s höchstens %d (natürliches Maximum)".formatted(type.getAbbreviation(), max);
      }
    }
    return null;
  }

  /** Ob an {@code location} schon ein angeschlossenes Glied sitzt (ein neues kommt dann abgenommen dazu). */
  public boolean isOccupied(@NonNull BodyLocation location) {
    return getAttachedLimbs().stream().anyMatch(limb -> limb.getLocation() == location);
  }

  /**
   * Warum {@code child} in Stufe {@code rating} nicht in {@code limb} eingebaut werden kann, oder {@code null}: erst
   * {@link Cyberware#installProblem}, dann die Regeln, die den Charakter brauchen: Head- und Bodyware nicht doppelt
   * und nicht neben Unverträglichem ({@link #getBlockReason}), Verbesserungen über Stufe 3 nur mit angeschlossenem
   * Cybertorso, Gliedwert höchstens das augmentierte Maximum (S. 390); die Sprunghydraulik braucht ein zweites
   * Cyberbein mit freier Kapazität (S. 391).
   */
  public @Nullable String getLimbInstallProblem(@NonNull Cyberware limb, @NonNull CyberwareData child, int rating) {
    String problem = limb.installProblem(child, rating);
    if (problem == null && !child.restrictions().installOnly()) {
      problem = conflictReason(child);
    }
    if (problem != null) {
      return problem;
    }
    LimbEnhancement enhancement = child.limb().enhancement();
    if (enhancement != null && rating > MAX_ENHANCEMENT_WITHOUT_TORSO && !hasAttachedTorso()) {
      return "Über Stufe " + MAX_ENHANCEMENT_WITHOUT_TORSO + " nur mit angeschlossenem Cybertorso";
    }
    AttributeType type = enhancement == null ? null : enhancement.attribute();
    if (type != null) {
      int max = owner.getAugmentedMaximum(type);
      if (limb.getBaseAttributes().get(type) + rating > max) {
        return "%s des Glieds höchstens %d (augmentiertes Maximum)".formatted(type.getAbbreviation(), max);
      }
    }
    if (child.limb().pairedLegs()) {
      Cyberware partner = pairedLegPartner(limb);
      if (partner == null) {
        return "Braucht ein zweites Cyberbein";
      }
      String partnerProblem = partner.installProblem(child, rating);
      if (partnerProblem != null) {
        return "Im zweiten Bein: " + partnerProblem;
      }
    }
    return null;
  }

  /**
   * Baut {@code child} in {@code limb} ein; eine Sprunghydraulik zugleich in das zweite Cyberbein, dort ohne eigenen
   * Preis (S. 391: Preis für beide Beine zusammen).
   *
   * @throws IllegalArgumentException wenn {@link #getLimbInstallProblem} einen Grund liefert
   */
  public void installInLimb(@NonNull Cyberware limb, @NonNull Cyberware child) {
    String problem = getLimbInstallProblem(limb, child.getData(), child.getRating());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    limb.install(child);
    Cyberware partner = child.getData().limb().pairedLegs() ? pairedLegPartner(limb) : null;
    if (partner != null) {
      partner.install(child.pairedCopy());
    }
  }

  /** Baut {@code child} aus {@code limb} aus; eine Sprunghydraulik verschwindet samt ihrem Gegenstück. */
  public void uninstallFromLimb(@NonNull Cyberware limb, @NonNull Cyberware child) {
    limb.uninstall(child);
    Cyberware counterpart = child.getPairedPart();
    if (counterpart != null) {
      getLimbs().forEach(other -> other.uninstall(counterpart));
    }
  }

  /** Entfernt ein Implantat; eine Sprunghydraulik in einem entfernten Bein verschwindet auch aus dem anderen. */
  public void remove(@NonNull Cyberware implant) {
    implant.getInstalled().stream()
        .filter(c -> c.getData().limb().pairedLegs())
        .toList()
        .forEach(c -> uninstallFromLimb(implant, c));
    owner.getEquipment().removeCyberware(implant);
  }

  /**
   * Das andere Cyberbein für eine Sprunghydraulik (ganzes Bein oder Unterschenkel auf der anderen Seite, angeschlossene
   * zuerst), oder {@code null}.
   */
  private @Nullable Cyberware pairedLegPartner(@NonNull Cyberware leg) {
    BodyLocation side = leg.getLocation();
    if (side == null || !side.isLeg()) {
      return null;
    }
    return getLimbs().stream()
        .filter(c -> c.getLocation() != null && c.getLocation().isLeg() && c.getLocation() != side)
        .filter(c -> Objects.requireNonNull(c.getData().limb().part()).isLeg())
        .min(Comparator.comparing(c -> !c.isAttached()))
        .orElse(null);
  }

  private boolean hasAttachedTorso() {
    return getAttachedLimbs().stream().anyMatch(c -> c.getData().limb().part() == LimbPart.TORSO);
  }

  /**
   * Stellen, an denen mehr als ein Glied angeschlossen ist (z.B. ganzer Arm und Hand links). Keine Sperre, nur ein
   * Hinweis: Spätere Regelwerke (Surge) erlauben zusätzliche Gliedmaßen.
   */
  public @NonNull List<BodyLocation> getOverOccupiedLocations() {
    Map<BodyLocation, Long> counts = getAttachedLimbs().stream()
        .collect(Collectors.groupingBy(c -> Objects.requireNonNull(c.getLocation()),
            () -> new EnumMap<>(BodyLocation.class), Collectors.counting()));
    return counts.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
  }

  /**
   * Durchschnitt von KON, STR und GES über die fünf Körperteile (S. 389/390): je Teil das angeschlossene ganze Glied
   * bzw. der Torso, sonst das Charakterattribut.
   */
  public @NonNull LimbAttributes getLimbAverages() {
    return new LimbAttributes(average(AttributeType.BOD), average(AttributeType.STR), average(AttributeType.AGI));
  }

  /**
   * Gliedpanzerung für ballistisch und Stoß: Summe der Panzerungsverbesserungen aller angeschlossenen Glieder, wie im
   * C#-Original (Nutzerentscheidung; das GRW sagt nur, dass sie sich mit getragener Panzerung addiert, S. 390).
   * Einzige Stelle dieser Regel, damit eine spätere Einstellung (Durchschnitt über die fünf Körperteile) nur hier
   * eingreift.
   */
  public int getLimbArmor() {
    return getAttachedLimbs().stream().mapToInt(Cyberware::getLimbArmor).sum();
  }

  private int average(@NonNull AttributeType type) {
    int natural = owner.getAugmentedAttributeValue(type);
    return AVERAGE_PARTS.stream()
        .mapToInt(location -> averagePart(location).map(c -> c.getLimbAttribute(type)).orElse(natural))
        .sum() / AVERAGE_PARTS.size();
  }

  /** Angeschlossenes ganzes Glied bzw. Torso an {@code location}, das in den Durchschnitt eingeht. */
  private @NonNull Optional<Cyberware> averagePart(@NonNull BodyLocation location) {
    return getAttachedLimbs().stream()
        .filter(c -> c.getLocation() == location && Objects.requireNonNull(c.getData().limb().part()).countsForAverage())
        .findFirst();
  }

  // --- Wirkungen ---

  /**
   * Höchster Wert einer Wirkung über alle Implantate samt Einbauten in Cybergliedmaßen, z.B. ob ein Direktes
   * Neuralinterface vorhanden ist oder welches Sim-Modul implantiert ist (GRW S. 258, 386).
   */
  public int maxLevel(@NonNull ImplantEffectType type) {
    return implantsWithLimbParts().stream().mapToInt(c -> c.getData().effectValue(type, null, c.getRating())).max()
        .orElse(0);
  }

  /** Eingebaute Kommlinks der Kommlink-Headware, auch in Cybergliedmaßen (S. 386). */
  public @NonNull List<Gear> getCommlinks() {
    return implantsWithLimbParts().stream().flatMap(c -> Stream.ofNullable(c.getCommlink())).toList();
  }

  /** Summe einer Wirkung über alle Implantate, unabhängig vom Ziel. */
  public int level(@NonNull ImplantEffectType type) {
    return implants().stream().mapToInt(c -> c.getData().effectValue(type, null, c.getRating())).sum();
  }

  /** Summe einer Wirkung mit diesem Ziel (Attributkürzel, Fertigkeitsgruppe). */
  public int level(@NonNull ImplantEffectType type, @NonNull String target) {
    return implants().stream().mapToInt(c -> c.getData().effectValue(type, target, c.getRating())).sum();
  }

  /**
   * Würfel aus Reflexrekordern für eine Fertigkeit: gewählt als Fertigkeit selbst oder als ihre Gruppe (S. 394). Je
   * Fertigkeit zählt nur einer; der höchste Wert gilt, falls eine Speicherdatei doch zwei enthält.
   */
  public int selectedSkillDice(@NonNull Skill skill) {
    return implants().stream()
        .filter(c -> c.getChoice().equals(skill.getName()) || c.getChoice().equals(skill.getGroupName()))
        .mapToInt(c -> c.getData().effectValue(ImplantEffectType.SELECTED_SKILL_DICE, null, c.getRating()))
        .max()
        .orElse(0);
  }
}
