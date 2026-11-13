package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.detailKey;
import static de.shalorian.chummer.view.grid.GridSupport.detailValue;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.technomancer.LivingPersona;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Abschnitt „Lebende Persona“ im Bereich Technomancer (GRW S. 279): die vier Persona-Attribute, Biofeedback-Filter und
 * VR-Initiative. Der Abschnitt baut sich bei jedem Aufruf von {@link #build(Character)} neu auf, damit Attribut- und
 * Resonanzänderungen sofort sichtbar sind.
 */
final class LivingPersonaSection {

  private final @NonNull VBox box = new VBox(6);

  /** Baut den Abschnitt für {@code ch} (neu) und liefert ihn zum Einhängen; leer ohne Resonanz. */
  @NonNull Node build(@NonNull Character ch) {
    box.getChildren().clear();
    LivingPersona persona = ch.getLivingPersona();
    if (persona == null) {
      return box;
    }
    Label title = new Label("Lebende Persona");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    MigPane grid = new MigPane("", "[][]24[][]24[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    addRowCells(grid, 0, detailKey("Firewall"), detailValue(String.valueOf(persona.firewall())),
        detailKey("Prozessor"), detailValue(persona.processor() + " (VR " + persona.vrProcessor() + ")"),
        detailKey("Signal"), detailValue(String.valueOf(persona.signal())));
    addRowCells(grid, 1, detailKey("System"), detailValue(String.valueOf(persona.system())),
        detailKey("Biofeedback-Filter"), detailValue(String.valueOf(persona.biofeedbackFilter())),
        detailKey("VR-Initiative"), detailValue(ch.getMatrixInitiative() + " / " + ch.getMatrixInitiativePasses()
            + " Durchgänge"));
    Label hint = new Label("Kein Persona-Attribut liegt über der Resonanz (" + persona.resonance()
        + "); in VR immer heißes Sim (S. 279).");
    hint.getStyleClass().add(STYLE_ATTR_RANGE);
    hint.setWrapText(true);
    box.getChildren().setAll(title, grid, hint);
    VBox.setMargin(box, new Insets(0, 0, 16, 0));
    return box;
  }
}
