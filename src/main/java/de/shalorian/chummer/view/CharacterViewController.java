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
import de.shalorian.chummer.view.shell.CharacterSection;
import de.shalorian.chummer.view.shell.FloatingButtonPosition;
import de.shalorian.chummer.view.shell.FooterText;
import de.shalorian.chummer.view.shell.SectionNavigator;
import de.shalorian.chummer.view.shell.SlidePanel;
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
import de.shalorian.chummer.view.tab.StatusOverviewBuilder;
import de.shalorian.chummer.view.tab.StreetGearTabBuilder;
import de.shalorian.chummer.view.tab.TabServices;
import de.shalorian.chummer.view.tab.TechnomancerTabBuilder;
import de.shalorian.chummer.view.tab.VehicleTabBuilder;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Charakterbogen: hält die FXML-Knoten, die Seitenleiste ({@link SectionNavigator}) mit den Seiten der Bereiche, den
 * Kopfbereich (BP, Nuyen, Abschluss, Modus) und die Nutzbarkeit der Bereiche Magie, Adeptenkräfte und Technomancer.
 * Den Inhalt der Bereiche bauen die Builder in {@code view.tab}; ihre fachlichen Rückmeldungen ({@link
 * CharacterTabHost}) laufen hier zusammen, und hier wird entschieden, was neu gebaut wird.
 */
public class CharacterViewController implements CharacterTabHost {

  private static final String NUYEN_SUFFIX = " ¥";
  @FXML
  private Label nameLabel;
  @FXML
  private ComboBox<MetatypeData> metatypeBox;
  @FXML
  private VBox attributeGridHost;
  @FXML
  private ScrollPane skillsScroll;
  @FXML
  private VBox skillsContainer;
  @FXML
  private Button finalizeBtn;
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
  private Label footerLabel;
  @FXML
  private VBox qualitiesContainer;
  @FXML
  private VBox conditionContainer;
  @FXML
  private VBox contactsContainer;
  @FXML
  private VBox biographyContainer;
  @FXML
  private BorderPane rootPane;
  @FXML
  private StackPane contentHost;
  @FXML
  private VBox magicContainer;
  @FXML
  private VBox adeptContainer;
  @FXML
  private VBox technomancerContainer;
  // Seiten der Bereiche (FXML fx:define), angezeigt vom SectionNavigator
  @FXML
  private Node biographyPage;
  @FXML
  private Node attributesPage;
  @FXML
  private Node skillsPage;
  @FXML
  private Node qualitiesPage;
  @FXML
  private Node conditionPage;
  @FXML
  private Node magicPage;
  @FXML
  private Node adeptPage;
  @FXML
  private Node technomancerPage;
  @FXML
  private Node implantsPage;
  @FXML
  private Node lifestylePage;
  @FXML
  private Node contactsPage;
  @FXML
  private Node logPage;
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
  private VBox knowledgeSkillsContainer;
  @FXML
  private ToggleGroup skillSortToggleGroup;
  @FXML
  private ToggleButton sortByCategoryBtn;
  @FXML
  private ToggleButton sortByAlphabetBtn;
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
  @SuppressWarnings("NullAway.Init")
  private SectionNavigator navigator;
  /** Seite des Status als fester Bereich (Einstellung „Status statisch anzeigen“). */
  private final @NonNull ScrollPane statusPage = new ScrollPane();
  /** Slide-Panel mit dem Status; erst nach {@link #configureStatus} vorhanden. */
  private @Nullable SlidePanel slidePanel;
  private boolean statusStatic;

  @FXML
  public void initialize() {
    attributeTab = new AttributeTabBuilder(attributeGridHost, wiring);
    skillTab = new SkillTabBuilder(skillsScroll, skillsContainer, knowledgeSkillsContainer, skillSortToggleGroup,
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
    logTab = new LogTabBuilder(karmaTable, nuyenTable, karmaTotalLabel, nuyenTotalLabel, this);

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
      refreshRows();
    });

