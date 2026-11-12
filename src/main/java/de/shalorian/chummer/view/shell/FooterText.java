package de.shalorian.chummer.view.shell;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.view.converter.EssenceStringConverter;
import org.jspecify.annotations.NonNull;

/**
 * Text der Fußzeile des Charakterbogens: bei der Erschaffung {@code BP: 12 | Nuyen: 4350 ¥ | Essenz: 3,60}
 * (verbleibende BP und verbleibendes Ressourcengeld), im Karrieremodus {@code Karma: 7 | Nuyen: 2350 ¥ | Essenz: 3,60}
 * (Stand von Karma- und Nuyen-Protokoll). Zahlen ohne Tausenderpunkt wie im Rest der Anwendung.
 */
public final class FooterText {

  private static final EssenceStringConverter ESSENCE_FORMAT = new EssenceStringConverter();
  private static final String SEPARATOR = "  |  ";

  private FooterText() {
  }

  public static @NonNull String of(@NonNull Character character) {
    String first = character.isCreated()
        ? "Karma: " + character.getKarmaTotal()
        : "BP: " + character.getRemainingBP();
    int nuyen = character.isCreated() ? character.getNuyenTotal() : character.getRemainingNuyen();
    return first + SEPARATOR + "Nuyen: " + nuyen + " ¥" + SEPARATOR + "Essenz: "
        + ESSENCE_FORMAT.toString(character.getEssence());
  }
}
