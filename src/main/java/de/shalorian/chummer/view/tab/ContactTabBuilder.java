package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogCategory;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.reputation.Reputation;
import de.shalorian.chummer.model.character.reputation.ReputationScore;
import de.shalorian.chummer.view.dialog.ContactEditDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Kontakte-Tab: Kontakte mit Connection- und Loyalitätsstufe, Anlegen und Bearbeiten. Bei der Erschaffung kosten
 * die Stufen BP, im Karrieremodus sind sie frei änderbar (SR4-Grundregelwerk kennt keinen Karma-Kauf für Connections).
 */
public final class ContactTabBuilder {

  /** Obergrenze der Spielleiter-Eingaben je Wert; eine reine Bediengrenze, das GRW nennt keine. */
  private static final int ADJUSTMENT_LIMIT = 30;

  private final @NonNull VBox contactsContainer;
  private final @NonNull CharacterTabHost host;
  private final @NonNull IncrementWiring wiring;
  private final @NonNull List<Runnable> contactRefreshers = new ArrayList<>();
  private @Nullable Character character;

  public ContactTabBuilder(@NonNull VBox contactsContainer, @NonNull CharacterTabHost host,
      @NonNull IncrementWiring wiring) {
    this.contactsContainer = contactsContainer;
    this.host = host;
    this.wiring = wiring;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  /** Bewertet die +/−-Zeilen dieses Tabs neu (Restpunkte, Karma, Caps). */
  public void refresh() {
    contactRefreshers.forEach(Runnable::run);
  }

  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    contactsContainer.getChildren().clear();
    contactRefreshers.clear();


    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddContact());

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().add(spacer);
    header.getChildren().add(addBtn);
    contactsContainer.getChildren().add(header);

    MigPane grid = buildContactGrid();
    contactsContainer.getChildren().add(withRightFiller(grid));

    int row = 1;
    for (Contact contact : ch.getContacts()) {
      addContactRow(grid, row++, contact, ch);
    }

