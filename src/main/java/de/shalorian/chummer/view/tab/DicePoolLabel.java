package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.skill.DicePool;
import java.util.Optional;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Gemeinsame Anzeige eines Würfelpools in den Fertigkeitszeilen: "8", mit Spezialisierung "8 (10)" (siehe {@link
 * Character#withSpecialization(DicePool)}), "–" ohne mögliche Probe; Proben ohne Fertigkeitswert gedämpft. Tooltip
 * mit der Aufschlüsselung.
 */
final class DicePoolLabel {

  static final String STYLE_POOL = "pool-value";
  static final String STYLE_POOL_DEFAULT = "pool-default";

  private DicePoolLabel() {
  }

  /** Neues Label mit der Grund-Styleklasse. */
  static @NonNull Label create() {
    Label label = new Label();
    label.getStyleClass().add(STYLE_POOL);
    return label;
  }

  static void show(@NonNull Label label, @NonNull Optional<DicePool> pool, int rating,
      @Nullable String specialization) {
    label.getStyleClass().remove(STYLE_POOL_DEFAULT);
    if (pool.isEmpty()) {
      label.setText("–");
      label.setTooltip(new Tooltip("Keine Probe möglich"));
      return;
    }
    int dice = pool.get().dice();
    int special = Character.withSpecialization(pool.get());
    label.setText(specialization == null ? String.valueOf(dice) : "%d (%d)".formatted(dice, special));
    String breakdown = pool.get().breakdown();
    label.setTooltip(new Tooltip(specialization == null ? breakdown
        : "%s\n%d im Spezialgebiet %s".formatted(breakdown, special, specialization)));
    if (rating == 0) {
      label.getStyleClass().add(STYLE_POOL_DEFAULT);
    }
  }
}
