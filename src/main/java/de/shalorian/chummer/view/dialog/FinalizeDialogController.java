package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.lifestyle.LifestyleLevel;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Abschlussdialog der Charaktererschaffung: zeigt den Startkapital-Wurf nach Lebensstil (GRW S. 114).
 * Die App würfelt beim Öffnen; der Wurf ist editierbar (z.B. für echte Würfel am Tisch) und wird auf
 * den mit der Würfelzahl möglichen Bereich geprüft. Die Berechnung selbst liegt im Modell ({@link
 * LifestyleLevel#startingCapital(int, int)}), gebucht wird erst über {@link
 * Character#finalizeCreation(int)} durch den Aufrufer.
 */
public class FinalizeDialogController {

  @FXML
  private Label lifestyleLabel;
  @FXML
  private Label diceLabel;
  @FXML
  private TextField diceSumField;
  @FXML
  private Label remainingLabel;
  @FXML
  private Label bonusLabel;
  @FXML
  private Label resultLabel;
  @FXML
  private Button okButton;

  private @Nullable Stage stage;
  private @NonNull LifestyleLevel level = LifestyleLevel.STREET;
  private int remainingNuyen;
  private @Nullable Integer result;

  @FXML
  public void initialize() {
    diceSumField.textProperty().addListener((obs, old, val) -> updateResult());
  }

  public void setStage(@NonNull Stage stage) {
    this.stage = stage;
  }

  /** Übernimmt Lebensstil und Restgeld des Charakters und würfelt einen ersten Wurf. */
  public void setCharacter(@NonNull Character character) {
    this.level = character.getLifestyle().getLevel();
    this.remainingNuyen = character.getRemainingNuyen();
    lifestyleLabel.setText(level.getDisplayName());
    diceLabel.setText(level.getDice() + "W6 × " + level.getMultiplier() + " ¥"
        + "  (Summe " + level.getMinDiceSum() + "–" + level.getMaxDiceSum() + ")");
    remainingLabel.setText(remainingNuyen + " ¥ (verfällt)");
    bonusLabel.setText("+" + level.restBonus(remainingNuyen)
        + "  (+1 je 100 ¥ Restgeld, max. " + (level.getDice() * 3) + ")");
    onRoll();
  }

  @FXML
  private void onRoll() {
    diceSumField.setText(String.valueOf(level.rollDiceSum(ThreadLocalRandom.current())));
  }

  /** Die eingegebene Würfelsumme, oder {@code null}, wenn sie keine mit der Würfelzahl mögliche Zahl ist. */
  private @Nullable Integer parseDiceSum() {
    String text = diceSumField.getText().trim();
    if (!text.matches("[0-9]{1,3}")) {
      return null;
    }
    int sum = Integer.parseInt(text);
    return sum < level.getMinDiceSum() || sum > level.getMaxDiceSum() ? null : sum;
  }

  private void updateResult() {
    Integer sum = parseDiceSum();
    if (sum == null) {
      resultLabel.setText("Ungültiger Wurf");
      okButton.setDisable(true);
      return;
    }
    resultLabel.setText(level.startingCapital(sum, remainingNuyen) + " ¥");
    okButton.setDisable(false);
  }

  @FXML
  private void onOk() {
    Integer sum = parseDiceSum();
    if (sum == null) {
      return;
    }
    result = sum;
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

  /** Die bestätigte Würfelsumme; leer bei Abbruch. */
  public @NonNull Optional<Integer> getResult() {
    return Optional.ofNullable(result);
  }
}
