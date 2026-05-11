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
  public @NonNull Map<String, Integer> skills = new HashMap<>(); // name → rating (nur > 0)
  public @NonNull List<String> decoupledGroups = new ArrayList<>();
  public @NonNull List<LogEntryData> karmaLog = new ArrayList<>();
  public @NonNull List<LogEntryData> nuyenLog = new ArrayList<>();

  public CharacterData() {
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

  public static class LogEntryData {

    public @NonNull String description = "";
    public int amount = 0;
    public @NonNull String date = ""; // ISO-Format: "2026-04-24"

    public LogEntryData() {
    }

    public LogEntryData(@NonNull String description, int amount, @NonNull String date) {
      this.description = description;
      this.amount = amount;
      this.date = date;
    }
  }
}