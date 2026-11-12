package de.shalorian.chummer.view.tab;

import de.shalorian.chummer.model.character.Character;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Hinweise der Status-Übersicht: was am Charakter gerade nicht stimmt. Bei der Erschaffung überzogene BP oder
 * überzogenes Ressourcengeld und Essenz auf 0 oder darunter (GRW S. 111), in beiden Modi überzogene Kraftpunkte.
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
        hints.add("Ressourcengeld um " + -nuyen + " ¥ überzogen");
      }
      if (character.isEssenceBelowCreationMinimum()) {
        hints.add("Essenz darf bei der Erschaffung nicht auf 0 oder darunter fallen");
      }
    }
    String adeptProblem = character.getAdeptPowers().getProblem();
    if (adeptProblem != null) {
      hints.add(adeptProblem);
    }
    return hints;
  }
}
