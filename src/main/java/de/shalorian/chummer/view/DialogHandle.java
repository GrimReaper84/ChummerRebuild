package de.shalorian.chummer.view;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

record DialogHandle(@NonNull FXMLLoader loader, @NonNull Stage stage) {

}
