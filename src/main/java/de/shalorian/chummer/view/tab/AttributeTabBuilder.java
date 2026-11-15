package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_ABBR;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_AUG;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_AUG_BOOSTED;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.CharacterAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.attribute.SpecialAttributeType;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.grid.AttributeRowLabel;
import de.shalorian.chummer.view.grid.GridSupport;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Attribute-Tab: körperliche und geistige Attribute mit Aug-Spalte (Panzerungsbehinderung), Sonderattribute
 * Edge/Magie/Resonanz und die Essenz-Anzeige. BP- bzw. Karma-Kosten über {@link IncrementWiring}.
 */
public final class AttributeTabBuilder {

  /**
   * Platzhalter für den augmentierten Attributwert bzw. das augmentierte Metatyp-Maximum, solange es noch keine Essenz-/Cyberware-Berechnung gibt
   * (Roadmap Stufe 2, Cyberware/Bioware). Struktur (eigene Spalte + Klammerzusatz im Bereichs-Label, analog zum alten WinForms-UI) ist bereits
   * vorbereitet — bei Einführung der echten Berechnung wird hier nur dieser Platzhalter ersetzt.
   */
  private static final String ATTRIBUTE_AUGMENTED_PLACEHOLDER = "—";
  /**
   * Feste Breite der Attribut-"Wert"-Spalte — einzige Rating-Spalte, die zweistellig werden kann (Metatyp-Varianten erlauben Attributmaxima bis 10,
   * siehe {@code max="10"} in den Metatyp-XML-Dateien). Alle anderen Rating-Spalten (Fertigkeiten, Wissensfertigkeiten, Kontakte, Gruppen) sind durch
   * {@code MAX_RATING = 6} einstellig und brauchen daher keine feste Breite — ohne diese Ausnahme würde die Spalte beim Überschreiten von 9 sichtbar
   * nachspringen, da die direkt angrenzenden +/−-Buttons mitverschoben würden.
   */
  private static final int ATTRIBUTE_VALUE_COLUMN_WIDTH = 46;
  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();

  private final @NonNull VBox attributeGridHost;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> attrRefreshers = new ArrayList<>();
  private final @NonNull List<Runnable> specialAttrRefreshers = new ArrayList<>();
  private @Nullable Character character;

  public AttributeTabBuilder(@NonNull VBox attributeGridHost, @NonNull IncrementWiring wiring) {
    this.attributeGridHost = attributeGridHost;
    this.wiring = wiring;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Bewertet die +/−-Zeilen dieses Tabs neu (Restpunkte, Karma, Caps). */
  public void refresh() {
    attrRefreshers.forEach(Runnable::run);
    specialAttrRefreshers.forEach(Runnable::run);
  }

  public void build() {
    if (character == null) {
      return;
    }

    attributeGridHost.getChildren().clear();
    attrRefreshers.clear();

    MigPane grid = buildAttributeMigPane();

    boolean career = character.isCreated();
    String[] headers = {"Attribut", "", "", "Wert", "", "(Aug)", "Bereich",
        career ? "Karma" : "BP-Kosten"};
    addHeaderRow(grid, headers);

    int row = 1;
    for (AttributeType type : AttributeType.values()) {
      addAttributeRow(grid, row++, type, character.getAttribute(type), character);
    }

    attributeGridHost.getChildren().add(withRightFiller(grid));

    buildSpecialAttributeSection();
  }

  /**
   * Baut die Sektion für die Sonderattribute Edge/Magic/Resonance unterhalb des Standardattribut-Grids. Edge ist immer sichtbar; Magic bzw. Resonance
   * werden nur gerendert, wenn der Charakter eine entsprechende Quality besitzt (siehe {@link Character#hasMagic()}/
   * {@link Character#hasResonance()}) — analog zum Verbergen im Original, siehe SR4-Regelwerksabgleich.
   */
  private void buildSpecialAttributeSection() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    specialAttrRefreshers.clear();

    Label sectionLabel = new Label("Sonderattribute");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(12, 0, 6, 0));
    attributeGridHost.getChildren().add(sectionLabel);

    MigPane grid = buildAttributeMigPane();

    addSpecialAttributeRow(grid, 0, ch.getEdge(), ch);

    SpecialAttribute magicOrResonance = ch.getMagicOrResonance();
    if (magicOrResonance != null) {
      addSpecialAttributeRow(grid, 1, magicOrResonance, ch);
    }

