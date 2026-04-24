package de.shalorian.chummer.model.character;

public enum AttributeType {
    BOD("Körper",           "BOD"),
    AGI("Geschicklichkeit", "AGI"),
    REA("Reaktion",         "REA"),
    STR("Stärke",           "STR"),
    CHA("Charisma",         "CHA"),
    INT("Intuition",        "INT"),
    LOG("Logik",            "LOG"),
    WIL("Willenskraft",     "WIL");

    private final String displayName;
    private final String abbreviation;

    AttributeType(String displayName, String abbreviation) {
        this.displayName = displayName;
        this.abbreviation = abbreviation;
    }

    public String getDisplayName()  { return displayName; }
    public String getAbbreviation() { return abbreviation; }
}