package de.shalorian.chummer.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

/**
 * Die vier UI-Elemente einer +/−-Zeile, wie sie {@link CharacterViewController#wireIncrementable} verdrahtet.
 */
record RowControls(
    @NonNull Label valueLabel, @NonNull Label costLabel, @NonNull Button minus,
    @NonNull Button plus) {

}
