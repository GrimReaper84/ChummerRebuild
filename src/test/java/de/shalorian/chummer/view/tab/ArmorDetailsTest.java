package de.shalorian.chummer.view.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorDataLoader;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModDataLoader;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Anzeigewerte eines Rüstungsteils im Detailbereich. */
class ArmorDetailsTest {

  private static Armor armor(String name) {
    return new Armor(Objects.requireNonNull(ArmorDataLoader.findByName(name), name));
  }

  private static ArmorMod mod(String name, int rating) {
    return new ArmorMod(Objects.requireNonNull(ArmorModDataLoader.findByName(name), name), rating);
  }

  @Test
  void protectionIncludesAccessoryAndSpecialProtection() {
    Armor jacket = armor("Panzerjacke");
    jacket.install(mod("Elektrische Isolierung", 3));
    jacket.install(mod("Wärmedämpfung", 2));
    ArmorDetails d = ArmorDetails.of(jacket);
    assertEquals("8/6", d.protection());
    assertEquals(List.of(new SpecialProtection("gegen Elektrizität", "11/9"),
        new SpecialProtection("Infiltration gegen IR", "+2 Würfel")), d.special());
    assertEquals((900 + 600 + 1000) + " ¥", d.price());
    assertEquals("angelegt", d.state());
  }

  @Test
  void chemicalInsulationGivesToxinDiceNotArmor() {
    // Tabelle „Schutz vor Chemikalien“: Würfel auf die Toxinwiderstandsprobe; gegen Säure zur Panzerung (S. 194)
    Armor jacket = armor("Panzerjacke");
    jacket.install(mod("Chemische Isolierung", 3));
    assertEquals(List.of(new SpecialProtection("Toxinwiderstand (Kontakt)", "+3 Würfel, gegen Säure +3 Panzerung")),
        ArmorDetails.of(jacket).special());
  }

  @Test
  void chemicalInsulationOnFullBodyArmorAlsoCoversInhalation() {
    Armor fullBody = armor("Ganzkörperpanzerung");
    fullBody.install(mod("Chemische Isolierung", 2));
    assertEquals("Toxinwiderstand (Kontakt, Inhalation)", ArmorDetails.of(fullBody).special().getFirst().label());
  }

  @Test
  void shieldSpecialProtectionShowsBonusPrefix() {
    Armor shield = armor("Ballistischer Schild");
    shield.install(mod("Feuerresistenz", 2));
    assertEquals(List.of(new SpecialProtection("gegen Feuer", "+%d/+%d".formatted(
        shield.getBallistic() + 2, shield.getImpact() + 2))), ArmorDetails.of(shield).special());
  }

  @Test
  void shieldShowsBonusAndPenaltyHint() {
    Armor shield = armor("Schutzschild");
    shield.setEquipped(false);
    ArmorDetails d = ArmorDetails.of(shield);
    assertEquals("+2/+6", d.protection());
    assertEquals("Helme und Schilde (Schild: −1 auf körperliche Proben)", d.category());
    assertEquals("abgelegt", d.state());
  }

  @Test
  void fullBodyHelmetRaisesProtection() {
    Armor fullBody = armor("Ganzkörperpanzerung");
    fullBody.install(mod("Helm (Ganzkörperpanzerung)", 0));
    assertEquals("12/10", ArmorDetails.of(fullBody).protection());
  }
}
