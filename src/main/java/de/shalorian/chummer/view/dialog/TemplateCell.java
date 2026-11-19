package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.rulebook.RulebookData;
import de.shalorian.chummer.model.character.rulebook.RulebookDataLoader;
import de.shalorian.chummer.model.template.DocumentTemplate;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Zelle der Vorlagen-Auswahl im Dialog „Neu“: „Name (SR4)“ mit Tooltip „Grundregelwerk, S. 135“. In der Liste steht über
 * der ersten Vorlage jeder Art deren Überschrift (z.B. „Archetypen“); die Überschrift ist kein eigener Eintrag und
 * damit nicht wählbar.
 */
final class TemplateCell extends ListCell<DocumentTemplate> {

  @Override
  protected void updateItem(@Nullable DocumentTemplate template, boolean empty) {
    super.updateItem(template, empty);
    setText(null);
    setGraphic(null);
    setTooltip(null);
    if (empty || template == null) {
      return;
    }
    String text = template.name() + " (" + template.source() + ")";
    // nur in der aufgeklappten Liste: die Zelle des Auswahlfelds selbst hat keine ListView und Index -1
    List<DocumentTemplate> items = getListView() == null ? List.of() : getListView().getItems();
    int index = getIndex();
    boolean firstOfKind = !items.isEmpty() && index >= 0 && index < items.size()
        && (index == 0 || items.get(index - 1).kind() != template.kind());
    if (firstOfKind) {
      Label header = new Label(template.kind().getDisplayName());
      header.getStyleClass().add("key-caption");
      Label label = new Label(text);
      // ein Label als Grafik übernimmt die Schriftfarbe der Zelle nicht von selbst (auch nicht beim Hover)
      label.textFillProperty().bind(textFillProperty());
      setGraphic(new VBox(2, header, label));
    } else {
      setText(text);
    }
    setTooltip(new Tooltip(bookName(template.source()) + ", S. " + template.page()));
  }

  private static @NonNull String bookName(@NonNull String code) {
    return RulebookDataLoader.getAll().stream().filter(b -> b.code().equals(code)).map(RulebookData::name)
        .findFirst().orElse(code);
  }
}
