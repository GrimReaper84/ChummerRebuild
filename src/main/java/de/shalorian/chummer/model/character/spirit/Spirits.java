package de.shalorian.chummer.model.character.spirit;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.skill.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Geister und Sprites eines Charakters (Teilobjekt von {@link Character}, Vorbild {@code AdeptPowers}).
 *
 * <ul>
 *   <li>Geister nur für Zauberer und Magieradepten, und nur die fünf Arten der eigenen Tradition (GRW S. 213); Sprites
 *       nur für Technomancer, alle fünf Arten (S. 282).</li>
 *   <li>Erschaffung (S. 112/113): Kraft bzw. Stufe = wirksame Magie bzw. Resonanz; ein gebundener Geist bzw.
 *       registriertes Sprite schuldet 1 bis Herbeirufen- bzw. Kompilieren-Stufe Dienste, jeder Dienst kostet 1 BP.</li>
 *   <li>Höchstens Charisma gebundene Geister bzw. registrierte Sprites (S. 112, 222, 281); ungebundene zählen nicht.</li>
 *   <li>Im Spiel wird nur eingetragen: keine Kosten, Dienste frei (Verbrauch, neuerliches Binden bzw. Registrieren).</li>
 * </ul>
 */
public class Spirits {

  private final @NonNull Character owner;
  private final List<Spirit> entries = new ArrayList<>();

  /** Geister und Sprites für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Spirits(@NonNull Character owner) {
    this.owner = owner;
  }

  /** Ob der Charakter Einträge dieser Art haben kann: Geister mit Zauberei, Sprites mit Resonanz. */
  public boolean isAvailable(@NonNull SpiritKind kind) {
    return kind == SpiritKind.SPIRIT ? owner.isMagician() : owner.hasResonance();
  }

  /** Einträge einer Art in Reihenfolge des Hinzufügens. */
  public @NonNull List<Spirit> getEntries(@NonNull SpiritKind kind) {
    return entries.stream().filter(s -> s.getData().kind() == kind).toList();
  }

  /** Alle Einträge (Persistenz). */
  public @NonNull List<Spirit> getAll() {
    return Collections.unmodifiableList(entries);
  }

  /** Wählbare Arten: Geister der eigenen Tradition bzw. alle Sprites. */
  public @NonNull List<SpiritData> getTypes(@NonNull SpiritKind kind) {
    return SpiritDataLoader.getAll().values().stream()
        .filter(d -> d.kind() == kind
            && (kind == SpiritKind.SPRITE || d.categoryIn(owner.getTradition()) != null))
        .toList();
  }

  /** Kraft bzw. Stufe bei der Erschaffung: wirksame Magie bzw. Resonanz (S. 112/113). */
  public int getCreationForce() {
    return Math.max(1, owner.getEffectiveMagicOrResonanceValue());
  }

  /** Höchste Zahl Dienste bei der Erschaffung: Stufe von Herbeirufen bzw. Kompilieren (S. 112/113). */
  public int getCreationMaxServices(@NonNull SpiritKind kind) {
    Skill skill = owner.getSkill(kind.getSkillName());
    return skill == null ? 0 : skill.getRating();
  }

  /** Höchste Zahl gebundener Geister bzw. registrierter Sprites: Charisma (S. 112, 222, 281). */
  public int getBoundLimit() {
    return owner.getAugmentedAttributeValue(AttributeType.CHA);
  }

  /** Zahl der gebundenen Geister bzw. registrierten Sprites. */
  public int countBound(@NonNull SpiritKind kind) {
    return (int) getEntries(kind).stream().filter(Spirit::isBound).count();
  }

  /** Warum dieser Eintrag nicht hinzugefügt werden kann, oder {@code null}. */
  public @Nullable String getAddProblem(@NonNull SpiritData data, int force, int services, boolean bound) {
    SpiritKind kind = data.kind();
    if (!isAvailable(kind)) {
      return kind.getUnavailableText();
    }
    if (kind == SpiritKind.SPIRIT && data.categoryIn(owner.getTradition()) == null) {
      return "Die Tradition " + owner.getTradition().getDisplayName() + " ruft keinen " + data.name();
    }
    if (force < 1 || services < 0) {
      return kind.getPowerName() + " mindestens 1, Dienste nicht negativ";
    }
    String forceProblem = creationForceProblem(kind, force);
    if (forceProblem != null) {
      return forceProblem;
    }
    if (bound && countBound(kind) >= getBoundLimit()) {
      return boundLimitText(kind);
    }
    return bound ? creationServicesProblem(kind, services) : null;
  }

  /**
   * Fügt {@code spirit} hinzu.
   *
   * @throws IllegalArgumentException wenn {@link #getAddProblem} einen Grund liefert
   */
  public void add(@NonNull Spirit spirit) {
    String problem = getAddProblem(spirit.getData(), spirit.getForce(), spirit.getServices(), spirit.isBound());
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    entries.add(spirit);
  }

  /** Entfernt genau diesen Eintrag. */
  public void remove(@NonNull Spirit spirit) {
    entries.remove(spirit);
  }

