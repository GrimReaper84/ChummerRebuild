package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.addHeaderRow;
import static de.shalorian.chummer.view.grid.GridSupport.addRowCells;
import static de.shalorian.chummer.view.grid.GridSupport.useIcon;
import static de.shalorian.chummer.view.grid.GridSupport.withRightFiller;
import static de.shalorian.chummer.view.grid.StyleClasses.MIGPANE_HIDEMODE_3;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BP;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_BTN;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_GRID;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_NAME;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_ATTR_VALUE;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_BTN_SECONDARY;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.Contact;
import de.shalorian.chummer.view.dialog.ContactEditDialogController;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.grid.RowControls;
import de.shalorian.chummer.view.grid.RowModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    Label sectionLabel = new Label("Kontakte");
    sectionLabel.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(sectionLabel, new Insets(0, 0, 4, 0));

    Button addBtn = new Button("Hinzufügen");
    addBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    useIcon(addBtn, FontAwesomeSolid.PLUS);
    addBtn.setOnAction(_ -> onAddContact());

    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getChildren().add(sectionLabel);
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
    Button connectionMinus = new Button("−");
    Label connectionValue = new Label();
    Button connectionPlus = new Button("+");
    Label connectionCost = new Label();
    Button loyaltyMinus = new Button("−");
    Label loyaltyValue = new Label();
    Button loyaltyPlus = new Button("+");
    Label loyaltyCost = new Label();
    Button removeBtn = new Button("Entfernen");

    nameLabelDummy.getStyleClass().add(STYLE_ATTR_NAME);
    editBtn.getStyleClass().add(STYLE_BTN_SECONDARY);
    connectionMinus.getStyleClass().add(STYLE_ATTR_BTN);
    connectionValue.getStyleClass().add(STYLE_ATTR_VALUE);
    connectionPlus.getStyleClass().add(STYLE_ATTR_BTN);
    connectionCost.getStyleClass().add(STYLE_ATTR_BP);
    loyaltyMinus.getStyleClass().add(STYLE_ATTR_BTN);
    loyaltyValue.getStyleClass().add(STYLE_ATTR_VALUE);
    loyaltyPlus.getStyleClass().add(STYLE_ATTR_BTN);
    loyaltyCost.getStyleClass().add(STYLE_ATTR_BP);
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
          new RowControls(connectionValue, connectionCost, connectionMinus, connectionPlus),
          connectionModel);
      wiring.wireFree(contactRefreshers,
          new RowControls(loyaltyValue, loyaltyCost, loyaltyMinus, loyaltyPlus), loyaltyModel);
    } else {
      wiring.wire(contactRefreshers,
          new RowControls(connectionValue, connectionCost, connectionMinus, connectionPlus), false,
          connectionModel, character);
      wiring.wire(contactRefreshers,
          new RowControls(loyaltyValue, loyaltyCost, loyaltyMinus, loyaltyPlus), false, loyaltyModel,
          character);
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
