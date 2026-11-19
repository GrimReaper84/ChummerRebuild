package de.shalorian.chummer.view.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Erkennung ungespeicherter Änderungen (ADR-066). */
class DocumentStateTest {

  private static final byte[] A = "a".getBytes(StandardCharsets.UTF_8);
  private static final byte[] B = "b".getBytes(StandardCharsets.UTF_8);
  private static final File FILE = new File("kira.json");

  @Test
  void neverSavedIsDirty() {
    DocumentState state = DocumentState.unsaved(false);
    assertTrue(state.isDirty(A));
    assertNull(state.getFile());
  }

  @Test
  void loadedIsCleanUntilChanged() {
    DocumentState state = DocumentState.loaded(FILE, A);
    assertFalse(state.isDirty(A.clone()));
    assertTrue(state.isDirty(B));
  }

  @Test
  void saveUpdatesFileAndClearsBeta() {
    DocumentState state = DocumentState.unsaved(true);
    assertTrue(state.isBetaCopy());
    state.markSaved(FILE, B);
    assertEquals(FILE, state.getFile());
    assertFalse(state.isBetaCopy());
    assertFalse(state.isDirty(B));
    assertTrue(state.isDirty(A));
  }
}
