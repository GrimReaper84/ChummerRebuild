package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.detailHint;
import static de.shalorian.chummer.view.grid.GridSupport.detailKey;
import static de.shalorian.chummer.view.grid.GridSupport.detailValue;
import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_SCROLL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_LABEL;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BP_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.armor.Armor;
import de.shalorian.chummer.model.character.armor.ArmorCategory;
import de.shalorian.chummer.model.character.armor.ArmorMod;
import de.shalorian.chummer.model.character.armor.ArmorModData;
import de.shalorian.chummer.model.character.armor.SpecialProtection;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.dialog.ArmorModSelectDialogController;
import de.shalorian.chummer.view.dialog.ArmorSelectDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Bereich Rüstung als Master-Detail wie Waffen und Ausrüstung (ADR-035): Kopfzeile mit Schutz (GRW S. 190/372),
 * Behinderung (S. 190) und Kosten, Werkzeugleiste, links ein Baum Kategorie → Teil → Zubehör/Modifikationen, rechts die
 * Werte des gewählten Knotens mit Sonderschutz (S. 372). Zubehör und Modifikationen werden über den Einbau-Dialog
 * gekauft ({@link ArmorModSelectDialogController}); nach jeder Änderung wird der Bereich neu aufgebaut und der Rest
 * über {@link CharacterTabHost#onArmorChanged()} informiert.
 */
final class ArmorTabBuilder {

  private final @NonNull VBox container;
  private final @NonNull CharacterTabHost host;
  private final @NonNull List<Runnable> refreshers = new ArrayList<>();

  ArmorTabBuilder(@NonNull VBox container, @NonNull CharacterTabHost host) {
    this.container = container;
    this.host = host;
  }

  void build(@NonNull Character character) {
    build(character, null, null);
  }

  /** Zieht Schutz und Behinderung (hängen an der Konstitution) und den Detailbereich nach. */
  void refresh() {
    refreshers.forEach(Runnable::run);
  }

  /** Baut den Bereich neu und wählt {@code selectedMod} bzw. sonst {@code selectedArmor} aus, falls angegeben. */
  private void build(@NonNull Character character, @Nullable Armor selectedArmor, @Nullable ArmorMod selectedMod) {
    container.getChildren().clear();
    refreshers.clear();

    TreeView<ArmorTreeEntry> tree = new TreeView<>();
    tree.setShowRoot(false);
    tree.setMinWidth(220);
    VBox details = new VBox(12);
    details.setPadding(new Insets(0, 0, 0, 16));
    Runnable showSelected = () -> showDetails(details, selectedEntry(tree), character);
    tree.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelected.run());
    refreshers.add(showSelected);

    TreeItem<ArmorTreeEntry> toSelect = fillTree(tree, character, selectedArmor, selectedMod);
    if (toSelect != null) {
      tree.getSelectionModel().select(toSelect);
      tree.scrollTo(tree.getRow(toSelect));
    } else {
      showSelected.run();
    }

    ScrollPane detailScroll = new ScrollPane(details);
    detailScroll.setFitToWidth(true);
    detailScroll.getStyleClass().add(STYLE_ATTR_SCROLL);
    SplitPane split = new SplitPane(tree, detailScroll);
    split.setDividerPositions(0.35);
    VBox.setVgrow(split, Priority.ALWAYS);

    container.getChildren().addAll(buildHeader(character), buildToolbar(character, tree), split);
  }

  /**
   * Füllt den Baum: je belegter Kategorie ein aufgeklappter Knoten in Tabellenreihenfolge, darunter die Teile in
   * Kaufreihenfolge, darunter ihre Einbauten.
   *
   * @return der Knoten der gewünschten Auswahl, oder {@code null}
   */
  @SuppressWarnings("ReferenceEquality") // Identität gewollt: dieselbe Instanz wie die Auswahl
  private static @Nullable TreeItem<ArmorTreeEntry> fillTree(@NonNull TreeView<ArmorTreeEntry> tree,
      @NonNull Character character, @Nullable Armor selectedArmor, @Nullable ArmorMod selectedMod) {
    Map<ArmorCategory, TreeItem<ArmorTreeEntry>> groups = new EnumMap<>(ArmorCategory.class);
    TreeItem<ArmorTreeEntry> selectedItem = null;
    for (Armor armor : character.getEquipment().getArmor()) {
      TreeItem<ArmorTreeEntry> group = groups.computeIfAbsent(armor.getData().category(), category -> {
        TreeItem<ArmorTreeEntry> item = new TreeItem<>(ArmorTreeEntry.ofCategory(category));
        item.setExpanded(true);
        return item;
      });
      TreeItem<ArmorTreeEntry> item = new TreeItem<>(ArmorTreeEntry.ofArmor(armor));
      item.setExpanded(true);
      group.getChildren().add(item);
      if (armor == selectedArmor) {
        selectedItem = item;
      }
      for (ArmorMod mod : armor.getMods()) {
        TreeItem<ArmorTreeEntry> child = new TreeItem<>(ArmorTreeEntry.ofMod(armor, mod));
        item.getChildren().add(child);
        if (mod == selectedMod) {
          selectedItem = child;
        }
      }
    }
    TreeItem<ArmorTreeEntry> root = new TreeItem<>();
    root.getChildren().addAll(groups.values());
    tree.setRoot(root);
    return selectedItem;
  }

  private static @Nullable ArmorTreeEntry selectedEntry(@NonNull TreeView<ArmorTreeEntry> tree) {
    TreeItem<ArmorTreeEntry> item = tree.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  private static @Nullable Armor selectedArmor(@NonNull TreeView<ArmorTreeEntry> tree) {
    ArmorTreeEntry entry = selectedEntry(tree);
    return entry == null ? null : entry.armor();
  }

  /** Kopfzeile mit Schutz, Behinderung und Kosten; Schutz und Behinderung hängen an der Konstitution. */
  private @NonNull HBox buildHeader(@NonNull Character character) {
    Label protectionLabel = new Label();
    protectionLabel.getStyleClass().add(STYLE_BP_VALUE);
    Label encumbranceLabel = new Label();
    encumbranceLabel.getStyleClass().add(STYLE_BP_LABEL);
    Label costLabel = new Label("Kosten: "
        + SpendCurrency.NUYEN.formatAmount(character.getEquipment().getArmorNuyenCost()));
    costLabel.getStyleClass().add(STYLE_BP_LABEL);
    Runnable refreshSummary = () -> {
      protectionLabel.setText("Schutz: " + character.getArmorBallistic() + "/" + character.getArmorImpact());
      int penalty = character.getEquipment().getArmorEncumbrancePenalty();
      encumbranceLabel.setText(penalty == 0 ? "Keine Behinderung" : "Behinderung: " + Numbers.integer(penalty) + " (GES/REA)");
      setWarnStyle(encumbranceLabel, penalty < 0);
    };
    refreshers.add(refreshSummary);
    refreshSummary.run();

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(12, protectionLabel, encumbranceLabel, spacer, costLabel);
    header.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(header, new Insets(0, 0, 12, 0));
    return header;
  }

  private @NonNull HBox buildToolbar(@NonNull Character character, @NonNull TreeView<ArmorTreeEntry> tree) {
    Button addBtn = toolbarButton("Rüstung hinzufügen", FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddArmor(character));

    Button installBtn = toolbarButton("Einbauen", FontAwesomeSolid.WRENCH);
    installBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> selectedArmor(tree) == null,
        tree.getSelectionModel().selectedItemProperty()));
    installBtn.setOnAction(_ -> {
      Armor armor = selectedArmor(tree);
      if (armor != null) {
        onInstall(character, armor);
      }
    });

    Button equipBtn = toolbarButton("Anlegen/Ablegen", FontAwesomeSolid.SHIELD_ALT);
    equipBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> selectedArmor(tree) == null,
        tree.getSelectionModel().selectedItemProperty()));
    equipBtn.setOnAction(_ -> {
      Armor armor = selectedArmor(tree);
      if (armor != null) {
        armor.setEquipped(!armor.isEquipped());
        onChanged(character, armor, null);
      }
    });

    Button equipAllBtn = toolbarButton("Alle anlegen", FontAwesomeSolid.CHECK_DOUBLE);
    equipAllBtn.setOnAction(_ -> onSetAllEquipped(character, true));
    Button unequipAllBtn = toolbarButton("Alle ablegen", FontAwesomeSolid.SQUARE);
    unequipAllBtn.setOnAction(_ -> onSetAllEquipped(character, false));

    Button removeBtn = toolbarButton("Entfernen", FontAwesomeSolid.TRASH);
    // Kategorieknoten und Einbauten, die ein anderer Einbau braucht, lassen sich nicht entfernen.
    removeBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {
      ArmorTreeEntry entry = selectedEntry(tree);
      if (entry == null || entry.armor() == null) {
        return true;
      }
      ArmorMod mod = entry.mod();
      return mod != null && entry.armor().uninstallProblem(mod) != null;
    }, tree.getSelectionModel().selectedItemProperty()));
    removeBtn.setOnAction(_ -> onRemoveSelected(character, tree));

    // Verkaufen (Karriere): ganzes Rüstungsteil samt Einbauten
    Button sellBtn = host.services().sellButton(character, "Ausgewähltes Rüstungsteil verkaufen",
        tree.getSelectionModel().selectedItemProperty(), () -> {
          ArmorTreeEntry entry = selectedEntry(tree);
          return entry == null || entry.mod() != null ? null : entry.armor();
        }, Armor::getName, Armor::getNuyenCost, armor -> {
          character.getEquipment().removeArmor(armor);
          onChanged(character, null, null);
        });

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox toolbar = new HBox(8, spacer, sellBtn, removeBtn, equipAllBtn, unequipAllBtn, equipBtn, installBtn, addBtn);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(toolbar, new Insets(0, 0, 8, 0));
    return toolbar;
  }

  private static @NonNull Button toolbarButton(@NonNull String text, @NonNull FontAwesomeSolid icon) {
    Button button = new Button(text);
    button.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(button, icon);
    return button;
  }

  // ── Detailbereich ─────────────────────────────────────────

  private static void showDetails(@NonNull VBox details, @Nullable ArmorTreeEntry entry,
      @NonNull Character character) {
    details.getChildren().clear();
    Armor armor = entry == null ? null : entry.armor();
    if (entry == null || armor == null) {
      details.getChildren().add(detailHint(character.getEquipment().getArmor().isEmpty()
          ? "Keine Rüstung gekauft"
          : "Teil auswählen, um seine Werte zu sehen"));
      return;
    }
    ArmorMod mod = entry.mod();
    if (mod != null) {
      showModDetails(details, armor, mod);
    } else {
      showArmorDetails(details, armor);
    }
  }

  private static void showArmorDetails(@NonNull VBox details, @NonNull Armor armor) {
    ArmorDetails d = ArmorDetails.of(armor);
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][]");
    int row = 0;
    addRowCells(grid, row++, detailKey("Kategorie"), detailValue(d.category()));
    addRowCells(grid, row++, detailKey("Schutz (B/S)"), detailValue(d.protection()));
    for (SpecialProtection special : d.special()) {
      addRowCells(grid, row++, detailKey(special.label()), detailValue(special.value()));
    }
    addRowCells(grid, row++, detailKey("Zustand"), detailValue(d.state()));
    addRowCells(grid, row++, detailKey("Verfügbarkeit"), detailValue(d.availability()));
    addRowCells(grid, row++, detailKey("Preis inkl. Einbauten"), detailValue(d.price()));
    addRowCells(grid, row, detailKey("Quelle"), detailValue(d.source()));
    details.getChildren().addAll(title(d.name()), grid);
    if (!armor.getData().weapon().isEmpty()) {
      details.getChildren().add(detailHint("Angelegt im Waffenbereich als Nahkampfwaffe verfügbar."));
    }
  }

  private static void showModDetails(@NonNull VBox details, @NonNull Armor armor, @NonNull ArmorMod mod) {
    ArmorModData data = mod.getData();
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][]");
    int row = 0;
    addRowCells(grid, row++, detailKey("Art"), detailValue(data.kind().getDisplayName() + " an " + armor.getName()));
    if (data.hasRating()) {
      addRowCells(grid, row++, detailKey("Stufe"), detailValue(String.valueOf(mod.getRating())));
    }
    addRowCells(grid, row++, detailKey("Wirkung"), detailValue(data.effectText(mod.getRating())));
    addRowCells(grid, row++, detailKey("Verfügbarkeit"),
        detailValue(armor.availabilityText(data, mod.getRating())));
    addRowCells(grid, row++, detailKey("Preis"), detailValue(Numbers.nuyen(mod.getNuyenCost())));
    addRowCells(grid, row, detailKey("Quelle"), detailValue(data.source() + " S. " + data.page()));
    details.getChildren().addAll(title(mod.getDisplayName()), grid);
    if (!data.note().isEmpty()) {
      Label note = detailHint(data.note());
      note.setWrapText(true);
      details.getChildren().add(note);
    }
    String blocked = armor.uninstallProblem(mod);
    if (blocked != null) {
      details.getChildren().add(detailHint("Nicht entfernbar: " + blocked + "."));
    }
  }

  private static @NonNull Label title(@NonNull String text) {
    Label title = new Label(text);
    title.getStyleClass().add(STYLE_SECTION_TITLE);
    return title;
  }

  // ── Aktionen ──────────────────────────────────────────────

  /**
   * Öffnet den Rüstungs-Auswahldialog und kauft die Auswahl. Bei der Erschaffung zählt der Preis gegen das
   * Ressourcengeld, im Karrieremodus wird er nach Bestätigung vom Nuyen-Guthaben abgebucht.
   */
  private void onAddArmor(@NonNull Character character) {
    try {
      DialogHandle dialog = host.services().openDialog("ArmorSelectDialog.fxml", "Rüstung hinzufügen");
      ArmorSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(character);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(piece -> {
        if (chargeFailed(character, piece.getName(), piece.getNuyenCost())) {
          return;
        }
        character.getEquipment().addArmor(piece);
        onChanged(character, piece, null);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  /** Öffnet den Einbau-Dialog für {@code armor} und baut die Auswahl ein (Kauf wie bei Rüstung). */
  private void onInstall(@NonNull Character character, @NonNull Armor armor) {
    try {
      DialogHandle dialog = host.services().openDialog("ArmorModSelectDialog.fxml", "Einbau in " + armor.getName());
      ArmorModSelectDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setTarget(character, armor);
      dialog.stage().showAndWait();
      ctrl.getResult().ifPresent(mod -> {
        if (chargeFailed(character, mod.getDisplayName() + " in " + armor.getName(), mod.getNuyenCost())) {
          return;
        }
        armor.install(mod);
        onChanged(character, armor, mod);
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private boolean chargeFailed(@NonNull Character character, @NonNull String label, int cost) {
    return character.isCreated() && host.services().nuyenChargeFailed(character, "Kauf: " + label, cost);
  }

  private void onRemoveSelected(@NonNull Character character, @NonNull TreeView<ArmorTreeEntry> tree) {
    ArmorTreeEntry entry = selectedEntry(tree);
    Armor armor = entry == null ? null : entry.armor();
    if (entry == null || armor == null) {
      return;
    }
    ArmorMod mod = entry.mod();
    if (mod != null) {
      armor.uninstall(mod);
      onChanged(character, armor, null);
    } else {
      character.getEquipment().removeArmor(armor);
      onChanged(character, null, null);
    }
  }

  private void onSetAllEquipped(@NonNull Character character, boolean equipped) {
    character.getEquipment().getArmor().forEach(piece -> piece.setEquipped(equipped));
    onChanged(character, null, null);
  }

  private void onChanged(@NonNull Character character, @Nullable Armor selectArmor, @Nullable ArmorMod selectMod) {
    build(character, selectArmor, selectMod);
    host.onArmorChanged();
  }
}
