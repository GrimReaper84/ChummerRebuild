package de.shalorian.chummer.view.tab;

import static de.shalorian.chummer.view.grid.GridSupport.iconTextButton;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_DETAIL_HINT;
import static de.shalorian.chummer.view.grid.StyleClasses.STYLE_SECTION_TITLE;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterBiography;
import de.shalorian.chummer.model.character.PortraitImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/**
 * Biografie-Tab: Aussehen, Hintergrund, Spieler-Metadaten und (im Karrieremodus) Spielnotizen als
 * Freitextfelder, live an {@link de.shalorian.chummer.model.character.CharacterBiography} gebunden.
 */
public final class BiographyTabBuilder {

  /** Kantenlänge der Portrait-Vorschau in Pixeln. */
  private static final int PORTRAIT_PREVIEW = 200;

  private final @NonNull VBox biographyContainer;
  private final @NonNull CharacterTabHost host;
  private @Nullable Character character;

  public BiographyTabBuilder(@NonNull VBox biographyContainer, @NonNull CharacterTabHost host) {
    this.biographyContainer = biographyContainer;
    this.host = host;
  }

  /** Charakter, den dieser Tab anzeigt; vor dem ersten Aufbau zu setzen. */
  public void setCharacter(@NonNull Character character) {
    this.character = character;
  }

  public void build() {
    Character ch = this.character;
    if (ch == null) {
      return;
    }
    biographyContainer.getChildren().clear();
    CharacterBiography bio = ch.getBiography();

    biographyContainer.getChildren().add(biographySectionTitle("Portrait"));
    biographyContainer.getChildren().add(buildPortrait(bio));
    biographyContainer.getChildren().add(biographySectionTitle("Aussehen"));
    biographyContainer.getChildren().add(buildAppearanceGrid(bio));

    biographyContainer.getChildren().add(biographySectionTitle("Hintergrund"));
    biographyContainer.getChildren().add(buildBackgroundGrid(bio));

    biographyContainer.getChildren().add(biographySectionTitle("Spieler-Metadaten"));
    biographyContainer.getChildren().add(buildPlayerGrid(bio, ch));

    if (ch.isCreated()) {
      biographyContainer.getChildren().add(biographySectionTitle("Spielnotizen"));
      biographyContainer.getChildren().add(buildGameNotesGrid(bio));
    }
  }

  /**
   * Portrait mit „Bild wählen …“ und „Entfernen“ (ADR-059): das Bild wird beim Einlesen auf {@value
   * PortraitImage#MAX_SIDE} Pixel verkleinert und als PNG im Charakter gespeichert.
   */
  private @NonNull HBox buildPortrait(@NonNull CharacterBiography bio) {
    ImageView view = new ImageView();
    view.setFitWidth(PORTRAIT_PREVIEW);
    view.setFitHeight(PORTRAIT_PREVIEW);
    view.setPreserveRatio(true);
    view.setSmooth(true);
    Label empty = new Label("Kein Portrait");
    empty.getStyleClass().add(STYLE_DETAIL_HINT);
    Button choose = iconTextButton("Bild wählen", FontAwesomeSolid.IMAGE, "Portrait aus einer Bilddatei laden");
    Button remove = iconTextButton("Entfernen", FontAwesomeSolid.TRASH, "Portrait entfernen");
    Runnable show = () -> {
      byte[] png = bio.getPortrait();
      view.setImage(png == null ? null : new Image(new ByteArrayInputStream(png)));
      view.setVisible(png != null);
      view.setManaged(png != null);
      empty.setVisible(png == null);
      empty.setManaged(png == null);
      remove.setDisable(png == null);
    };
    choose.setOnAction(_ -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Portrait wählen");
      chooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
      File file = chooser.showOpenDialog(biographyContainer.getScene().getWindow());
      if (file == null) {
        return;
      }
      try {
        bio.setPortrait(PortraitImage.toStoredPng(Files.readAllBytes(file.toPath())));
        show.run();
      } catch (IOException e) {
        host.services().warn("Portrait", "Das Bild ließ sich nicht lesen: " + e.getMessage() + ".");
      }
    });
    remove.setOnAction(_ -> {
      bio.setPortrait(null);
      show.run();
    });
    show.run();
    VBox buttons = new VBox(8, choose, remove);
    HBox box = new HBox(16, view, empty, buttons);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private @NonNull Label biographySectionTitle(@NonNull String text) {
    Label label = new Label(text);
    label.getStyleClass().add(STYLE_SECTION_TITLE);
    VBox.setMargin(label, new Insets(16, 0, 4, 0));
    return label;
  }

