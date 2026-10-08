package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Ausrüstungskategorie = Tabelle der Ausrüstungsliste im SR4-Grundregelwerk, in Buchreihenfolge
 * (S. 368–395; die Seite steht je Eintrag in {@code gear.xml}). Der Anzeigename ist die
 * Tabellenüberschrift und zugleich der Wert von {@code <category>} in {@code gear.xml}; wo die
 * Überschrift allein mehrdeutig ist, ist der Bereich vorangestellt ("Elektronik-Zubehör" statt
 * "Zubehör"). Die Zauberformeln stehen im GRW unter "Spruchkategorie".
 * Pfeile ({@link #ARROWS}) werden im Waffen-Tab am Bogen gekauft, nicht als eigenständige Ausrüstung.
 *
 * <p>Einige Kategorien werden nur als Einbau in ein passendes Gerät gekauft ({@link
 * #isInstalledOnly()}): Betriebssysteme in Kommlinks (Nutzerentscheidung 2026-10-04), Sicht- und
 * Audioverstärkungen in Sicht- bzw. Audiosensoren (S. 379), Sensor-Funktionen in Sensorpakete
 * (S. 380) und die Zusätze des Magschlosses (S. 382). Welches Gerät was aufnimmt, steht je Eintrag in
 * {@link GearData#accepts()}.
 */
public enum GearCategory {
  ARROWS("Pfeile", GearArea.PROJECTILES),
  BOLTS("Bolzen", GearArea.PROJECTILES),
  FIREARM_ACCESSORIES("Feuerwaffenzubehör", GearArea.FIREARM_ACCESSORIES),
  AMMUNITION("Munition", GearArea.AMMUNITION),
  GRENADES("Granaten", GearArea.EXPLOSIVES),
  ROCKETS("Raketen", GearArea.EXPLOSIVES),
  MISSILES("Lenkraketen", GearArea.EXPLOSIVES),
  EXPLOSIVES("Sprengstoffe", GearArea.EXPLOSIVES),
  EXPLOSIVE_ACCESSORIES("Sprengstoff-Zubehör", GearArea.EXPLOSIVES),
  COMMLINKS("Kommlink-Modell", GearArea.ELECTRONICS),
  OPERATING_SYSTEMS("Handelsübliche Betriebssysteme", GearArea.ELECTRONICS),
  ELECTRONIC_ACCESSORIES("Elektronik-Zubehör", GearArea.ELECTRONICS),
  OTHER_ELECTRONICS("Sonstige Elektronik", GearArea.ELECTRONICS),
  RFID_TAGS("RFID-Marker", GearArea.ELECTRONICS),
  COMMUNICATION("Kommunikationssystem", GearArea.ELECTRONICS),
  MATRIX_PROGRAMS("Matrixprogramme", GearArea.DATACHIPS_AND_SOFTWARE),
  DATA_STORAGE("Speichermedien", GearArea.DATACHIPS_AND_SOFTWARE),
  SOFTWARE("Software", GearArea.DATACHIPS_AND_SOFTWARE),
  SKILLSOFTS("Talentsoft", GearArea.DATACHIPS_AND_SOFTWARE),
  SIMSENSE("SimSinn", GearArea.DATACHIPS_AND_SOFTWARE),
  ARE_PROGRAMS("ARE-Programme", GearArea.DATACHIPS_AND_SOFTWARE),
  IDS_AND_CREDSTICKS("ID/Credsticks", GearArea.IDS_AND_CREDSTICKS),
  TOOLS("Werkzeug", GearArea.TOOLS),
  VISUAL_DEVICES("Visuelle Sensoren und Optische Geräte", GearArea.SENSORS),
  VISION_ENHANCEMENTS("Sichtverstärkung", GearArea.SENSORS),
  AUDIO_DEVICES("Audiosensoren", GearArea.SENSORS),
  AUDIO_ENHANCEMENTS("Audioverstärkung", GearArea.SENSORS),
  SENSOR_PACKAGES("Sensorpakete", GearArea.SENSORS),
  SENSOR_FUNCTIONS("Sensor-Funktionen", GearArea.SENSORS),
  SECURITY_DEVICES("Sicherheitsgeräte", GearArea.SECURITY),
  MAGLOCK_OPTIONS("Magschloss-Zusätze", GearArea.SECURITY),
  RESTRAINTS("Handschellen", GearArea.SECURITY),
  BREAKING_AND_ENTERING("Einbruchswerkzeug", GearArea.BREAKING_AND_ENTERING),
  DRUGS("Drogen", GearArea.CHEMICALS_AND_DRUGS),
  CHEMICALS("Chemikalien", GearArea.CHEMICALS_AND_DRUGS),
  TOXINS("Toxine", GearArea.CHEMICALS_AND_DRUGS),
  SURVIVAL("Überlebensausrüstung", GearArea.SURVIVAL),
  GRAPPLE_GUN("Enterhakenkanone", GearArea.SURVIVAL),
  BIOTECH("Biotech", GearArea.BIOTECH),
  DOCWAGON("DocWagon-Vertrag", GearArea.BIOTECH),
  SLAP_PATCHES("Slap-Patches", GearArea.BIOTECH),
  DISGUISE("Verkleidung", GearArea.DISGUISE),
  FOCI("Foki", GearArea.MAGICAL),
  MAGICAL_SUPPLIES("Magisches Zubehör", GearArea.MAGICAL),
  FETISHES("Fetische", GearArea.MAGICAL),
  SPELL_FORMULAE("Zauberformeln", GearArea.MAGICAL);

  private final @NonNull String displayName;
  private final @NonNull GearArea area;

  GearCategory(@NonNull String displayName, @NonNull GearArea area) {
    this.displayName = displayName;
    this.area = area;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Bereich der Ausrüstungsliste, unter dem die Tabelle steht. */
  public @NonNull GearArea area() {
    return area;
  }

  /**
   * Ob Gegenstände dieser Kategorie nur als Einbau in ein Gerät gekauft werden (Betriebssystem,
   * Verstärkungen, Sensor-Funktionen, Magschloss-Zusätze).
   */
  public boolean isInstalledOnly() {
    return switch (this) {
      case OPERATING_SYSTEMS, VISION_ENHANCEMENTS, AUDIO_ENHANCEMENTS, SENSOR_FUNCTIONS,
           MAGLOCK_OPTIONS -> true;
      default -> false;
    };
  }

  /**
   * Liest die Kategorie aus {@code <category>} in {@code gear.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull GearCategory fromDisplayName(@NonNull String displayName) {
    for (GearCategory category : values()) {
      if (category.displayName.equals(displayName)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unbekannte Ausrüstungskategorie: \"" + displayName + "\"");
  }
}
