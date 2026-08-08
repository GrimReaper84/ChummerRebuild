package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.magic.SpellData;
import de.shalorian.chummer.model.character.magic.SpellDataLoader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SpellSelectDialogController extends CatalogSelectDialogController<SpellData> {

  private static final List<String> CATEGORIES =
      List.of("Kampf", "Wahrnehmung", "Heilung", "Illusion", "Manipulation");

  @FXML
  private Label costLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @Nullable Character character;
  private @Nullable SpellData result;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    okButton.setDisable(true);
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  public void setCharacter(@NonNull Character character) {
    this.character = character;
    costLabel.setText(character.isCreated() ? character.getSpellKarmaCost() + " Karma" : "3 BP");
    updateList();
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return CATEGORIES;
  }

  @Override
  protected @NonNull List<SpellData> allEntries() {
    return List.copyOf(SpellDataLoader.getAllSpells().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull SpellData spell, @NonNull String category) {
    return spell.category().equals(category);
  }

  @Override
  protected boolean additionalFilter(@NonNull SpellData spell) {
    return !alreadyKnownNames().contains(spell.name());
  }

  private @NonNull Set<String> alreadyKnownNames() {
    Character c = this.character;
    if (c == null) {
      return Set.of();
    }
    return c.getSpells().stream().map(SpellData::name).collect(Collectors.toSet());
  }

  @Override
  protected void onListUpdated() {
    okButton.setDisable(itemListView.getSelectionModel().getSelectedItem() == null);
  }

  @FXML
  private void onOk() {
    SpellData selected = itemListView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    result = selected;
    if (stage != null) {
      stage.close();
    }
  }

  @FXML
  private void onCancel() {
    if (stage != null) {
      stage.close();
    }
  }

  public @NonNull Optional<SpellData> getResult() {
    return Optional.ofNullable(result);
  }
}
