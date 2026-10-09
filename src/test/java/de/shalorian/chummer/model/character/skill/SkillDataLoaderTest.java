package de.shalorian.chummer.model.character.skill;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SkillDataLoaderTest {

    @Test
    void createFreshSkillsReturnsIndependentInstances() {
        List<Skill> first = SkillDataLoader.createFreshSkills();
        List<Skill> second = SkillDataLoader.createFreshSkills();

        first.get(0).setRating(5);

        assertEquals(0, second.get(0).getRating(), "Änderungen an einer Liste dürfen andere nicht beeinflussen");
    }

    /**
     * Vollständiger Abgleich mit SR4-Grundregelwerk S. 146 (Tabellen "Fertigkeiten und damit
     * verbundene Attribute" und "Fertigkeitsgruppen") und den Kategorien der Beschreibungen S. 147–154.
     * Format je Zeile: Name|Attribut|Gruppe (leer = keine)|Kategorie.
     */
    private static final List<String> GRW_SKILLS = List.of(
            "Klingenwaffen|AGI|Nahkampf|COMBAT",
            "Knüppel|AGI|Nahkampf|COMBAT",
            "Waffenloser Kampf|AGI|Nahkampf|COMBAT",
            "Gewehre|AGI|Feuerwaffen|COMBAT",
            "Pistolen|AGI|Feuerwaffen|COMBAT",
            "Schnellfeuerwaffen|AGI|Feuerwaffen|COMBAT",
            "Ausweichen|REA||COMBAT",
            "Exotische Fernkampfwaffe|AGI||COMBAT",
            "Exotische Nahkampfwaffe|AGI||COMBAT",
            "Projektilwaffen|AGI||COMBAT",
            "Schwere Waffen|AGI||COMBAT",
            "Wurfwaffen|AGI||COMBAT",
            "Akrobatik|AGI|Athletik|PHYSICAL",
            "Klettern|STR|Athletik|PHYSICAL",
            "Laufen|STR|Athletik|PHYSICAL",
            "Schwimmen|STR|Athletik|PHYSICAL",
            "Beschatten|INT|Heimlichkeit|PHYSICAL",
            "Fingerfertigkeit|AGI|Heimlichkeit|PHYSICAL",
            "Infiltration|AGI|Heimlichkeit|PHYSICAL",
            "Verkleiden|INT|Heimlichkeit|PHYSICAL",
            "Navigation|INT|Natur|PHYSICAL",
            "Spurenlesen|INT|Natur|PHYSICAL",
            "Survival|WIL|Natur|PHYSICAL",
            "Entfesseln|AGI||PHYSICAL",
            "Fallschirmspringen|BOD||PHYSICAL",
            "Tauchen|BOD||PHYSICAL",
            "Wahrnehmung|INT||PHYSICAL",
            "Führung|CHA|Einfluss|SOCIAL",
            "Gebräuche|CHA|Einfluss|SOCIAL",
            "Überreden|CHA|Einfluss|SOCIAL",
            "Verhandlung|CHA|Einfluss|SOCIAL",
            "Einschüchtern|CHA||SOCIAL",
            "Unterricht|CHA||SOCIAL",
            "Erste Hilfe|LOG|Biotech|TECHNICAL",
            "Kybernetik|LOG|Biotech|TECHNICAL",
            "Medizin|LOG|Biotech|TECHNICAL",
            "Elektronische Kriegsführung|LOG|Cracken|TECHNICAL",
            "Hacking|LOG|Cracken|TECHNICAL",
            "Matrixkampf|LOG|Cracken|TECHNICAL",
            "Computer|LOG|Elektronik|TECHNICAL",
            "Datensuche|LOG|Elektronik|TECHNICAL",
            "Hardware|LOG|Elektronik|TECHNICAL",
            "Software|LOG|Elektronik|TECHNICAL",
            "Fahrzeugmechanik|LOG|Mechanik|TECHNICAL",
            "Industriemechanik|LOG|Mechanik|TECHNICAL",
            "Luftfahrtmechanik|LOG|Mechanik|TECHNICAL",
            "Seefahrtmechanik|LOG|Mechanik|TECHNICAL",
            "Chemie|LOG||TECHNICAL",
            "Fälschen|AGI||TECHNICAL",
            "Handwerk|INT||TECHNICAL",
            "Schlosser|AGI||TECHNICAL",
            "Sprengstoffe|LOG||TECHNICAL",
            "Waffenbau|LOG||TECHNICAL",
            "Bodenfahrzeuge|REA||VEHICLE",
            "Exotisches Fahrzeug|REA||VEHICLE",
            "Flugzeuge|REA||VEHICLE",
            "Geschütze|AGI||VEHICLE",
            "Läufer|REA||VEHICLE",
            "Raumfahrzeuge|REA||VEHICLE",
            "Schiffe|REA||VEHICLE",
            "Binden|MAG|Beschwören|MAGICAL",
            "Herbeirufen|MAG|Beschwören|MAGICAL",
            "Verbannen|MAG|Beschwören|MAGICAL",
            "Antimagie|MAG|Hexerei|MAGICAL",
            "Ritualzauberei|MAG|Hexerei|MAGICAL",
            "Spruchzauberei|MAG|Hexerei|MAGICAL",
            "Arcana|LOG||MAGICAL",
            "Askennen|INT||MAGICAL",
            "Astralkampf|WIL||MAGICAL",
            "Verzaubern|MAG||MAGICAL",
            "Dekompilieren|RES|Tasken|RESONANCE",
            "Kompilieren|RES|Tasken|RESONANCE",
            "Registrieren|RES|Tasken|RESONANCE");

    @Test
    void matchesGrwSkillTableExactly() {
        List<String> actual = SkillDataLoader.createFreshSkills().stream()
                .map(s -> s.getName() + "|" + s.getLinkedAttribute() + "|"
                        + Objects.requireNonNullElse(s.getGroupName(), "") + "|" + s.getCategory().name())
                .sorted()
                .toList();

        assertEquals(GRW_SKILLS.stream().sorted().toList(), actual);
    }
}
