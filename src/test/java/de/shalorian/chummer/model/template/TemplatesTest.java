package de.shalorian.chummer.model.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.shalorian.chummer.model.character.option.RuleOptions;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TemplatesTest {

  private final List<DocumentTemplate> templates = Templates.available(new RuleOptions());

  @Test
  void listsTemplatesWithSourceAndPage() {
    assertFalse(templates.isEmpty());
    for (DocumentTemplate t : templates) {
      assertEquals("SR4", t.source(), t.name());
      assertTrue(t.page() > 0, t.name());
      assertFalse(t.description().isBlank(), t.name());
    }
  }

  @Test
  void sortsByKindThenName() {
    Collator collator = Collator.getInstance(Locale.GERMAN);
    for (int i = 1; i < templates.size(); i++) {
      DocumentTemplate before = templates.get(i - 1);
      DocumentTemplate after = templates.get(i);
      int byKind = before.kind().compareTo(after.kind());
      assertTrue(byKind < 0 || (byKind == 0 && collator.compare(before.name(), after.name()) <= 0),
          before.name() + " vor " + after.name());
    }
  }
}