  /** Warum {@code spirit} nicht auf {@code services} Dienste gesetzt werden kann, oder {@code null}. */
  public @Nullable String getServicesProblem(@NonNull Spirit spirit, int services) {
    if (services < 0) {
      return "Dienste nicht negativ";
    }
    return spirit.isBound() ? creationServicesProblem(spirit.getData().kind(), services) : null;
  }

  /**
   * Setzt die Dienste.
   *
   * @throws IllegalArgumentException wenn {@link #getServicesProblem} einen Grund liefert
   */
  public void setServices(@NonNull Spirit spirit, int services) {
    String problem = getServicesProblem(spirit, services);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    spirit.setServices(services);
  }

  /** Warum {@code spirit} nicht gebunden bzw. registriert werden kann, oder {@code null}; Lösen geht immer. */
  public @Nullable String getBindProblem(@NonNull Spirit spirit) {
    SpiritKind kind = spirit.getData().kind();
    if (spirit.isBound()) {
      return null;
    }
    if (countBound(kind) >= getBoundLimit()) {
      return boundLimitText(kind);
    }
    return creationServicesProblem(kind, spirit.getServices());
  }

  /**
   * Bindet bzw. registriert {@code spirit} oder löst ihn.
   *
   * @throws IllegalArgumentException wenn das Binden laut {@link #getBindProblem} nicht geht
   */
  public void setBound(@NonNull Spirit spirit, boolean bound) {
    if (bound) {
      String problem = getBindProblem(spirit);
      if (problem != null) {
        throw new IllegalArgumentException(problem);
      }
    }
    spirit.setBound(bound);
  }

  /** BP bei der Erschaffung: 1 je Dienst gebundener Geister und registrierter Sprites (S. 112/113); danach 0. */
  public int getBpCost() {
    if (owner.isCreated()) {
      return 0;
    }
    return entries.stream().filter(Spirit::isBound).mapToInt(Spirit::getServices).sum();
  }

  /**
   * Hinweise auf Einträge einer Art, die nach späteren Änderungen (Charisma, Magie bzw. Resonanz, Essenz, Herbeirufen
   * bzw. Kompilieren) nicht mehr den Regeln entsprechen, sonst leer: mehr gebundene bzw. registrierte als Charisma
   * (S. 112, 222, 281), bei der Erschaffung außerdem Kraft bzw. Stufe ungleich Magie bzw. Resonanz und Dienste
   * außerhalb 1 bis Herbeirufen- bzw. Kompilieren-Stufe (S. 112/113). Nur Hinweis, es wird nichts geändert.
   */
  public @NonNull List<String> getProblems(@NonNull SpiritKind kind) {
    List<String> problems = new ArrayList<>();
    int bound = countBound(kind);
    if (bound > getBoundLimit()) {
      problems.add(boundLimitText(kind) + ", eingetragen sind " + bound);
    }
    for (Spirit spirit : getEntries(kind)) {
      String forceProblem = creationForceProblem(kind, spirit.getForce());
      if (forceProblem != null) {
        problems.add(spirit.getName() + ": " + forceProblem);
      }
      String servicesProblem = spirit.isBound() ? creationServicesProblem(kind, spirit.getServices()) : null;
      if (servicesProblem != null) {
        problems.add(spirit.getName() + ": " + servicesProblem);
      }
    }
    return problems;
  }

  /** Entfernt Einträge, deren Art der Charakter nicht mehr haben kann (Gabe entfernt). */
  public void syncWithGifts() {
    entries.removeIf(s -> !isAvailable(s.getData().kind()));
  }

  /** Stellt gespeicherte Einträge ohne Grenzprüfung wieder her (Ladepfad); unpassende Arten bleiben draußen. */
  public void restore(@NonNull List<Spirit> saved) {
    entries.clear();
    saved.stream().filter(s -> isAvailable(s.getData().kind())).forEach(entries::add);
  }

  /** Bei der Erschaffung: Kraft bzw. Stufe = wirksame Magie bzw. Resonanz (S. 112/113). */
  private @Nullable String creationForceProblem(@NonNull SpiritKind kind, int force) {
    if (owner.isCreated() || force == getCreationForce()) {
      return null;
    }
    return "%s bei der Erschaffung = %s (%d)".formatted(kind.getPowerName(), kind.getAttributeName(),
        getCreationForce());
  }

  /** Bei der Erschaffung: gebundene schulden 1 bis Herbeirufen- bzw. Kompilieren-Stufe Dienste. */
  private @Nullable String creationServicesProblem(@NonNull SpiritKind kind, int services) {
    if (owner.isCreated()) {
      return null;
    }
    int max = getCreationMaxServices(kind);
    if (max == 0) {
      return "Erst " + kind.getSkillName() + " steigern";
    }
    if (services < 1 || services > max) {
      return "Bei der Erschaffung 1 bis %d Dienste (%s %d)".formatted(max, kind.getSkillName(), max);
    }
    return null;
  }

  private @NonNull String boundLimitText(@NonNull SpiritKind kind) {
    return "Höchstens %d %s %s (Charisma)".formatted(getBoundLimit(), kind.getPluralName(), kind.getBoundName());
  }
}
