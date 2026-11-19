package de.shalorian.chummer.view.document;

import java.io.File;
import java.util.Arrays;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Speicherzustand eines offenen Dokuments (ADR-066): Datei, ob es eine Kopie aus dem Beta-Menü ist, und der zuletzt
 * geladene bzw. gespeicherte Stand im Speicherformat. Ungespeichert ist ein Dokument, wenn sein aktueller Stand davon
 * abweicht; ein nie gespeichertes Dokument (Neu, Vorlage, Beta-Kopie) gilt immer als ungespeichert (Nutzerentscheidung
 * 2026-11-19). Der Vergleich ersetzt ein von Hand gesetztes Änderungs-Flag wie im alten Chummer.
 */
public final class DocumentState {

  private @Nullable File file;
  private boolean betaCopy;
  private byte @Nullable [] saved;

  private DocumentState(@Nullable File file, boolean betaCopy, byte @Nullable [] saved) {
    this.file = file;
    this.betaCopy = betaCopy;
    this.saved = saved;
  }

  /** Ein nie gespeichertes Dokument; {@code betaCopy} für Kopien aus dem Beta-Menü. */
  public static @NonNull DocumentState unsaved(boolean betaCopy) {
    return new DocumentState(null, betaCopy, null);
  }

  /** Ein aus {@code file} geladenes Dokument mit dem Stand {@code current} direkt nach dem Laden. */
  public static @NonNull DocumentState loaded(@NonNull File file, byte @NonNull [] current) {
    return new DocumentState(file, false, current.clone());
  }

  /** Nach dem Speichern nach {@code file}: neue Datei, keine Beta-Kopie mehr, {@code current} ist der Speicherstand. */
  public void markSaved(@NonNull File file, byte @NonNull [] current) {
    this.file = file;
    this.betaCopy = false;
    this.saved = current.clone();
  }

  /** Ob {@code current} vom Speicherstand abweicht bzw. das Dokument nie gespeichert wurde. */
  public boolean isDirty(byte @NonNull [] current) {
    return saved == null || !Arrays.equals(saved, current);
  }

  public @Nullable File getFile() {
    return file;
  }

  public boolean isBetaCopy() {
    return betaCopy;
  }
}
