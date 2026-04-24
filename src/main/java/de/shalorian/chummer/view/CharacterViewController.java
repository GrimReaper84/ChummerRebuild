package de.shalorian.chummer.view;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterAttribute;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.Skill;
import de.shalorian.chummer.model.character.SkillCategory;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

public class CharacterViewController {

    @FXML private Label                   nameLabel;
    @FXML private ComboBox<MetatypeData>  metatypeBox;
    @FXML private Label                   remainingBpLabel;
    @FXML private GridPane                attributeGrid;
    @FXML private VBox                    skillsContainer;

    @FXML private Button                  finalizeBtn;
    @FXML private HBox                    bpBox;
    @FXML private Label                   metatypeLabelCareer;
    @FXML private TableView<LogEntry>     karmaTable;
    @FXML private TableView<LogEntry>     nuyenTable;
    @FXML private Label                   karmaTotalLabel;
    @FXML private Label                   nuyenTotalLabel;
    @FXML private Label                   karmaStatusLabel;
    @FXML private Label                   nuyenStatusLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private @Nullable Character character;

    // Attribute-Refresher bleiben stabil; Skill-Refresher werden beim Entkoppeln neu aufgebaut
    private final List<Runnable> attrRefreshers  = new ArrayList<>();
    private final List<Runnable> skillRefreshers = new ArrayList<>();

    @FXML
    public void initialize() {
        metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
        metatypeBox.setConverter(new StringConverter<>() {
            @Override public String toString(@Nullable MetatypeData m) {
                if (m == null) return "";
                String prefix = m.isVariant() ? "  " : "";
                String bp     = m.bpCost() > 0 ? "  (" + m.bpCost() + " BP)" : "";
                return prefix + m.name() + bp;
            }
            @Override public @Nullable MetatypeData fromString(String s) { return null; }
        });
        metatypeBox.valueProperty().addListener((obs, old, newVal) -> {
            if (character == null || newVal == null || old == null) return;
            character.setMetatype(newVal);
            buildAttributeGrid();
            updateBpDisplay();
        });

        buildLogTable(karmaTable);
        buildLogTable(nuyenTable);
    }

    public void setCharacter(Character character) {
        this.character = character;
        nameLabel.setText(character.getName());
        metatypeBox.setValue(character.getMetatype());
        updateBpDisplay();
        updateLogDisplay();
        updateModeDisplay();
    }

    // ── Attribute-Tab ─────────────────────────────────────────

