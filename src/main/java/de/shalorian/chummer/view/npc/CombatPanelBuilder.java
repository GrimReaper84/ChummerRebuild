package de.shalorian.chummer.view.npc;

import static de.shalorian.chummer.view.grid.GridSupport.styled;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.model.npc.NpcCombatant;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcRules;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.model.npc.ProfessionalRating;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.grid.MonitorBoxRow;
import de.shalorian.chummer.view.tab.TabServices;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

/**
 * Kampfkarte einer Schergengruppe (GRW S. 324/325): Gruppen-Edge zum Abhaken, Führungsprobe, je Mitglied und Anführer
 * ein gemeinsamer Zustandsmonitor mit Verletzungsmodifikator und der Fluchthinweis nach der Professionalitätsstufe.
 */
final class CombatPanelBuilder {

  private CombatPanelBuilder() {
  }

  /**
   * Baut die Karte.
   *
   * @param onChanged nach jeder Änderung des Kampfzustands (baut die Karte neu)
   */
  static @NonNull Node build(@NonNull NpcGroup group, @NonNull TabServices services, @NonNull Runnable onChanged) {
    VBox box = new VBox(10);
    box.getStyleClass().add("card");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Button reset = new Button("Kampf zurücksetzen");
    reset.getStyleClass().add(STYLE_BTN_SECONDARY);
    reset.setOnAction(_ -> {
      if (services.confirmYesNo("Kampf zurücksetzen", "Schaden und Gruppen-Edge zurücksetzen?",
          "Alle Monitore werden geleert, verbrauchtes Edge kommt zurück, die Führungsprobe entfällt.")) {
        group.resetCombat();
        onChanged.run();
      }
    });
    HBox head = new HBox(8, styled(new Label("KAMPF"), "card-title"), spacer, reset);
    head.setAlignment(Pos.CENTER_LEFT);
    box.getChildren().addAll(head, edgeRow(group, onChanged));

    NpcStatblock leader = group.getLeader();
    if (leader != null) {
      box.getChildren().add(monitorRow("Anführer", group.getLeaderState(), leader.getMonitorBoxes(), onChanged));
    }
    int boxes = group.getGrunt().getMonitorBoxes();
    List<NpcCombatant> members = group.getMembers();
    for (int i = 0; i < members.size(); i++) {
      box.getChildren().add(monitorRow("Scherge " + (i + 1), members.get(i), boxes, onChanged));
    }
    box.getChildren().add(fleeHint(group));
    Label notes = styled(new Label("Eine Initiativeprobe für alle Schergen, der Anführer würfelt selbst und handelt bei "
        + "Gleichstand zuerst. Ausgeschaltet überlebt, wer zuletzt geistigen oder höchstens KON körperlichen Schaden "
        + "erlitt (Anführer: KON × 1,5). Die Professionalitätsstufe gilt als Würfelpool-Modifikator gegen soziale "
        + "Fertigkeiten (GRW S. 324/325)."), "key-caption");
    notes.setWrapText(true);
    box.getChildren().add(notes);
    return box;
  }

  private static @NonNull Node edgeRow(@NonNull NpcGroup group, @NonNull Runnable onChanged) {
    int pool = group.getEdgePool();
    Node boxes = pool == 0 ? styled(new Label("kein Gruppen-Edge (Stufe 0)"), "key-caption")
        : MonitorBoxRow.build(pool, group.getEdgeSpent(), "cm-mini-accent", true, _ -> false, spent -> {
          group.setEdgeSpent(spent);
          onChanged.run();
        });
    Button refresh = new Button("Auffrischen");
    refresh.getStyleClass().add(STYLE_BTN_SECONDARY);
    refresh.setDisable(group.getEdgeSpent() == 0);
    refresh.setOnAction(_ -> {
      group.setEdgeSpent(0);
      onChanged.run();
    });
    CheckBox leadership = new CheckBox("Führung gelungen (+1 Stufe)");
    leadership.setSelected(group.isLeadershipBonus());
    leadership.setDisable(group.getLeader() == null);
    leadership.selectedProperty().addListener((_, _, on) -> {
      group.setLeadershipBonus(on);
      onChanged.run();
    });
    HBox row = new HBox(10, styled(new Label("Gruppen-Edge %d/%d".formatted(pool - group.getEdgeSpent(), pool)),
        "attr-name"), boxes, refresh, leadership);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private static @NonNull Node monitorRow(@NonNull String name, @NonNull NpcCombatant combatant, int boxes,
      @NonNull Runnable onChanged) {
    int damage = Math.min(combatant.getDamage(), boxes);
    int wound = NpcRules.woundModifier(damage);
    String state = combatant.isKnockedOut(boxes) ? "ausgeschaltet"
        : wound < 0 ? "Verletzung " + Numbers.integer(wound) : "unverletzt";
    Label stateLabel = styled(new Label(state), combatant.isKnockedOut(boxes) || wound < 0 ? "warn-text" : "key-caption");
    Node row = MonitorBoxRow.build(boxes, damage, "cm-mini-physical", true,
        box -> NpcRules.woundModifier(box) != NpcRules.woundModifier(box - 1), value -> {
          combatant.setDamage(value, boxes);
          onChanged.run();
        });
    Label nameLabel = styled(new Label(name), "attr-name");
    nameLabel.setMinWidth(80);
    HBox line = new HBox(10, nameLabel, row, stateLabel);
    line.setAlignment(Pos.CENTER_LEFT);
    return line;
  }

  private static @NonNull Node fleeHint(@NonNull NpcGroup group) {
    ProfessionalRating rating = group.getRatingClass();
    String text = "Ausgeschaltet %d von %d".formatted(group.getKnockedOut(), group.getMemberCount());
    if (!rating.flees()) {
      text += " – %s: kämpft bis zum Ende".formatted(rating.getDisplayName());
    } else if (group.isFleeing()) {
      text += " – die Gruppe flieht bzw. zieht sich zurück (%s: %s)".formatted(rating.getDisplayName(),
          rating.fleeThresholdText());
    } else {
      text += " – %s flieht bei %s".formatted(rating.getDisplayName(), rating.fleeThresholdText());
    }
    Label label = styled(new Label(text), group.isFleeing() ? "hint-text" : "key-caption");
    label.setWrapText(true);
    if (!group.isFleeing()) {
      return label;
    }
    VBox card = new VBox(label);
    card.getStyleClass().add("hint-card");
    return card;
  }
}
