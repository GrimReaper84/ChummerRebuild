package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.option.RuleOptions;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.css.PseudoClass;
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

  /** Abgeblendeter, aber auswählbarer Eintrag (siehe {@link #isSelectable}). */
  private static final PseudoClass BLOCKED = PseudoClass.getPseudoClass("blocked");

  private static final String CATEGORY_ALL = "Alle";

  @FXML
  protected ComboBox<String> categoryBox;
  @FXML
  protected TextField searchField;
  @FXML
  protected ListView<T> itemListView;

  private @Nullable RuleOptions ruleOptions;

  @FXML
  public void initialize() {
    refreshCategoryOptions();

    categoryBox.valueProperty().addListener((_, _, _) -> updateList());
    searchField.textProperty().addListener((_, _, _) -> updateList());

    itemListView.getSelectionModel().selectedItemProperty()
        .addListener((_, _, _) -> onListUpdated());

    itemListView.setCellFactory(_ -> new ListCell<>() {
      @Override
      protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.name());
        // abgeblendet statt deaktiviert: auswählbar, damit der Detailbereich den Grund nennt; OK sperrt die Subklasse
        pseudoClassStateChanged(BLOCKED, !empty && item != null && !isSelectable(item));
      }
    });

    updateList();
  }

  /**
   * Baut die Kategorie-Auswahl neu aus {@link #categoryOptions()} auf und setzt sie auf "Alle" zurück —
   * auch für Subklassen, deren Kategorien von einem erst nach {@link #initialize()} gesetzten Zustand
   * abhängen (z.B. Cyberware- vs. Bioware-Kategorien). Die Kategorien stehen alphabetisch (deutsche
   * Sortierung, Umlaute wie im Wörterbuch) unter "Alle" (Nutzervorgabe 2026-10-08, gilt für alle
   * Kategorie-Dropdowns).
   */
  protected void refreshCategoryOptions() {
    Collator collator = Collator.getInstance(Locale.GERMAN);
    categoryBox.getItems().setAll(CATEGORY_ALL);
    categoryBox.getItems().addAll(categoryOptions().stream().filter(this::hasEntriesFromEnabledBooks)
        .sorted(collator).toList());
    categoryBox.setValue(CATEGORY_ALL);
  }

  /**
   * Übernimmt die Regel-Einstellungen des Charakters: Einträge aus abgeschalteten Regelwerken werden ausgeblendet, nicht
   * deaktiviert (Nutzerentscheidung 2026-11-18, ADR-061). Ohne Aufruf gilt alles als erlaubt. Gesetzt wird das zentral
   * beim Öffnen des Dialogs ({@code TabServices#openDialog}).
   */
  public void setRuleOptions(@NonNull RuleOptions ruleOptions) {
    this.ruleOptions = ruleOptions;
    refreshCategoryOptions();
    updateList();
  }

  /** Eine Kategorie bleibt, solange sie leer ist oder einen Eintrag aus einem aktiven Regelwerk hat. */
  private boolean hasEntriesFromEnabledBooks(@NonNull String category) {
    if (ruleOptions == null) {
      return true;
    }
    List<T> inCategory = allEntries().stream().filter(e -> matchesCategory(e, category)).toList();
    return inCategory.isEmpty() || inCategory.stream().anyMatch(this::allowedByRulebooks);
  }

  private boolean allowedByRulebooks(@NonNull T entry) {
    return ruleOptions == null || ruleOptions.allows(entry);
  }

  /**
   * Kategorie-Bezeichnungen (ohne {@code "Alle"}, das wird automatisch vorangestellt); die Reihenfolge
   * spielt keine Rolle, sortiert wird in {@link #refreshCategoryOptions()}.
   */
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
   * Ob {@code entry} aktuell gewählt werden darf. Nicht wählbare Einträge bleiben in der Liste sichtbar und auswählbar,
   * werden aber abgeblendet dargestellt (Pseudoklasse {@code :blocked}, z.B. Verfügbarkeit über 12 bei der
   * Charaktererschaffung). Subklassen nennen den Grund im Detailbereich und sperren OK. Default: alle wählbar.
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
        .filter(this::allowedByRulebooks)
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
