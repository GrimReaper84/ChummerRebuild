package de.shalorian.chummer.model.character.quality;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Wählbarkeitsregeln einer Quality laut GRW S. 114–122.
 *
 * @param group        Gruppe, aus der ein Charakter nur einen Eintrag haben darf (Stufen und Varianten), sonst
 *                     {@code null}
 * @param multiple     ob die Quality mehrfach (mit verschiedener Auswahl) gewählt werden darf
 * @param requires     Gaben, von denen der Charakter mindestens eine haben muss; leer ohne Voraussetzung
 * @param excludes     Gaben, mit denen sie nicht kombiniert werden darf
 * @param metatypes    Grundmetatypen, die sie wählen dürfen; leer für alle
 * @param metatypeOnly ob sie nur durch den Metatyp kommt und nicht wählbar ist
 */
public record QualityRules(
    @Nullable String group,
    boolean multiple,
    @NonNull List<String> requires,
    @NonNull List<String> excludes,
    @NonNull List<String> metatypes,
    boolean metatypeOnly
) {

  public QualityRules {
    requires = List.copyOf(requires);
    excludes = List.copyOf(excludes);
    metatypes = List.copyOf(metatypes);
  }
}
