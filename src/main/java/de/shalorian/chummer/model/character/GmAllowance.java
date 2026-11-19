package de.shalorian.chummer.model.character;

import de.shalorian.chummer.model.character.equipment.Equipment;
import de.shalorian.chummer.model.character.gear.Gear;
import de.shalorian.chummer.model.character.lifestyle.Lifestyle;
import de.shalorian.chummer.model.character.spirit.Spirit;
import de.shalorian.chummer.model.character.technomancer.ComplexForm;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Gratis-Käufe im Spielleiter-Modus bei der Erschaffung (ADR-070). Beim Einschalten merkt sich der Charakter die Kosten
 * jedes Eintrags; jeder spätere Zuwachs (Neukauf, Einbau, Steigerung von Connection/Loyalität, Stufe, Diensten,
 * Lebensstil) wird dem Eintrag als Gratis-Anteil gutgeschrieben und zählt damit nie gegen BP bzw. Restgeld, auch nach
 * dem Ausschalten nicht. Der Ausgangsstand je Eintrag steigt nur (Senken und erneutes Steigern im Modus schenkt
 * nichts). Attribut-, Fertigkeits- und Wissenspunkte sowie Gaben (dort der Gratis-Haken) sind nicht erfasst. Der
 * Ausgangsstand wird nicht gespeichert, die Gratis-Anteile schon (in den Einträgen).
 */
final class GmAllowance {

  private final @NonNull Character owner;
  /** Höchste bisher gesehene Kosten je Eintrag seit dem Einschalten; leer, solange der Modus aus ist. */
  private final IdentityHashMap<Object, Integer> nuyenBaseline = new IdentityHashMap<>();
  private final IdentityHashMap<Object, Integer> bpBaseline = new IdentityHashMap<>();
  /** Ob seit der letzten Abfrage etwas gutgeschrieben wurde (die Anzeige muss dann neu aufbauen). */
  private boolean credited;

  GmAllowance(@NonNull Character owner) {
    this.owner = owner;
  }

  /** Merkt sich den Ausgangsstand beim Einschalten des Modus bei der Erschaffung. */
  void start() {
    reset();
    nuyenEntries().forEach(e -> nuyenBaseline.put(e.key(), e.cost()));
    bpEntries().forEach(e -> bpBaseline.put(e.key(), e.cost()));
  }

  /** Schreibt alle Zuwächse seit dem letzten Stand gut; außerhalb des Modus oder in der Karriere ohne Wirkung. */
  void cover() {
    if (!owner.isGmMode() || owner.isCreated()) {
      return;
    }
    cover(nuyenEntries(), nuyenBaseline);
    cover(bpEntries(), bpBaseline);
  }

  /** Beim Ausschalten: letzte Zuwächse gutschreiben, dann den Ausgangsstand vergessen. */
  void stop() {
    cover();
    reset();
  }

  /** Schreibt gut und meldet, ob seit der letzten Abfrage etwas gutgeschrieben wurde; setzt die Meldung zurück. */
  boolean consumeCredit() {
    cover();
    boolean result = credited;
    credited = false;
    return result;
  }

  private void reset() {
    nuyenBaseline.clear();
    bpBaseline.clear();
  }

  private void cover(@NonNull List<GmCostEntry> entries, @NonNull IdentityHashMap<Object, Integer> baseline) {
    IdentityHashMap<Object, Integer> next = new IdentityHashMap<>();
    for (GmCostEntry entry : entries) {
      int before = baseline.getOrDefault(entry.key(), 0);
      if (entry.cost() > before) {
        entry.addFree().accept(entry.cost() - before);
        credited = true;
      }
      next.put(entry.key(), Math.max(before, entry.cost()));
    }
    baseline.clear();
    baseline.putAll(next);
  }

  private @NonNull List<GmCostEntry> nuyenEntries() {
    Equipment equipment = owner.getEquipment();
    List<GmCostEntry> entries = new ArrayList<>();
    equipment.getGear().forEach(g -> entries.add(new GmCostEntry(g, g.getNuyenCost(),
        add -> g.setGmFreeNuyen(g.getGmFreeNuyen() + add))));
    equipment.getCyberware().forEach(c -> entries.add(new GmCostEntry(c, c.getNuyenCost(),
        add -> c.setGmFreeNuyen(c.getGmFreeNuyen() + add))));
    equipment.getArmor().forEach(a -> entries.add(new GmCostEntry(a, a.getNuyenCost(),
        add -> a.setGmFreeNuyen(a.getGmFreeNuyen() + add))));
    equipment.getWeapons().forEach(w -> entries.add(new GmCostEntry(w, w.getTotalNuyenCost(),
        add -> w.setGmFreeNuyen(w.getGmFreeNuyen() + add))));
    equipment.getVehicles().forEach(v -> entries.add(new GmCostEntry(v, v.getNuyenCost(),
        add -> v.setGmFreeNuyen(v.getGmFreeNuyen() + add))));
    Lifestyle lifestyle = equipment.getLifestyle();
    entries.add(new GmCostEntry(lifestyle, lifestyle.getNuyenCost(),
        add -> lifestyle.setGmFreeNuyen(lifestyle.getGmFreeNuyen() + add)));
    return entries;
  }

  private @NonNull List<GmCostEntry> bpEntries() {
    List<GmCostEntry> entries = new ArrayList<>();
    for (Contact contact : owner.getContacts()) {
      entries.add(new GmCostEntry(contact, contact.getConnection() + contact.getLoyalty(),
          add -> contact.setGmFreeBp(contact.getGmFreeBp() + add)));
    }
    for (ComplexForm form : owner.getComplexForms()) {
      entries.add(new GmCostEntry(form, form.getRating() * form.getBpCostPerRating(),
          add -> form.setGmFreeBp(form.getGmFreeBp() + add)));
    }
    for (Spirit spirit : owner.getSpirits().getAll()) {
      entries.add(new GmCostEntry(spirit, spirit.isBound() ? spirit.getServices() : 0,
          add -> spirit.setGmFreeBp(spirit.getGmFreeBp() + add)));
    }
    for (Gear focus : owner.getFoci().getAll()) {
      entries.add(new GmCostEntry(focus, focus.isBound() ? focus.getRating() : 0,
          add -> focus.setGmFreeBindingBp(focus.getGmFreeBindingBp() + add)));
    }
    owner.getSpells().forEach(spell -> entries.add(new GmCostEntry(spell, Character.SPELL_BP_COST,
        _ -> owner.markSpellGmFree(spell.name()))));
    return entries;
  }
}
