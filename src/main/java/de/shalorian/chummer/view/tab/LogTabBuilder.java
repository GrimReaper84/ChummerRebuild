package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.utils.Numbers;
import de.shalorian.chummer.view.dialog.DialogHandle;
import de.shalorian.chummer.view.dialog.LogEntryDialogController;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Log-Tab: Karma- und Nuyen-Tabelle samt Summen (auch in der Statuszeile) und der Dialog für neue Einträge.
 */
public final class LogTabBuilder {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final @NonNull TableView<LogEntry> karmaTable;
  private final @NonNull TableView<LogEntry> nuyenTable;
  private final @NonNull Label karmaTotalLabel;
  private final @NonNull Label nuyenTotalLabel;
  private final @NonNull CharacterTabHost host;
  private @Nullable Character character;

  public LogTabBuilder(@NonNull TableView<LogEntry> karmaTable,
      @NonNull TableView<LogEntry> nuyenTable, @NonNull Label karmaTotalLabel,
      @NonNull Label nuyenTotalLabel, @NonNull CharacterTabHost host) {
    this.karmaTable = karmaTable;
    this.nuyenTable = nuyenTable;
    this.karmaTotalLabel = karmaTotalLabel;
    this.nuyenTotalLabel = nuyenTotalLabel;
    this.host = host;
    buildLogTable(karmaTable);
    buildLogTable(nuyenTable);
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  @SuppressWarnings("unchecked")
  private void buildLogTable(@NonNull TableView<LogEntry> table) {
    TableColumn<LogEntry, String> categoryCol = new TableColumn<>("Art");
    categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().category().getDisplayName()));
    categoryCol.setPrefWidth(110);

    TableColumn<LogEntry, String> dateCol = new TableColumn<>("Datum");
    dateCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().date().format(DATE_FMT)));
    dateCol.setPrefWidth(90);

    TableColumn<LogEntry, String> amountCol = new TableColumn<>("Menge");
    amountCol.setCellValueFactory(c -> {
      int a = c.getValue().amount();
      return new SimpleStringProperty(Numbers.signed(a));
    });
    amountCol.setPrefWidth(70);
    amountCol.getStyleClass().add("number-cell");

    TableColumn<LogEntry, String> descCol = new TableColumn<>("Bezeichnung");
    descCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().description()));

    table.getColumns().addAll(dateCol, categoryCol, amountCol, descCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPlaceholder(new Label("Keine Einträge"));
  }

  public void update() {
    if (character == null) {
      return;
    }

    karmaTable.getItems().setAll(character.getKarmaLog());
    nuyenTable.getItems().setAll(character.getNuyenLog());

    int karma = character.getKarmaTotal();
    karmaTotalLabel.setText(String.valueOf(karma));

    int nuyen = character.getNuyenTotal();
    nuyenTotalLabel.setText(SpendCurrency.NUYEN.formatAmount(nuyen));
  }

  public @NonNull Optional<LogEntry> openEntryDialog(@NonNull String title) {
    try {
      DialogHandle dialog = host.services().openDialog("LogEntryDialog.fxml", title);
      LogEntryDialogController ctrl = dialog.loader().getController();
      ctrl.setStage(dialog.stage());
      dialog.stage().showAndWait();
      return ctrl.getResult();
    } catch (IOException _) {
      host.services().showDialogLoadError();
      return Optional.empty();
    }
  }
}
