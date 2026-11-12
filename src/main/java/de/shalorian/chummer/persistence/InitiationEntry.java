package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/** Gespeicherte Initiation bzw. Wandlung: Grad und gelernte Techniken (mehrfach gelernte mehrfach). */
class InitiationEntry {

  public int grade;
  public @NonNull List<String> techniques = new ArrayList<>();

  public InitiationEntry() {
  }

  public InitiationEntry(int grade, @NonNull List<String> techniques) {
    this.grade = grade;
    this.techniques = techniques;
  }
}
