package de.shalorian.chummer.model.character.cyberware;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPowerEffect;
import de.shalorian.chummer.model.character.skill.Skill;
import de.shalorian.chummer.model.character.skill.SkillCategory;
import java.util.List;
import java.util.Objects;
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

  private final @NonNull Character owner;

  /** Implantatregeln für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Implants(@NonNull Character owner) {
    this.owner = owner;
  }

  private @NonNull List<Cyberware> implants() {
    return owner.getEquipment().getCyberware();
  }

  // --- Kaufprüfung ---

  /**
   * Warum {@code data} grundsätzlich nicht implantiert werden kann, oder {@code null}. Einzige Prüfung für Modell,
   * Dialog und Tab:
   * <ul>
   *   <li>nur als Einbau in ein Basissystem erhältlich (Okular-Drohne, S. 386)</li>
   *   <li>schon vorhanden und nicht {@link ImplantRestrictions#multiple()}</li>
   *   <li>Voraussetzung fehlt ({@link ImplantRestrictions#requires()})</li>
   *   <li>Unverträglichkeit mit einem vorhandenen Implantat, in beide Richtungen (S. 388, 389, 393, 394)</li>
   *   <li>Basissystem und einzeln implantiertes Zubehör derselben Kategorie schließen sich aus: Zubehör kostet im
   *       Cyberauge/-ohr Kapazität oder im natürlichen Organ Essenz, nicht beides (S. 386, 387)</li>
   *   <li>technische Initiativesteigerung neben der Adeptenkraft Gesteigerte Reflexe (S. 230)</li>
   * </ul>
   */
  public @Nullable String getBlockReason(@NonNull CyberwareData data) {
    if (data.restrictions().installOnly()) {
      return "Nur in ein Basissystem einbaubar";
    }
    if (!data.restrictions().multiple() && implants().stream().anyMatch(c -> c.getData().equals(data))) {
      return "Bereits eingebaut";
    }
    String requires = data.restrictions().requires();
    if (!requires.isEmpty() && implants().stream().noneMatch(c -> c.getName().equals(requires))) {
      return "Nur zusammen mit " + requires;
    }
    for (Cyberware other : implants()) {
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
    return null;
  }

  /**
   * Warum {@code data} mit dieser Auswahl nicht implantiert werden kann, oder {@code null}: erst {@link
   * #getBlockReason}, dann die Auswahl beim Reflexrekorder (Kampf- oder körperliche Fertigkeit bzw. -gruppe, je
   * Fertigkeit oder Gruppe nur einer, S. 394).
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
    return null;
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

  private static boolean excludes(@NonNull CyberwareData data, @NonNull CyberwareData other) {
    return data.restrictions().excludes().contains(other.name())
        || data.restrictions().excludedCategories().contains(other.category());
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

  // --- Wirkungen ---

  /** Summe einer Wirkung über alle Implantate, unabhängig vom Ziel. */
  public int level(@NonNull ImplantEffectType type) {
    return implants().stream().mapToInt(c -> c.getData().effectValue(type, null, c.getRating())).sum();
  }

  /** Summe einer Wirkung mit diesem Ziel (Attributkürzel, Fertigkeitsgruppe). */
  public int level(@NonNull ImplantEffectType type, @NonNull String target) {
    return implants().stream().mapToInt(c -> c.getData().effectValue(type, target, c.getRating())).sum();
  }

  /**
   * Würfel aus Reflexrekordern für eine Fertigkeit: gewählt als Fertigkeit selbst oder als ihre Gruppe (S. 394).
   */
  public int selectedSkillDice(@NonNull Skill skill) {
    return implants().stream()
        .filter(c -> c.getChoice().equals(skill.getName()) || c.getChoice().equals(skill.getGroupName()))
        .mapToInt(c -> c.getData().effectValue(ImplantEffectType.SELECTED_SKILL_DICE, null, c.getRating()))
        .sum();
  }
}
