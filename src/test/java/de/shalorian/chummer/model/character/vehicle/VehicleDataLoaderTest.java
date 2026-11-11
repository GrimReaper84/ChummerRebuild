package de.shalorian.chummer.model.character.vehicle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class VehicleDataLoaderTest {

  /**
   * Fahrzeugtabelle des SR4-Grundregelwerks S. 397, am PDF-Bild übertragen: Name | Kategorie |
   * Handling | Beschl. | Geschw. | Pilot | Rumpf | Panz. | Sensor | Verfüg. | Preis.
   */
  private static final List<String> GRW_TABLE = List.of(
      "Dodge Scoot (Scooter) | Motorräder | +1 | 10/15 | 60 | 1 | 4 | 2 | 1 | – | 3000",
      "Harley-Davidson Scorpion (Chopper) | Motorräder | +2 | 15/30 | 120 | 2 | 8 | 4 | 1 | – | 12000",
      "Yamaha Growler (Offroad) | Motorräder | +1 | 15/40 | 150 | 1 | 6 | 6 | 1 | – | 5500",
      "Suzuki Mirage (Rennmaschine) | Motorräder | +2 | 20/50 | 200 | 1 | 6 | 4 | 1 | – | 6500",
      "Eurocar Westwind 3K (Sportwagen) | Pkw | +3 | 20/60 | 240 | 3 | 10 | 6 | 1 | – | 85000",
      "Ford Spiral 115Ti (Günstiger Sportwagen) | Pkw | +2 | 15/55 | 180 | 2 | 10 | 6 | 1 | – | 45000",
      "Honda Spirit (Kleinwagen) | Pkw | +1 | 10/20 | 80 | 1 | 8 | 6 | 1 | – | 10000",
      "Mercury Comet (Mittelklassewagen) | Pkw | 0 | 15/30 | 110 | 2 | 10 | 6 | 1 | – | 14000",
      "Mitsubishi Nightsky (Limousine) | Pkw | -2 | 15/25 | 100 | 3 | 12 | 10 | 1 | – | 120000",
      "Toyota Gopher (Pick-Up) | Pkw | 0 | 5/20 | 100 | 1 | 14 | 8 | 1 | – | 35000",
      "GMC Bulldog Step-Van (Kleinlaster) | Lkw | 0 | 5/10 | 90 | 2 | 16 | 8 | 1 | – | 35000",
      "GMC Everglades (Hovercraft) | Hovercrafts | +1 | 10/20 | 120 | 2 | 14 | 6 | 1 | 4 | 25500",
      "Chrysler-Nissan Patrol-1 (Streifenwagen) | Militär- und Sicherheitsfahrzeuge | +3 | 10/45 | 180 | 3 | 10 | 10 | 1 | 12E | 22700",
      "GMC Beachcraft (Patrouillen-Hovercraft) | Militär- und Sicherheitsfahrzeuge | +2 | 10/40 | 160 | 2 | 12 | 16 | 2 | 16E | 63000",
      "Ares Citymaster (Aufruhrbekämpfungsfahrzeug) | Militär- und Sicherheitsfahrzeuge | -1 | 5/30 | 120 | 3 | 16 | 20 | 3 | 20E | 51200",
      "Morgan Cutlass (Patrouillenboot) | Schiffe und U-Boote | +2 | 15/30 | 60 | 3 | 16 | 10 | 3 | 12E | 90000",
      "MT Sea Nymph (Jacht) | Schiffe und U-Boote | -3 | 10/20 | 45 | 2 | 18 | 6 | 1 | – | 170000",
      "SC Otter (Sportboot) | Schiffe und U-Boote | +1 | 5/10 | 45 | 1 | 12 | 6 | 1 | – | 12500",
      "Vulkan Electronaut (Mini-U-Boot) | Schiffe und U-Boote | 0 | 5/10 | 30 | 2 | 12 | 10 | 2 | 10 | 158000",
      "Cessna C750 (Propellermaschine) | Flugzeuge | -1 | 20/90 | 340 | 2 | 18 | 4 | 1 | 8 | 164000",
      "Ares Dragon (Frachthubschrauber) | Rotormaschinen | -1 | 10/40 | 260 | 3 | 22 | 8 | 1 | 12 | 495000",
      "Hughes Stallion (Mehrzweckhubschrauber) | Rotormaschinen | -1 | 15/50 | 190 | 3 | 14 | 8 | 1 | 12 | 225000",
      "Northrup Wasp (Gyrokopter) | Rotormaschinen | 0 | 15/30 | 130 | 2 | 10 | 6 | 2 | 12E | 106000",
      "GMC Banshee (Thunderbird) | Senkrechtstarter | +1 | 50/250 | 1000 | 2 | 20 | 18 | 5 | 24V | 2350000",
      "Fed Boeing Commuter (Kippflügelmaschine) | Senkrechtstarter | -2 | 10/30 | 120 | 3 | 16 | 8 | 1 | 16E | 320000",
      "Shiawase Kanmushi (Mikrodrohne) | Drohnen | +1 | 2/10 | 10 | 3 | 0 | 0 | 1 | 8 | 1000",
      "S-B Microskimmer (Mikrodrohne) | Drohnen | +1 | 2/10 | 10 | 3 | 0 | 0 | 1 | 8 | 1000",
      "Lone Star iBall (Minidrohne) | Drohnen | +1 | 3/15 | 15 | 3 | 1 | 0 | 2 | 6 | 1500",
      "MCT Fly-Spy (Minidrohne) | Drohnen | +1 | 3/15 | 15 | 3 | 1 | 0 | 2 | 6 | 2000",
      "Aztechnology Crawler (Klein) | Drohnen | 0 | 5/20 | 50 | 3 | 2 | 0 | 2 | 4 | 1700",
      "Lockheed Optic-X (Klein) | Drohnen | 0 | 5/20 | 75 | 3 | 2 | 0 | 2 | 6 | 1700",
      "GM-Nissan Doberman (Mittel) | Drohnen | 0 | 10/25 | 75 | 3 | 3 | 6 | 3 | 6 | 3000",
      "MCT-Nissan Roto-Drohne (Mittel) | Drohnen | 0 | 10/25 | 100 | 3 | 3 | 2 | 3 | 6 | 2000",
      "Renraku Stormcloud (Mittel) | Drohnen | -3 | 5/10 | 25 | 3 | 3 | 2 | 3 | 6 | 2600",
      "C-D Dalmatian (Groß) | Drohnen | 0 | 15/40 | 120 | 3 | 4 | 2 | 3 | 9 | 2200",
      "CrashCart AutoDoc (Groß) | Drohnen | 0 | 5/10 | 30 | 3 | 4 | 2 | 1 | 8E | 4000",
      "Steel Lynx (Groß) | Drohnen | 0 | 15/40 | 120 | 3 | 4 | 9 | 3 | 12E | 5000");

  private static VehicleData find(String name) {
    return Objects.requireNonNull(VehicleDataLoader.findByName(name), name);
  }

  private static String row(VehicleData d) {
    return String.join(" | ", d.name(), d.category().getDisplayName(), d.handlingText(),
        d.accelText(), String.valueOf(d.speed()), String.valueOf(d.pilot()),
        String.valueOf(d.body()), String.valueOf(d.armor()), String.valueOf(d.sensor()),
        d.availability().displayText(0), String.valueOf(d.cost()));
  }

  @Test
  void matchesGrwVehicleTableExactly() {
    List<String> actual = VehicleDataLoader.getAllVehicles().values().stream()
        .map(VehicleDataLoaderTest::row)
        .toList();
    assertEquals(GRW_TABLE, actual);
  }

  /** Werte, in denen das C#-Original vom GRW abweicht (S. 397). */
  @Test
  void usesRulebookValuesWhereTheOriginalDeviates() {
    assertEquals(25_500, find("GMC Everglades (Hovercraft)").cost());
    assertEquals(5, find("GMC Banshee (Thunderbird)").sensor());
    assertEquals(2, find("Northrup Wasp (Gyrokopter)").sensor());
    assertEquals(1, find("Cessna C750 (Propellermaschine)").sensor());
  }

  @Test
  void everyVehicleComesWithItsSensorPackageFirst() {
    for (VehicleData data : VehicleDataLoader.getAllVehicles().values()) {
      String pack = data.includedGear().getFirst().name();
      assertTrue(pack.startsWith("Sensorpaket ("), data.name());
    }
    assertEquals("Sensorpaket (Fahrzeug)", firstGear("Mercury Comet (Mittelklassewagen)"));
    assertEquals("Sensorpaket (Mikro)", firstGear("Shiawase Kanmushi (Mikrodrohne)"));
    assertEquals("Sensorpaket (Hand/Minidrohne)", firstGear("MCT Fly-Spy (Minidrohne)"));
    assertEquals("Sensorpaket (Stationär/kleine Drohne)", firstGear("Lockheed Optic-X (Klein)"));
    assertEquals("Sensorpaket (Mittlere Drohne)", firstGear("GM-Nissan Doberman (Mittel)"));
    assertEquals("Sensorpaket (Große Drohne)", firstGear("Steel Lynx (Groß)"));
  }

  private static String firstGear(String vehicle) {
    return find(vehicle).includedGear().getFirst().name();
  }

  /** Mitgelieferte Ausstattung laut Fahrzeugtexten GRW S. 395–398. */
  @Test
  void includedEquipmentFollowsVehicleTexts() {
    assertEquals(List.of(new IncludedVehicleGear("Autosoft", 3, "Clearsight"),
            new IncludedVehicleGear("Autosoft", 3, "Zielerfassung")),
        find("GM-Nissan Doberman (Mittel)").includedGear().subList(1, 3));
    assertEquals(List.of(new IncludedVehicleGear("Autosoft", 3, "Abwehr"),
            new IncludedVehicleGear("Autosoft", 3, "Zielerfassung")),
        find("Steel Lynx (Groß)").includedGear().subList(1, 3));
    assertEquals(new IncludedVehicleGear("Autosoft", 3, "Clearsight"),
        find("Renraku Stormcloud (Mittel)").includedGear().get(1));
    assertEquals(new IncludedVehicleGear("Autosoft", 2, "Steuerung"),
        find("MCT Fly-Spy (Minidrohne)").includedGear().get(1));
    assertEquals(new IncludedVehicleGear("Medkit", 4, ""),
        find("CrashCart AutoDoc (Groß)").includedGear().get(1));

    assertEquals(1, find("GM-Nissan Doberman (Mittel)").includedMounts());
    assertEquals(1, find("Steel Lynx (Groß)").includedMounts());
    assertEquals("Geschützturm", find("Steel Lynx (Groß)").mountNote());
    assertEquals(1, find("Northrup Wasp (Gyrokopter)").includedMounts());
    assertEquals(1, find("Ares Citymaster (Aufruhrbekämpfungsfahrzeug)").includedMounts());
    assertEquals(2, find("Morgan Cutlass (Patrouillenboot)").includedMounts());
    assertEquals(0, find("Mercury Comet (Mittelklassewagen)").includedMounts());
  }

  /**
   * Mitgelieferte Ausrüstung muss zum Ausrüstungskatalog passen (Name, Stufe, Zusatztext); das prüft
   * der {@link Vehicle}-Konstruktor über {@code Gear}, deshalb wird jedes Katalogfahrzeug probeweise
   * gebaut.
   */
  @Test
  void everyCatalogVehicleCanBeBuilt() {
    for (VehicleData data : VehicleDataLoader.getAllVehicles().values()) {
      assertDoesNotThrow(() -> new Vehicle(data), data.name());
    }
  }

  /** Mitgelieferte Halterungen halten die Grenze Rumpf ÷ 3 ein (GRW S. 395). */
  @Test
  void includedMountsRespectTheBodyLimit() {
    for (VehicleData data : VehicleDataLoader.getAllVehicles().values()) {
      assertTrue(data.includedMounts() <= data.body() / 3, data.name());
    }
  }
}
