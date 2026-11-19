package de.shalorian.chummer.view.shell;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Bereiche des Charakterbogens in der Reihenfolge der Seitenleiste. Der Status steht nur dort, wenn er statisch
 * angezeigt wird (Einstellungen), sonst liegt er im Slide-Panel.
 */
public enum CharacterSection {
  STATUS("Status", SectionGroup.CHARACTER),
  BIOGRAPHY("Biografie", SectionGroup.CHARACTER),
  ATTRIBUTES("Attribute", SectionGroup.CHARACTER),
  SKILLS("Fertigkeiten", SectionGroup.CHARACTER),
  QUALITIES("Gaben & Handicaps", SectionGroup.CHARACTER),
  CHEAT_SHEET("Cheat-Sheet", SectionGroup.CHARACTER),
  MAGIC("Magie", SectionGroup.MAGIC_RESONANCE),
  ADEPT("Adeptenkräfte", SectionGroup.MAGIC_RESONANCE),
  TECHNOMANCER("Technomancer", SectionGroup.MAGIC_RESONANCE),
  /** Initiation bei Magie, Wandlung bei Resonanz; der Titel folgt dem Charakter ({@code SectionNavigator#setTitle}). */
  INITIATION("Initiation", SectionGroup.MAGIC_RESONANCE),
  IMPLANTS("Cyber- & Bioware", SectionGroup.EQUIPMENT),
  LIFESTYLE("Lebensstil", SectionGroup.EQUIPMENT),
  ARMOR("Rüstung", SectionGroup.EQUIPMENT),
  WEAPONS("Waffen", SectionGroup.EQUIPMENT),
  GEAR("Ausrüstung", SectionGroup.EQUIPMENT),
  VEHICLES("Fahrzeuge & Drohnen", SectionGroup.EQUIPMENT),
  CONTACTS("Kontakte", SectionGroup.NETWORK),
  LOG("Protokoll", SectionGroup.NETWORK);

  /** Bereich, der beim Öffnen gewählt ist und auf den ausgewichen wird, wenn der gewählte wegfällt. */
  public static final CharacterSection DEFAULT = ATTRIBUTES;

  private final @NonNull String title;
  private final @NonNull SectionGroup group;

  CharacterSection(@NonNull String title, @NonNull SectionGroup group) {
    this.title = title;
    this.group = group;
  }

  public @NonNull String getTitle() {
    return title;
  }

  public @NonNull SectionGroup getGroup() {
    return group;
  }

  /** Die Bereiche einer Gruppe in Leistenreihenfolge. */
  public static @NonNull List<CharacterSection> of(@NonNull SectionGroup group) {
    return Arrays.stream(values()).filter(s -> s.group == group).toList();
  }
}
