package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.LogEntry;
import de.shalorian.chummer.model.character.MetatypeData;
import de.shalorian.chummer.model.character.MetatypeDataLoader;
import de.shalorian.chummer.model.character.Skill;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CharacterStore {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  public static void save(Character character, File file) throws IOException {
    MAPPER.writeValue(file, toData(character));
  }

  public static Character load(File file) throws IOException {
    return fromData(MAPPER.readValue(file, CharacterData.class));
  }

  private static CharacterData toData(Character character) {
    CharacterData data = new CharacterData();
    data.name = character.getName();
    data.metatype = character.getMetatype().name(); // MetatypeData.name() Accessor

    for (AttributeType type : AttributeType.values()) {
      data.attributes.put(type.name(), character.getAttribute(type).getValue());
    }
    for (Skill skill : character.getSkills()) {
      if (skill.getRating() > 0) {
        data.skills.put(skill.getName(), skill.getRating());
      }
    }
    data.created = character.isCreated();
    data.decoupledGroups = new ArrayList<>(character.getDecoupledGroups());
    data.karmaLog = character.getKarmaLog().stream()
        .map(e -> new CharacterData.LogEntryData(e.description(), e.amount(), e.date().toString()))
        .collect(Collectors.toList());
    data.nuyenLog = character.getNuyenLog().stream()
        .map(e -> new CharacterData.LogEntryData(e.description(), e.amount(), e.date().toString()))
        .collect(Collectors.toList());

    return data;
  }

  private static Character fromData(CharacterData data) {
    String charName = Objects.requireNonNull(data.name, "Name fehlt in Speicherdatei");
    String metatypeName = Objects.requireNonNull(data.metatype, "Metatyp fehlt in Speicherdatei");
    MetatypeData metatype = MetatypeDataLoader.find(metatypeName);
    Character character = new Character(charName, metatype);

    for (Map.Entry<String, Integer> e : data.attributes.entrySet()) {
      character.getAttribute(AttributeType.valueOf(e.getKey())).setValue(e.getValue());
    }
    if (data.skills != null) {
      for (Map.Entry<String, Integer> e : data.skills.entrySet()) {
        Skill skill = character.getSkill(e.getKey());
        if (skill != null) {
          skill.setRating(e.getValue());
        }
      }
    }
    character.setCreated(data.created);
    if (data.decoupledGroups != null) {
      data.decoupledGroups.forEach(character::decoupleGroup);
    }
    for (CharacterData.LogEntryData e : data.karmaLog) {
      if (!e.date.isEmpty()) {
        character.addKarmaEntry(new LogEntry(e.description, e.amount, LocalDate.parse(e.date)));
      }
    }
    for (CharacterData.LogEntryData e : data.nuyenLog) {
      if (!e.date.isEmpty()) {
        character.addNuyenEntry(new LogEntry(e.description, e.amount, LocalDate.parse(e.date)));
      }
    }

    return character;
  }
}