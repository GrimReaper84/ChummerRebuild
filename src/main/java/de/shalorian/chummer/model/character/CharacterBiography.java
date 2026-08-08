package de.shalorian.chummer.model.character;

import org.jspecify.annotations.NonNull;

/**
 * Rein deskriptive Rollenspiel-Metadaten eines Charakters ohne jede BP-/Karma-/Fachregel-Bedeutung
 * (SR4-Grundregelwerk kennt für diese Felder keine Mechanik, siehe {@code
 * docs/charakter_biografie_analyse.md}). Reiner Datencontainer: 15 Freitextfelder, alle defaulten
 * auf {@code ""}, nie {@code null}, ohne Validierung oder Trimmen. Mugshot/Portrait ist bewusst
 * nicht Teil dieser Klasse (separater Feature-Checklisten-Punkt).
 */
public class CharacterBiography {

  private @NonNull String alias = "";
  private @NonNull String sex = "";
  private @NonNull String age = "";
  private @NonNull String eyes = "";
  private @NonNull String height = "";
  private @NonNull String weight = "";
  private @NonNull String skin = "";
  private @NonNull String hair = "";
  private @NonNull String description = "";
  private @NonNull String background = "";
  private @NonNull String concept = "";
  private @NonNull String notes = "";
  private @NonNull String playerName = "";
  private @NonNull String gameNotes = "";

  public @NonNull String getAlias() {
    return alias;
  }

  public void setAlias(@NonNull String alias) {
    this.alias = alias;
  }

  public @NonNull String getSex() {
    return sex;
  }

  public void setSex(@NonNull String sex) {
    this.sex = sex;
  }

  /** Freitext-Alter, bewusst kein numerischer Typ — SR4-Original bildet auch dies als Freitext ab. */
  public @NonNull String getAge() {
    return age;
  }

  public void setAge(@NonNull String age) {
    this.age = age;
  }

  public @NonNull String getEyes() {
    return eyes;
  }

  public void setEyes(@NonNull String eyes) {
    this.eyes = eyes;
  }

  /** Freitext-Größe, bewusst kein numerischer Typ (analog {@link #getAge()}). */
  public @NonNull String getHeight() {
    return height;
  }

  public void setHeight(@NonNull String height) {
    this.height = height;
  }

  /** Freitext-Gewicht, bewusst kein numerischer Typ (analog {@link #getAge()}). */
  public @NonNull String getWeight() {
    return weight;
  }

  public void setWeight(@NonNull String weight) {
    this.weight = weight;
  }

  public @NonNull String getSkin() {
    return skin;
  }

  public void setSkin(@NonNull String skin) {
    this.skin = skin;
  }

  public @NonNull String getHair() {
    return hair;
  }

  public void setHair(@NonNull String hair) {
    this.hair = hair;
  }

  public @NonNull String getDescription() {
    return description;
  }

  public void setDescription(@NonNull String description) {
    this.description = description;
  }

  public @NonNull String getBackground() {
    return background;
  }

  public void setBackground(@NonNull String background) {
    this.background = background;
  }

  public @NonNull String getConcept() {
    return concept;
  }

  public void setConcept(@NonNull String concept) {
    this.concept = concept;
  }

  public @NonNull String getNotes() {
    return notes;
  }

  public void setNotes(@NonNull String notes) {
    this.notes = notes;
  }

  public @NonNull String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(@NonNull String playerName) {
    this.playerName = playerName;
  }

  /**
   * Laufende Spielnotizen. Anders als die übrigen Felder im C#-Original nur im Karrieremodus-Tab
   * sichtbar, nicht in der Erstellung (siehe {@code docs/charakter_biografie_analyse.md}) — dieses
   * Sichtbarkeits-Gating liegt in der UI ({@code CharacterViewController}), nicht in dieser Klasse.
   */
  public @NonNull String getGameNotes() {
    return gameNotes;
  }

  public void setGameNotes(@NonNull String gameNotes) {
    this.gameNotes = gameNotes;
  }
}
