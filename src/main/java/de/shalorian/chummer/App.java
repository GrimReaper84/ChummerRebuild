package de.shalorian.chummer;

import de.shalorian.chummer.view.MainWindowController;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

public class App extends Application {

  /**
   * Größen der Fenster-Icons (Ressourcen {@code icon-<größe>.png}) und des Windows-Installer-Icons, gezeichnet vom
   * Test-Task {@code generateIcons}.
   */
  public static final @NonNull List<Integer> ICON_SIZES = List.of(16, 24, 32, 48, 64, 128, 256);

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
    Scene scene = new Scene(loader.load(), 1100, 750);
    scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());

    MainWindowController controller = loader.getController();
    controller.setStage(stage);

    stage.setTitle(AppInfo.title());
    addIcons(stage);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }

  /** Das Drachen-Icon in allen Größen, damit das Fenstersystem die passende wählt; eigene Dialoge erben es. */
  private static void addIcons(@NonNull Stage stage) throws IOException {
    for (int size : ICON_SIZES) {
      try (InputStream in = App.class.getResourceAsStream("icon-" + size + ".png")) {
        if (in == null) {
          throw new IOException("Fenster-Icon icon-" + size + ".png fehlt");
        }
        stage.getIcons().add(new Image(in));
      }
    }
  }
}
