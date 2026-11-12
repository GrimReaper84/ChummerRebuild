package de.shalorian.chummer.model.character;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.technomancer.ComplexFormData;
import de.shalorian.chummer.model.character.technomancer.ComplexFormDataLoader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** Obergrenze Komplexer Formen bei der Erschaffung (GRW S. 112). */
class CharacterComplexFormCapTest {

  private static Character of(String metatype) {
    return new Character("Test", MetatypeDataLoader.find(metatype));
  }

  private static QualityData q(String name) {
    return Objects.requireNonNull(QualityDataLoader.findByName(name), name);
  }

  @Test
  void complexFormsAtCreationLimitedToTwiceLogic() {
    // GRW S. 112
    Character c = of("Mensch");
    c.addQuality(q("Technomancer"), false);
    c.getAttribute(AttributeType.LOG).setValue(3);
    List<ComplexFormData> forms = List.copyOf(ComplexFormDataLoader.getAllComplexForms().values());
    for (int i = 0; i < 5; i++) {
      c.addComplexForm(forms.get(i));
    }
    assertTrue(c.canAddComplexFormAtCreation());
    c.addComplexForm(forms.get(5));
    assertFalse(c.canAddComplexFormAtCreation());
    c.setCreated(true);
    assertTrue(c.canAddComplexFormAtCreation());
  }
}
