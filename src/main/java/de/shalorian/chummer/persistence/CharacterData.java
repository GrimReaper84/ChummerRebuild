package de.shalorian.chummer.persistence;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterData {
    public @Nullable String name;
    public @Nullable String metatype;
    public boolean          created  = false;
    public Map<String, Integer> attributes      = new HashMap<>();
    public Map<String, Integer> skills          = new HashMap<>(); // name → rating (nur > 0)
    public List<String>         decoupledGroups = new ArrayList<>();
    public List<LogEntryData>   karmaLog        = new ArrayList<>();
    public List<LogEntryData>   nuyenLog        = new ArrayList<>();

    public CharacterData() {}

    public static class LogEntryData {
        public String description = "";
        public int    amount      = 0;
        public String date        = ""; // ISO-Format: "2026-04-24"

        public LogEntryData() {}
        public LogEntryData(String description, int amount, String date) {
            this.description = description;
            this.amount      = amount;
            this.date        = date;
        }
    }
}