package de.shalorian.chummer.view.dialog;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.magic.MentorData;
import de.shalorian.chummer.model.character.magic.MentorDataLoader;
import de.shalorian.chummer.model.character.quality.QualityData;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.quality.QualitySelection;
import de.shalorian.chummer.model.character.quality.QualityType;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Auswahl einer Gabe oder eines Handicaps. Nicht wählbare Einträge bleiben deaktiviert sichtbar; der Detailbereich
 * zeigt Seite, Kurzwirkung und gegebenenfalls den Grund aus {@link Character#getQualityBlockReason}. Verlangt die
 * Quality eine Auswahl (Fertigkeit, Attribut oder Text), ist OK erst mit gültiger Auswahl aktiv.
 */
public class QualitySelectDialogController
    extends ChoiceCatalogSelectDialogController<QualityData, QualitySelectResult> {

  private static final String CATEGORY_POSITIVE = "Gaben";
  private static final String CATEGORY_NEGATIVE = "Handicaps";

  @FXML
  private CheckBox gratisBox;

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    gratisBox.selectedProperty().addListener((_, _, _) -> updateList());
  }

  /**
   * Setzt den vorausgewählten Kategoriefilter beim Öffnen des Dialogs aus einer Sektion heraus.
   */
  public void setFilter(@NonNull QualityType filter) {
    categoryBox.setValue(filter == QualityType.POSITIVE ? CATEGORY_POSITIVE : CATEGORY_NEGATIVE);
  }

  @Override
  protected @NonNull List<String> categoryOptions() {
    return List.of(CATEGORY_POSITIVE, CATEGORY_NEGATIVE);
  }

  /** Alle wählbaren Katalogeinträge; Metatyp-Fähigkeiten kommen nur über den Metatyp und erscheinen nicht. */
  @Override
  protected @NonNull List<QualityData> allEntries() {
    return QualityDataLoader.getAllQualities().values().stream()
        .filter(q -> !q.rules().metatypeOnly())
        .toList();
  }

  @Override
  protected boolean matchesCategory(@NonNull QualityData q, @NonNull String category) {
    return CATEGORY_POSITIVE.equals(category)
        ? q.type() == QualityType.POSITIVE
        : q.type() == QualityType.NEGATIVE;
  }

  @Override
  protected boolean isSelectable(@NonNull QualityData q) {
    return character == null
        || (character.getQualityBlockReason(q, gratisBox.isSelected()) == null && bpProblem(character, q) == null);
  }

  /** Gaben kosten bei der Erschaffung BP; reichen sie nicht, ist die Gabe gesperrt wie ein +-Knopf (Gratis nie). */
  private @Nullable String bpProblem(@NonNull Character ch, @NonNull QualityData q) {
    return gratisBox.isSelected() || q.type() != QualityType.POSITIVE ? null : ch.getBpShortfallProblem(q.bp());
  }

  @Override
  protected @NonNull QualitySelection selection(@NonNull QualityData q) {
    return q.selection();
  }

  @Override
  protected @NonNull String selectLabel(@NonNull QualityData q) {
    return q.selectLabel();
  }

  @Override
  protected @NonNull List<AttributeType> attributeOptions() {
    return List.of(AttributeType.values());
  }

  @Override
  protected @Nullable String problem(@NonNull Character ch, @NonNull QualityData q, @NonNull String choice) {
    String problem = ch.getQualityAddProblem(q, gratisBox.isSelected(), choice);
    return problem != null ? problem : bpProblem(ch, q);
  }

  @Override
  protected @NonNull String costText(@NonNull Character ch, @NonNull QualityData q, @NonNull String choice) {
    return gratisBox.isSelected() ? "Gratis" : Math.abs(q.bp()) + " BP";
  }

  @Override
  protected @NonNull String details(@NonNull QualityData q, @NonNull String choice) {
    String text = (q.page() > 0 ? "GRW S. %d. ".formatted(q.page()) : "") + q.note();
    MentorData mentor = q.selection() == QualitySelection.MENTOR ? MentorDataLoader.findByChoice(choice) : null;
    return mentor == null ? text : text + "\n" + mentor.name() + " (S. " + mentor.page() + "): "
        + mentor.describe(choice);
  }

  @Override
  protected @NonNull QualitySelectResult createResult(@NonNull QualityData q, @NonNull String choice,
      boolean addAnother) {
    return new QualitySelectResult(q, gratisBox.isSelected(), addAnother, choice);
  }
}
