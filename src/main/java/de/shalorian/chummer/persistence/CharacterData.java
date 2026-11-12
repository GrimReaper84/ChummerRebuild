package de.shalorian.chummer.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CharacterData {

  public @Nullable String name;
  public @Nullable String metatype;
  public boolean created = false;
  public int nuyenBP = 0;
  /** Eingetragener Schaden im Zustandsmonitor (Kästchen); fehlt in Dateien vor 2026-11-12. */
  public int physicalDamage = 0;
  public int stunDamage = 0;
  public @NonNull List<SelectedQualityEntry> selectedQualities = new ArrayList<>();
  public @NonNull Map<String, Integer> attributes = new HashMap<>();
  public int edge = 0;
  /**
   * Wert des freigeschalteten Magic- oder Resonance-Attributs, oder null wenn nicht freigeschaltet.
   */
  public @Nullable Integer magic = null;
  public @Nullable Integer resonance = null;
  public @NonNull List<SkillEntry> skills = new ArrayList<>(); // nur Fertigkeiten mit Rating > 0 oder Spezialisierung
  public @NonNull List<KnowledgeSkillEntry> knowledgeSkills = new ArrayList<>();
  public @NonNull List<ContactEntry> contacts = new ArrayList<>();
  public @NonNull List<String> decoupledGroups = new ArrayList<>();
  public @NonNull List<LogEntryData> karmaLog = new ArrayList<>();
  public @NonNull List<LogEntryData> nuyenLog = new ArrayList<>();
  public @NonNull BiographyData biography = new BiographyData();
  public @NonNull String tradition = "HERMETIC";
  public @NonNull List<String> spellNames = new ArrayList<>();
  public @NonNull List<ComplexFormEntry> complexForms = new ArrayList<>();
  /** Gekaufte Adeptenkräfte; fehlt in Dateien vor 2026-11-12. */
  public @NonNull List<AdeptPowerEntry> adeptPowers = new ArrayList<>();
  /** Magieradept: Magiepunkte für Kräfte. */
  public int adeptMagic = 0;
  /** Initiation bzw. Wandlung; fehlt in Dateien vor ADR-052 (dann Grad 0). */
  public @NonNull InitiationEntry initiation = new InitiationEntry();
  public @NonNull List<CyberwareEntry> cyberware = new ArrayList<>();
  public @NonNull List<ArmorEntry> armor = new ArrayList<>();
  public @NonNull List<WeaponEntry> weapons = new ArrayList<>();
  public @NonNull List<GearEntry> gear = new ArrayList<>();
  public @NonNull List<VehicleEntry> vehicles = new ArrayList<>();
  public @NonNull LifestyleEntry lifestyle = new LifestyleEntry();

  public CharacterData() {
    // Für Jackson-Deserialisierung benötigt (public Felder werden nach dem Konstruktoraufruf per Reflection befüllt).
  }
}