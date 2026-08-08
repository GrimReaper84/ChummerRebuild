package de.shalorian.chummer.persistence;

import org.jspecify.annotations.NonNull;

/**
 * Flaches Persistenz-DTO, 1:1-Spiegel von {@link de.shalorian.chummer.model.character.CharacterBiography}.
 */
class BiographyData {

  public @NonNull String alias = "";
  public @NonNull String sex = "";
  public @NonNull String age = "";
  public @NonNull String eyes = "";
  public @NonNull String height = "";
  public @NonNull String weight = "";
  public @NonNull String skin = "";
  public @NonNull String hair = "";
  public @NonNull String description = "";
  public @NonNull String background = "";
  public @NonNull String concept = "";
  public @NonNull String notes = "";
  public @NonNull String playerName = "";
  public @NonNull String gameNotes = "";

  public BiographyData() {
  }
}