    private void buildAttributeGrid() {
        attributeGrid.getChildren().clear();
        attributeGrid.getColumnConstraints().clear();
        attrRefreshers.clear();

        int[]  widths = {170, 46, 30, 46, 30, 90, 70};
        HPos[] aligns = {HPos.LEFT, HPos.CENTER, HPos.CENTER,
                         HPos.CENTER, HPos.CENTER, HPos.LEFT, HPos.LEFT};
        for (int i = 0; i < widths.length; i++) {
            ColumnConstraints cc = new ColumnConstraints(widths[i]);
            cc.setHalignment(aligns[i]);
            attributeGrid.getColumnConstraints().add(cc);
        }

        boolean career = character.isCreated();
        String[] headers = {"Attribut", "", "", "Wert", "", "Bereich",
                            career ? "Karma" : "BP-Kosten"};
        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].isEmpty()) {
                Label h = new Label(headers[i]);
                h.getStyleClass().add("attr-header");
                attributeGrid.add(h, i, 0);
            }
        }

        int row = 1;
        for (AttributeType type : AttributeType.values()) {
            addAttributeRow(row++, type, character.getAttribute(type));
        }
    }

    private void addAttributeRow(int row, AttributeType type, CharacterAttribute attr) {
        Label  nameLabel  = new Label(type.getDisplayName());
        Label  abbrLabel  = new Label(type.getAbbreviation());
        Button minus      = new Button("−");
        Label  valueLabel = new Label();
        Button plus       = new Button("+");
        Label  rangeLabel = new Label(attr.getMin() + " – " + attr.getMax());
        Label  bpLabel    = new Label();

        nameLabel.getStyleClass().add("attr-name");
        abbrLabel.getStyleClass().add("attr-abbr");
        minus.getStyleClass().add("attr-btn");
        valueLabel.getStyleClass().add("attr-value");
        plus.getStyleClass().add("attr-btn");
        rangeLabel.getStyleClass().add("attr-range");
        bpLabel.getStyleClass().add("attr-bp");

        boolean career = character.isCreated();
        Runnable refresh;

        if (career) {
            minus.setVisible(false);
            minus.setManaged(false);
            refresh = () -> {
                int val       = attr.getValue();
                int karmaCost = attr.getKarmaCost();
                valueLabel.setText(String.valueOf(val));
                bpLabel.setText(val >= attr.getMax() ? "–" : karmaCost + " Karma");
                plus.setDisable(val >= attr.getMax() || character.getKarmaTotal() < karmaCost);
            };
            attrRefreshers.add(refresh);

            plus.setOnAction(e -> {
                int val       = attr.getValue();
                int karmaCost = attr.getKarmaCost();
                String label  = type.getDisplayName() + " " + val + " → " + (val + 1);
                if (val < attr.getMax() && character.getKarmaTotal() >= karmaCost
                        && confirmKarmaSpend(label, karmaCost)) {
                    character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now()));
                    attr.setValue(val + 1);
                    refreshAllRows();
                    updateLogDisplay();
                }
            });
        } else {
            refresh = () -> {
                int val = attr.getValue();
                valueLabel.setText(String.valueOf(val));
                bpLabel.setText(attr.getBpCost() + " BP");
                minus.setDisable(val <= attr.getMin());
                plus.setDisable(val >= attr.getMax() || character.getRemainingBP() < 10);
            };
            attrRefreshers.add(refresh);

            minus.setOnAction(e -> {
                if (attr.getValue() > attr.getMin()) {
                    attr.setValue(attr.getValue() - 1);
                    refreshAllRows();
                    updateBpDisplay();
                }
            });
            plus.setOnAction(e -> {
                if (attr.getValue() < attr.getMax() && character.getRemainingBP() >= 10) {
                    attr.setValue(attr.getValue() + 1);
                    refreshAllRows();
                    updateBpDisplay();
                }
            });
        }

        refresh.run();

        attributeGrid.add(nameLabel,  0, row);
        attributeGrid.add(abbrLabel,  1, row);
        attributeGrid.add(minus,      2, row);
        attributeGrid.add(valueLabel, 3, row);
        attributeGrid.add(plus,       4, row);
        attributeGrid.add(rangeLabel, 5, row);
        attributeGrid.add(bpLabel,    6, row);
    }

    // ── Skills-Tab ────────────────────────────────────────────

    private void buildSkillsTab() {
        skillsContainer.getChildren().clear();
        skillRefreshers.clear();

        // Gruppe: Kategorie → (Gruppenname|"" → Skills)
        Map<SkillCategory, Map<String, List<Skill>>> grouped = new LinkedHashMap<>();
        for (SkillCategory cat : SkillCategory.values()) grouped.put(cat, new LinkedHashMap<>());
        for (Skill skill : character.getSkills()) {
            String key = skill.getGroupName() != null ? skill.getGroupName() : "";
            grouped.get(skill.getCategory()).computeIfAbsent(key, k -> new ArrayList<>()).add(skill);
        }

        boolean firstSection = true;
        for (Map.Entry<SkillCategory, Map<String, List<Skill>>> catEntry : grouped.entrySet()) {
            if (catEntry.getValue().isEmpty()) continue;

            Label catLabel = new Label(catEntry.getKey().getDisplayName());
            catLabel.getStyleClass().add("section-title");
            VBox.setMargin(catLabel, new Insets(firstSection ? 0 : 20, 0, 4, 0));
            skillsContainer.getChildren().add(catLabel);
            firstSection = false;

            GridPane grid = buildSkillGrid();
            skillsContainer.getChildren().add(grid);

            int row = 0;
            for (Map.Entry<String, List<Skill>> groupEntry : catEntry.getValue().entrySet()) {
                String groupName = groupEntry.getKey();
                if (!groupName.isEmpty()) {
                    boolean coupled = character.isGroupCoupled(groupName);
                    row = addGroupHeaderRow(grid, row, groupName, coupled);
                    for (Skill skill : groupEntry.getValue()) {
                        addSkillRow(grid, row++, skill, coupled);
                    }
                } else {
                    for (Skill skill : groupEntry.getValue()) {
                        addSkillRow(grid, row++, skill, false);
                    }
                }
            }
        }
    }

    /** Baut den Skills-Tab neu auf, z.B. nach dem Entkoppeln einer Gruppe. */
    private void rebuildSkillsTab() {
        buildSkillsTab();
        attrRefreshers.forEach(Runnable::run);
        updateBpDisplay();
    }

    private GridPane buildSkillGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.getStyleClass().add("attr-grid");

        int[]  widths = {200, 46, 30, 46, 30, 70};
        HPos[] aligns = {HPos.LEFT, HPos.CENTER, HPos.CENTER,
                         HPos.CENTER, HPos.CENTER, HPos.LEFT};
        for (int i = 0; i < widths.length; i++) {
            ColumnConstraints cc = new ColumnConstraints(widths[i]);
            cc.setHalignment(aligns[i]);
            grid.getColumnConstraints().add(cc);
        }
        return grid;
    }

    /** Fügt eine Gruppen-Kopfzeile mit +/−-Buttons und Auskoppeln-Button ein. */
    private int addGroupHeaderRow(GridPane grid, int row, String groupName, boolean coupled) {
        HBox nameBox = new HBox(8);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Label groupLabel = new Label(groupName);
        groupLabel.getStyleClass().add(coupled ? "skill-group-header-coupled" : "skill-group-header");
        nameBox.getChildren().add(groupLabel);

        if (coupled) {
            Button decoupleBtn = new Button("Auskoppeln");
            decoupleBtn.getStyleClass().add("btn-decouple");
            decoupleBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Gruppe auskoppeln");
                confirm.setHeaderText("Skillgruppe \"" + groupName + "\" wirklich auskoppeln?");
                int rating = character.getGroupRating(groupName);
                String costInfo = rating > 0
                    ? "\nAchtung: Die " + groupName + "-Skills werden ab jetzt einzeln bepreist "
                      + "(je " + (rating * 4) + " BP statt " + (rating * 10) + " BP gesamt)."
                    : "";
                confirm.setContentText(
                    "Nach dem Auskoppeln können die Skills der Gruppe unabhängig voneinander "
                    + "erhöht werden." + costInfo);
                confirm.showAndWait()
                    .filter(btn -> btn == ButtonType.OK)
                    .ifPresent(btn -> {
                        character.decoupleGroup(groupName);
                        rebuildSkillsTab();
                    });
            });
            nameBox.getChildren().add(decoupleBtn);
        }

        grid.add(nameBox, 0, row);

        if (coupled) {
            Button minus       = new Button("−");
            Label  ratingLabel = new Label();
            Button plus        = new Button("+");
            Label  bpLabel     = new Label();

            minus.getStyleClass().add("attr-btn");
            ratingLabel.getStyleClass().add("attr-value");
            plus.getStyleClass().add("attr-btn");
            bpLabel.getStyleClass().add("attr-bp");

            boolean career = character.isCreated();

            Runnable refresh;
            if (career) {
                minus.setVisible(false);
                minus.setManaged(false);
                refresh = () -> {
                    int rating    = character.getGroupRating(groupName);
                    int karmaCost = (rating + 1) * 5;
                    ratingLabel.setText(String.valueOf(rating));
                    bpLabel.setText(rating >= Skill.MAX_RATING ? "–" : karmaCost + " Karma");
                    plus.setDisable(rating >= Skill.MAX_RATING || character.getKarmaTotal() < karmaCost);
                };
                skillRefreshers.add(refresh);

                plus.setOnAction(e -> {
                    int rating    = character.getGroupRating(groupName);
                    int karmaCost = (rating + 1) * 5;
                    String label  = groupName + " " + rating + " → " + (rating + 1);
                    if (rating < Skill.MAX_RATING && character.getKarmaTotal() >= karmaCost
                            && confirmKarmaSpend(label, karmaCost)) {
                        character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now()));
                        character.raiseGroup(groupName);
                        refreshAllRows();
                        updateLogDisplay();
                    }
                });
            } else {
                refresh = () -> {
                    int rating = character.getGroupRating(groupName);
                    ratingLabel.setText(String.valueOf(rating));
                    bpLabel.setText(rating == 0 ? "–" : (rating * 10) + " BP");
                    minus.setDisable(rating <= 0);
                    plus.setDisable(rating >= Skill.MAX_RATING || character.getRemainingBP() < 10);
                };
                skillRefreshers.add(refresh);

                minus.setOnAction(e -> {
                    if (character.getGroupRating(groupName) > 0) {
                        character.lowerGroup(groupName);
                        refreshAllRows();
                        updateBpDisplay();
                    }
                });
                plus.setOnAction(e -> {
                    if (character.getGroupRating(groupName) < Skill.MAX_RATING
                            && character.getRemainingBP() >= 10) {
                        character.raiseGroup(groupName);
                        refreshAllRows();
                        updateBpDisplay();
                    }
                });
            }

            refresh.run();

            grid.add(minus,       2, row);
            grid.add(ratingLabel, 3, row);
            grid.add(plus,        4, row);
            grid.add(bpLabel,     5, row);
        }

        return row + 1;
    }

    private void addSkillRow(GridPane grid, int row, Skill skill, boolean coupled) {
        Label  nameLabel   = new Label(skill.getName());
        Label  attrLabel   = new Label(skill.getLinkedAttribute().getAbbreviation());
        Button minus       = new Button("−");
        Label  ratingLabel = new Label();
        Button plus        = new Button("+");
        Label  bpLabel     = new Label();

        nameLabel.getStyleClass().add(coupled ? "attr-name-coupled" : "attr-name");
        attrLabel.getStyleClass().add("attr-abbr");
        minus.getStyleClass().add("attr-btn");
        ratingLabel.getStyleClass().add("attr-value");
        plus.getStyleClass().add("attr-btn");
        bpLabel.getStyleClass().add("attr-bp");

        boolean career = character.isCreated();

        if (coupled) {
            if (career) {
                minus.setVisible(false);
                minus.setManaged(false);
            }
            Runnable refresh = () -> {
                ratingLabel.setText(String.valueOf(skill.getRating()));
                bpLabel.setText("");
                minus.setDisable(true);
                plus.setDisable(true);
            };
            skillRefreshers.add(refresh);
            refresh.run();
        } else if (career) {
            minus.setVisible(false);
            minus.setManaged(false);
            Runnable refresh = () -> {
                int val       = skill.getRating();
                int karmaCost = skill.getKarmaCost();
                ratingLabel.setText(String.valueOf(val));
                bpLabel.setText(val >= Skill.MAX_RATING ? "–" : karmaCost + " Karma");
                plus.setDisable(val >= Skill.MAX_RATING || character.getKarmaTotal() < karmaCost);
            };
            skillRefreshers.add(refresh);

            plus.setOnAction(e -> {
                int val       = skill.getRating();
                int karmaCost = skill.getKarmaCost();
                String label  = skill.getName() + " " + val + " → " + (val + 1);
                if (val < Skill.MAX_RATING && character.getKarmaTotal() >= karmaCost
                        && confirmKarmaSpend(label, karmaCost)) {
                    character.addKarmaEntry(new LogEntry(label, -karmaCost, LocalDate.now()));
                    skill.setRating(val + 1);
                    refreshAllRows();
                    updateLogDisplay();
                }
            });

            refresh.run();
        } else {
            Runnable refresh = () -> {
                int val = skill.getRating();
                ratingLabel.setText(String.valueOf(val));
                bpLabel.setText(val == 0 ? "–" : skill.getBpCost() + " BP");
                minus.setDisable(val <= 0);
                plus.setDisable(val >= Skill.MAX_RATING || character.getRemainingBP() < 4);
            };
            skillRefreshers.add(refresh);

            minus.setOnAction(e -> {
                if (skill.getRating() > 0) {
                    skill.setRating(skill.getRating() - 1);
                    refreshAllRows();
                    updateBpDisplay();
                }
            });
            plus.setOnAction(e -> {
                if (skill.getRating() < Skill.MAX_RATING && character.getRemainingBP() >= 4) {
                    skill.setRating(skill.getRating() + 1);
                    refreshAllRows();
                    updateBpDisplay();
                }
            });

            refresh.run();
        }

        grid.add(nameLabel,   0, row);
        grid.add(attrLabel,   1, row);
        grid.add(minus,       2, row);
        grid.add(ratingLabel, 3, row);
        grid.add(plus,        4, row);
        grid.add(bpLabel,     5, row);
    }

    // ── Gemeinsam ─────────────────────────────────────────────

    private boolean confirmKarmaSpend(String label, int karmaCost) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Karma ausgeben");
        confirm.setHeaderText(label);
        confirm.setContentText(karmaCost + " Karma ausgeben?\n\nVerbleibendes Karma danach: "
            + (character.getKarmaTotal() - karmaCost));
        confirm.initOwner(nameLabel.getScene().getWindow());
        return confirm.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    private void refreshAllRows() {
        attrRefreshers.forEach(Runnable::run);
        skillRefreshers.forEach(Runnable::run);
    }

    private void updateBpDisplay() {
        int remaining = character.getRemainingBP();
        remainingBpLabel.setText(String.valueOf(remaining));
        remainingBpLabel.getStyleClass().remove("bp-value-warn");
        if (remaining < 0) remainingBpLabel.getStyleClass().add("bp-value-warn");
    }

    // ── Karrieremodus-Transition ──────────────────────────────

    @FXML
    private void onFinalize() {
        if (character == null) return;
        int remainingBp    = character.getRemainingBP();
        int conversionNuyen = remainingBp * 2_000;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Charakter abschließen");
        confirm.setHeaderText("Charakter in den Karrieremodus überführen?");
        String bpInfo = remainingBp > 0
            ? remainingBp + " verbleibende BP werden umgerechnet in " + conversionNuyen + " ¥.\n\n"
            : "";
        confirm.setContentText(bpInfo + "Dieser Schritt kann nicht rückgängig gemacht werden.");
        confirm.initOwner(nameLabel.getScene().getWindow());

        confirm.showAndWait()
            .filter(btn -> btn == ButtonType.OK)
            .ifPresent(btn -> {
                if (remainingBp > 0) {
                    character.addNuyenEntry(new LogEntry(
                        "Startkapital (BP-Konvertierung)", conversionNuyen, LocalDate.now()));
                }
                character.setCreated(true);
                updateModeDisplay();
                updateLogDisplay();
            });
    }

    private void updateModeDisplay() {
        if (character == null) return;
        boolean career = character.isCreated();

        finalizeBtn.setVisible(!career);
        finalizeBtn.setManaged(!career);
        bpBox.setVisible(!career);
        bpBox.setManaged(!career);
        metatypeBox.setVisible(!career);
        metatypeBox.setManaged(!career);
        metatypeLabelCareer.setVisible(career);
        metatypeLabelCareer.setManaged(career);
        if (career) metatypeLabelCareer.setText(character.getMetatype().name());

        buildAttributeGrid();
        buildSkillsTab();
    }

    // ── Log-Tab ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void buildLogTable(TableView<LogEntry> table) {
        TableColumn<LogEntry, String> dateCol = new TableColumn<>("Datum");
        dateCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
        dateCol.setPrefWidth(90);

        TableColumn<LogEntry, String> descCol = new TableColumn<>("Bezeichnung");
        descCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().description()));

        TableColumn<LogEntry, String> amountCol = new TableColumn<>("Menge");
        amountCol.setCellValueFactory(c -> {
            int a = c.getValue().amount();
            return new SimpleStringProperty(a > 0 ? "+" + a : String.valueOf(a));
        });
        amountCol.setPrefWidth(70);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(dateCol, descCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Keine Einträge"));
    }

    private void updateLogDisplay() {
        if (character == null) return;

        karmaTable.getItems().setAll(character.getKarmaLog());
        nuyenTable.getItems().setAll(character.getNuyenLog());

        int karma = character.getKarmaTotal();
        karmaTotalLabel.setText(String.valueOf(karma));
        karmaStatusLabel.setText(String.valueOf(karma));

        int nuyen = character.getNuyenTotal();
        nuyenTotalLabel.setText(nuyen + " ¥");
        nuyenStatusLabel.setText(nuyen + " ¥");
    }

    @FXML
    private void onAddKarma() {
        openLogEntryDialog("Karma-Eintrag hinzufügen").ifPresent(entry -> {
            character.addKarmaEntry(entry);
            updateLogDisplay();
            refreshAllRows();
        });
    }

    @FXML
    private void onAddNuyen() {
        openLogEntryDialog("Nuyen-Eintrag hinzufügen").ifPresent(entry -> {
            character.addNuyenEntry(entry);
            updateLogDisplay();
        });
    }

    private Optional<LogEntry> openLogEntryDialog(String title) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("LogEntryDialog.fxml"));
            Parent root = loader.load();
            LogEntryDialogController ctrl = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(nameLabel.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            dialogStage.setScene(scene);

            ctrl.setStage(dialogStage);
            dialogStage.showAndWait();
            return ctrl.getResult();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}