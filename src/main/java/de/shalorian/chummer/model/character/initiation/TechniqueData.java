package de.shalorian.chummer.model.character.initiation;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/**
 * Katalogeintrag einer Metamagie (GRW S. 232/233) oder eines Echos (S. 284) aus {@code metamagic.xml}.
 *
 * @param noAdept   reine Adepten können sie nicht lernen (Adepten nur vier Techniken, S. 232)
 * @param adeptOnly nur für Adepten und Magieradepten (Adeptenzentrierung)
 * @param maxTimes  wie oft sie gelernt werden darf (Echos bis zu dreimal, S. 284)
 * @param effect    Hinweis zur Wirkung mit den Platzhaltern {@value #GRADE} und {@value #COUNT}
 */
public record TechniqueData(@NonNull String name, @NonNull TechniqueKind kind, boolean noAdept, boolean adeptOnly,
                            int maxTimes, @NonNull String effect, @NonNull String source, @NonNull String page)
    implements CatalogEntry, Sourced {

  /** Platzhalter für den Initiations- bzw. Wandlungsgrad. */
  public static final String GRADE = "{grade}";
  /** Platzhalter dafür, wie oft die Technik gelernt wurde. */
  public static final String COUNT = "{count}";

  /** Wirkung mit eingesetztem Grad und Anzahl, z.B. „Entzugswiderstand +2 Würfel …“. */
  public @NonNull String effectText(int grade, int count) {
    return effect.replace(GRADE, String.valueOf(grade)).replace(COUNT, String.valueOf(count));
  }
}
