package de.shalorian.chummer.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.shalorian.chummer.model.character.AttributeType;
import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.CharacterBiography;
import de.shalorian.chummer.model.character.Contact;
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
    data.skills = character.getSkills().stream()
        .filter(s -> s.getRating() > 0 || s.getSpecialization() != null)
        .map(s -> new SkillEntry(s.getName(), s.getRating(), s.getSpecialization()))
        .toList();
    data.created = character.isCreated();
    data.nuyenBP = character.getNuyenBP();
    data.selectedQualities = character.getQualities().stream()
        .filter(q -> q.source() == QualitySource.SELECTED)
        .map(q -> new SelectedQualityEntry(q.data().name(), q.gratis()))
        .toList();
    data.knowledgeSkills = character.getKnowledgeSkills().stream()
        .map(k -> new KnowledgeSkillEntry(k.getName(), k.getCategory().name(), k.getRating(),
            k.isNativeLanguage(), k.getSpecialization()))
        .toList();
    data.contacts = character.getContacts().stream()
        .map(c -> new ContactEntry(c.getName(), c.getDescription(), c.getConnection(),
            c.getLoyalty()))
        .toList();
    data.decoupledGroups = new ArrayList<>(character.getDecoupledGroups());
    data.karmaLog = character.getKarmaLog().stream()
        .map(e -> new LogEntryData(e.description(), e.amount(), e.date().toString()))
        .toList();
    data.nuyenLog = character.getNuyenLog().stream()
        .map(e -> new LogEntryData(e.description(), e.amount(), e.date().toString()))
        .toList();
    data.biography = toBiographyData(character.getBiography());

    return data;
  }

  private static @NonNull BiographyData toBiographyData(@NonNull CharacterBiography biography) {
    BiographyData data = new BiographyData();
    data.alias = biography.getAlias();
    data.sex = biography.getSex();
    data.age = biography.getAge();
    data.eyes = biography.getEyes();
    data.height = biography.getHeight();
    data.weight = biography.getWeight();
    data.skin = biography.getSkin();
    data.hair = biography.getHair();
    data.description = biography.getDescription();
    data.background = biography.getBackground();
    data.concept = biography.getConcept();
    data.notes = biography.getNotes();
    data.playerName = biography.getPlayerName();
    data.gameNotes = biography.getGameNotes();
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
    applyContacts(character, data);
    data.decoupledGroups.forEach(character::decoupleGroup);
    applyLog(data.karmaLog, character::addKarmaEntry);
    applyLog(data.nuyenLog, character::addNuyenEntry);
    applyBiography(character, data);

    return character;
  }

  private static void applyBiography(@NonNull Character character, @NonNull CharacterData data) {
    CharacterBiography biography = character.getBiography();
    biography.setAlias(data.biography.alias);
    biography.setSex(data.biography.sex);
    biography.setAge(data.biography.age);
    biography.setEyes(data.biography.eyes);
    biography.setHeight(data.biography.height);
    biography.setWeight(data.biography.weight);
    biography.setSkin(data.biography.skin);
    biography.setHair(data.biography.hair);
    biography.setDescription(data.biography.description);
    biography.setBackground(data.biography.background);
    biography.setConcept(data.biography.concept);
    biography.setNotes(data.biography.notes);
    biography.setPlayerName(data.biography.playerName);
    biography.setGameNotes(data.biography.gameNotes);
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
    for (SkillEntry entry : data.skills) {
      Skill skill = character.getSkill(entry.name);
      if (skill != null) {
        skill.setRating(entry.rating);
        skill.setSpecialization(entry.specialization);
      }
    }
  }

  private static void applySelectedQualities(@NonNull Character character,
      @NonNull CharacterData data) {
    for (SelectedQualityEntry entry : data.selectedQualities) {
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
    for (KnowledgeSkillEntry entry : data.knowledgeSkills) {
      if (entry.nativeLanguage) {
        character.setNativeLanguage(entry.name);
        continue;
      }
      character.addKnowledgeSkill(entry.name, KnowledgeSkillCategory.valueOf(entry.category));
      character.getKnowledgeSkills().stream()
          .filter(k -> k.getName().equals(entry.name))
          .findFirst()
          .ifPresent(k -> {
            k.setRating(entry.rating);
            k.setSpecialization(entry.specialization);
          });
    }
  }

  private static void applyContacts(@NonNull Character character, @NonNull CharacterData data) {
    for (ContactEntry entry : data.contacts) {
      character.addContact(entry.name, entry.description);
      List<Contact> contacts = character.getContacts();
      Contact added = contacts.get(contacts.size() - 1);
      added.setConnection(entry.connection);
      added.setLoyalty(entry.loyalty);
    }
  }

  private static void applyLog(@NonNull List<LogEntryData> entries,
      @NonNull Consumer<LogEntry> sink) {
    for (LogEntryData e : entries) {
      if (!e.date.isEmpty()) {
        sink.accept(new LogEntry(e.description, e.amount, LocalDate.parse(e.date)));
      }
    }
  }
}