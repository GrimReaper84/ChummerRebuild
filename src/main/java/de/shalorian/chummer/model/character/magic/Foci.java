package de.shalorian.chummer.model.character.magic;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.gear.Gear;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Foki des Charakters: Bindung und Aktivierung (Teilobjekt von {@link Character}, Vorbild {@code Spirits}).
 *
 * <ul>
 *   <li>Nur Erwachte binden Foki; höchstens so viele wie die Magie, zusammen höchstens Kraft 5 × Magie (GRW S. 233).</li>
 *   <li>Erschaffung: 1 BP je Kraftpunkt (S. 112, 123); Karriere: Kraft × Faktor der Fokusart Karma (Tabelle S. 234).</li>
 *   <li>Aktiv höchstens Logik Foki zugleich (S. 234); nur aktive Foki geben Würfel.</li>
 *   <li>Lösen geht immer; bei der Erschaffung werden die BP wieder frei, gezahltes Karma kommt nicht zurück.</li>
 * </ul>
 * Gezählt werden nur Foki der Ausrüstung des Charakters, nicht Gegenstände in Fahrzeugen.
 */
public class Foci {

  /** Höchste Gesamtkraft gebundener Foki je Magiepunkt (S. 233). */
  public static final int FORCE_PER_MAGIC = 5;

  private final @NonNull Character owner;

  /** Foki für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Foci(@NonNull Character owner) {
    this.owner = owner;
  }

  /** Alle gekauften Foki in Kaufreihenfolge. */
  public @NonNull List<Gear> getAll() {
    return owner.getEquipment().getGear().stream().filter(Gear::isFocus).toList();
  }

  public @NonNull List<Gear> getBound() {
    return getAll().stream().filter(Gear::isBound).toList();
  }

  public @NonNull List<Gear> getActive() {
    return getAll().stream().filter(Gear::isFocusActive).toList();
  }

  /** Gesamtkraft der gebundenen Foki. */
  public int getBoundForce() {
    return getBound().stream().mapToInt(Gear::getRating).sum();
  }

  /** Wirksame Magie, an der die Grenzen hängen. */
  public int getMagic() {
    return owner.hasMagic() ? owner.getEffectiveMagicOrResonanceValue() : 0;
  }

  /** Warum {@code focus} nicht gebunden werden kann, oder {@code null}. */
  public @Nullable String getBindProblem(@NonNull Gear focus) {
    if (!focus.isFocus()) {
      return focus.getName() + " ist kein Fokus";
    }
    if (!owner.hasMagic()) {
      return "Nur Erwachte binden Foki";
    }
    if (focus.isBound()) {
      return focus.getName() + " ist schon gebunden";
    }
    int magic = getMagic();
    if (getBound().size() >= magic) {
      return "Höchstens %d gebundene Foki (Magie)".formatted(magic);
    }
    if (getBoundForce() + focus.getRating() > FORCE_PER_MAGIC * magic) {
      return "Gesamtkraft gebundener Foki höchstens %d (5 × Magie)".formatted(FORCE_PER_MAGIC * magic);
    }
    return null;
  }

  /** Karma für die Bindung im Karrieremodus: Kraft × Faktor der Fokusart (S. 234). */
  public int getBindingKarma(@NonNull Gear focus) {
    return Objects.requireNonNull(focus.getData().focusType()).bindingKarma(focus.getRating());
  }

  /**
   * Bindet {@code focus}; die Kosten (BP über {@link #getBpCost()}, Karma im Karrieremodus) bucht der Aufrufer.
   *
   * @throws IllegalArgumentException wenn {@link #getBindProblem} einen Grund liefert
   */
  public void bind(@NonNull Gear focus) {
    String problem = getBindProblem(focus);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    focus.setFocusState(true, false);
  }

  /**
   * Löst die Bindung (und damit die Aktivierung). Bei der Erschaffung sinken die BP über {@link #getBpCost()}; im
   * Karrieremodus wird kein Karma erstattet.
   */
  public void unbind(@NonNull Gear focus) {
    focus.setFocusState(false, false);
  }

  /** Warum {@code focus} nicht aktiviert werden kann, oder {@code null}; Deaktivieren geht immer. */
  public @Nullable String getActivateProblem(@NonNull Gear focus) {
    if (!focus.isBound()) {
      return "Erst binden";
    }
    int logic = owner.getAugmentedAttributeValue(AttributeType.LOG);
    if (!focus.isFocusActive() && getActive().size() >= logic) {
      return "Höchstens %d aktive Foki (Logik)".formatted(logic);
    }
    return null;
  }

  /**
   * Aktiviert bzw. deaktiviert {@code focus}.
   *
   * @throws IllegalArgumentException wenn das Aktivieren laut {@link #getActivateProblem} nicht geht
   */
  public void setActive(@NonNull Gear focus, boolean active) {
    if (active) {
      String problem = getActivateProblem(focus);
      if (problem != null) {
        throw new IllegalArgumentException(problem);
      }
    }
    focus.setFocusState(focus.isBound(), active);
  }

  /** BP bei der Erschaffung: 1 je Kraftpunkt gebundener Foki (S. 112, 123); danach 0. */
  public int getBpCost() {
    return owner.isCreated() ? 0 : getBoundForce();
  }

  /**
   * Hinweise, wenn gebundene oder aktive Foki nach späteren Änderungen (Magie, Logik) über den Grenzen liegen, sonst
   * leer. Nichts wird automatisch gelöst.
   */
  public @NonNull List<String> getProblems() {
    List<String> problems = new ArrayList<>();
    int magic = getMagic();
    List<Gear> bound = getBound();
    if (!bound.isEmpty() && bound.size() > magic) {
      problems.add("%d gebundene Foki, Magie erlaubt %d".formatted(bound.size(), magic));
    }
    if (getBoundForce() > FORCE_PER_MAGIC * magic) {
      problems.add("Gesamtkraft gebundener Foki %d, erlaubt %d (5 × Magie)".formatted(getBoundForce(),
          FORCE_PER_MAGIC * magic));
    }
    int logic = owner.getAugmentedAttributeValue(AttributeType.LOG);
    if (getActive().size() > logic) {
      problems.add("%d aktive Foki, Logik erlaubt %d".formatted(getActive().size(), logic));
    }
    return problems;
  }
}
