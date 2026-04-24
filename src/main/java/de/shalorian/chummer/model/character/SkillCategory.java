package de.shalorian.chummer.model.character;

public enum SkillCategory {
    COMBAT("Kampf"),
    PHYSICAL("Körperlich"),
    SOCIAL("Sozial"),
    TECHNICAL("Technisch"),
    VEHICLE("Fahrzeuge");

    private final String displayName;

    SkillCategory(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
}