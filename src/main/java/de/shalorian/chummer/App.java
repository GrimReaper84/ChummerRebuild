package de.shalorian.chummer;

import de.shalorian.chummer.view.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 750);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());

        MainWindowController controller = loader.getController();
        controller.setStage(stage);

        stage.setTitle("Chummer – SR4 Charakter Manager");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}