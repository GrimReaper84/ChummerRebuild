package de.shalorian.chummer.model.template;

import de.shalorian.chummer.model.character.Character;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Ein Archetyp des GRW (S. 124–139) aus {@code archetypes.xml}: ein vorgefertigter Charakter nach den normalen
 * Erschaffungsregeln (S. 105), der in der Erschaffung öffnet und dort weiter angepasst werden kann.
 *
 * @param description ein Satz in eigenen Worten (kein Hintergrundtext aus dem GRW); wird das Konzept der Biografie
 * @param source      Kürzel des Regelwerks aus {@code books.xml}
 * @param page        gedruckte Seite des Archetyps
 * @param spec        Bauanleitung; ihr Ausrüstungstext nennt, was kein Katalog abbildet, und wird Notiz der Biografie
 */
public record ArchetypeTemplate(@NonNull String name, @NonNull String description, @NonNull String source, int page,
    @NonNull CharacterSpec spec) implements DocumentTemplate {

  @Override
  public @NonNull TemplateKind kind() {
    return TemplateKind.ARCHETYPE;
  }

  /** Baut den Charakter in der Erschaffung; Einträge, die das Modell nicht übernehmen konnte, landen in {@code problems}. */
  public @NonNull Character create(@NonNull List<String> problems) {
    Character c = spec.build(name, BuildMode.CREATION, problems);
    c.getBiography().setConcept(description);
    if (!spec.gearText().isEmpty()) {
      c.getBiography().setNotes("Laut GRW S. " + page + " außerdem: " + spec.gearText());
    }
    return c;
  }

  /** Wie {@link #create(List)}, ohne die Probleme auszuwerten. */
  public @NonNull Character create() {
    return create(new ArrayList<>());
  }
}
