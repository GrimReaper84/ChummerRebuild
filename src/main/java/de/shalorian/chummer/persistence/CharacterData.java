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
  public @NonNull Map<String, Integer> skills = new HashMap<>(); // name → rating (nur > 0)
  public @NonNull List<KnowledgeSkillEntry> knowledgeSkills = new ArrayList<>();
  public @NonNull List<String> decoupledGroups = new ArrayList<>();
  public @NonNull List<LogEntryData> karmaLog = new ArrayList<>();
  public @NonNull List<LogEntryData> nuyenLog = new ArrayList<>();

  public CharacterData() {
    // Für Jackson-Deserialisierung benötigt (public Felder werden nach dem Konstruktoraufruf per Reflection befüllt).
  }

  public static class SelectedQualityEntry {

    public @NonNull String name = "";
    public boolean gratis = false;

    public SelectedQualityEntry() {
    }

    public SelectedQualityEntry(@NonNull String name, boolean gratis) {
      this.name = name;
      this.gratis = gratis;
    }
  }

  public static class KnowledgeSkillEntry {

    public @NonNull String name = "";
    public @NonNull String category = "";
    public int rating = 0;
    public boolean nativeLanguage = false;

    public KnowledgeSkillEntry() {
    }

    public KnowledgeSkillEntry(@NonNull String name, @NonNull String category, int rating,
        boolean nativeLanguage) {
      this.name = name;
      this.category = category;
      this.rating = rating;
      this.nativeLanguage = nativeLanguage;
    }
  }

  public static class LogEntryData {

    public @NonNull String description = "";
    public int amount = 0;
    public @NonNull String date = ""; // ISO-Format: "2026-04-24"

    @SuppressWarnings("unused") // Für Jackson-Deserialisierung benötigt
    public LogEntryData() {
    }

    public LogEntryData(@NonNull String description, int amount, @NonNull String date) {
      this.description = description;
      this.amount = amount;
      this.date = date;
    }
  }
}