  private @NonNull GridPane newBiographyGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(14);
    grid.setVgap(10);
    ColumnConstraints labelCol = new ColumnConstraints();
    labelCol.setMinWidth(120);
    labelCol.setPrefWidth(140);
    ColumnConstraints fieldCol = new ColumnConstraints();
    fieldCol.setMinWidth(220);
    fieldCol.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(labelCol, fieldCol);
    return grid;
  }

  private @NonNull GridPane buildAppearanceGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    int row = 0;
    row = addTextFieldRow(grid, row, "Alias:", bio::getAlias, bio::setAlias);
    row = addTextFieldRow(grid, row, "Geschlecht:", bio::getSex, bio::setSex);
    row = addTextFieldRow(grid, row, "Alter:", bio::getAge, bio::setAge);
    row = addTextFieldRow(grid, row, "Augen:", bio::getEyes, bio::setEyes);
    row = addTextFieldRow(grid, row, "Haare:", bio::getHair, bio::setHair);
    row = addTextFieldRow(grid, row, "Größe:", bio::getHeight, bio::setHeight);
    row = addTextFieldRow(grid, row, "Gewicht:", bio::getWeight, bio::setWeight);
    addTextFieldRow(grid, row, "Haut:", bio::getSkin, bio::setSkin);
    return grid;
  }

  private @NonNull GridPane buildBackgroundGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    int row = 0;
    row = addTextAreaRow(grid, row, "Beschreibung:", bio::getDescription, bio::setDescription);
    row = addTextAreaRow(grid, row, "Hintergrund:", bio::getBackground, bio::setBackground);
    row = addTextAreaRow(grid, row, "Konzept:", bio::getConcept, bio::setConcept);
    addTextAreaRow(grid, row, "Notizen:", bio::getNotes, bio::setNotes);
    return grid;
  }

  /**
   * "Name:" bindet direkt an {@link Character#getName()}/{@link Character#setName(String)} (nicht an {@link CharacterBiography}) — im alten
   * WinForms-UI stand dieses Feld (bürgerlicher Name, getrennt vom Straßennamen/Alias) in derselben Zeile wie der Spielername. Der Setter
   * meldet zusätzlich {@link CharacterTabHost#onNameChanged()}, da die Namensanzeige im Kopf sonst nur beim initialen
   * Setzen des Charakters gefüllt wird und nach einer Live-Änderung hier veraltet bliebe.
   */
  private @NonNull GridPane buildPlayerGrid(@NonNull CharacterBiography bio,
      @NonNull Character character) {
    GridPane grid = newBiographyGrid();
    int row = addTextFieldRow(grid, 0, "Name:", character::getName,
        val -> {
          character.setName(val);
          host.onNameChanged();
        });
    addTextFieldRow(grid, row, "Spielername:", bio::getPlayerName, bio::setPlayerName);
    return grid;
  }

  private @NonNull GridPane buildGameNotesGrid(@NonNull CharacterBiography bio) {
    GridPane grid = newBiographyGrid();
    addTextAreaRow(grid, 0, "Spielnotizen:", bio::getGameNotes, bio::setGameNotes);
    return grid;
  }

  private int addTextFieldRow(@NonNull GridPane grid, int row, @NonNull String labelText,
      @NonNull Supplier<String> getter, @NonNull Consumer<String> setter) {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");
    TextField field = new TextField();
    bindTextControl(field, getter, setter);
    grid.add(label, 0, row);
    grid.add(field, 1, row);
    return row + 1;
  }

  private int addTextAreaRow(@NonNull GridPane grid, int row, @NonNull String labelText,
      @NonNull Supplier<String> getter, @NonNull Consumer<String> setter) {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");
    GridPane.setValignment(label, VPos.TOP);
    TextArea area = new TextArea();
    area.setPrefRowCount(3);
    area.setWrapText(true);
    bindTextControl(area, getter, setter);
    grid.add(label, 0, row);
    grid.add(area, 1, row);
    return row + 1;
  }

  /**
   * Bindet ein einzeiliges Textfeld live ans Modell: der Wert wird beim Aufbau vorbefüllt, jede Änderung schreibt sofort über {@code setter} ins
   * Modell — kein Trimmen, keine Validierung, kein Bestätigungsdialog (anders als die bestehenden Dialog-Formulare, die nur beim Bestätigen einmalig
   * auslesen).
   */
  private void bindTextControl(@NonNull TextField field, @NonNull Supplier<String> getter,
      @NonNull Consumer<String> setter) {
    field.setText(getter.get());
    field.textProperty().addListener((_, _, val) -> setter.accept(val));
  }

  /**
   * Mehrzeiliges Gegenstück zu {@link #bindTextControl(TextField, Supplier, Consumer)}.
   */
  private void bindTextControl(@NonNull TextArea area, @NonNull Supplier<String> getter,
      @NonNull Consumer<String> setter) {
    area.setText(getter.get());
    area.textProperty().addListener((_, _, val) -> setter.accept(val));
  }
}
