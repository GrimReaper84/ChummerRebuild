package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import org.jspecify.annotations.NonNull;

public record KnowledgeSkillAddResult(
    @NonNull String name,
    @NonNull KnowledgeSkillCategory category
) {}
