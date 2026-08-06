package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.KnowledgeSkillCategory;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.QualityData;
import de.shalorian.chummer.model.character.QualityDataLoader;
import de.shalorian.chummer.model.character.QualitySource;
import de.shalorian.chummer.model.character.Skill;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

public class CharacterStore {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private CharacterStore() {
  }

  public static void save(@NonNull Character character, @NonNull File file) throws IOException {
    MAPPER.writeValue(file, toData(character));
  }

  public static @NonNull Character load(@NonNull File file) throws IOException {
    return fromData(MAPPER.readValue(file, CharacterData.class));
  }

  private static @NonNull CharacterData toData(@NonNull Character character) {
    CharacterData data = new CharacterData();
    data.name = character.getName();
    data.metatype = character.getMetatype().name(); // MetatypeData.name() Accessor

    for (AttributeType type : AttributeType.values()) {
      data.attributes.put(type.name(), character.getAttribute(type).getValue());
    }
    data.edge = character.getEdge().getValue();
    if (character.hasMagic()) {
      data.magic = Objects.requireNonNull(character.getMagicOrResonance()).getValue();
    } else if (character.hasResonance()) {
      data.resonance = Objects.requireNonNull(character.getMagicOrResonance()).getValue();
    }
    for (Skill skill : character.getSkills()) {
      if (skill.getRating() > 0) {
        data.skills.put(skill.getName(), skill.getRating());
      }
    }
    data.created = character.isCreated();
    data.nuyenBP = character.getNuyenBP();
    data.selectedQualities = character.getQualities().stream()
        .filter(q -> q.source() == QualitySource.SELECTED)
        .map(q -> new CharacterData.SelectedQualityEntry(q.data().name(), q.gratis()))
        .toList();
    data.knowledgeSkills = character.getKnowledgeSkills().stream()
        .map(k -> new CharacterData.KnowledgeSkillEntry(
            k.getName(), k.getCategory().name(), k.getRating(), k.isNativeLanguage()))
        .toList();
    data.decoupledGroups = new ArrayList<>(character.getDecoupledGroups());
    data.karmaLog = character.getKarmaLog().stream()
        .map(e -> new CharacterData.LogEntryData(e.description(), e.amount(), e.date().toString()))
        .toList();
    data.nuyenLog = character.getNuyenLog().stream()
        .map(e -> new CharacterData.LogEntryData(e.description(), e.amount(), e.date().toString()))
        .toList();

    return data;
  }

  private static @NonNull Character fromData(@NonNull CharacterData data) {
    String charName = Objects.requireNonNull(data.name, "Name fehlt in Speicherdatei");
    String metatypeName = Objects.requireNonNull(data.metatype, "Metatyp fehlt in Speicherdatei");
    MetatypeData metatype = MetatypeDataLoader.find(metatypeName);
    Character character = new Character(charName, metatype);

    applyAttributes(character, data);
    applySkills(character, data);
    character.setCreated(data.created);
    character.setNuyenBP(data.nuyenBP);
    applySelectedQualities(character, data);
    applyMagicOrResonance(character, data);
    applyKnowledgeSkills(character, data);
    data.decoupledGroups.forEach(character::decoupleGroup);
    applyLog(data.karmaLog, character::addKarmaEntry);
    applyLog(data.nuyenLog, character::addNuyenEntry);

    return character;
  }

  private static void applyAttributes(@NonNull Character character, @NonNull CharacterData data) {
    for (Map.Entry<String, Integer> e : data.attributes.entrySet()) {
      character.getAttribute(AttributeType.valueOf(e.getKey())).setValue(e.getValue());
    }
    if (data.edge > 0) {
      character.getEdge().setValue(data.edge);
    }
  }

  private static void applySkills(@NonNull Character character, @NonNull CharacterData data) {
    for (Map.Entry<String, Integer> e : data.skills.entrySet()) {
      Skill skill = character.getSkill(e.getKey());
      if (skill != null) {
        skill.setRating(e.getValue());
      }
    }
  }

  private static void applySelectedQualities(@NonNull Character character,
      @NonNull CharacterData data) {
    for (CharacterData.SelectedQualityEntry entry : data.selectedQualities) {
      QualityData qualityData = QualityDataLoader.findByName(entry.name);
      if (qualityData != null) {
        character.loadSelectedQuality(qualityData, entry.gratis);
      }
    }
  }

  private static void applyMagicOrResonance(@NonNull Character character,
      @NonNull CharacterData data) {
    if (data.magic != null && character.hasMagic()) {
      Objects.requireNonNull(character.getMagicOrResonance()).setValue(data.magic);
    } else if (data.resonance != null && character.hasResonance()) {
      Objects.requireNonNull(character.getMagicOrResonance()).setValue(data.resonance);
    }
  }

  private static void applyKnowledgeSkills(@NonNull Character character,
      @NonNull CharacterData data) {
    for (CharacterData.KnowledgeSkillEntry entry : data.knowledgeSkills) {
      if (entry.nativeLanguage) {
        character.setNativeLanguage(entry.name);
        continue;
      }
      character.addKnowledgeSkill(entry.name, KnowledgeSkillCategory.valueOf(entry.category));
      character.getKnowledgeSkills().stream()
          .filter(k -> k.getName().equals(entry.name))
          .findFirst()
          .ifPresent(k -> k.setRating(entry.rating));
    }
  }

  private static void applyLog(@NonNull List<CharacterData.LogEntryData> entries,
      @NonNull Consumer<LogEntry> sink) {
    for (CharacterData.LogEntryData e : entries) {
      if (!e.date.isEmpty()) {
        sink.accept(new LogEntry(e.description, e.amount, LocalDate.parse(e.date)));
      }
    }
  }
}