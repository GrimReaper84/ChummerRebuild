package de.shalorian.chummer.model.template;

import de.shalorian.chummer.model.character.rulebook.Sourced;
import org.jspecify.annotations.NonNull;

/**
 * Eine Vorlage aus einem Regelwerk, aus der der Dialog „Neu“ ein Dokument anlegt: ein Archetyp (Charakter in der
 * Erschaffung) oder eine Schergengruppe. Nicht {@code sealed}, weil die Umsetzungen in verschiedenen Paketen liegen.
 */
public interface DocumentTemplate extends Sourced {

  @NonNull String name();

  /** Ein Satz in eigenen Worten. */
  @NonNull String description();

  /** Gedruckte Seite im Regelwerk {@link #source()}. */
  int page();

  @NonNull TemplateKind kind();
}
