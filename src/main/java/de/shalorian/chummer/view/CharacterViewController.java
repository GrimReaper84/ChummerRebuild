package de.shalorian.chummer.view;

import static de.shalorian.chummer.view.grid.GridSupport.setWarnStyle;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.metatype.MetatypeData;
import de.shalorian.chummer.model.character.metatype.MetatypeDataLoader;
import de.shalorian.chummer.model.character.quality.CharacterQuality;
import de.shalorian.chummer.view.converter.MetatypeStringConverter;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.FinalizeDialogController;
import de.shalorian.chummer.view.tab.AdeptTabBuilder;
import de.shalorian.chummer.view.tab.AttributeTabBuilder;
import de.shalorian.chummer.view.tab.BiographyTabBuilder;
import de.shalorian.chummer.view.tab.CharacterTabHost;
import de.shalorian.chummer.view.tab.ConditionTabBuilder;
import de.shalorian.chummer.view.tab.ContactTabBuilder;
import de.shalorian.chummer.view.tab.CyberwareTabBuilder;
import de.shalorian.chummer.view.tab.IncrementWiring;
import de.shalorian.chummer.view.tab.LogTabBuilder;
import de.shalorian.chummer.view.tab.MagicTabBuilder;
import de.shalorian.chummer.view.tab.QualityTabBuilder;
import de.shalorian.chummer.view.tab.SkillTabBuilder;
import de.shalorian.chummer.view.tab.StreetGearTabBuilder;
import de.shalorian.chummer.view.tab.TabServices;
import de.shalorian.chummer.view.tab.TechnomancerTabBuilder;
import de.shalorian.chummer.view.tab.VehicleTabBuilder;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Charakterbogen: hält die FXML-Knoten, den Kopfbereich (BP, Nuyen, Abschluss, Modus) und die Sichtbarkeit der
 * Magie-, Adepten- und Technomancer-Tabs. Den Inhalt der Tabs bauen die Builder in {@code view.tab}; ihre fachlichen
 * Rückmeldungen ({@link CharacterTabHost}) laufen hier zusammen, und hier wird entschieden, was neu gebaut wird.
 */
public class CharacterViewController implements CharacterTabHost {

  private static final String NUYEN_SUFFIX = " ¥";
  @FXML
  private Label nameLabel;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private Label remainingBpLabel;
  @FXML
  private VBox attributeGridHost;
  @FXML
  private VBox skillsContainer;
  @FXML
  private Button finalizeBtn;
  @FXML
  private HBox bpBox;
  @FXML
  private HBox nuyenBpBox;
  @FXML
  private Label nuyenBpValueLabel;
  @FXML
  private Button nuyenBpMinusBtn;
  @FXML
  private Button nuyenBpPlusBtn;
  @FXML
  private Label metatypeLabelCareer;
  @FXML
  private TableView<LogEntry> karmaTable;
  @FXML
  private TableView<LogEntry> nuyenTable;
  @FXML
  private Label karmaTotalLabel;
  @FXML
  private Label nuyenTotalLabel;
  @FXML
  private Label karmaStatusLabel;
  @FXML
  private Label nuyenStatusLabel;
  @FXML
  private VBox qualitiesContainer;
  @FXML
  private VBox conditionContainer;
  @FXML
  private VBox contactsContainer;
  @FXML
  private VBox biographyContainer;
  @FXML
  private TabPane mainTabPane;
  @FXML
  private Tab magicTab;
  @FXML
  private VBox magicContainer;
  @FXML
  private Tab adeptTab;
  @FXML
  private VBox adeptContainer;
  @FXML
  private Tab technomancerTab;
  @FXML
  private VBox technomancerContainer;
  @FXML
  private VBox cyberwareContainer;
  @FXML
  private VBox lifestyleContainer;
  @FXML
  private VBox armorContainer;
  @FXML
  private VBox weaponContainer;
  @FXML
  private VBox gearContainer;
  @FXML
  private VBox vehicleContainer;
  @FXML
  private Label remainingNuyenLabel;
  @FXML
  private VBox knowledgeSkillsContainer;
  @FXML
  private ToggleGroup skillSortToggleGroup;
  @FXML
  private ToggleButton sortByCategoryBtn;
  @FXML
  private ToggleButton sortByAlphabetBtn;
  /** Alle Tabs in FXML-Reihenfolge, damit ein wieder eingeblendeter Tab an seinen Platz kommt. */
  private @NonNull List<Tab> tabOrder = List.of();
  private @Nullable Character character;
  private final @NonNull TabServices services =
      new TabServices(() -> nameLabel.getScene().getWindow(), this::onLogChanged);
  private final @NonNull IncrementWiring wiring = new IncrementWiring(this);