    attributeGridHost.getChildren().add(withRightFiller(grid));
    attributeGridHost.getChildren().add(buildEssenceInfoLabel(ch));
  }

  /**
   * Essenz-Zeile unter den Sonderattributen. Weist zusätzlich auf eine Magie-/Resonanz-Reduktion durch Essenzverlust hin, damit der Unterschied
   * zwischen angezeigtem (wirksamem) Wert und den nach dem investierten Wert berechneten BP-/Karma-Kosten nachvollziehbar bleibt.
   */
  private @NonNull Label buildEssenceInfoLabel(@NonNull Character ch) {
    String text = "Essenz: " + ESSENCE_FORMAT.toString(ch.getEssence());
    SpecialAttribute magicOrResonance = ch.getMagicOrResonance();
    int penalty = ch.getEssencePenalty();
    if (magicOrResonance != null && penalty > 0) {
      text += "  —  " + magicOrResonance.getType().getDisplayName() + " (Wert und Maximum) um "
          + penalty + " reduziert";
    }
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_ATTR_RANGE);
    VBox.setMargin(label, new Insets(8, 0, 0, 0));
    return label;
  }

  /**
   * Sonderattribut-Zeile. Für Magie/Resonanz zeigen Wert und Maximum die durch Essenzverlust reduzierten Werte
   * ({@link Character#getEffectiveMagicOrResonanceValue()}/{@link Character#getEffectiveMagicOrResonanceMax()}); Grenzen, +/−-Logik und
   * BP-/Karma-Kosten arbeiten dagegen weiterhin auf dem investierten Wert. Da investierter Wert und Maximum um dieselbe Penalty verschoben werden,
   * bleibt die Sperre am Maximum trotzdem konsistent mit der Anzeige.
   */
  private void addSpecialAttributeRow(@NonNull MigPane grid, int row,
      @NonNull SpecialAttribute attr, @NonNull Character character) {
    SpecialAttributeType type = attr.getType();
    boolean reducedByEssence = type != SpecialAttributeType.EDGE;
    int displayMax = reducedByEssence ? character.getEffectiveMagicOrResonanceMax() : attr.getMax();
    addAttributeGridRow(grid, row, specialAttrRefreshers,
        new AttributeRowLabel(type.getDisplayName(), type.getAbbreviation(), attr.getLowerBound(),
            displayMax, ""), null, new RowModel() {
          @Override
          public int value() {
            return attr.getValue();
          }

          @Override
          public @NonNull String valueDisplayText() {
            return String.valueOf(reducedByEssence
                ? character.getEffectiveMagicOrResonanceValue()
                : attr.getValue());
          }

          @Override
          public int min() {
            return attr.getLowerBound();
          }

          @Override
          public int max() {
            return attr.getMax();
          }

          @Override
          public int bpStepCost() {
            return 10;
          }

          @Override
          public @NonNull String bpDisplayText() {
            return attr.getBpCost() + " BP";
          }

          @Override
          public int karmaStepCost() {
            return attr.getKarmaCost();
          }

          @Override
          public void increment() {
            attr.setValue(attr.getValue() + 1);
          }

          @Override
          public void decrement() {
            attr.setValue(attr.getValue() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return type.getDisplayName() + " " + from + " → " + to;
          }
        }, character);
  }

  private void addAttributeRow(@NonNull MigPane grid, int row, @NonNull AttributeType type,
      @NonNull CharacterAttribute attr, @NonNull Character character) {
    addAttributeGridRow(grid, row, attrRefreshers,
        new AttributeRowLabel(type.getDisplayName(), type.getAbbreviation(), attr.getMin(),
            attr.getMax(), String.valueOf(character.getAugmentedMaximum(type))),
        type, new RowModel() {
          @Override
          public int value() {
            return attr.getValue();
          }

          @Override
          public int min() {
            return attr.getMin();
          }

          @Override
          public int max() {
            return attr.getMax();
          }

          @Override
          public boolean canIncrement() {
            return character.canRaiseAttributeAtCreation(type);
          }

          @Override
          public int bpStepCost() {
            return 10;
          }

          @Override
          public @NonNull String bpDisplayText() {
            return attr.getBpCost() + " BP";
          }

          @Override
          public int karmaStepCost() {
            return character.getAttributeKarmaCost(type);
          }

          @Override
          public void increment() {
            attr.setValue(attr.getValue() + 1);
          }

          @Override
          public void decrement() {
            attr.setValue(attr.getValue() - 1);
          }

          @Override
          public @NonNull String actionLabel(int from, int to) {
            return type.getDisplayName() + " " + from + " → " + to;
          }
        }, character);
  }

  /**
   * Baut eine Attribut-Zeile (Name, Abkürzung, +/−, Wert, Aug-Platzhalter, Bereich, BP-/Karma-Kosten) in ein MigPane ein und verdrahtet sie über
   * {@link IncrementWiring#wire}. Gemeinsam genutzt von {@link #addAttributeRow} und {@link #addSpecialAttributeRow}, die sich nur in Datenquelle und
   * {@link RowModel#canIncrement()} unterscheiden. Spaltenreihenfolge orientiert sich strukturell am alten WinForms-{@code panAttributes}
   * (Name/Abkürzung, Basiswert, augmentierter Wert, Metatyp-Grenzen inkl. augmentiertem Maximum). Die Aug-Spalte zeigt den wirksamen Wert von
   * {@code augmentedType} (bei jedem Refresh neu ermittelt, damit z.B. eine KON-Änderung die Panzerungsbehinderung sofort nachzieht), verstärkt
   * in der Akzentfarbe, bzw. {@link #ATTRIBUTE_AUGMENTED_PLACEHOLDER} bei Sonderattributen ({@code null}); der Klammerzusatz im Bereichs-Label zeigt das
   * augmentierte Maximum ({@link Character#getAugmentedMaximum(AttributeType)}, GRW S. 91), bei Sonderattributen keinen.
   */
  private void addAttributeGridRow(@NonNull MigPane grid, int row,
      @NonNull List<Runnable> refresherList, @NonNull AttributeRowLabel label,
      @Nullable AttributeType augmentedType, @NonNull RowModel model,
      @NonNull Character character) {
    Label attrNameLabel = new Label(label.displayName());
    Label abbrLabel = new Label(label.abbreviation());
    Button minus = new Button("−");
    Label valueLabel = new Label();
    Button plus = new Button("+");
    Label augLabel = new Label(ATTRIBUTE_AUGMENTED_PLACEHOLDER);
    Label rangeLabel = new Label(label.min() + " / " + label.max()
        + (label.augmentedMaxText().isEmpty() ? "" : " (" + label.augmentedMaxText() + ")"));
    Label bpLabel = new Label();

    attrNameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    abbrLabel.getStyleClass().add(STYLE_ATTR_ABBR);
    minus.getStyleClass().add(STYLE_ATTR_BTN);
    valueLabel.getStyleClass().add(STYLE_ATTR_VALUE);
    plus.getStyleClass().add(STYLE_ATTR_BTN);
    augLabel.getStyleClass().add(STYLE_ATTR_AUG);
    rangeLabel.getStyleClass().add(STYLE_ATTR_RANGE);
    bpLabel.getStyleClass().add(STYLE_ATTR_BP);

    if (augmentedType != null) {
      Runnable refreshAugmented = () -> {
        augLabel.setText(augmentedAttributeText(character, augmentedType));
        // Verstärkte Werte (Implantate, Adeptenkräfte) im Akzent; Abzüge durch Behinderung bleiben neutral.
        augLabel.getStyleClass().remove(STYLE_ATTR_AUG_BOOSTED);
        if (character.getAugmentedAttributeValue(augmentedType)
            > character.getAttribute(augmentedType).getValue()) {
          augLabel.getStyleClass().add(STYLE_ATTR_AUG_BOOSTED);
        }
      };
      refresherList.add(refreshAugmented);
      refreshAugmented.run();
    }
    wiring.wire(refresherList, new RowControls(valueLabel, bpLabel, minus, plus),
        character.isCreated(), model, character);

    addRowCells(grid, row, attrNameLabel, abbrLabel, minus, valueLabel, plus, augLabel, rangeLabel,
        bpLabel);
  }

  /**
   * Gemeinsames Spalten-Layout für Standard- und Sonderattribut-Grid: alle Spalten inhaltsbasiert ohne eigenes Wachstum — ungenutzter Platz wird
   * stattdessen außen über {@link GridSupport#withRightFiller} verteilt. Die Wert-Spalte ist die einzige feste Ausnahme, siehe
   * {@link #ATTRIBUTE_VALUE_COLUMN_WIDTH}.
   */
  private @NonNull MigPane buildAttributeMigPane() {
    return new MigPane(MIGPANE_HIDEMODE_3,
        "[][center][center][" + ATTRIBUTE_VALUE_COLUMN_WIDTH
            + "!,center][center][center][][]");
  }

  /**
   * Inhalt der Aug-Spalte eines Körperlichen/Geistigen Attributs: der wirksame Wert (derzeit nur durch Panzerungsbehinderung auf GES/REA reduziert,
   * siehe {@link Character#getAugmentedAttributeValue(AttributeType)}), sofern er vom investierten Wert abweicht.
   */
  private static @NonNull String augmentedAttributeText(@NonNull Character character,
      @NonNull AttributeType type) {
    int augmented = character.getAugmentedAttributeValue(type);
    return augmented == character.getAttribute(type).getValue()
        ? ATTRIBUTE_AUGMENTED_PLACEHOLDER
        : String.valueOf(augmented);
  }
}
