package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearData;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Bestand aus abgeschalteten Regelwerken bleibt und wird gemeldet (ADR-061). */
class CharacterRulebookTest {

  private static SpellData spellFrom(String source) {
    return new SpellData("Testzauber", "", "Kampf", "M", "BF", "K", "S", "(K/2)+1", source, "1", "");
  }

  @Test
  void coreContentIsNeverReported() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addSpell(SpellDataLoader.getAllSpells().values().iterator().next());
    assertEquals(Set.of(), c.getSourcesFromDisabledBooks());
  }

  @Test
  void contentFromDisabledBookIsReportedUntilEnabled() {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    c.addSpell(spellFrom("AR"));
    assertEquals(Set.of("AR"), c.getSourcesFromDisabledBooks());
    c.getRuleOptions().setBookEnabled("AR", true);
    assertEquals(Set.of(), c.getSourcesFromDisabledBooks());
  }

  @Test
  void commlinkInImplantIsReported() throws ReflectiveOperationException {
    Character c = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Cyberware headware = new Cyberware(Objects.requireNonNull(CyberwareDataLoader.findByName("Kommlink")),
        CyberwareGrade.STANDARD, 0);
    headware.setCommlink(new Gear(withSource(Objects.requireNonNull(GearDataLoader.findByName("Meta Link")), "AR"), 0));
    c.getImplants().add(headware);
    assertEquals(Set.of("AR"), c.getSourcesFromDisabledBooks());
  }

  /** Kopie eines Katalog-Records mit anderer Quelle (die Records haben dafür keinen eigenen Weg). */
  private static <R extends Record> R withSource(R original, String source) throws ReflectiveOperationException {
    RecordComponent[] components = original.getClass().getRecordComponents();
    Object[] values = new Object[components.length];
    for (int i = 0; i < components.length; i++) {
      values[i] = components[i].getName().equals("source") ? source : components[i].getAccessor().invoke(original);
    }
    Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
    @SuppressWarnings("unchecked")
    R copy = (R) original.getClass().getDeclaredConstructor(types).newInstance(values);
    return copy;
  }
}