  // Tab-Builder: werden in initialize() erzeugt, sobald die FXML-Container injiziert sind.
  @SuppressWarnings("NullAway.Init")
  private AttributeTabBuilder attributeTab;
  @SuppressWarnings("NullAway.Init")
  private SkillTabBuilder skillTab;
  @SuppressWarnings("NullAway.Init")
  private QualityTabBuilder qualityTab;
  @SuppressWarnings("NullAway.Init")
  private ContactTabBuilder contactTab;
  @SuppressWarnings("NullAway.Init")
  private ConditionTabBuilder conditionTab;
  @SuppressWarnings("NullAway.Init")
  private BiographyTabBuilder biographyTab;
  @SuppressWarnings("NullAway.Init")
  private MagicTabBuilder magicTabBuilder;
  @SuppressWarnings("NullAway.Init")
  private AdeptTabBuilder adeptTabBuilder;
  @SuppressWarnings("NullAway.Init")
  private TechnomancerTabBuilder technomancerTabBuilder;
  @SuppressWarnings("NullAway.Init")
  private CyberwareTabBuilder cyberwareTab;
  @SuppressWarnings("NullAway.Init")
  private StreetGearTabBuilder streetGear;
  @SuppressWarnings("NullAway.Init")
  private VehicleTabBuilder vehicleTab;
  @SuppressWarnings("NullAway.Init")
  private LogTabBuilder logTab;

  @FXML
  public void initialize() {
    attributeTab = new AttributeTabBuilder(attributeGridHost, wiring);
    skillTab = new SkillTabBuilder(skillsContainer, knowledgeSkillsContainer, skillSortToggleGroup,
        sortByAlphabetBtn, sortByCategoryBtn, this, wiring);
    qualityTab = new QualityTabBuilder(qualitiesContainer, this);
    contactTab = new ContactTabBuilder(contactsContainer, this, wiring);
    conditionTab = new ConditionTabBuilder(conditionContainer, this);
    biographyTab = new BiographyTabBuilder(biographyContainer, this);
    magicTabBuilder = new MagicTabBuilder(magicContainer, this);
    adeptTabBuilder = new AdeptTabBuilder(adeptContainer, this, wiring);
    technomancerTabBuilder = new TechnomancerTabBuilder(technomancerContainer, this, wiring);
    cyberwareTab = new CyberwareTabBuilder(cyberwareContainer, this);
    streetGear = new StreetGearTabBuilder(lifestyleContainer, armorContainer, weaponContainer,
        gearContainer, this);
    vehicleTab = new VehicleTabBuilder(vehicleContainer, this);
    logTab = new LogTabBuilder(karmaTable, nuyenTable, karmaTotalLabel, nuyenTotalLabel,
        karmaStatusLabel, nuyenStatusLabel, this);

    metatypeBox.getItems().setAll(MetatypeDataLoader.getAllMetatypes());
    metatypeBox.setConverter(new MetatypeStringConverter());
    metatypeBox.valueProperty().addListener((_, old, newVal) -> {
      if (character == null || newVal == null || old == null) {
        return;
      }
      List<CharacterQuality> removed = character.setMetatype(newVal);
      if (!removed.isEmpty()) {
        services.warn("Qualities entfernt", "Passen nicht zu %s: %s".formatted(newVal.name(),
            removed.stream().map(CharacterQuality::displayName).collect(Collectors.joining(", "))));
        onQualitiesChanged();
        return;
      }
      attributeTab.build();
      updateBpDisplay();
      refreshRows();
    });

    tabOrder = List.copyOf(mainTabPane.getTabs());
    mainTabPane.getTabs().removeAll(magicTab, adeptTab, technomancerTab);
  }

