package de.shalorian.chummer.model.template;

import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.npc.NpcTemplateLoader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.NonNull;

/** Alle Vorlagen für den Dialog „Neu“ an einer Stelle (Archetypen, Schergengruppen). */
public final class Templates {

  private Templates() {
  }

  /** Vorlagen der aktiven Regelwerke, nach Art und dann alphabetisch nach Name sortiert. */
  public static @NonNull List<DocumentTemplate> available(@NonNull RuleOptions options) {
    List<DocumentTemplate> all = new ArrayList<>();
    all.addAll(ArchetypeLoader.getAll());
    all.addAll(NpcTemplateLoader.getAll());
    Collator collator = Collator.getInstance(Locale.GERMAN);
    return all.stream().filter(options::allows)
        .sorted(Comparator.comparing(DocumentTemplate::kind).thenComparing(DocumentTemplate::name, collator))
        .toList();
  }
}
