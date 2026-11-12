package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

/**
 * Status-Übersicht des Charakters als Karten: Kopf (Name, Metatyp, Modus), Initiative, Zustandsmonitor, Panzerung,
 * Essenz, Sonderattribute und Hinweise ({@link StatusHints}). Sie wird bei jeder Änderung neu gebaut und im Slide-Panel
 * oder als statischer Bereich gezeigt (Plan UI-Rework, Abschnitt 6). Im Karrieremodus trägt ein Klick auf ein Kästchen
 * des Zustandsmonitors Schaden ein wie im Bereich „Zustand“; bei der Erschaffung sind die Kästchen gesperrt.
 */
public final class StatusOverviewBuilder {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  private static final double CARD_GAP = 12;
  /** Bevorzugte Breite der Übersicht; das Slide-Panel misst sie und ist mindestens so breit. */
  private static final double WIDTH = 400;
  private static final String CREATION_HINT = "Schaden erst im Karrieremodus eintragbar";

  private StatusOverviewBuilder() {
  }

  /**
   * Baut die Übersicht für {@code character}.
   *
   * @param onDamageChanged nach einem Klick auf ein Kästchen aufgerufen, damit alle Anzeigen den Schaden nachziehen
   */
  public static @NonNull Node build(@NonNull Character character, @NonNull Runnable onDamageChanged) {
    VBox root = new VBox(CARD_GAP);
    root.setPrefWidth(WIDTH);
    root.getChildren().addAll(header(character), initiative(character), conditionMonitor(character, onDamageChanged),
        protectionAndEssence(character), specialAttributes(character));
    List<String> hints = StatusHints.of(character);
    if (!hints.isEmpty()) {
      root.getChildren().add(hintCard(hints));
    }
    return root;
  }

  private static @NonNull Node header(@NonNull Character c) {
    Label name = styled(new Label(c.getName()), "page-title");
    Label meta = styled(new Label(c.getMetatype().name() + " · " + (c.isCreated() ? "Karrieremodus" : "Erschaffung")),
        "key-caption");
    return card(new VBox(2, name, meta));
  }

  private static @NonNull Node initiative(@NonNull Character c) {
    int wound = c.getWoundModifier();
    String composition = "REA %d + INT %d".formatted(c.getAugmentedAttributeValue(AttributeType.REA),
        c.getAugmentedAttributeValue(AttributeType.INT)) + (wound == 0 ? "" : " − " + -wound + " Verletzung");
    String astral = c.hasAstralAccess() ? String.valueOf(c.getAstralInitiative()) : "–";
    GridPane grid = columns(3);
    grid.add(figure(String.valueOf(c.getInitiative()), composition, false), 0, 0);
    grid.add(figure(String.valueOf(c.getInitiativePasses()), "Durchgänge", c.getInitiativePasses() > 1), 1, 0);
    grid.add(figure(astral, c.hasAstralAccess()
        ? "Astral, " + Character.ASTRAL_INITIATIVE_PASSES + " Durchgänge" : "Astral", false), 2, 0);
    return card(titled("INITIATIVE", grid));
  }

  private static @NonNull Node conditionMonitor(@NonNull Character c, @NonNull Runnable onDamageChanged) {
    int physicalBoxes = c.getPhysicalBoxes();
    int physicalDamage = c.getPhysicalDamage();
    int overflow = Math.max(0, physicalDamage - physicalBoxes);
    String physicalText = "%d / %d · Überzahl %d / %d".formatted(Math.min(physicalDamage, physicalBoxes),
        physicalBoxes, overflow, c.getPhysicalOverflowBoxes());
    String stunText = "%d / %d".formatted(c.getStunDamage(), c.getStunBoxes());

    HBox title = new HBox(styled(new Label("ZUSTANDSMONITOR"), "card-title"));
    int wound = c.getWoundModifier();
    if (wound < 0) {
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      title.getChildren().addAll(spacer, styled(new Label("Verletzung " + wound), "warn-text"));
    }
    VBox box = new VBox(10, title,
        monitorRow(c, "Körperlich", physicalText, physicalBoxes, physicalDamage, "cm-mini-physical",
            c::setPhysicalDamage, onDamageChanged),
        monitorRow(c, "Geistig", stunText, c.getStunBoxes(), c.getStunDamage(), "cm-mini-stun",
            c::setStunDamage, onDamageChanged));
    return card(box);
  }

