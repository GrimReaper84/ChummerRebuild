package de.shalorian.chummer.model.character.skill;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSkillCategoryTest {

    @Test
    void academicAndProfessionalAreLinkedToLog() {
        assertEquals(AttributeType.LOG, KnowledgeSkillCategory.ACADEMIC.getLinkedAttribute());
        assertEquals(AttributeType.LOG, KnowledgeSkillCategory.PROFESSIONAL.getLinkedAttribute());
    }

    @Test
    void streetHobbyAndLanguageAreLinkedToInt() {
        assertEquals(AttributeType.INT, KnowledgeSkillCategory.STREET.getLinkedAttribute());
        assertEquals(AttributeType.INT, KnowledgeSkillCategory.HOBBY.getLinkedAttribute());
        assertEquals(AttributeType.INT, KnowledgeSkillCategory.LANGUAGE.getLinkedAttribute());
    }
}
