package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.tab.StatusCards.CARD_GAP;
import static de.shalorian.chummer.view.tab.StatusCards.CARD_WIDTH;
import static de.shalorian.chummer.view.tab.StatusCards.card;
import static de.shalorian.chummer.view.tab.StatusCards.columns;
import static de.shalorian.chummer.view.tab.StatusCards.figure;
import static de.shalorian.chummer.view.tab.StatusCards.poolRow;
import static de.shalorian.chummer.view.tab.StatusCards.titled;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.condition.CheatSheetItem;
import de.shalorian.chummer.model.character.metatype.Movement;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.utils.Numbers;
import java.util.Optional;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Bereich „Cheat-Sheet“ (ADR-065): Nachschlagewerte, die der Nutzer in den Einstellungen wählt. Die Karte „PROBEN“
 * zeigt Wahrnehmung und Ausweichen, die Karte „BEWEGUNG“ Gehen, Laufen und Sprinten (GRW S. 177); ohne Auswahl steht
 * dort ein Hinweis. Zustandsmonitor, Initiative und Attributproben stehen nur im Status-Panel. Der Bereich ist klein und
 * wird bei jedem {@link #refresh()} komplett neu aufgebaut.
 */
public final class CheatSheetTabBuilder {

  private final @NonNull VBox container;
  private @Nullable Character character;
  private @NonNull Set<CheatSheetItem> items = Set.of();

  public CheatSheetTabBuilder(@NonNull VBox container) {
    this.container = container;
    container.setSpacing(CARD_GAP);
  }

  /** Charakter, den dieser Bereich anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Gewählte Inhalte (Anzeige-Einstellung); wirken beim nächsten Aufbau. */
  public void setItems(@NonNull Set<CheatSheetItem> items) {
    this.items = Set.copyOf(items);
  }

  /** Baut den Bereich neu auf, siehe Klassenbeschreibung. */
  public void refresh() {
    build();
  }

  public void build() {
    if (character == null) {
      return;
    }
    container.getChildren().clear();
    if (items.isEmpty()) {
      Label hint = new Label("Inhalte unter Einstellungen › Einstellungen… › Cheat-Sheet auswählen");
      hint.getStyleClass().add(STYLE_DETAIL_HINT);
      container.getChildren().add(hint);
      return;
    }
    VBox tests = new VBox(4);
    for (CheatSheetItem item : CheatSheetItem.values()) {
      if (items.contains(item) && item.getSkillName() != null) {
        Optional<DicePool> pool = character.getCheatSheetPool(item);
        tests.getChildren().add(poolRow(item.getDisplayName(), pool.map(p -> String.valueOf(p.dice())).orElse("–"),
            pool.map(DicePool::breakdown).orElse(null)));
      }
    }
    if (!tests.getChildren().isEmpty()) {
      container.getChildren().add(sized(card(titled("PROBEN", tests))));
    }
    if (items.contains(CheatSheetItem.MOVEMENT)) {
      container.getChildren().add(sized(movement(character)));
    }
  }

  /** Karte so breit wie im Status-Panel statt über die ganze Seite. */
  private static @NonNull VBox sized(@NonNull VBox card) {
    card.setMaxWidth(CARD_WIDTH);
    return card;
  }

  /** Gehen, Laufen und Sprinten nebeneinander; die Erklärungen stehen im Tooltip (GRW S. 177). */
  private static @NonNull VBox movement(@NonNull Character ch) {
    Movement movement = ch.getMovement();
    Optional<DicePool> sprint = ch.getSprintPool();
    GridPane grid = columns(3);
    grid.add(withTooltip(figure(Numbers.integer(movement.walk()) + " m", "Gehen", false),
        "je Kampfrunde, auf die Initiativedurchgänge verteilt (GRW S. 177)"), 0, 0);
    grid.add(withTooltip(figure(Numbers.integer(movement.run()) + " m", "Laufen", false),
        "je Kampfrunde; laufend −2 auf Proben außer Sturmangriff und Verteidigung"), 1, 0);
    grid.add(withTooltip(figure(sprint.map(p -> String.valueOf(p.dice())).orElse("–"), "Sprinten", false),
        "Einfache Handlung, Laufen + Stärke; +2 m Laufen je Erfolg"
            + sprint.map(p -> "\n" + p.breakdown()).orElse("")), 2, 0);
    return card(titled("BEWEGUNG", grid));
  }

  private static @NonNull Node withTooltip(@NonNull Node node, @NonNull String text) {
    Tooltip.install(node, new Tooltip(text));
    return node;
  }
}
