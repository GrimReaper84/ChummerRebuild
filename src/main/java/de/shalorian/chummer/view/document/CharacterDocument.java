package de.shalorian.chummer.view.document;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.persistence.CharacterStore;
import de.shalorian.chummer.view.CharacterViewController;
import de.shalorian.chummer.view.dialog.SettingsResult;
import de.shalorian.chummer.view.shell.FloatingButtonPosition;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Ein offener Charakter (ADR-066). Ersetzt „Rückgängig“ den Charakter durch einen früheren Stand, übernimmt das
 * Dokument das neue Objekt, damit Speichern und Vergleich immer den angezeigten Stand nutzen.
 */
public final class CharacterDocument extends OpenDocument {

  private final @NonNull CharacterViewController view;
  private @NonNull Character character;

  private CharacterDocument(@NonNull DocumentState state, @NonNull CharacterViewController view,
      @NonNull Character character) {
    super(state, FontAwesomeSolid.USER);
    this.view = view;
    this.character = character;
  }

  /** Baut die Ansicht für {@code character} mit den Anzeige-Einstellungen {@code display}. */
  public static @NonNull CharacterDocument create(@NonNull Character character, @NonNull DocumentState state,
      @NonNull SettingsResult display, @NonNull FloatingButtonPosition statusButtonPosition) throws IOException {
    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("CharacterView.fxml")));
    loader.load();
    CharacterViewController view = loader.getController();
    view.configureStatus(display.statusStatic(), statusButtonPosition);
    view.setCheatSheetItems(display.cheatSheetItems());
    view.setCharacter(character);
    CharacterDocument document = new CharacterDocument(state, view, character);
    view.setOnCharacterReplaced(restored -> {
      document.character = restored;
      document.refreshLabel();
    });
    document.attach(view.node());
    return document;
  }

  public @NonNull Character character() {
    return character;
  }

  /** Übernimmt geänderte Anzeige-Einstellungen; der Neuaufbau folgt über {@link #rebuild()}. */
  public void applyDisplay(@NonNull SettingsResult display) {
    view.setCheatSheetItems(display.cheatSheetItems());
    if (view.isStatusStatic() != display.statusStatic()) {
      view.setStatusStatic(display.statusStatic());
    }
  }

  /** Spielleiter-Modus des Charakters (ADR-060, nicht gespeichert). */
  public void setGmMode(boolean on) {
    view.setGmMode(on);
  }

  @Override
  public @NonNull String name() {
    return character.getName();
  }

  @Override
  protected byte @NonNull [] serialize() {
    return CharacterStore.serialize(character);
  }

  @Override
  protected void write(@NonNull File file) throws IOException {
    CharacterStore.save(character, file);
  }

  @Override
  public void rebuild() {
    view.rebuild();
  }
}
