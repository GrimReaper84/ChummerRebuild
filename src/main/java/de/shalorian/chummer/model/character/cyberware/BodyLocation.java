package de.shalorian.chummer.model.character.cyberware;

import org.jspecify.annotations.NonNull;

/**
 * Körperstelle, an der eine Cybergliedmaße sitzt (GRW S. 389–391). Je Stelle zählt die Essenz nur des teuersten Glieds
 * (Ersatzglieder, Nutzerentscheidung); mehrere angeschlossene Glieder an einer Stelle lösen nur eine Warnung aus, weil
 * spätere Regelwerke (Surge) zusätzliche Gliedmaßen kennen.
 */
public enum BodyLocation {
  ARM_LEFT("linker Arm"),
  ARM_RIGHT("rechter Arm"),
  LEG_LEFT("linkes Bein"),
  LEG_RIGHT("rechtes Bein"),
  TORSO("Torso"),
  SKULL("Schädel");

  private final @NonNull String displayName;

  BodyLocation(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String getDisplayName() {
    return displayName;
  }

  /** Ob die Stelle ein Bein ist (Sprunghydraulik braucht zwei Cyberbeine, S. 391). */
  public boolean isLeg() {
    return this == LEG_LEFT || this == LEG_RIGHT;
  }
}
