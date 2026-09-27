package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.CatalogEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Wiederverwendbares Fundament für Katalog-Auswahl-Dialoge: Kategorie-Filter-{@code ComboBox},
 * Such-{@code TextField} und {@code ListView} mit namensbasierter Cellfactory. Konkrete Subklassen
 * (z.B. {@link QualitySelectDialogController}) implementieren die vier Hooks, um den jeweiligen
 * Katalog anzubinden, und ergänzen ihr eigenes Info-Panel sowie ihre eigenen Bestätigungs-Buttons —
 * dieses Fundament kümmert sich ausschließlich um Filtern/Anzeigen der Liste, nicht um Auswahl-
 * Bestätigung oder Kosten, da sich diese je Katalogtyp zu stark unterscheiden (Gratis-Checkbox bei
 * Qualities, feste Kosten bei Zaubern, künftig Rating-Auswahl bei Adeptenkräften etc.).
 *
 * @param <T> Typ der Katalogeinträge (z.B. {@code QualityData}, {@code SpellData})
 */
public abstract class CatalogSelectDialogController<T extends CatalogEntry> {

  private static final String CATEGORY_ALL = "Alle";

  @FXML
  protected ComboBox<String> categoryBox;
  @FXML
  protected TextField searchField;
  @FXML
  protected ListView<T> itemListView;

  @FXML
  public void initialize() {
    categoryBox.getItems().add(CATEGORY_ALL);
    categoryBox.getItems().addAll(categoryOptions());
    categoryBox.setValue(CATEGORY_ALL);

    categoryBox.valueProperty().addListener((obs, old, val) -> updateList());
    searchField.textProperty().addListener((obs, old, val) -> updateList());

    itemListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, old, val) -> onListUpdated());

    itemListView.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.name());
        setDisable(!empty && item != null && !isSelectable(item));
      }
    });

    updateList();
  }

  /**
   * Baut die Kategorie-Auswahl neu aus {@link #categoryOptions()} auf und setzt sie auf "Alle" zurück —
   * für Subklassen, deren Kategorien von einem erst nach {@link #initialize()} gesetzten Zustand
   * abhängen (z.B. Cyberware- vs. Bioware-Kategorien).
   */
  protected void refreshCategoryOptions() {
    categoryBox.getItems().setAll(CATEGORY_ALL);
    categoryBox.getItems().addAll(categoryOptions());
    categoryBox.setValue(CATEGORY_ALL);
  }

  /** Kategorie-Bezeichnungen (ohne {@code "Alle"}, das wird automatisch vorangestellt). */
  protected abstract @NonNull List<String> categoryOptions();

  /** Der vollständige, ungefilterte Katalog. */
  protected abstract @NonNull List<T> allEntries();

  /** Prüft, ob {@code entry} zur gewählten Kategorie passt ({@code category} ist nie {@code "Alle"}). */
  protected abstract boolean matchesCategory(@NonNull T entry, @NonNull String category);

  /**
   * Zusätzlicher, katalogspezifischer Filter (z.B. "Nur wählbare anzeigen" bei Qualities, bereits
   * gelernte Zauber ausblenden bei Spells). Default: kein Zusatzfilter.
   */
  protected boolean additionalFilter(@NonNull T entry) {
    return true;
  }

  /**
   * Ob {@code entry} aktuell gewählt werden darf. Nicht wählbare Einträge bleiben in der Liste
   * sichtbar, werden aber deaktiviert dargestellt (z.B. Verfügbarkeit über 12 bei der
   * Charaktererschaffung). Subklassen müssen die Regel zusätzlich beim Bestätigen prüfen, da die
   * Auswahl per Tastatur deaktivierte Zellen nicht zuverlässig überspringt. Default: alle wählbar.
   */
  protected boolean isSelectable(@NonNull T entry) {
    return true;
  }

  /** Wird nach jedem {@link #updateList()} sowie bei Selektionsänderung aufgerufen — Subklassen aktualisieren hier ihr Info-Panel. */
  protected void onListUpdated() {
  }

  protected void updateList() {
    String category = categoryBox.getValue();
    String search = searchField.getText().trim().toLowerCase(Locale.ROOT);

    List<T> filtered = allEntries().stream()
        .filter(e -> category == null || CATEGORY_ALL.equals(category) || matchesCategory(e, category))
        .filter(e -> search.isEmpty() || e.name().toLowerCase(Locale.ROOT).contains(search))
        .filter(this::additionalFilter)
        .sorted(Comparator.comparing(CatalogEntry::name))
        .toList();

    T selected = itemListView.getSelectionModel().getSelectedItem();
    itemListView.getItems().setAll(filtered);
    itemListView.refresh();
    if (selected != null && filtered.contains(selected)) {
      itemListView.getSelectionModel().select(selected);
    }
    onListUpdated();
  }
}
