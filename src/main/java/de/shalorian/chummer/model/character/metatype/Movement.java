package de.shalorian.chummer.model.character.metatype;

/**
 * Bewegungsweiten eines Metatyps in Metern je Kampfrunde (GRW S. 177): Gehen und Laufen. Bewegt sich ein Charakter in
 * einem Initiativedurchgang, teilt sich die Weite auf die Durchgänge auf.
 *
 * @param walk Gehen-Bewegungsweite
 * @param run  Laufen-Bewegungsweite
 */
public record Movement(int walk, int run) {
}
