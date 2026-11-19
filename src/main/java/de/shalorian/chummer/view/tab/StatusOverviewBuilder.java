package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.tab.StatusCards.CARD_GAP;
import static de.shalorian.chummer.view.tab.StatusCards.CARD_WIDTH;
import static de.shalorian.chummer.view.tab.StatusCards.card;
import static de.shalorian.chummer.view.tab.StatusCards.columns;
import static de.shalorian.chummer.view.tab.StatusCards.figure;
import static de.shalorian.chummer.view.tab.StatusCards.poolRow;
import static de.shalorian.chummer.view.tab.StatusCards.styled;
import static de.shalorian.chummer.view.tab.StatusCards.titled;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.MatrixAccess;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import de.shalorian.chummer.model.character.condition.AttributeTest;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.situation.SituationalPool;
import de.shalorian.chummer.model.character.skill.DicePool;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import de.shalorian.chummer.view.grid.MonitorBoxRow;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Status-Übersicht des Charakters als Karten: Kopf (Name, Metatyp, Modus), Initiative, Zustandsmonitor mit Überzahl
 * (GRW S. 192/193), Panzerung, Essenz, Sonderattribute (mit dem Durchschnitt angeschlossener Cybergliedmaßen),
 * Attributproben (S. 166/167), situative Pools und Hinweise ({@link StatusHints}). Zustandsmonitor und Initiative
 * stehen nur hier (ADR-065). Sie wird bei jeder Änderung neu gebaut und im Slide-Panel oder als statischer Bereich
 * gezeigt (Plan UI-Rework, Abschnitt 6). Im Karrieremodus trägt ein Klick auf ein Kästchen des Zustandsmonitors
 * Schaden ein; bei der Erschaffung sind die Kästchen gesperrt.
 */
public final class StatusOverviewBuilder {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  private static final String CREATION_HINT = "Schaden erst im Karrieremodus eintragbar";
  private static final String EDGE_CREATION_HINT = "Edgepunkte erst im Karrieremodus nutzbar";

  private StatusOverviewBuilder() {
  }

  /**
   * Baut die Übersicht für {@code character}.
   *
   * @param services        Rückfragen (Edge verheizen)
   * @param onDamageChanged nach einem Klick auf ein Kästchen, auf die Edge-Knöpfe bzw. nach der Wahl des aktiven
   *                        Kommlinks aufgerufen, damit alle Anzeigen nachziehen
   */
  public static @NonNull Node build(@NonNull Character character, @NonNull TabServices services,
      @NonNull Runnable onDamageChanged) {
    VBox root = new VBox(CARD_GAP);
    root.setPrefWidth(CARD_WIDTH);
    root.getChildren().addAll(header(character), initiative(character, onDamageChanged), conditionMonitor(character, onDamageChanged),
        protectionAndEssence(character), specialAttributes(character, services, onDamageChanged));
    root.getChildren().add(attributeTests(character));
    List<SituationalPool> pools = character.getSituationalPools();
    if (!pools.isEmpty()) {
      root.getChildren().add(situationalPools(pools));
    }
    List<String> hints = StatusHints.of(character);
    if (!hints.isEmpty()) {
      root.getChildren().add(hintCard(hints));
    }
    return root;
  }

  /** Situative Würfelpools kompakt: Anlass und Würfel, Aufschlüsselung im Tooltip (Plan Magie abrunden). */
  private static @NonNull Node situationalPools(@NonNull List<SituationalPool> pools) {
    VBox rows = new VBox(4);
    for (SituationalPool pool : pools) {
      rows.getChildren().add(poolRow(pool.label(), pool.diceText(), pool.breakdown()));
    }
    return card(titled("SITUATIVE POOLS", rows));
  }

  /** Die vier Attributproben (GRW S. 166/167), immer sichtbar; Aufschlüsselung und Seite im Tooltip. */
  private static @NonNull Node attributeTests(@NonNull Character c) {
    VBox rows = new VBox(4);
    for (AttributeTest test : AttributeTest.values()) {
      DicePool pool = c.getAttributeTestPool(test);
      rows.getChildren().add(poolRow(test.getDisplayName(), String.valueOf(pool.dice()),
          "%s (GRW S. %d)".formatted(pool.breakdown(), test.getPage())));
    }
    return card(titled("ATTRIBUTPROBEN", rows));
  }

  private static @NonNull Node header(@NonNull Character c) {
    Label name = styled(new Label(c.getName()), "page-title");
    // Der Modus steht nur bei der Erschaffung dabei; im Spiel ist die Karriere der Normalfall.
    Label meta = styled(new Label(c.getMetatype().name() + (c.isCreated() ? "" : " · Erschaffung")), "key-caption");
    return card(new VBox(2, name, meta));
  }

