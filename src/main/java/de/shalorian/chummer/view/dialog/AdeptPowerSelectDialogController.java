package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.adept.AdeptPowerData;
import de.shalorian.chummer.model.character.adept.AdeptPowerDataLoader;
import de.shalorian.chummer.model.character.adept.AdeptPowers;
import de.shalorian.chummer.model.character.adept.AdeptPowerEffect;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl einer Adeptenkraft (GRW S. 229–232). Nicht kaufbare Kräfte bleiben deaktiviert sichtbar; der Detailbereich
 * zeigt Seite, Kurzwirkung und gegebenenfalls den Grund aus {@link de.shalorian.chummer.model.character.adept.AdeptPowers#getAddProblem}. Attribute
 * stehen nur die körperlichen zur Wahl.
 */
public class AdeptPowerSelectDialogController
    extends ChoiceCatalogSelectDialogController<AdeptPowerData, AdeptPowerSelectResult> {

  private static final String CATEGORY_CALCULATED = "Mit Rechenwirkung";
  private static final String CATEGORY_NOTE = "Nur Hinweis";

  @Override
  protected @NonNull List<String> categoryOptions() {
    return List.of(CATEGORY_CALCULATED, CATEGORY_NOTE);
  }

  @Override
  protected @NonNull List<AdeptPowerData> allEntries() {
    return List.copyOf(AdeptPowerDataLoader.getAllPowers().values());
  }

  @Override
  protected boolean matchesCategory(@NonNull AdeptPowerData power, @NonNull String category) {
    return CATEGORY_CALCULATED.equals(category) == (power.effect() != AdeptPowerEffect.NONE);
  }

  @Override
  protected boolean isSelectable(@NonNull AdeptPowerData power) {
    return character == null || character.getAdeptPowers().getBlockReason(power) == null;
  }

  @Override
  protected @NonNull QualitySelection selection(@NonNull AdeptPowerData power) {
    return power.selection();
  }

  @Override
  protected @NonNull String selectLabel(@NonNull AdeptPowerData power) {
    return power.selectLabel();
  }

  @Override
  protected @NonNull List<AttributeType> attributeOptions() {
    return Arrays.stream(AttributeType.values()).filter(AttributeType::isPhysical).toList();
  }

  @Override
  protected @Nullable String problem(@NonNull Character ch, @NonNull AdeptPowerData power, @NonNull String choice) {
    return ch.getAdeptPowers().getAddProblem(power, choice);
  }

  @Override
  protected @NonNull String costText(@NonNull Character ch, @NonNull AdeptPowerData power, @NonNull String choice) {
    String cost = AdeptPowers.formatPoints(ch.getAdeptPowers().getCost(power, choice, 1));
    return power.leveled() ? cost + " (Stufe 1)" : cost;
  }

  @Override
  protected @NonNull String details(@NonNull AdeptPowerData power) {
    return "GRW S. %d. %s".formatted(power.page(), power.note());
  }

  @Override
  protected @NonNull AdeptPowerSelectResult createResult(@NonNull AdeptPowerData power, @NonNull String choice,
      boolean addAnother) {
    return new AdeptPowerSelectResult(power, choice, addAnother);
  }
}
