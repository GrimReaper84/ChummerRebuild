package de.shalorian.chummer.view.dialog;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

public record DialogHandle(@NonNull FXMLLoader loader, @NonNull Stage stage) {

}
