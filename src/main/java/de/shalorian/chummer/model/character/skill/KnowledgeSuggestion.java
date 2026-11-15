package de.shalorian.chummer.model.character.skill;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Vorschlag für eine Wissens- oder Sprachfertigkeit aus {@code knowledge_skills.xml} (GRW S. 110, 155, 156).
 *
 * @param name            Name, z.B. „Seattler Straßengangs“
 * @param category        Kategorie
 * @param specializations vorgeschlagene Spezialisierungen, ggf. leer
 */
public record KnowledgeSuggestion(@NonNull String name, @NonNull KnowledgeSkillCategory category,
                                  @NonNull List<String> specializations) {
}
