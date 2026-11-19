package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.markGmFree;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_RANGE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import de.shalorian.chummer.model.character.lifestyle.MissedPaymentResult;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.converter.LifestyleLevelStringConverter;
import de.shalorian.chummer.view.grid.RowControls;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Baut die Ausrüstungsbereiche Lebensstil, Rüstung, Waffen und Ausrüstung (ursprünglich der Tab
 * {@code tabStreetGearTabs} aus {@code frmCreate.Designer.cs}; die Rüstung baut {@link ArmorTabBuilder}, die Waffen
 * {@link WeaponTabBuilder}, die Ausrüstung {@link GearTabBuilder}).
 * Die Unter-Tabs werden bei jeder Änderung komplett neu aufgebaut; die Rüstungs-Kopfzeile
 * (Konstitution) und die Waffenwerte (Stärke, Fertigkeiten) hängen zusätzlich am Charakter und werden
 * deshalb über {@link #refresh()} beim Refresh des {@code CharacterViewController} nachgezogen. Alles
 * außerhalb dieses Tabs erreicht der Builder ausschließlich über den {@link CharacterTabHost}.
 */
public final class StreetGearTabBuilder {


  private final @NonNull VBox lifestyleContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull WeaponTabBuilder weaponTab;
  private final @NonNull GearTabBuilder gearTab;
  private final @NonNull ArmorTabBuilder armorTab;

  public StreetGearTabBuilder(@NonNull VBox lifestyleContainer, @NonNull VBox armorContainer,
      @NonNull VBox weaponContainer, @NonNull VBox gearContainer, @NonNull CharacterTabHost host) {
    this.lifestyleContainer = lifestyleContainer;
    this.armorTab = new ArmorTabBuilder(armorContainer, host);
    this.host = host;
    this.weaponTab = new WeaponTabBuilder(weaponContainer, host);
    this.gearTab = new GearTabBuilder(gearContainer, host);
  }

  /** Baut nur die Waffen neu (Waffen aus Rüstung kommen und gehen mit angelegten Schilden, GRW S. 373). */
  public void rebuildWeapons(@NonNull Character character) {
    weaponTab.build(character);
  }

  /** Baut alle Unter-Tabs für {@code character} neu auf. */
  public void build(@NonNull Character character) {
    buildLifestyleTab(character);
    armorTab.build(character);
    weaponTab.build(character);
    gearTab.build(character);
  }

  /**
   * Zieht die von Attributen und Fertigkeiten abhängigen Anzeigen nach: Schutz/Behinderung der Rüstung
   * sowie der Detailbereich der gewählten Waffe (Schaden, Reichweiten, Fertigkeitswert).
   */
  public void refresh() {
    armorTab.refresh();
    weaponTab.refresh();
  }

  // ── Lebensstil ────────────────────────────────────────────

  /**
   * Lebensstil-Unter-Tab: Stufe und im Voraus bezahlte Monate (GRW S. 111), Kosten und — bei der
   * Erschaffung — der Startkapital-Wurf, den die Stufe beim Abschluss festlegt (GRW S. 114). Im
   * Karrieremodus Monat bezahlen und Stufe wechseln (S. 310).
   */
  private void buildLifestyleTab(@NonNull Character character) {
    lifestyleContainer.getChildren().clear();


    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    VBox.setMargin(grid, new Insets(12, 0, 0, 0));
    lifestyleContainer.getChildren().add(withRightFiller(grid));

    if (character.isCreated()) {
      fillLifestyleInCareerMode(grid, character);
    } else {
      fillLifestyleInCreationMode(grid, character);
    }
  }

  private void fillLifestyleInCreationMode(@NonNull MigPane grid, @NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    LifestyleLevel level = lifestyle.getLevel();
    addLifestyleRow(grid, 0, "Stufe:", buildLifestyleLevelBox(character));
    addLifestyleRow(grid, 1, "Monate im Voraus:", buildLifestyleMonthsStepper(character));
    addLifestyleCostRows(grid, 2, lifestyle);
    addLifestyleRow(grid, 4, "Startkapital-Wurf:", new Label(
        level.getDice() + "W6 × " + SpendCurrency.NUYEN.formatAmount(level.getMultiplier())));
    addLifestyleHint("Die Stufe bestimmt beim Abschluss den Startkapital-Wurf; nicht ausgegebene "
        + "Ressourcen verfallen (GRW S. 114).");
  }

  /**
   * Karrieremodus (GRW S. 310): „Monat bezahlen“ bucht die Monatskosten und zählt einen Monat dazu; eine neue Stufe
   * bucht ihren ersten Monat. Gebucht wird vor der Änderung (Rückgängig, ADR-058). Mit der Regel-Einstellung für
   * versäumte Zahlungen kommen „Monat aussetzen“, Rückstand und Schulden dazu (S. 312, ADR-061).
   */
  private void fillLifestyleInCareerMode(@NonNull MigPane grid, @NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    LifestyleLevel current = lifestyle.getLevel();
    ComboBox<LifestyleLevel> levelBox = new ComboBox<>();
    levelBox.getItems().setAll(LifestyleLevel.values());
    levelBox.setConverter(new LifestyleLevelStringConverter());
    levelBox.setValue(current);
    Button change = new Button("Stufe wechseln");
    change.getStyleClass().add(STYLE_BTN_SECONDARY);
    change.disableProperty().bind(levelBox.valueProperty().isEqualTo(current));
    change.setOnAction(_ -> {
      LifestyleLevel target = levelBox.getValue();
      // kostenlose Stufe (Straße): wechseln ohne Buchung, wie „Monat bezahlen“ bei 0 ¥ gesperrt ist
      if (target != null && (target.getMonthlyCost() == 0 || !host.services().nuyenChargeFailed(character,
          LogCategory.LIFESTYLE, "Lebensstil " + target.getDisplayName() + " (1. Monat)",
          target.getMonthlyCost()))) {
        lifestyle.changeLevel(target);
        onLifestyleChanged(character);
      }
    });
    HBox levelRow = new HBox(8, levelBox, change);
    levelRow.setAlignment(Pos.CENTER_LEFT);
    addLifestyleRow(grid, 0, "Stufe:", levelRow);

    Button pay = new Button("Monat bezahlen");
    pay.getStyleClass().add(STYLE_BTN_SECONDARY);
    pay.setDisable(current.getMonthlyCost() == 0);
    pay.setOnAction(_ -> {
      if (!host.services().nuyenChargeFailed(character, LogCategory.LIFESTYLE,
          "Lebensstil " + current.getDisplayName() + " (Monat " + (lifestyle.getMonths() + 1) + ")",
          current.getMonthlyCost())) {
        lifestyle.payMonth();
        onLifestyleChanged(character);
      }
    });
    HBox monthsRow = new HBox(8, new Label(String.valueOf(lifestyle.getMonths())), pay);
    monthsRow.setAlignment(Pos.CENTER_LEFT);
    addLifestyleRow(grid, 1, "Bezahlte Monate:", monthsRow);
    addLifestyleCostRows(grid, 2, lifestyle);
    if (!character.getRuleOptions().isMissedLifestylePayments()) {
      addLifestyleHint("Versäumte Zahlungen regelt der Spielleiter (GRW S. 312).");
      return;
    }
    Button skip = new Button("Monat aussetzen");
    skip.getStyleClass().add(STYLE_BTN_SECONDARY);
    skip.setDisable(current.getMonthlyCost() == 0);
    skip.setOnAction(_ -> skipLifestyleMonth(character));
    monthsRow.getChildren().add(skip);
    int row = 4;
    if (lifestyle.getMissedMonths() > 0) {
      addLifestyleRow(grid, row++, "Rückstand:", new Label(lifestyle.getMissedMonths() + " Monat"
          + (lifestyle.getMissedMonths() == 1 ? "" : "e") + " in Folge"));
    }
    if (lifestyle.getDebt() > 0) {
      Button payDebt = new Button("Schulden tilgen");
      payDebt.getStyleClass().add(STYLE_BTN_SECONDARY);
      payDebt.setOnAction(_ -> {
        if (!host.services().nuyenChargeFailed(character, LogCategory.LIFESTYLE, "Schulden aus Lebensstil getilgt",
            lifestyle.getDebt())) {
          lifestyle.payDebt();
          onLifestyleChanged(character);
        }
      });
      HBox debtRow = new HBox(8, new Label(Numbers.nuyen(lifestyle.getDebt())), payDebt);
      debtRow.setAlignment(Pos.CENTER_LEFT);
      addLifestyleRow(grid, row, "Schulden:", debtRow);
    }
    addLifestyleHint("„Monat aussetzen“ würfelt nach GRW S. 312: Ein Wurf bis zur Zahl der ausgesetzten Monate in "
        + "Folge senkt den Lebensstil um eine Stufe, ein Monat der alten Stufe wird Schuld.");
  }

  /**
   * Setzt einen Monat aus: würfelt einen W6, bucht einen Log-Eintrag ohne Betrag (damit Rückgängig greift, vor der
   * Änderung wie jede Buchung) und meldet das Ergebnis.
   */
  private void skipLifestyleMonth(@NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    int roll = ThreadLocalRandom.current().nextInt(1, 7);
    LifestyleLevel before = lifestyle.getLevel();
    MissedPaymentResult result = lifestyle.skipOutcome(roll);
    String outcome = result.downgraded()
        ? "Abstieg auf %s, Schulden +%s".formatted(result.level().getDisplayName(), Numbers.nuyen(result.debtAdded()))
        : "kein Problem";
    String text = "Monat ausgesetzt (%d. in Folge), Wurf %d: %s".formatted(result.missedMonths(), roll, outcome);
    host.services().bookNuyen(character, LogCategory.LIFESTYLE, "Lebensstil " + before.getDisplayName() + ": " + text,
        0);
    lifestyle.skipMonth(roll);
    host.services().warn("Lebensstil", text);
    onLifestyleChanged(character);
  }

  private void addLifestyleCostRows(@NonNull MigPane grid, int row, @NonNull Lifestyle lifestyle) {
    addLifestyleRow(grid, row, "Monatskosten:",
        new Label(SpendCurrency.NUYEN.formatAmount(lifestyle.getLevel().getMonthlyCost())));
    Label total = new Label(SpendCurrency.NUYEN.formatAmount(lifestyle.getNuyenCost()));
    markGmFree(total, lifestyle.getGmFreeNuyen() > 0, Numbers.nuyen(lifestyle.getGmFreeNuyen()));
    addLifestyleRow(grid, row + 1, "Gesamtkosten:", total);
  }

  private @NonNull ComboBox<LifestyleLevel> buildLifestyleLevelBox(@NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    ComboBox<LifestyleLevel> levelBox = new ComboBox<>();
    levelBox.getItems().setAll(LifestyleLevel.values());
    levelBox.setConverter(new LifestyleLevelStringConverter());
    levelBox.setValue(lifestyle.getLevel());
    levelBox.valueProperty().addListener((_, _, val) -> {
      if (val != null) {
        lifestyle.setLevel(val);
        onLifestyleChanged(character);
      }
    });
    return levelBox;
  }

  private @NonNull HBox buildLifestyleMonthsStepper(@NonNull Character character) {
    Lifestyle lifestyle = character.getEquipment().getLifestyle();
    Button minus = RowControls.minusButton();
    Label value = new Label(String.valueOf(lifestyle.getMonths()));
    Button plus = RowControls.plusButton();
    value.getStyleClass().add(STYLE_ATTR_VALUE);
    // Der Tab wird nach jeder Änderung neu aufgebaut, daher bleibt dieser Zustand aktuell.
    minus.setDisable(lifestyle.getMonths() <= Lifestyle.MIN_MONTHS);
    minus.setOnAction(_ -> {
      if (lifestyle.getMonths() > Lifestyle.MIN_MONTHS) {
        lifestyle.setMonths(lifestyle.getMonths() - 1);
        onLifestyleChanged(character);
      }
    });
    plus.setOnAction(_ -> {
      lifestyle.setMonths(lifestyle.getMonths() + 1);
      onLifestyleChanged(character);
    });
    HBox box = new HBox(6, minus, value, plus);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private static void addLifestyleRow(@NonNull MigPane grid, int row, @NonNull String caption,
      @NonNull Node control) {
    Label captionLabel = new Label(caption);
    captionLabel.getStyleClass().add(STYLE_ATTR_NAME);
    if (control instanceof Label label) {
      label.getStyleClass().add(STYLE_ATTR_BP);
    }
    addRowCells(grid, row, captionLabel, control);
  }

  private void addLifestyleHint(@NonNull String text) {
    Label hint = new Label(text);
    hint.getStyleClass().add(STYLE_ATTR_RANGE);
    VBox.setMargin(hint, new Insets(12, 0, 0, 0));
    lifestyleContainer.getChildren().add(hint);
  }

  private void onLifestyleChanged(@NonNull Character character) {
    buildLifestyleTab(character);
    host.onNuyenSpentChanged();
  }
}