  private static @NonNull Node initiative(@NonNull Character c, @NonNull Runnable onChanged) {
    int wound = c.getWoundModifier();
    String composition = "REA %d + INT %d".formatted(c.getAugmentedAttributeValue(AttributeType.REA),
        c.getAugmentedAttributeValue(AttributeType.INT)) + (wound == 0 ? "" : " − " + -wound + " Verletzung");
    String astral = c.hasAstralAccess() ? String.valueOf(c.getAstralInitiative()) : "–";
    GridPane grid = columns(3);
    Node initiative = figure(String.valueOf(c.getInitiative()), composition, false);
    Tooltip.install(initiative, new Tooltip("Höchstwert %d (GRW S. 172)".formatted(c.getInitiativeMaximum())));
    grid.add(initiative, 0, 0);
    grid.add(figure(String.valueOf(c.getInitiativePasses()), "Durchgänge", c.getInitiativePasses() > 1), 1, 0);
    grid.add(figure(astral, c.hasAstralAccess()
        ? "Astral, " + Character.ASTRAL_INITIATIVE_PASSES + " Durchgänge" : "Astral", false), 2, 0);
    addMatrixRow(grid, c);
    List<Gear> commlinks = c.getEquipment().getCommlinks();
    if (commlinks.size() < 2) {
      return card(titled("INITIATIVE", grid));
    }
    return card(titled("INITIATIVE", new VBox(8, grid, commlinkChooser(c, commlinks, onChanged))));
  }

  /**
   * Auswahl des aktiven Kommlinks bei mehreren (Ausrüstung und Kommlink-Headware); ohne Wahl gilt keins, und die
   * Matrixzeile nennt das. Das Panel wird nach jeder Änderung neu gebaut, die Auswahl veraltet also nicht.
   */
  private static @NonNull Node commlinkChooser(@NonNull Character c, @NonNull List<Gear> commlinks,
      @NonNull Runnable onChanged) {
    List<Gear> carried = c.getEquipment().getGear();
    ComboBox<String> box = new ComboBox<>();
    box.setPromptText("Aktives Kommlink wählen");
    box.getItems().setAll(commlinks.stream()
        .map(g -> g.getDisplayName() + (carried.contains(g) ? "" : " (implantiert)")).toList());
    Gear active = c.getEquipment().getActiveCommlink();
    if (active != null) {
      box.getSelectionModel().select(commlinks.indexOf(active));
    }
    box.getSelectionModel().selectedIndexProperty().addListener((_, _, index) -> {
      if (index.intValue() >= 0) {
        c.getEquipment().setActiveCommlink(commlinks.get(index.intValue()));
        onChanged.run();
      }
    });
    box.setMaxWidth(Double.MAX_VALUE);
    return new VBox(2, styled(new Label("Aktives Kommlink"), "key-caption"), box);
  }

