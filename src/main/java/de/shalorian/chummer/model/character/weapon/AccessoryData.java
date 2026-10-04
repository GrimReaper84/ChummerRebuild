package de.shalorian.chummer.model.character.weapon;

import de.shalorian.chummer.model.character.CatalogEntry;
import de.shalorian.chummer.model.character.equipment.Availability;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Unveränderlicher Katalogeintrag eines Feuerwaffenzubehörs aus {@code weapon_accessories.xml}
 * (Tabelle <i>Feuerwaffenzubehör</i>, SR4-Grundregelwerk S. 368, gegen das PDF-Bild verifiziert).
 *
 * @param mounts     mögliche Halterungen; leer, wenn das Teil keine Halterung braucht (GRW "–")
 * @param rc         Rückstoßkompensation des Teils; zählt nur in den Klammerwert, außer bei
 *                   {@link AccessoryRule#isPermanent() fest eingebauten} Teilen (siehe {@link Recoil})
 * @param rule       Sonderregel aus dem Zubehörtext, oder {@code null}
 * @param projectile ob das Teil auch an Projektilwaffen passt (GRW S. 357: "Projektilwaffen können nur
 *                   mit einem Lasermarkierer oder Smartgunsystem ausgerüstet werden")
 * @param cost       Preis in Nuyen, oder {@value #WEAPON_PRICE} (internes Smartgunsystem)
 * @param note       Regelhinweis ohne eigene Mechanik (z.B. Würfelpool-Modifikator), sonst leer
 */
public record AccessoryData(
    @NonNull String name,
    @NonNull List<Mount> mounts,
    int rc,
    @Nullable AccessoryRule rule,
    boolean projectile,
    @NonNull Availability availability,
    @NonNull String cost,
    @NonNull String note,
    @NonNull String source,
    @NonNull String page
) implements CatalogEntry {

  /**
   * Preisangabe des internen Smartgunsystems. GRW S. 368: "Eine Feuerwaffe nachträglich mit einem
   * internen Smartgunsystem auszurüsten, verdoppelt den Preis der Waffe."
   */
  public static final String WEAPON_PRICE = "Waffenpreis";

  public AccessoryData {
    mounts = List.copyOf(mounts);
  }

  /** Ob das Teil an einer Halterung montiert werden muss. */
  public boolean needsMount() {
    return !mounts.isEmpty();
  }

  /** Ob das Teil nach dem Einbau nicht mehr entfernt werden kann (Gasventilsystem). */
  public boolean isPermanent() {
    return rule != null && rule.isPermanent();
  }

  /** Preis in Nuyen an einer Waffe mit dem Grundpreis {@code weaponPrice}. */
  public int costFor(int weaponPrice) {
    return WEAPON_PRICE.equals(cost) ? weaponPrice : Integer.parseInt(cost);
  }

  /** Halterungen als Anzeigetext, z.B. "Aufmontiert/Unterlauf", bzw. "–" ohne Halterung. */
  public @NonNull String mountsText() {
    if (mounts.isEmpty()) {
      return "–";
    }
    return String.join("/", mounts.stream().map(Mount::getDisplayName).toList());
  }

  /** RK als Anzeigetext, z.B. "6", bzw. "–" ohne RK. */
  public @NonNull String rcText() {
    return rc == 0 ? "–" : String.valueOf(rc);
  }

  /** Preis als Anzeigetext, z.B. "400 ¥" bzw. "Waffenpreis". */
  public @NonNull String costText() {
    return WEAPON_PRICE.equals(cost) ? WEAPON_PRICE : cost + " ¥";
  }
}
