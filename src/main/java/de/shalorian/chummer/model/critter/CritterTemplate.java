package de.shalorian.chummer.model.critter;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.AttributeType;
import de.shalorian.chummer.model.character.quality.QualityDataLoader;
import de.shalorian.chummer.model.character.skill.KnowledgeSkillCategory;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.model.npc.NpcStatblock;
import de.shalorian.chummer.model.template.DocumentTemplate;
import de.shalorian.chummer.model.template.TemplateEntry;
import de.shalorian.chummer.model.template.TemplateKind;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Ein Critter als Vorlage im Dialog „Neu“: legt eine Gruppe auf dem Schergen-Bogen an (Stufe 0, ein Mitglied, ohne
 * Anführer); ein Rudel entsteht, indem man die Anzahl erhöht (Schergenregeln für Critter-Gruppen, GRW S. 337).
 */
public record CritterTemplate(@NonNull CritterData data) implements DocumentTemplate {

  @Override
  public @NonNull String name() {
    return data.name();
  }

  @Override
  public @NonNull String description() {
    return data.category().getDisplayName() + ": " + data.note();
  }

  @Override
  public @NonNull String source() {
    return data.source();
  }

  @Override
  public int page() {
    return data.page();
  }

  @Override
  public @NonNull TemplateKind kind() {
    return TemplateKind.CRITTER;
  }

  public @NonNull NpcGroup create() {
    return new NpcGroup(data.name(), 0, 1, new NpcStatblock(statblock(data), ""));
  }

  /**
   * Werteblock eines Critters im Spielleiter-Modus: Attribute, Edge und Magie wie im Katalog in der Spanne ±3 (S. 337),
   * Fertigkeiten bis Katalogwert + 3, alle übrigen bis {@value CritterTraits#UNLISTED_SKILL_MAX}; Gaben aus dem
   * Katalog (Zauberer bei Dracoformen und Wendigo, S. 347/348).
   */
  public static @NonNull Character statblock(@NonNull CritterData data) {
    Character c = new Character(data.name(), CritterMetatype.of(data));
    c.setCreated(true);
    c.setGmMode(true);
    for (AttributeType type : AttributeType.values()) {
      c.getAttribute(type).setValue(data.attribute(type));
    }
    c.getEdge().setValue(data.edge());
    c.setCritterTraits(new CritterTraits(data));
    for (String quality : data.qualities()) {
      c.addQuality(Objects.requireNonNull(QualityDataLoader.findByName(quality), quality), false);
    }
    for (TemplateEntry entry : data.skills()) {
      switch (entry.kind()) {
        case "skill" -> Objects.requireNonNull(c.getSkill(entry.name()), entry.name()).setRating(entry.rating());
        case "group" -> {
          while (c.getGroupRating(entry.name()) < entry.rating()) {
            c.raiseGroup(entry.name());
          }
        }
        case "knowledge" -> {
          c.addKnowledgeSkill(entry.name(), KnowledgeSkillCategory.valueOf(entry.detail()));
          c.getKnowledgeSkills().getLast().setRating(entry.rating());
        }
        default -> { } // Sonderfertigkeiten (Fliegen) führt CritterTraits
      }
    }
    return c;
  }
}