  /**
   * Eine Spalte des Monitors; Kästchen n steht für n Schaden. Ein Klick setzt den Schaden auf n, ein Klick auf das
   * letzte gefüllte Kästchen nimmt eines zurück (wie im Bereich „Zustand“).
   */
  private static @NonNull Node monitorRow(@NonNull Character c, @NonNull String label, @NonNull String values,
      int boxes, int damage, @NonNull String filledStyle, @NonNull IntConsumer setDamage,
      @NonNull Runnable onDamageChanged) {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox caption = new HBox(styled(new Label(label), "attr-name"), spacer, styled(new Label(values), "key-caption"));
    FlowPane row = new FlowPane(4, 4);
    for (int i = 0; i < boxes; i++) {
      int box = i + 1;
      Button button = new Button();
      button.getStyleClass().setAll("cm-mini");
      if (box <= damage) {
        button.getStyleClass().add(filledStyle);
      }
      button.setDisable(!c.isCreated());
      button.setOnAction(_ -> {
        setDamage.accept(box == damage ? box - 1 : box);
        onDamageChanged.run();
      });
      row.getChildren().add(button);
    }
    if (!c.isCreated()) {
      // Deaktivierte Knöpfe zeigen keinen Tooltip, deshalb trägt ihn die Zeile.
      Tooltip.install(row, new Tooltip(CREATION_HINT));
    }
    return new VBox(4, caption, row);
  }

  private static @NonNull Node protectionAndEssence(@NonNull Character c) {
    int penalty = c.getEquipment().getArmorEncumbrancePenalty();
    String armorCaption = "Ballistik / Stoß · " + (penalty == 0 ? "keine Behinderung" : "Behinderung " + penalty);
    VBox armorBox = titled("PANZERUNG",
        figure(c.getArmorBallistic() + " / " + c.getArmorImpact(), armorCaption, false));
    armorBox.getChildren().addAll(armorExtras(c));
    Node armor = card(armorBox);
    String essenceCaption = "Cyberware %s · Bioware %s".formatted(ESSENCE_FORMAT.toString(c.getCyberwareEssenceLoss()),
        ESSENCE_FORMAT.toString(c.getBiowareEssenceLoss()));
    Node essence = card(titled("ESSENZ", figure(ESSENCE_FORMAT.toString(c.getEssence()), essenceCaption, false)));
    GridPane grid = columns(2);
    grid.setHgap(CARD_GAP);
    grid.add(armor, 0, 0);
    grid.add(essence, 1, 0);
    return grid;
  }

  /**
   * Sonderschutz der angelegten Rüstung (GRW S. 372) und Schild-Malus als kurze Zeilen unter der Panzerung, z.B.
   * „gegen Elektrizität 11/9“, „Infiltration gegen IR +2 Würfel“, „Schild: −1 auf körperliche Proben“.
   */
  private static @NonNull List<Label> armorExtras(@NonNull Character c) {
    List<Label> lines = new ArrayList<>();
    for (SpecialProtection protection : c.getEquipment().getSpecialProtections()) {
      lines.add(styled(new Label(protection.toString()), "effect-text"));
    }
    if (c.getEquipment().hasShieldEquipped()) {
      lines.add(styled(new Label("Schild: −1 auf körperliche Proben"), "warn-text"));
    }
    lines.forEach(line -> line.setWrapText(true));
    return lines;
  }

  private static @NonNull Node specialAttributes(@NonNull Character c) {
    GridPane grid = columns(2);
    grid.add(figure(String.valueOf(c.getEdge().getValue()), "Edge", false), 0, 0);
    SpecialAttribute magicOrResonance = c.getMagicOrResonance();
    if (magicOrResonance == null) {
      grid.add(figure("–", "Magie / Resonanz", false), 1, 0);
    } else {
      grid.add(figure(String.valueOf(c.getEffectiveMagicOrResonanceValue()),
          magicOrResonance.getType().getDisplayName(), false), 1, 0);
    }
    return card(titled("SONDERATTRIBUTE", grid));
  }

  private static @NonNull Node hintCard(@NonNull List<String> hints) {
    VBox box = new VBox(4, styled(new Label("HINWEISE"), "hint-card-title"));
    for (String hint : hints) {
      Label line = styled(new Label("• " + hint), "hint-text");
      line.setWrapText(true);
      box.getChildren().add(line);
    }
    box.getStyleClass().add("hint-card");
    return box;
  }

  private static @NonNull VBox titled(@NonNull String title, @NonNull Node content) {
    return new VBox(8, styled(new Label(title), "card-title"), content);
  }

  private static @NonNull Node figure(@NonNull String value, @NonNull String caption, boolean accent) {
    Label number = styled(new Label(value), "key-figure");
    if (accent) {
      number.getStyleClass().add("key-figure-accent");
    }
    Label text = styled(new Label(caption), "key-caption");
    text.setWrapText(true);
    return new VBox(0, number, text);
  }

  private static @NonNull Node card(@NonNull Node content) {
    VBox card = new VBox(content);
    card.getStyleClass().add("card");
    return card;
  }

  /** Raster mit {@code count} gleich breiten Spalten. */
  private static @NonNull GridPane columns(int count) {
    GridPane grid = new GridPane();
    grid.setHgap(8);
    for (int i = 0; i < count; i++) {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(100.0 / count);
      grid.getColumnConstraints().add(column);
    }
    return grid;
  }

  private static <T extends Label> @NonNull T styled(@NonNull T label, @NonNull String styleClass) {
    label.getStyleClass().add(styleClass);
    return label;
  }
}
