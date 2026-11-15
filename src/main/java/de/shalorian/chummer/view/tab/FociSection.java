package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.magic.Foci;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Abschnitt „Foki“ im Magie-Tab (GRW S. 233/234): gekaufte Foki mit Kraft, Bindung und Aktivierung, die Grenzen
 * (gebunden gegen Magie, Kraft gegen 5 × Magie) und Hinweise bei Überschreitung. Binden und Aktivieren geschieht im
 * Detail des Fokus im Bereich Ausrüstung; dieser Abschnitt zeigt nur den Stand.
 */
final class FociSection {

  private final @NonNull VBox box = new VBox();

  /** Baut den Abschnitt für {@code ch} (neu) und liefert ihn zum Einhängen. */
  @NonNull Node build(@NonNull Character ch) {
    box.getChildren().clear();
    Foci foci = ch.getFoci();
    int magic = foci.getMagic();
    Separator separator = new Separator();
    VBox.setMargin(separator, new Insets(20, 0, 12, 0));
    Label title = new Label("Foki");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    String summary = "gebunden %d / Magie %d · Kraft %d / %d".formatted(foci.getBound().size(), magic,
        foci.getBoundForce(), Foci.FORCE_PER_MAGIC * magic) + (ch.isCreated() ? "" : " · " + foci.getBpCost() + " BP");
    Label summaryLabel = new Label(summary);
    summaryLabel.getStyleClass().add(STYLE_BP_LABEL);
    HBox header = new HBox(12, title, summaryLabel);
    header.setAlignment(Pos.CENTER_LEFT);
    box.getChildren().addAll(separator, header);
    for (String problem : foci.getProblems()) {
      Label warning = styled(new Label(problem), STYLE_ATTR_RANGE);
      setWarnStyle(warning, true);
      box.getChildren().add(warning);
    }
    List<Gear> all = foci.getAll();
    if (all.isEmpty()) {
      box.getChildren().add(styled(new Label("Keine Foki gekauft (Bereich Ausrüstung)"), STYLE_ATTR_RANGE));
      return box;
    }
    MigPane grid = new MigPane("", "[][center][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(8, 0, 0, 0));
    // Bindungskosten je Modus: bei der Erschaffung 1 BP je Kraft (S. 112), danach Kraft × Faktor Karma (S. 234)
    addHeaderRow(grid, "Fokus", "Kraft", "Zustand", ch.isCreated() ? "Bindung (Karma)" : "Bindung (BP)");
    int row = 1;
    for (Gear focus : all) {
      String state = !focus.isBound() ? "ungebunden" : focus.isFocusActive() ? "gebunden, aktiv" : "gebunden";
      addRowCells(grid, row++, styled(new Label(focus.getDisplayName()), STYLE_ATTR_NAME),
          styled(new Label(String.valueOf(focus.getRating())), STYLE_ATTR_VALUE),
          styled(new Label(state), STYLE_ATTR_RANGE),
          styled(new Label(String.valueOf(ch.isCreated() ? foci.getBindingKarma(focus) : focus.getRating())),
              STYLE_ATTR_RANGE));
    }
    Label hint = styled(new Label("Binden und Aktivieren im Bereich Ausrüstung, im Detail des Fokus."),
        STYLE_ATTR_RANGE);
    box.getChildren().addAll(withRightFiller(grid), hint);
    return box;
  }

  private static @NonNull Label styled(@NonNull Label label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
