package de.shalorian.chummer.view.npc;

import static de.shalorian.chummer.view.grid.GridSupport.styled;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.model.npc.ProfessionalRating;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.view.grid.IntegerSpinner;
import de.shalorian.chummer.view.tab.TabServices;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Schergen-Bogen (ADR-062): Kopf mit Name, Professionalitätsstufe, Mitgliederzahl und Anführer-Schalter, die
 * Werteblöcke von Schergen und Anführer und die Kampfkarte. Jede Änderung baut den Bogen neu auf.
 */
public class NpcGroupViewController {

  @FXML
  private ScrollPane root;
  @FXML
  private VBox content;

  private @Nullable NpcGroup group;
  private @Nullable TabServices services;
  private @NonNull Runnable onNameChanged = () -> { };
  private boolean editGrunt;
  private boolean editLeader;

  /** Zeigt {@code group}; {@code onNameChanged} hält den Fenstertitel aktuell. */
  public void setGroup(@NonNull NpcGroup group, @NonNull TabServices services, @NonNull Runnable onNameChanged) {
    this.group = group;
    this.services = services;
    this.onNameChanged = onNameChanged;
    rebuild();
  }

  public @NonNull Node node() {
    return root;
  }

  /** Baut den Bogen neu, z.B. nach geänderten Anzeige-Einstellungen. */
  public void rebuild() {
    if (group == null || services == null) {
      return;
    }
    double scroll = root.getVvalue();
    content.getChildren().setAll(header(group), statblocks(group, services), combatCard(group, services));
    root.layout();
    root.setVvalue(scroll);
  }

  /** Schaden und Edge ändern nur die Kampfkarte; sie wird allein neu gebaut. */
  private void rebuildCombat() {
    if (group == null || services == null || content.getChildren().isEmpty()) {
      return;
    }
    content.getChildren().set(content.getChildren().size() - 1, combatCard(group, services));
  }

  private @NonNull Node combatCard(@NonNull NpcGroup g, @NonNull TabServices s) {
    return CombatPanelBuilder.build(g, s, this::rebuildCombat);
  }

  private @NonNull Node header(@NonNull NpcGroup g) {
    VBox box = new VBox(8);
    box.getStyleClass().add("card");
    TextField name = new TextField(g.getName());
    name.getStyleClass().add("page-title");
    name.textProperty().addListener((_, _, text) -> {
      g.setName(text);
      onNameChanged.run();
    });
    Spinner<Integer> rating = IntegerSpinner.create(0, ProfessionalRating.MAX_RATING, g.getProfessionalRating());
    rating.valueProperty().addListener((_, _, value) -> {
      g.setProfessionalRating(value);
      rebuild();
    });
    Spinner<Integer> members = IntegerSpinner.create(1, NpcGroup.MAX_MEMBERS, g.getMemberCount());
    members.valueProperty().addListener((_, _, value) -> {
      g.setMemberCount(value);
      rebuild();
    });
    CheckBox leader = new CheckBox("Anführer");
    leader.setSelected(g.getLeader() != null);
    leader.selectedProperty().addListener((_, _, on) -> toggleLeader(g, on, leader));
    // umbrechend statt gequetscht: in einem schmalen Fenster rutschen Mitglieder und Anführer in die nächste Reihe
    FlowPane row = new FlowPane(10, 6, styled(new Label("Professionalitätsstufe"), "attr-name"), rating,
        styled(new Label(ProfessionalRating.of(g.getProfessionalRating()).getDisplayName()), "key-caption"),
        styled(new Label("Mitglieder"), "attr-name"), members, leader);
    row.setAlignment(Pos.CENTER_LEFT);
    box.getChildren().addAll(styled(new Label("SCHERGENGRUPPE"), "card-title"), name, row);
    return box;
  }

  /**
   * Anführer an: Kopie der Schergenwerte; aus: nach Rückfrage entfernen (Annahmen des Plans). Setzt ein „Nein“ den
   * Haken zurück, meldet der Listener erneut „an“; dann gibt es den Anführer schon, und nichts passiert.
   */
  private void toggleLeader(@NonNull NpcGroup g, boolean on, @NonNull CheckBox box) {
    TabServices s = Objects.requireNonNull(services);
    if (on && g.getLeader() != null) {
      return;
    }
    if (on) {
      Character copy = CharacterStore.restore(CharacterStore.snapshot(g.getGrunt().getCharacter()));
      copy.setName("Anführer");
      g.setLeader(new NpcStatblock(copy, g.getGrunt().getGearText()));
    } else if (s.confirmYesNo("Anführer entfernen", "Anführer entfernen?",
        "Seine Werte gehen verloren.")) {
      g.setLeader(null);
      editLeader = false;
    } else {
      box.setSelected(true);
      return;
    }
    rebuild();
  }

  private @NonNull Node statblocks(@NonNull NpcGroup g, @NonNull TabServices s) {
    GridPane grid = new GridPane();
    grid.setHgap(12);
    // zwei gleich breite Spalten, auch ohne Anführer: die Schergenkarte springt beim Umschalten nicht in der Breite
    for (int column = 0; column < 2; column++) {
      ColumnConstraints constraints = new ColumnConstraints();
      constraints.setPercentWidth(50);
      constraints.setFillWidth(true);
      grid.getColumnConstraints().add(constraints);
    }
    grid.add(StatblockBuilder.build(g.getGrunt(), "SCHERGEN", null, editGrunt, () -> {
      editGrunt = !editGrunt;
      rebuild();
    }, s, this::rebuild), 0, 0);
    NpcStatblock leader = g.getLeader();
    if (leader != null) {
      grid.add(StatblockBuilder.build(leader, "ANFÜHRER", g.getLeaderHint(), editLeader, () -> {
        editLeader = !editLeader;
        rebuild();
      }, s, this::rebuild), 1, 0);
    }
    return grid;
  }
}
