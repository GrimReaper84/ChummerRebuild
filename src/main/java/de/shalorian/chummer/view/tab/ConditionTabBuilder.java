package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.view.grid.ConditionBoxes;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Tab „Zustand“: Zustandsmonitor (SR4-Grundregelwerk S. 94, 192/193), Initiative (S. 172, 227) und Attributproben
 * (S. 166/167), wie „Other Info“ und „Condition Monitor“ im alten Chummer. Schaden lässt sich nur im Karrieremodus
 * eintragen: ein Klick auf Kästchen n setzt den Schaden auf n, ein Klick auf das letzte gefüllte Kästchen leert es.
 * Der Tab ist klein und wird bei jedem {@link #refresh()} komplett neu aufgebaut, weil sich mit Konstitution,
 * Willenskraft oder Qualities auch die Anzahl der Kästchen ändert.
 */
public final class ConditionTabBuilder {

  private static final String STYLE_BOX_FILLED_STUN = "cm-box-filled-stun";
  private static final String STYLE_BOX_OVERFLOW = "cm-box-overflow";
  private static final int BOXES_PER_ROW = 3;

  private final @NonNull VBox container;
  private final @NonNull CharacterTabHost host;
  private @Nullable Character character;

  public ConditionTabBuilder(@NonNull VBox container, @NonNull CharacterTabHost host) {
    this.container = container;
    this.host = host;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Baut den Tab neu auf, siehe Klassenbeschreibung. */
  public void refresh() {
    build();
  }

  public void build() {
    if (character == null) {
      return;
    }
    container.getChildren().clear();
    container.getChildren().add(sectionTitle("Zustandsmonitor", 0));
    container.getChildren().add(buildMonitor(character));
    container.getChildren().add(woundLabel(character));
    container.getChildren().add(sectionTitle("Initiative", 16));
    container.getChildren().add(withRightFiller(buildInitiativeGrid(character)));
    container.getChildren().add(sectionTitle("Attributproben", 16));
    container.getChildren().add(withRightFiller(buildAttributeTestGrid(character)));
  }

  private static @NonNull Label sectionTitle(@NonNull String text, int top) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(label, new Insets(top, 0, 6, 0));
    return label;
  }

  private @NonNull HBox buildMonitor(@NonNull Character ch) {
    VBox physical = new VBox(6, new Label("Körperlich (%d)".formatted(ch.getPhysicalBoxes())),
        boxGrid(ch, ch.getPhysicalBoxes(), 0, ch.getPhysicalDamage(), false, false, ch::setPhysicalDamage));
    physical.getChildren().add(new Label("Überzahl (%d)".formatted(ch.getPhysicalOverflowBoxes())));
    physical.getChildren().add(boxGrid(ch, ch.getPhysicalOverflowBoxes(), ch.getPhysicalBoxes(),
        ch.getPhysicalDamage(), true, false, ch::setPhysicalDamage));
    VBox stun = new VBox(6, new Label("Geistig (%d)".formatted(ch.getStunBoxes())),
        boxGrid(ch, ch.getStunBoxes(), 0, ch.getStunDamage(), false, true, ch::setStunDamage));
    physical.getChildren().forEach(n -> n.getStyleClass().add(STYLE_ATTR_NAME));
    stun.getChildren().forEach(n -> n.getStyleClass().add(STYLE_ATTR_NAME));
    return new HBox(40, physical, stun);
  }

  /**
   * Kästchenraster einer Spalte. {@code offset} ist die Anzahl Kästchen davor (bei den Überzahlkästchen die
   * Körperliche Spalte), damit Kästchen n immer für n Schaden steht. Kästchen, ab denen ein weiterer
   * Verletzungsmodifikator gilt, tragen ihn als Beschriftung. Gefüllte Kästchen der geistigen Spalte ({@code stun})
   * sind blau statt rot, wie in der Status-Übersicht.
   */
  private @NonNull GridPane boxGrid(@NonNull Character ch, int count, int offset, int damage, boolean overflow,
      boolean stun, @NonNull IntConsumer setDamage) {
    return ConditionBoxes.grid(count, offset, damage, BOXES_PER_ROW, ch.isCreated(),
        box -> overflow ? "" : woundMarker(ch, box),
        overflow ? List.of(STYLE_BOX_OVERFLOW) : List.of(), stun ? List.of(STYLE_BOX_FILLED_STUN) : List.of(),
        newDamage -> {
          setDamage.accept(newDamage);
          host.refreshRows();
        });
  }

  /** "−n", wenn mit diesem Kästchen der Verletzungsmodifikator der Spalte auf −n steigt, sonst leer. */
  private static @NonNull String woundMarker(@NonNull Character ch, int box) {
    int modifier = ch.getColumnWoundModifier(box);
    return modifier != ch.getColumnWoundModifier(box - 1) ? "−" + -modifier : "";
  }

  private static @NonNull Label woundLabel(@NonNull Character ch) {
    int wound = ch.getWoundModifier();
    Label label = new Label("Verletzungsmodifikator: " + (wound == 0 ? "keiner" : "−" + -wound)
        + (ch.isCreated() ? "" : "  (Schaden erst im Karrieremodus eintragbar)"));
    label.getStyleClass().add(STYLE_DETAIL_HINT);
    VBox.setMargin(label, new Insets(8, 0, 0, 0));
    return label;
  }

  private static @NonNull MigPane buildInitiativeGrid(@NonNull Character ch) {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][center][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    int wound = ch.getWoundModifier();
    String initiativeHint = wound == 0
        ? "Reaktion + Intuition, max. %d".formatted(ch.getInitiativeMaximum())
        : "%d − %d Verletzung, max. %d".formatted(ch.getInitiativeAttribute(), -wound, ch.getInitiativeMaximum());
    addRowCells(grid, 0, nameLabel("Initiative"), poolLabel(String.valueOf(ch.getInitiative()), null),
        hintLabel("%s; %d Durchgang".formatted(initiativeHint, ch.getInitiativePasses())));
    if (ch.hasAstralAccess()) {
      addRowCells(grid, 1, nameLabel("Astrale Initiative"),
          poolLabel(String.valueOf(ch.getAstralInitiative()), null),
          hintLabel("Intuition × 2; %d Durchgänge".formatted(Character.ASTRAL_INITIATIVE_PASSES)));
    }
    return grid;
  }

  private static @NonNull MigPane buildAttributeTestGrid(@NonNull Character ch) {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][][center]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    int row = 0;
    for (AttributeTest test : AttributeTest.values()) {
      DicePool pool = ch.getAttributeTestPool(test);
      Label attributes = new Label("%s + %s".formatted(test.getFirst().getAbbreviation(),
          test.getSecond().getAbbreviation()));
      attributes.getStyleClass().add(STYLE_ATTR_ABBR);
      addRowCells(grid, row++, nameLabel(test.getDisplayName()), attributes,
          poolLabel(String.valueOf(pool.dice()), "%s (GRW S. %d)".formatted(pool.breakdown(), test.getPage())));
    }
    return grid;
  }

  private static @NonNull Label nameLabel(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_ATTR_NAME);
    return label;
  }

  private static @NonNull Label poolLabel(@NonNull String text, @Nullable String tooltip) {
    Label label = new Label(text);
    label.getStyleClass().add(DicePoolLabel.STYLE_POOL);
    if (tooltip != null) {
      label.setTooltip(new Tooltip(tooltip));
    }
    return label;
  }

  private static @NonNull Label hintLabel(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_DETAIL_HINT);
    return label;
  }
}