    navigator = new SectionNavigator(contentHost, sectionPages());
    navigator.setShown(CharacterSection.STATUS, false);
    rootPane.setLeft(navigator.node());
  }

  /**
   * Legt fest, wo der Status erscheint: im Slide-Panel mit schwebendem Knopf an {@code buttonPosition} oder, bei
   * {@code statusStatic}, als oberster Bereich unter „Charakter“. Einmal vor {@link #setCharacter} aufzurufen; danach
   * schaltet {@link #setStatusStatic} um.
   */
  public void configureStatus(boolean statusStatic, @NonNull FloatingButtonPosition buttonPosition) {
    slidePanel = new SlidePanel(rootPane, FontAwesomeSolid.HEARTBEAT, "Status (zum Verschieben ziehen)",
        buttonPosition);
    setStatusStatic(statusStatic);
  }

  /** Der Charakterbogen samt Slide-Panel, so wie er ins Hauptfenster gehört. */
  public @NonNull Node node() {
    return slidePanel().node();
  }

  private @NonNull SlidePanel slidePanel() {
    return Objects.requireNonNull(slidePanel, "configureStatus wurde nicht aufgerufen");
  }

  /** Zeigt den Status sofort als festen Bereich oder im Slide-Panel. */
  public void setStatusStatic(boolean statusStatic) {
    this.statusStatic = statusStatic;
    slidePanel().setAvailable(!statusStatic);
    navigator.setShown(CharacterSection.STATUS, statusStatic);
    if (statusStatic) {
      navigator.select(CharacterSection.STATUS);
    }
    updateSummary();
  }

  /**
   * Aktualisiert, was den ganzen Charakter zusammenfasst: Fußzeile ({@link FooterText}, rot bei überzogener Erschaffung)
   * und Status-Übersicht dort, wo sie gerade angezeigt wird. Einzige Stelle dafür; {@link #refreshRows()} ruft sie auf,
   * Pfade ohne Zeilen-Refresh direkt.
   */
  private void updateSummary() {
    if (character == null) {
      return;
    }
    footerLabel.setText(FooterText.of(character));
    setWarnStyle(footerLabel, character.isCreationOverspent());
    Node overview = StatusOverviewBuilder.build(character, this::refreshRows);
    if (statusStatic) {
      statusPage.setContent(overview);
    } else {
      slidePanel().setContent(overview);
    }
  }

  /** Die Seite je Bereich in der Reihenfolge von {@link CharacterSection}. */
  private @NonNull Map<CharacterSection, Node> sectionPages() {
    statusPage.setFitToWidth(true);
    statusPage.getStyleClass().add("attr-scroll");
    statusPage.setPadding(new Insets(12, 36, 24, 36));
    Map<CharacterSection, Node> pages = new EnumMap<>(CharacterSection.class);
    pages.put(CharacterSection.STATUS, statusPage);
    pages.put(CharacterSection.BIOGRAPHY, biographyPage);
    pages.put(CharacterSection.ATTRIBUTES, attributesPage);
    pages.put(CharacterSection.SKILLS, skillsPage);
    pages.put(CharacterSection.QUALITIES, qualitiesPage);
    pages.put(CharacterSection.CONDITION, conditionPage);
    pages.put(CharacterSection.MAGIC, magicPage);
    pages.put(CharacterSection.ADEPT, adeptPage);
    pages.put(CharacterSection.TECHNOMANCER, technomancerPage);
    pages.put(CharacterSection.IMPLANTS, implantsPage);
    pages.put(CharacterSection.LIFESTYLE, lifestylePage);
    pages.put(CharacterSection.ARMOR, armorContainer);
    pages.put(CharacterSection.WEAPONS, weaponContainer);
    pages.put(CharacterSection.GEAR, gearContainer);
    pages.put(CharacterSection.VEHICLES, vehicleContainer);
    pages.put(CharacterSection.CONTACTS, contactsPage);
    pages.put(CharacterSection.LOG, logPage);
    return pages;
  }

  /**
   * Macht die Bereiche Magie ({@link Character#isMagician()}), Adeptenkräfte ({@link Character#hasAdeptPowers()}) und
   * Technomancer ({@link Character#hasResonance()}) passend nutzbar, in beiden Modi. Ohne Gabe bleiben sie sichtbar und
   * deaktiviert, mit Grund im Tooltip (ADR-033). Magieradepten nutzen Magie und Adeptenkräfte zugleich.
   */
  private void syncSpecialSections() {
    if (character == null) {
      return;
    }
    navigator.setEnabled(CharacterSection.MAGIC, character.isMagician(), "Nur mit der Gabe Zauberer oder Magieradept");
    navigator.setEnabled(CharacterSection.ADEPT, character.hasAdeptPowers(), "Nur mit der Gabe Adept oder Magieradept");
    navigator.setEnabled(CharacterSection.TECHNOMANCER, character.hasResonance(), "Nur mit der Gabe Technomancer");
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
    updateSummary();
  }

  @Override
  public void onBpChanged() {
    updateSummary();
  }

  @Override
  public void onLogChanged() {
    logTab.update();
    updateSummary();
  }

  @Override
  public void onNuyenSpentChanged() {
    updateNuyenBpDisplay();
    updateSummary();
  }

  @Override
  public void onQualitiesChanged() {
    buildQualitiesTab();
    attributeTab.build();
    skillTab.build();
    cyberwareTab.build();
    adeptTabBuilder.build();
    refreshRows();
  }

  /** Baut den Bereich Gaben &amp; Handicaps neu und macht Magie, Adeptenkräfte und Technomancer passend nutzbar. */
  private void buildQualitiesTab() {
    qualityTab.build();
    syncSpecialSections();
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
    // Implantate wirken auf Essenz, Attribute, Würfelpools, Initiative, Zustandsmonitor, Panzerung und Waffen
    // (Cyber-Nahkampfwaffen, auch in Cybergliedmaßen).
    attributeTab.build();
    skillTab.build();
    conditionTab.build();
    if (character != null) {
      streetGear.rebuildWeapons(character);
    }
    updateNuyenBpDisplay();
    refreshRows();
  }

  /**
   * Rüstung wirkt auf Attribute (Aug-Spalte durch Behinderung), die Waffen aus Rüstung (Schilde, Schockstreifen),
   * Status und Restgeld; die Fertigkeitspools (Schild-Malus) zieht {@link #refreshRows()} nach.
   */
  @Override
  public void onArmorChanged() {
    attributeTab.build();
    if (character != null) {
      streetGear.rebuildWeapons(character);
    }
    updateNuyenBpDisplay();
    refreshRows();
  }

  @Override
  public void refreshRows() {
    updateSummary();
    attributeTab.refresh();
    skillTab.refresh();
    qualityTab.refresh();
    contactTab.refresh();
    conditionTab.refresh();
    adeptTabBuilder.refresh();
    technomancerTabBuilder.refresh();
    streetGear.refresh();
  }

  private void updateNuyenBpDisplay() {
    if (character == null) {
      return;
    }
    int bp = character.getNuyenBP();
    nuyenBpValueLabel.setText(bp + " BP (" + character.getResourceNuyen() + NUYEN_SUFFIX + ")");
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
    refreshRows();
  }

  @FXML
  private void onNuyenBpPlus() {
    if (character == null) {
      return;
    }
    character.setNuyenBP(character.getNuyenBP() + 1);
    updateNuyenBpDisplay();
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

    updateSummary();
    finalizeBtn.setVisible(!career);
    finalizeBtn.setManaged(!career);
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
    onLogChanged();
  }

}