package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkillDataLoaderTest {

    @Test
    void createFreshSkillsReturnsIndependentInstances() {
        List<Skill> first = SkillDataLoader.createFreshSkills();
        List<Skill> second = SkillDataLoader.createFreshSkills();

        first.get(0).setRating(5);

        assertEquals(0, second.get(0).getRating(), "Änderungen an einer Liste dürfen andere nicht beeinflussen");
    }
}