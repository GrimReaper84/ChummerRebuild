package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class CyberwareEntry {

  /** Gratis-Anteil aus dem Spielleiter-Modus (ADR-070); fehlt in älteren Dateien, dann 0. */
  public int gmFreeNuyen;

  public @NonNull String name = "";
  /** {@code CyberwareGrade}-Enum-Name, z.B. {@code "ALPHAWARE"}. */
  public @NonNull String grade = "STANDARD";
  /** Gewählte Stufe, {@code 0} bei Implantaten ohne Stufe. */
  public int rating = 0;
  /** Gewählte Fertigkeit bzw. Fertigkeitsgruppe (Reflexrekorder), sonst leer. */
  public @NonNull String choice = "";
  /** Gewählter Preis bei Preisspanne (Kosmetische Modifikation), sonst {@code null}. */
  public @Nullable Integer price;
  /** In ein Basissystem eingebautes Zubehör; ihre Grade folgt beim Laden dem Basissystem. */
  public @NonNull List<CyberwareEntry> installed = new ArrayList<>();
  /** In die Kommlink-Headware eingebautes Kommlink samt Betriebssystem, sonst {@code null}. */
  public @Nullable GearEntry commlink;
  /** Körperstelle einer Cybergliedmaße ({@code BodyLocation}-Enum-Name), sonst {@code null}. */
  public @Nullable String location;
  /** Ob die Cybergliedmaße angeschlossen ist; fehlt das Feld (ältere Dateien), gilt sie als angeschlossen. */
  public boolean attached = true;
  /** Grundwerte einer maßgefertigten Cybergliedmaße; {@code 0} (oder fehlend) = Standardwert 3. */
  public int body;
  public int strength;
  public int agility;
  /** Zweiter Teil einer Sprunghydraulik im anderen Bein (ohne eigenen Preis). */
  public boolean pairedCopy;
  /** Kennung, die Original und zweiten Teil einer Sprunghydraulik verbindet; {@code 0} ohne Paar. */
  public int pair;

  public CyberwareEntry() {
  }

  public CyberwareEntry(@NonNull String name, @NonNull String grade, int rating) {
    this.name = name;
    this.grade = grade;
    this.rating = rating;
  }
}
