package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.situation.SituationalPool;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Abschnitt „Situative Würfelpools“ (Magie- und Adepten-Tab): je Anlass der Pool mit allen Boni, die nur dort gelten
 * (Foki, Schutzpatron, Adeptenkräfte) und ihrer Aufschlüsselung. Baut sich bei jedem {@link #build}
 * neu auf, damit Attribut- und Fertigkeitsänderungen sofort sichtbar sind.
 */
final class SituationalPoolSection {

  private final @NonNull VBox box = new VBox();

  /** Baut den Abschnitt für {@code ch} (neu) und liefert ihn zum Einhängen. */
  @NonNull Node build(@NonNull Character ch) {
    box.getChildren().clear();
    Separator separator = new Separator();
    VBox.setMargin(separator, new Insets(20, 0, 12, 0));
    Label title = new Label("Situative Würfelpools");
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    box.getChildren().addAll(separator, title);
    List<SituationalPool> pools = ch.getSituationalPools();
    if (pools.isEmpty()) {
      Label empty = new Label("Keine (aktive Foki, Schutzpatron oder Adeptenkräfte geben hier Würfel)");
      empty.getStyleClass().add(STYLE_ATTR_RANGE);
      box.getChildren().add(empty);
      return box;
    }
    MigPane grid = new MigPane("", "[][]24[]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(8, 0, 0, 0));
    addHeaderRow(grid, "Anlass", "Würfel", "Aufschlüsselung");
    int row = 1;
    for (SituationalPool pool : pools) {
      Label label = styled(new Label(pool.label()), STYLE_ATTR_NAME);
      Label dice = styled(new Label(pool.diceText()), STYLE_ATTR_VALUE);
      Label breakdown = styled(new Label(pool.breakdown()), STYLE_ATTR_RANGE);
      addRowCells(grid, row++, label, dice, breakdown);
    }
    box.getChildren().add(withRightFiller(grid));
    return box;
  }

  private static @NonNull Label styled(@NonNull Label label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
