package de.shalorian.chummer.model.character.vehicle;

import org.jspecify.annotations.NonNull;

/**
 * Fahrzeugkategorie laut den Tabellenüberschriften der Fahrzeugtabelle im SR4-Grundregelwerk
 * (S. 397), in Tabellenreihenfolge. Der Anzeigename ist zugleich der Wert von {@code <category>} in
 * {@code vehicles.xml}.
 */
public enum VehicleCategory {
  MOTORCYCLES("Motorräder"),
  CARS("Pkw"),
  TRUCKS("Lkw"),
  HOVERCRAFT("Hovercrafts"),
  MILITARY_AND_SECURITY("Militär- und Sicherheitsfahrzeuge"),
  BOATS_AND_SUBMARINES("Schiffe und U-Boote"),
  AIRPLANES("Flugzeuge"),
  ROTORCRAFT("Rotormaschinen"),
  VTOL("Senkrechtstarter"),
  DRONES("Drohnen");

  private static final int STANDARD_DEVICE_RATING = 3;
  private static final int SECURITY_DEVICE_RATING = 4;

  private final @NonNull String displayName;

  VehicleCategory(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob es sich um Drohnen handelt (GRW S. 398: Drohnen haben immer einen Riggeradapter). */
  public boolean isDrone() {
    return this == DRONES;
  }

  /**
   * Gerätestufe laut Tabelle "Gerätestufen" (SR4-Grundregelwerk S. 261): "Durchschnittlich 3 –
   * … Fahrzeuge, Drohnen", "Komplex 4 – Sicherheitsfahrzeuge". Die deutsche Fahrzeugtabelle hat keine
   * eigene Spalte dafür; die Militär- und Sicherheitsfahrzeuge des GRW werden als
   * Sicherheitsfahrzeuge eingestuft ("Intelligent 5 – Militärfahrzeuge" betrifft kein GRW-Fahrzeug).
   */
  public int deviceRating() {
    return this == MILITARY_AND_SECURITY ? SECURITY_DEVICE_RATING : STANDARD_DEVICE_RATING;
  }

  /**
   * Liest die Kategorie aus {@code <category>} in {@code vehicles.xml}.
   *
   * @throws IllegalArgumentException bei unbekannter Bezeichnung (Datenfehler im Katalog)
   */
  public static @NonNull VehicleCategory fromDisplayName(@NonNull String displayName) {
    for (VehicleCategory category : values()) {
      if (category.displayName.equals(displayName)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unbekannte Fahrzeugkategorie: \"" + displayName + "\"");
  }
}