  /**
   * Matrixinitiative (GRW S. 265, 276, 279): Wert und Durchgänge je Zugang, dazu ein Hinweis, was für VR fehlt; in AR
   * gilt die Körperliche Initiative.
   */
  private static void addMatrixRow(@NonNull GridPane grid, @NonNull Character c) {
    MatrixAccess access = c.getMatrixAccess();
    boolean online = access != MatrixAccess.NONE;
    String caption = "Matrix · " + access.getDisplayName() + (access == MatrixAccess.AR ? " (körperlich)" : "");
    grid.add(figure(online ? String.valueOf(c.getMatrixInitiative()) : "–", caption, false), 0, 1);
    grid.add(figure(online ? String.valueOf(c.getMatrixInitiativePasses()) : "–", "Matrix-Durchgänge",
        access.isVirtual()), 1, 1);
    String hint = c.getMatrixAccessHint();
    if (hint != null) {
      Label hintLabel = styled(new Label(hint), "key-caption");
      hintLabel.setWrapText(true);
      grid.add(hintLabel, 2, 1);
    }
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
        monitorRow(c, "Körperlich", physicalText, physicalBoxes, c.getPhysicalOverflowBoxes(), physicalDamage,
            "cm-mini-physical", c::setPhysicalDamage, onDamageChanged),
        monitorRow(c, "Geistig", stunText, c.getStunBoxes(), 0, c.getStunDamage(), "cm-mini-stun",
            c::setStunDamage, onDamageChanged));
    return card(box);
  }

  /**
   * Eine Spalte des Monitors; Kästchen n steht für n Schaden. Ein Klick setzt den Schaden auf n, ein Klick auf das
   * letzte gefüllte Kästchen nimmt eines zurück. Die körperliche Spalte hängt die Überzahl-Kästchen an
   * ({@code overflowBoxes}, GRW S. 192/193), die geistige hat keine.
   */
  private static @NonNull Node monitorRow(@NonNull Character c, @NonNull String label, @NonNull String values,
      int boxes, int overflowBoxes, int damage, @NonNull String filledStyle, @NonNull IntConsumer setDamage,
      @NonNull Runnable onDamageChanged) {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox caption = new HBox(styled(new Label(label), "attr-name"), spacer, styled(new Label(values), "key-caption"));
    // Trenner hinter dem Kästchen, mit dem der Verletzungsmodifikator steigt (S. 193, mit Schmerztoleranz u.Ä.)
    FlowPane row = MonitorBoxRow.build(boxes, overflowBoxes, damage, filledStyle, c.isCreated(),
        box -> c.getColumnWoundModifier(box) != c.getColumnWoundModifier(box - 1), value -> {
          setDamage.accept(value);
          onDamageChanged.run();
        });
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

  /**
   * Edge mit Edgepunkten im Spiel (GRW S. 98/99): groß „verfügbar / Attribut“, direkt darunter ausgeben (−),
   * zurückgewinnen (+), auffrischen und Edge verheizen mit Rückfrage. Knöpfe nur im Karrieremodus; bei der Erschaffung
   * gesperrt, der Tooltip sitzt an der Knopfzeile.
   */
  private static @NonNull Node edgeControls(@NonNull Character c, @NonNull TabServices services,
      @NonNull Runnable onChanged) {
    boolean career = c.isCreated();
    int points = c.getEdgePoints();
    int edge = c.getEdge().getValue();
    Button spend = edgeButton("Edgepunkt ausgeben", FontAwesomeSolid.MINUS, !career || points == 0, () -> {
      c.spendEdge();
      onChanged.run();
    });
    Button regain = edgeButton("Edgepunkt zurückgewinnen", FontAwesomeSolid.PLUS, !career || points == edge, () -> {
      c.regainEdge();
      onChanged.run();
    });
    Button refresh = edgeButton("Edge auffrischen (auf vollen Wert)", FontAwesomeSolid.UNDO, !career || points == edge,
        () -> {
          c.refreshEdge();
          onChanged.run();
        });
    Button burn = edgeButton("Edge verheizen (S. 99): Edge dauerhaft −1", FontAwesomeSolid.FIRE, !career || edge == 0,
        () -> {
          if (services.confirmYesNo("Edge verheizen", "Edge dauerhaft um 1 senken?",
              "Das Edgeattribut sinkt von %d auf %d (GRW S. 99).".formatted(edge, edge - 1))) {
            c.burnEdge();
            onChanged.run();
          }
        });
    HBox buttons = new HBox(4, spend, regain, refresh, burn);
    buttons.setAlignment(Pos.CENTER_LEFT);
    if (!career) {
      Tooltip.install(buttons, new Tooltip(EDGE_CREATION_HINT));
    }
    return new VBox(6, figure("%d / %d".formatted(points, edge), "Edge", false), buttons);
  }

  private static @NonNull Button edgeButton(@NonNull String text, @NonNull FontAwesomeSolid icon, boolean disabled,
      @NonNull Runnable action) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    button.setDisable(disabled);
    button.setOnAction(_ -> action.run());
    return button;
  }

  private static @NonNull Node specialAttributes(@NonNull Character c, @NonNull TabServices services,
      @NonNull Runnable onChanged) {
    GridPane grid = columns(2);
    grid.add(edgeControls(c, services, onChanged), 0, 0);
    SpecialAttribute magicOrResonance = c.getMagicOrResonance();
    if (magicOrResonance == null) {
      grid.add(figure("–", "Magie / Resonanz", false), 1, 0);
    } else {
      grid.add(figure(String.valueOf(c.getEffectiveMagicOrResonanceValue()),
          magicOrResonance.getType().getDisplayName(), false), 1, 0);
    }
    // Edge ist mit den Knöpfen höher; Magie/Resonanz bleibt oben bündig
    grid.getChildren().forEach(n -> GridPane.setValignment(n, VPos.TOP));
    VBox box = titled("SONDERATTRIBUTE", grid);
    if (!c.getImplants().getAttachedLimbs().isEmpty()) {
      Label limbs = styled(new Label("Cybergliedmaßen, Durchschnitt: "
          + CyberwareTabBuilder.limbValuesText(c.getImplants().getLimbAverages())), "effect-text");
      limbs.setWrapText(true);
      box.getChildren().add(limbs);
    }
    return card(box);
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

}
