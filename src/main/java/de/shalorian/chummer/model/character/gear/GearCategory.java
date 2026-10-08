package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Ausrüstungskategorie = Tabelle der Ausrüstungsliste im SR4-Grundregelwerk, in Buchreihenfolge
 * (S. 368–395; die Seite steht je Eintrag in {@code gear.xml}). Der Anzeigename ist die
 * Tabellenüberschrift und zugleich der Wert von {@code <category>} in {@code gear.xml}; wo die
 * Überschrift allein mehrdeutig ist, ist der Bereich vorangestellt ("Elektronik-Zubehör" statt
 * "Zubehör"). Die Zauberformeln stehen im GRW unter "Spruchkategorie".
 * Kategorien der späteren Gear-Teile (Sensoren, Munition …) folgen mit diesen.
 *
 * <p>Kommlinks nehmen ein Betriebssystem als Einbau auf (Nutzerentscheidung 2026-10-04); handelsübliche
 * Betriebssysteme werden nur so gekauft, nicht als eigenständiger Gegenstand.
 */
public enum GearCategory {
  FIREARM_ACCESSORIES("Feuerwaffenzubehör", GearArea.FIREARM_ACCESSORIES),
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
  SECURITY_DEVICES("Sicherheitsgeräte", GearArea.SECURITY),
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

  /** Ob Gegenstände dieser Kategorie Einbauten aufnehmen (Kommlink: ein Betriebssystem). */
  public boolean acceptsInstalls() {
    return this == COMMLINKS;
  }

  /** Ob Gegenstände dieser Kategorie nur als Einbau gekauft werden (Betriebssystem im Kommlink). */
  public boolean isInstalledOnly() {
    return this == OPERATING_SYSTEMS;
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