    Label reputationTitle = new Label("Ruf");
    reputationTitle.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(reputationTitle, new Insets(16, 0, 6, 0));
    contactsContainer.getChildren().addAll(reputationTitle, withRightFiller(buildReputationGrid(ch)));
    Label hint = new Label();
    hint.getStyleClass().add(STYLE_DETAIL_HINT);
    // Als Refresher, weil sich Charisma ändern kann, ohne dass dieser Tab neu gebaut wird.
    Runnable hintRefresh = () -> hint.setText(("Straßenruf zählt als Würfelbonus höchstens bis Charisma (%d). Der"
        + " Schlechte Ruf wird bei Vertrauensproben abgezogen, beim Einschüchtern addiert (GRW S. 308/309).")
        .formatted(ch.getAugmentedAttributeValue(AttributeType.CHA)));
    contactRefreshers.add(hintRefresh);
    hintRefresh.run();
    hint.setWrapText(true);
    contactsContainer.getChildren().add(hint);
  }

  /**
   * Straßenruf, Schlechter Ruf und Prominenz (GRW S. 308/309) mit Rechenweg; im Karrieremodus dazu die Punkte des
   * Spielleiters und das Opfern von Straßenruf. Die Werte laufen über die Refresh-Liste, weil sie an Karma-Log,
   * Gaben und Handicaps hängen.
   */
  private @NonNull MigPane buildReputationGrid(@NonNull Character ch) {
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3, "[][center]16[][center][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);
    boolean career = ch.isCreated();
    addHeaderRow(grid, "", "Wert", "", career ? "Spielleiter" : "", "", "");
    Reputation reputation = ch.getReputation();
    addReputationRow(grid, 1, "Straßenruf", ch::getStreetCred, career
        ? adjustmentModel(reputation::getStreetCredBonus, reputation::setStreetCredBonus, 0) : null);
    addReputationRow(grid, 2, "Schlechter Ruf", ch::getNotoriety, career
        ? adjustmentModel(reputation::getNotorietyBonus, reputation::setNotorietyBonus, 0) : null);
    addReputationRow(grid, 3, "Prominenz", ch::getPublicAwareness, career
        ? adjustmentModel(reputation::getAwarenessAdjustment, reputation::setAwarenessAdjustment, -ADJUSTMENT_LIMIT) : null);
    if (career) {
      Button burn = new Button("2 Straßenruf opfern");
      burn.getStyleClass().add(STYLE_BTN_SECONDARY);
      useIcon(burn, FontAwesomeSolid.FIRE);
      burn.setOnAction(_ -> {
        if (ch.canBurnStreetCred() && host.services().confirmYesNo("Straßenruf opfern",
            "2 Punkte Straßenruf dauerhaft opfern?", "Der Schlechte Ruf sinkt dafür um 1 (GRW S. 308).")) {
          ch.burnStreetCred();
          host.refreshRows();
        }
      });
      Runnable burnRefresh = () -> burn.setDisable(!ch.canBurnStreetCred());
      contactRefreshers.add(burnRefresh);
      burnRefresh.run();
      grid.add(burn, "cell 0 4, span, gaptop 6");
    }
    return grid;
  }

  private void addReputationRow(@NonNull MigPane grid, int row, @NonNull String name,
      @NonNull Supplier<ReputationScore> score, @Nullable RowModel gm) {
    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add(STYLE_ATTR_NAME);
    Label value = new Label();
    value.getStyleClass().add(STYLE_ATTR_VALUE);
    Label breakdown = new Label();
    breakdown.getStyleClass().add(STYLE_DETAIL_HINT);
    Runnable refresh = () -> {
      ReputationScore current = score.get();
      value.setText(String.valueOf(current.value()));
      breakdown.setText(current.breakdown());
    };
    contactRefreshers.add(refresh);
    refresh.run();
    if (gm == null) {
      addRowCells(grid, row, nameLabel, value, new Label(), new Label(), new Label(), breakdown);
      return;
    }
    Button minus = RowControls.minusButton();
    Label gmValue = new Label();
    Button plus = RowControls.plusButton();
    gmValue.getStyleClass().add(STYLE_ATTR_VALUE);
    wiring.wireFree(contactRefreshers, new RowControls(gmValue, new Label(), minus, plus), gm);
    addRowCells(grid, row, nameLabel, value, minus, gmValue, plus, breakdown);
  }

  /** +/−-Modell für die Ruf-Eingaben des Spielleiters; keine Buchung, kein Log (Plan-Annahme A1, ADR-059). */
  private static @NonNull RowModel adjustmentModel(@NonNull IntSupplier getter, @NonNull IntConsumer setter, int min) {
    return new RowModel() {
      @Override
      public int value() {
        return getter.getAsInt();
      }

      @Override
      public int min() {
        return min;
      }

      @Override
      public int max() {
        return ADJUSTMENT_LIMIT;
      }

      @Override
      public int bpStepCost() {
        return 0;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return "";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        setter.accept(getter.getAsInt() + 1);
      }

      @Override
      public void decrement() {
        setter.accept(getter.getAsInt() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return "Spielleiter " + from + " → " + to;
      }
    };
  }

  /**
   * Baut das Kontakte-Grid inkl. Kopfzeile (Zeile 0) mit "Verbindung"/"Loyalität"-Spaltenüberschriften über den jeweiligen Wert-Spalten — ersetzt die
   * im alten WinForms-UI pro Zeile wiederholten "Connection:"/"Loyalty:"-Inline-Labels (dort nötig, da jede Kontakt-Zeile aus einer eigenständigen
   * UserControl-Instanz bestand) durch eine einzelne gemeinsame Kopfzeile, analog zum bestehenden Attribute-/Zauber-/Komplexe-Formen-Grid-Muster.
   */
  private @NonNull MigPane buildContactGrid() {
    // Beide Wert-Spalten (Verbindung/Loyalität) durch Contact.MAX_RATING = 6 einstellig, keine feste
    // Ausnahme nötig. Kein eigenes Spaltenwachstum — ungenutzter Platz wird außen über withRightFiller
    // verteilt.
    MigPane grid = new MigPane(MIGPANE_HIDEMODE_3,
        "[][][center][center][center][][center][center][center][][]");
    grid.getStyleClass().add(STYLE_ATTR_GRID);

    String[] headers = {"", "", "", "Verbindung", "", "", "", "Loyalität", "", "", ""};
    addHeaderRow(grid, headers);

    return grid;
  }

  private void addContactRow(@NonNull MigPane grid, int row, @NonNull Contact contact,
      @NonNull Character character) {
    Label nameLabelDummy = new Label(contact.getName());
    Button editBtn = new Button("Bearbeiten");
    RowControls connectionControls = RowControls.create();
    Button connectionMinus = connectionControls.minus();
    Label connectionValue = connectionControls.valueLabel();
    Button connectionPlus = connectionControls.plus();
    Label connectionCost = connectionControls.costLabel();
    RowControls loyaltyControls = RowControls.create();
    Button loyaltyMinus = loyaltyControls.minus();
    Label loyaltyValue = loyaltyControls.valueLabel();
    Button loyaltyPlus = loyaltyControls.plus();
    Label loyaltyCost = loyaltyControls.costLabel();
    Button removeBtn = new Button("Entfernen");

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    editBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    removeBtn.getStyleClass().add(STYLE_BTN_SECONDARY);

    Runnable tooltipRefresh = () -> {
      String description = contact.getDescription();
      Tooltip.install(nameLabelDummy, description != null ? new Tooltip(description) : null);
    };
    contactRefreshers.add(tooltipRefresh);
    tooltipRefresh.run();

    editBtn.setOnAction(_ -> onEditContact(contact));

    boolean career = character.isCreated();

    RowModel connectionModel = new RowModel() {
      @Override
      public int value() {
        return contact.getConnection();
      }

      @Override
      public int min() {
        return Contact.MIN_RATING;
      }

      @Override
      public int max() {
        return Contact.MAX_RATING;
      }

      @Override
      public int bpStepCost() {
        return 1;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return contact.getConnection() >= Contact.MAX_RATING ? "–" : "1 BP";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        contact.setConnection(contact.getConnection() + 1);
      }

      @Override
      public void decrement() {
        contact.setConnection(contact.getConnection() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return contact.getName() + " Connection " + from + " → " + to;
      }
    };

    RowModel loyaltyModel = new RowModel() {
      @Override
      public int value() {
        return contact.getLoyalty();
      }

      @Override
      public int min() {
        return Contact.MIN_RATING;
      }

      @Override
      public int max() {
        return Contact.MAX_RATING;
      }

      @Override
      public int bpStepCost() {
        return 1;
      }

      @Override
      public @NonNull String bpDisplayText() {
        return contact.getLoyalty() >= Contact.MAX_RATING ? "–" : "1 BP";
      }

      @Override
      public int karmaStepCost() {
        return 0;
      }

      @Override
      public void increment() {
        contact.setLoyalty(contact.getLoyalty() + 1);
      }

      @Override
      public void decrement() {
        contact.setLoyalty(contact.getLoyalty() - 1);
      }

      @Override
      public @NonNull String actionLabel(int from, int to) {
        return contact.getName() + " Loyalty " + from + " → " + to;
      }
    };

    if (career) {
      wiring.wireFree(contactRefreshers,
          connectionControls,
          connectionModel);
      wiring.wireFree(contactRefreshers,
          loyaltyControls, loyaltyModel);
    } else {
      wiring.wire(contactRefreshers,
          connectionControls, false,
          connectionModel, character, LogCategory.CONTACT);
      wiring.wire(contactRefreshers,
          loyaltyControls, false, loyaltyModel,
          character, LogCategory.CONTACT);
    }

    useIcon(removeBtn, FontAwesomeSolid.TRASH);
    removeBtn.setOnAction(_ -> {
      character.removeContact(contact);
      build();
      host.onBpChanged();
      host.refreshRows();
    });

    addRowCells(grid, row, nameLabelDummy, editBtn, connectionMinus, connectionValue, connectionPlus,
        connectionCost, loyaltyMinus, loyaltyValue, loyaltyPlus, loyaltyCost, removeBtn);
  }

  private void onAddContact() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    try {
      DialogHandle dialog = host.services().openDialog("ContactEditDialog.fxml", "Kontakt hinzufügen");
      ContactEditDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        ch.addContact(r.name(), r.description());
        build();
        host.onBpChanged();
        host.refreshRows();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }

  private void onEditContact(@NonNull Contact contact) {
    try {
      DialogHandle dialog = host.services().openDialog("ContactEditDialog.fxml", "Kontakt bearbeiten");
      ContactEditDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      ctrl.prefill(contact);
      dialog.stage().showAndWait();

      ctrl.getResult().ifPresent(r -> {
        contact.setName(r.name());
        contact.setDescription(r.description());
        build();
        host.refreshRows();
      });
    } catch (IOException _) {
      host.services().showDialogLoadError();
    }
  }
}
