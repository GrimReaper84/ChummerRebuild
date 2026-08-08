package de.shalorian.chummer.model.character.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SkillCategoryTest {

  @Test
  void magicalCategoryHasGermanDisplayName() {
    assertEquals("Magie", SkillCategory.MAGICAL.getDisplayName());
  }
}
