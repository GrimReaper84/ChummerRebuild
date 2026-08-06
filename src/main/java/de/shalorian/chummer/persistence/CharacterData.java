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
  public @NonNull List<String> decoupledGroups = new ArrayList<>();
  public @NonNull List<LogEntryData> karmaLog = new ArrayList<>();
  public @NonNull List<LogEntryData> nuyenLog = new ArrayList<>();

  public CharacterData() {
    // Für Jackson-Deserialisierung benötigt (public Felder werden nach dem Konstruktoraufruf per Reflection befüllt).
  }
}