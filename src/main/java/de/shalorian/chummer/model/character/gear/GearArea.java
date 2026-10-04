package de.shalorian.chummer.model.character.gear;

import org.jspecify.annotations.NonNull;

/**
 * Bereich der Ausrüstungsliste laut Inhaltsverzeichnis des SR4-Grundregelwerks (Kapitel
 * Straßenausrüstung, S. 367–395), in Buchreihenfolge. Der Ausrüstungsbaum gruppiert nach Bereich,
 * weil die rund 30 GRW-Tabellen ({@link GearCategory}) als Baumebene zu kleinteilig wären. Bereiche
 * der späteren Gear-Teile (Munition, Sensoren, Sprengstoffe …) werden erst mit diesen ergänzt.
 */
public enum GearArea {
  FIREARM_ACCESSORIES("Feuerwaffen-Zubehör"),
  ELECTRONICS("Elektronik"),
  DATACHIPS_AND_SOFTWARE("Datenchips und Software"),
  IDS_AND_CREDSTICKS("IDs und Credsticks"),
  TOOLS("Werkzeug"),
  SECURITY("Sicherheitsgeräte"),
  BREAKING_AND_ENTERING("Einbruchswerkzeuge"),
  CHEMICALS_AND_DRUGS("Chemikalien und Drogen"),
  SURVIVAL("Überlebensausrüstung"),
  BIOTECH("Biotech"),
  DISGUISE("Verkleidung"),
  MAGICAL("Magische Ausrüstung");

  private final @NonNull String displayName;

  GearArea(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }
}
