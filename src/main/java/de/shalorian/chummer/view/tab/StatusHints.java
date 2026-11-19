package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.spirit.SpiritKind;
import de.shalorian.chummer.utils.Numbers;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;

/**
 * Hinweise der Status-Übersicht: was am Charakter gerade nicht stimmt. Bei der Erschaffung überzogene BP oder
 * überzogenes Ressourcengeld und Essenz auf 0 oder darunter (GRW S. 111), in beiden Modi überzogene Kraftpunkte und ein
 * Initiations- bzw. Wandlungsgrad über dem wirksamen Magie- bzw. Resonanzwert sowie Geister und Sprites, die nicht mehr
 * den Regeln entsprechen ({@code Spirits#getProblems}), und Foki über den Grenzen von Magie bzw. Logik
 * ({@code Foci#getProblems}), dazu Einträge aus abgeschalteten Regelwerken (ADR-061).
 */
public final class StatusHints {

  private StatusHints() {
  }

  public static @NonNull List<String> of(@NonNull Character character) {
    List<String> hints = new ArrayList<>();
    if (!character.isCreated()) {
      int bp = character.getRemainingBP();
      if (bp < 0) {
        hints.add("BP um " + -bp + " überzogen");
      }
      int nuyen = character.getRemainingNuyen();
      if (nuyen < 0) {
        hints.add("Ressourcengeld um " + Numbers.nuyen(-nuyen) + " überzogen");
      }
      if (character.isEssenceBelowCreationMinimum()) {
        hints.add("Essenz bei der Erschaffung auf 0 oder darunter");
      }
    }
    String adeptProblem = character.getAdeptPowers().getProblem();
    if (adeptProblem != null) {
      hints.add(adeptProblem);
    }
    hints.addAll(character.getFoci().getProblems());
    for (SpiritKind kind : SpiritKind.values()) {
      hints.addAll(character.getSpirits().getProblems(kind));
    }
    String initiationHint = character.getInitiation().getAboveAttributeHint();
    if (initiationHint != null) {
      hints.add(initiationHint);
    }
    Set<String> disabledBooks = character.getSourcesFromDisabledBooks();
    if (!disabledBooks.isEmpty()) {
      hints.add("Enthält Inhalte aus abgeschalteten Regelwerken: " + String.join(", ", disabledBooks));
    }
    return hints;
  }
}
