package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.KnowledgeSkillCategory;
import org.jspecify.annotations.NonNull;

public record KnowledgeSkillAddResult(
    @NonNull String name,
    @NonNull KnowledgeSkillCategory category
) {}