  /**
   * Blendet die Tabs für Magie ({@link Character#isMagician()}), Adeptenkräfte ({@link Character#hasAdeptPowers()})
   * und Technomancer ({@link Character#hasResonance()}) passend ein oder aus, in beiden Modi. Die Tabs werden aus
   * {@link TabPane#getTabs()} entfernt bzw. an ihrer FXML-Position wieder eingefügt (wie im C#-Original). Magieradepten
   * sehen Magie- und Adepten-Tab zugleich.
   */
  private void syncSpecialTabs() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    syncTabVisibility(magicTab, ch.isMagician());
    syncTabVisibility(adeptTab, ch.hasAdeptPowers());
    syncTabVisibility(technomancerTab, ch.hasResonance());
  }

  private void syncTabVisibility(@NonNull Tab tab, boolean visible) {
    List<Tab> tabs = mainTabPane.getTabs();
    if (visible && !tabs.contains(tab)) {
      int index = (int) tabOrder.subList(0, tabOrder.indexOf(tab)).stream().filter(tabs::contains).count();
      tabs.add(index, tab);
    } else if (!visible) {
      tabs.remove(tab);
    }
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    attributeTab.setCharacter(character);
    skillTab.setCharacter(character);
    qualityTab.setCharacter(character);
    contactTab.setCharacter(character);
    conditionTab.setCharacter(character);
    biographyTab.setCharacter(character);
    magicTabBuilder.setCharacter(character);
    adeptTabBuilder.setCharacter(character);
    technomancerTabBuilder.setCharacter(character);
    cyberwareTab.setCharacter(character);
    logTab.setCharacter(character);
    nameLabel.setText(character.getName());
    metatypeBox.setValue(character.getMetatype());
    updateBpDisplay();
    updateNuyenBpDisplay();
    logTab.update();
    updateModeDisplay();
    buildQualitiesTab();
    biographyTab.build();
    magicTabBuilder.build();
    adeptTabBuilder.build();
    technomancerTabBuilder.build();
    cyberwareTab.build();
    buildStreetGearTabs();
  }

  /**
   * Baut den Straßenausrüstung-Tab über {@link StreetGearTabBuilder} und den Tab "Fahrzeuge &amp;
   * Drohnen" über {@link VehicleTabBuilder} neu auf.
   */
  private void buildStreetGearTabs() {
    if (character == null) {
      return;
    }
    streetGear.build(character);
    vehicleTab.build(character);
  }

  // ── Rückmeldungen der Tabs (CharacterTabHost) ─────────────

  @Override
  public @NonNull TabServices services() {
    return services;
  }


  @Override
  public void onNameChanged() {
    if (character != null) {
      nameLabel.setText(character.getName());
    }
  }

  @Override
  public void onBpChanged() {
    updateBpDisplay();
  }

  @Override
  public void onLogChanged() {
    logTab.update();
  }

  @Override
  public void onNuyenSpentChanged() {
    updateNuyenBpDisplay();
  }

  @Override
  public void onQualitiesChanged() {
    buildQualitiesTab();
    attributeTab.build();
    skillTab.build();
    cyberwareTab.build();
    adeptTabBuilder.build();
    updateBpDisplay();
    refreshRows();
  }

  /** Baut den Tab Gaben &amp; Handicaps neu und blendet Magie-, Adepten- und Technomancer-Tab passend ein oder aus. */
  private void buildQualitiesTab() {
    qualityTab.build();
    syncSpecialTabs();
  }

  /**
   * Adeptenkräfte wirken auf Attribute (Aug-Spalte), Fertigkeitspools, Initiative, Zustandsmonitor und Schutz: diese
   * Tabs und den Kräfte-Tab selbst neu bauen, dann alle Zeilen neu bewerten (Rüstungskopf, Fertigkeitssperren).
   */
  @Override
  public void onAdeptPowersChanged() {
    adeptTabBuilder.build();
    attributeTab.build();
    skillTab.build();
    conditionTab.build();
    refreshRows();
  }

  @Override
  public void onImplantsChanged() {
    // Implantate wirken auf Essenz, Attribute, Würfelpools, Initiative, Zustandsmonitor und Panzerung.
    attributeTab.build();
    skillTab.build();
    conditionTab.build();
    updateNuyenBpDisplay();
    refreshRows();
  }

  @Override
  public void onArmorChanged() {
    rebuildAttributesAndNuyen();
  }

  /** Rüstung wirkt auf Attribute (Aug-Spalte durch Behinderung) und das Restgeld. */
  private void rebuildAttributesAndNuyen() {
    attributeTab.build();
    updateNuyenBpDisplay();
    refreshRows();
  }

  @Override
  public void refreshRows() {
    attributeTab.refresh();
    skillTab.refresh();
    qualityTab.refresh();
    contactTab.refresh();
    conditionTab.refresh();
    adeptTabBuilder.refresh();
    technomancerTabBuilder.refresh();
    streetGear.refresh();
  }

  private void updateBpDisplay() {
    if (character == null) {
      return;
    }
    int remaining = character.getRemainingBP();
    remainingBpLabel.setText(String.valueOf(remaining));
    setWarnStyle(remainingBpLabel, remaining < 0);
  }

  private void updateNuyenBpDisplay() {
    if (character == null) {
      return;
    }
    int bp = character.getNuyenBP();
    nuyenBpValueLabel.setText(bp + " BP (" + character.getResourceNuyen() + NUYEN_SUFFIX + ")");
    int remaining = character.getRemainingNuyen();
    remainingNuyenLabel.setText(remaining + NUYEN_SUFFIX);
    setWarnStyle(remainingNuyenLabel, remaining < 0);
    nuyenBpMinusBtn.setDisable(bp <= 0);
    nuyenBpPlusBtn.setDisable(bp >= Character.MAX_NUYEN_BP || character.getRemainingBP() <= 0);
  }

  @FXML
  private void onNuyenBpMinus() {
    if (character == null) {
      return;
    }
    character.setNuyenBP(character.getNuyenBP() - 1);
    updateNuyenBpDisplay();
    updateBpDisplay();
    refreshRows();
  }

  @FXML
  private void onNuyenBpPlus() {
    if (character == null) {
      return;
    }
    character.setNuyenBP(character.getNuyenBP() + 1);
    updateNuyenBpDisplay();
    updateBpDisplay();
    refreshRows();
  }

  /**
   * Schließt die Charaktererschaffung ab. Hinderungsgründe aus {@link Character#getFinalizeProblems()} (derzeit negatives Restgeld) brechen mit einer
   * Warnung ab; sonst zeigt der Abschlussdialog den Startkapital-Wurf nach Lebensstil (GRW S. 114), gebucht wird über
   * {@link Character#finalizeCreation(int)}.
   */
  @FXML
  private void onFinalize() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    List<String> problems = ch.getFinalizeProblems();
    if (!problems.isEmpty()) {
      services.warn("Abschluss nicht möglich", String.join("\n", problems));
      return;
    }
    try {
      DialogHandle dialog = services.openDialog("FinalizeDialog.fxml", "Charakter abschließen");
      FinalizeDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.setCharacter(ch);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(diceSum -> {
        ch.finalizeCreation(diceSum);
        updateModeDisplay();
        logTab.update();
      });
    } catch (IOException _) {
      services.showDialogLoadError();
    }
  }

  private void updateModeDisplay() {
    if (character == null) {
      return;
    }
    boolean career = character.isCreated();

    finalizeBtn.setVisible(!career);
    finalizeBtn.setManaged(!career);
    bpBox.setVisible(!career);
    bpBox.setManaged(!career);
    nuyenBpBox.setVisible(!career);
    nuyenBpBox.setManaged(!career);
    metatypeBox.setVisible(!career);
    metatypeBox.setManaged(!career);
    metatypeLabelCareer.setVisible(career);
    metatypeLabelCareer.setManaged(career);
    if (career) {
      metatypeLabelCareer.setText(character.getMetatype().name());
    }

    attributeTab.build();
    skillTab.build();
    buildQualitiesTab();
    contactTab.build();
    conditionTab.build();
    biographyTab.build();
    magicTabBuilder.build();
    adeptTabBuilder.build();
    technomancerTabBuilder.build();
    cyberwareTab.build();
    buildStreetGearTabs();
  }

  @FXML
  private void onAddKarma() {
    if (character == null) {
      return;
    }
    Optional<LogEntry> result = logTab.openEntryDialog("Karma-Eintrag hinzufügen");
    if (result.isEmpty()) {
      return;
    }
    character.addKarmaEntry(result.get());
    logTab.update();
    refreshRows();
  }

  @FXML
  private void onAddNuyen() {
    if (character == null) {
      return;
    }
    Optional<LogEntry> result = logTab.openEntryDialog("Nuyen-Eintrag hinzufügen");
    if (result.isEmpty()) {
      return;
    }
    character.addNuyenEntry(result.get());
    logTab.update();
  }

}