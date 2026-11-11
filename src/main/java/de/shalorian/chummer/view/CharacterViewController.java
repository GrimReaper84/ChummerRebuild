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
import de.shalorian.chummer.view.tab.AttributeTabBuilder;
import de.shalorian.chummer.view.tab.BiographyTabBuilder;
import de.shalorian.chummer.view.tab.CharacterTabHost;
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
 * Magie-/Technomancer-Tabs. Den Inhalt der Tabs bauen die Builder in {@code view.tab}; ihre fachlichen
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
  private int magicTabIndex;
  private int technomancerTabIndex;
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
  private BiographyTabBuilder biographyTab;
  @SuppressWarnings("NullAway.Init")
  private MagicTabBuilder magicTabBuilder;
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
    biographyTab = new BiographyTabBuilder(biographyContainer, this);
    magicTabBuilder = new MagicTabBuilder(magicContainer, this);
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

    magicTabIndex = mainTabPane.getTabs().indexOf(magicTab);
    mainTabPane.getTabs().remove(magicTab);

    technomancerTabIndex = mainTabPane.getTabs().indexOf(technomancerTab);
    mainTabPane.getTabs().remove(technomancerTab);
  }

  /**
   * Blendet den Magie-Tab ein bzw. aus, je nachdem ob {@link Character#isMagician()} aktuell wahr ist — funktioniert identisch im Erstellungs- und im
   * Karrieremodus. Anders als das bestehende {@code setVisible()}/{@code setManaged()}-Muster für einzelne Controls wird der Tab tatsächlich aus
   * {@link TabPane#getTabs()} entfernt bzw. an der ursprünglichen FXML-Position wieder eingefügt (analog zum C#-Original, siehe
   * {@code docs/magie_analyse.md}).
   */
  private void syncMagicTabVisibility() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean shouldBeVisible = ch.isMagician();
    boolean isVisible = mainTabPane.getTabs().contains(magicTab);
    if (shouldBeVisible && !isVisible) {
      int index = Math.min(magicTabIndex, mainTabPane.getTabs().size());
      mainTabPane.getTabs().add(index, magicTab);
    } else if (!shouldBeVisible && isVisible) {
      mainTabPane.getTabs().remove(magicTab);
    }
  }

  /**
   * Blendet den Technomancer-Tab ein bzw. aus, je nachdem ob {@link Character#hasResonance()} aktuell wahr ist — 1:1 dasselbe Muster wie
   * {@link #syncMagicTabVisibility()}. Anders als bei Magie (Magier vs. Adept) gibt es bei Technomancer keine feinere Differenzierung nötig, da nur
   * eine einzige Quality ("Technomancer") RES freischaltet.
   */
  private void syncTechnomancerTabVisibility() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    boolean shouldBeVisible = ch.hasResonance();
    boolean isVisible = mainTabPane.getTabs().contains(technomancerTab);
    if (shouldBeVisible && !isVisible) {
      int index = Math.min(technomancerTabIndex, mainTabPane.getTabs().size());
      mainTabPane.getTabs().add(index, technomancerTab);
    } else if (!shouldBeVisible && isVisible) {
      mainTabPane.getTabs().remove(technomancerTab);
    }
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    attributeTab.setCharacter(character);
    skillTab.setCharacter(character);
    qualityTab.setCharacter(character);
    contactTab.setCharacter(character);
    biographyTab.setCharacter(character);
    magicTabBuilder.setCharacter(character);
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
    updateBpDisplay();
    refreshRows();
  }

  /** Baut den Tab Gaben &amp; Handicaps neu und blendet Magie-/Technomancer-Tab passend ein oder aus. */
  private void buildQualitiesTab() {
    qualityTab.build();
    syncMagicTabVisibility();
    syncTechnomancerTabVisibility();
  }

  @Override
  public void onImplantsChanged() {
    rebuildAttributesAndNuyen();
  }

  @Override
  public void onArmorChanged() {
    rebuildAttributesAndNuyen();
  }

  /** Implantate und Rüstung wirken auf Attribute (Essenz, MAG/RES bzw. Aug-Spalte) und das Restgeld. */
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
    biographyTab.build();
    magicTabBuilder.build();
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