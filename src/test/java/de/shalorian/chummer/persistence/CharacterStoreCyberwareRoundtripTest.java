package de.shalorian.chummer.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.cyberware.BodyLocation;
import de.shalorian.chummer.model.character.cyberware.Cyberware;
import de.shalorian.chummer.model.character.cyberware.CyberwareData;
import de.shalorian.chummer.model.character.cyberware.CyberwareDataLoader;
import de.shalorian.chummer.model.character.cyberware.CyberwareGrade;
import de.shalorian.chummer.model.character.cyberware.EquipmentKind;
import de.shalorian.chummer.model.character.cyberware.LimbAttributes;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.gear.GearDataLoader;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CharacterStoreCyberwareRoundtripTest {

  @TempDir
  File tempDir;

  private File tempFile() {
    return new File(tempDir, "charakter.json");
  }

  private static CyberwareData catalog(String name) {
    CyberwareData data = CyberwareDataLoader.findByName(name);
    assertNotNull(data);
    return data;
  }

  @Test
  void roundtripPreservesImplantsWithGradeAndRating() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Datenbuchse"), CyberwareGrade.ALPHAWARE, 0));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Dermalpanzerung"), CyberwareGrade.STANDARD, 2));
    original.getEquipment()
        .addCyberware(new Cyberware(catalog("Toxinextraktor"), CyberwareGrade.DELTAWARE, 4));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Cyberware> implants = loaded.getEquipment().getCyberware();
    assertEquals(3, implants.size());
    assertEquals("Datenbuchse", implants.getFirst().getName());
    assertEquals(CyberwareGrade.ALPHAWARE, implants.getFirst().getGrade());
    assertEquals(0, implants.getFirst().getRating());
    assertEquals("Dermalpanzerung", implants.get(1).getName());
    assertEquals(2, implants.get(1).getRating());
    assertEquals(EquipmentKind.BIOWARE, implants.get(2).getKind());
    assertEquals(CyberwareGrade.DELTAWARE, implants.get(2).getGrade());
    assertEquals(4, implants.get(2).getRating());
    assertEquals(original.getEssence(), loaded.getEssence());
  }

  @Test
  void fileWithoutCyberwareNodeLoadsWithoutImplants() throws IOException {
    Files.writeString(tempFile().toPath(), "{\"name\":\"Alt\",\"metatype\":\"Mensch\"}",
        StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertTrue(loaded.getEquipment().getCyberware().isEmpty());
    assertEquals(6.0, loaded.getEssence());
  }

  @Test
  void unknownImplantNameIsSkipped() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "cyberware":[{"name":"Gibt es nicht","grade":"STANDARD","rating":0},
                      {"name":"Datenbuchse","grade":"UNBEKANNT","rating":0}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(1, loaded.getEquipment().getCyberware().size());
    assertEquals(CyberwareGrade.STANDARD, loaded.getEquipment().getCyberware().getFirst().getGrade());
  }

  @Test
  void roundtripPreservesChoicePriceAccessoriesAndCommlink() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Cyberware eyes = new Cyberware(catalog("Cyberaugen-Basissystem"), CyberwareGrade.ALPHAWARE, 3);
    eyes.install(new Cyberware(catalog("Smartlink"), CyberwareGrade.STANDARD, 0));
    eyes.install(new Cyberware(catalog("Sichtverbesserung"), CyberwareGrade.STANDARD, 2));
    original.getEquipment().addCyberware(eyes);
    original.getEquipment().addCyberware(
        new Cyberware(catalog("Reflexrekorder (Fertigkeit)"), CyberwareGrade.STANDARD, 0, "Pistolen", null));
    original.getEquipment().addCyberware(
        new Cyberware(catalog("Kosmetische Modifikation"), CyberwareGrade.STANDARD, 0, "", 3500));
    Cyberware headware = new Cyberware(catalog("Kommlink"), CyberwareGrade.STANDARD, 0);
    Gear commlink = new Gear(Objects.requireNonNull(GearDataLoader.findByName("Meta Link")), 0);
    commlink.install(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Vector Xim")), 0));
    headware.setCommlink(commlink);
    original.getEquipment().addCyberware(headware);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Cyberware> implants = loaded.getEquipment().getCyberware();
    assertEquals(4, implants.size());
    Cyberware loadedEyes = implants.getFirst();
    assertEquals(List.of("Smartlink", "Sichtverbesserung"),
        loadedEyes.getInstalled().stream().map(Cyberware::getName).toList());
    assertEquals(CyberwareGrade.ALPHAWARE, loadedEyes.getInstalled().getFirst().getGrade());
    assertEquals(2, loadedEyes.getInstalled().get(1).getRating());
    assertEquals("Pistolen", implants.get(1).getChoice());
    assertEquals(3500, implants.get(2).getChosenPrice());
    Gear loadedCommlink = implants.get(3).getCommlink();
    assertNotNull(loadedCommlink);
    assertEquals("Meta Link", loadedCommlink.getName());
    assertEquals("Vector Xim", loadedCommlink.getInstalled().getFirst().getName());
    assertEquals(original.getEquipment().getCyberwareNuyenCost(), loaded.getEquipment().getCyberwareNuyenCost());
  }

  @Test
  void invalidCosmeticPriceFallsBackToMinimum() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "cyberware":[{"name":"Kosmetische Modifikation","grade":"STANDARD","rating":0,"price":99}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    assertEquals(200, loaded.getEquipment().getCyberware().getFirst().getChosenPrice());
  }

  @Test
  void damageAboveNaturalMonitorSurvivesThanksToImplant() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    original.getAttribute(AttributeType.BOD).setValue(2);
    original.getEquipment().addCyberware(new Cyberware(catalog("Hyperschilddrüse"), CyberwareGrade.STANDARD, 0));
    int damage = original.getPhysicalBoxes() + original.getPhysicalOverflowBoxes();
    original.setPhysicalDamage(damage);

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(damage, loaded.getPhysicalDamage());
  }

  @Test
  void roundtripPreservesCyberlimbs() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Cyberware left = new Cyberware(catalog("Ganzes Bein (offensichtlich)"), CyberwareGrade.ALPHAWARE,
        BodyLocation.LEG_LEFT, new LimbAttributes(5, 4, 3));
    Cyberware right = new Cyberware(catalog("Ganzes Bein (synthetisch)"), CyberwareGrade.STANDARD,
        BodyLocation.LEG_RIGHT, LimbAttributes.STANDARD);
    Cyberware spare = new Cyberware(catalog("Hand/Fuß (offensichtlich)"), CyberwareGrade.STANDARD,
        BodyLocation.ARM_LEFT, LimbAttributes.STANDARD);
    spare.setAttached(false);
    original.getImplants().add(left);
    original.getImplants().add(right);
    original.getImplants().add(spare);
    original.getImplants().installInLimb(left, new Cyberware(catalog("Stärkeverbesserung"), CyberwareGrade.STANDARD, 2));
    original.getImplants().installInLimb(left, new Cyberware(catalog("Sprunghydraulik"), CyberwareGrade.STANDARD, 2));
    original.getImplants().installInLimb(right, new Cyberware(catalog("Großer Schmuggelbehälter"),
        CyberwareGrade.STANDARD, 0));
    int cost = original.getEquipment().getCyberwareNuyenCost();

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    List<Cyberware> limbs = loaded.getImplants().getLimbs();
    assertEquals(3, limbs.size());
    Cyberware loadedLeft = limbs.getFirst();
    assertEquals(BodyLocation.LEG_LEFT, loadedLeft.getLocation());
    assertEquals(CyberwareGrade.ALPHAWARE, loadedLeft.getGrade());
    assertEquals(new LimbAttributes(5, 4, 3), loadedLeft.getBaseAttributes());
    assertEquals(6, loadedLeft.getLimbAttribute(AttributeType.STR));
    assertEquals(7, limbs.get(1).getUsedCapacity());
    assertEquals(cost, loaded.getEquipment().getCyberwareNuyenCost());
    Cyberware copy = limbs.get(1).getInstalled().getFirst();
    assertTrue(copy.isPairedCopy());
    // die Verknüpfung übersteht das Speichern: Ausbau des Originals entfernt den zweiten Teil
    loaded.getImplants().uninstallFromLimb(loadedLeft, Objects.requireNonNull(copy.getPairedPart()));
    assertEquals(1, limbs.get(1).getInstalled().size());
    assertFalse(limbs.get(2).isAttached());
  }

  @Test
  void roundtripKeepsWeaponInLimbAndCommlinkInSkull() throws IOException {
    Character original = new Character("Test", MetatypeDataLoader.find("Mensch"));
    Cyberware hand = new Cyberware(catalog("Hand/Fuß (offensichtlich)"), CyberwareGrade.STANDARD,
        BodyLocation.ARM_RIGHT, LimbAttributes.STANDARD);
    Cyberware skull = new Cyberware(catalog("Schädel (offensichtlich)"), CyberwareGrade.STANDARD,
        BodyLocation.SKULL, LimbAttributes.STANDARD);
    original.getImplants().add(hand);
    original.getImplants().add(skull);
    original.getImplants().installInLimb(hand, new Cyberware(catalog("Sporn (einziehbar)"), CyberwareGrade.STANDARD, 0));
    Cyberware commlinkHeadware = new Cyberware(catalog("Kommlink"), CyberwareGrade.STANDARD, 0);
    original.getImplants().installInLimb(skull, commlinkHeadware);
    commlinkHeadware.setCommlink(new Gear(Objects.requireNonNull(GearDataLoader.findByName("Meta Link")), 0));

    CharacterStore.save(original, tempFile());
    Character loaded = CharacterStore.load(tempFile());

    assertEquals(List.of("Sporn"),
        loaded.getEquipment().getImplantWeapons().stream().map(w -> w.getName()).toList());
    Cyberware loadedHeadware = loaded.getImplants().getLimbs().get(1).getInstalled().getFirst();
    assertEquals("Meta Link", Objects.requireNonNull(loadedHeadware.getCommlink()).getName());
  }

  @Test
  void oldLimbEntryWithoutNewFieldsLoadsAsStandardAttachedLimb() throws IOException {
    Files.writeString(tempFile().toPath(), """
        {"name":"Alt","metatype":"Mensch",
         "cyberware":[{"name":"Ganzer Arm (offensichtlich)","grade":"STANDARD","rating":0}]}
        """, StandardCharsets.UTF_8);

    Character loaded = CharacterStore.load(tempFile());

    Cyberware arm = loaded.getImplants().getLimbs().getFirst();
    assertTrue(arm.isAttached());
    assertEquals(BodyLocation.ARM_LEFT, arm.getLocation());
    assertEquals(LimbAttributes.STANDARD, arm.getBaseAttributes());
  }
}
