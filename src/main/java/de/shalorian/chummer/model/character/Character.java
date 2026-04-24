package de.shalorian.chummer.model.character;

import org.jspecify.annotations.Nullable;

import java.util.*;


public class Character {

    public static final int TOTAL_BP = 400;

    private String       name;
    private MetatypeData metatype;
    private final Map<AttributeType, CharacterAttribute> attributes;
    private final Map<String, Skill> skills;        // LinkedHashMap – behält XML-Reihenfolge
    private final Set<String> decoupledGroups;       // Gruppen die aufgebrochen wurden
    private final List<LogEntry> karmaLog = new ArrayList<>();
    private final List<LogEntry> nuyenLog = new ArrayList<>();

    public Character(String name, MetatypeData metatype) {
        this.name             = name;
        this.metatype         = metatype;
        this.attributes       = new EnumMap<>(AttributeType.class);
        this.decoupledGroups  = new HashSet<>();
        initAttributes(metatype);
        this.skills = new LinkedHashMap<>();
        for (Skill skill : SkillDataLoader.createFreshSkills()) {
            this.skills.put(skill.getName(), skill);
        }
    }

    private void initAttributes(MetatypeData metatype) {
        attributes.clear();
        for (AttributeType type : AttributeType.values()) {
            attributes.put(type, new CharacterAttribute(type, metatype.getRangeFor(type)));
        }
    }

    // --- Attribute ---

    public String       getName()     { return name; }
    public MetatypeData getMetatype() { return metatype; }
    public void         setName(String name) { this.name = name; }

    public void setMetatype(MetatypeData metatype) {
        this.metatype = metatype;
        initAttributes(metatype);
    }

    public Map<AttributeType, CharacterAttribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public CharacterAttribute getAttribute(AttributeType type) {
        return attributes.get(type);
    }

    // --- Skills ---

    public List<Skill> getSkills() {
        return List.copyOf(skills.values());
    }

    public @Nullable Skill getSkill(String name) {
        return skills.get(name);
    }

    // --- Skillgruppen ---

    /** Liefert true, solange die Gruppe noch nicht aufgebrochen wurde. */
    public boolean isGroupCoupled(String groupName) {
        return !decoupledGroups.contains(groupName);
    }

    /** Das aktuelle Rating einer gekoppelten Gruppe (Minimum aller Skills). */
    public int getGroupRating(String groupName) {
        return skills.values().stream()
            .filter(s -> groupName.equals(s.getGroupName()))
            .mapToInt(Skill::getRating)
            .min().orElse(0);
    }

    public void raiseGroup(String groupName) {
        int newRating = getGroupRating(groupName) + 1;
        skills.values().stream()
            .filter(s -> groupName.equals(s.getGroupName()))
            .forEach(s -> s.setRating(newRating));
    }

    public void lowerGroup(String groupName) {
        int newRating = Math.max(0, getGroupRating(groupName) - 1);
        skills.values().stream()
            .filter(s -> groupName.equals(s.getGroupName()))
            .forEach(s -> s.setRating(newRating));
    }

    /** Bricht die Gruppe auf – Skills behalten ihre Ratings, werden ab jetzt einzeln bepreist. */
    public void decoupleGroup(String groupName) {
        decoupledGroups.add(groupName);
    }

    public Set<String> getDecoupledGroups() {
        return Collections.unmodifiableSet(decoupledGroups);
    }

    // Wird beim Laden aus JSON aufgerufen
    public void markGroupDecoupled(String groupName) {
        decoupledGroups.add(groupName);
    }

    // --- BP ---

    public int getSpentBP() {
        int attrBP = attributes.values().stream().mapToInt(CharacterAttribute::getBpCost).sum();

        int skillBP = 0;
        Set<String> counted = new HashSet<>();
        for (Skill skill : skills.values()) {
            String g = skill.getGroupName();
            if (g != null && !decoupledGroups.contains(g)) {
                // Gekoppelte Gruppe: einmal mit Gruppenpreis (10 BP/Rating)
                if (counted.add(g)) {
                    skillBP += getGroupRating(g) * 10;
                }
            } else {
                // Entkoppelt oder kein Gruppen-Skill: Einzelpreis (4 BP/Rating)
                skillBP += skill.getBpCost();
            }
        }

        return metatype.bpCost() + attrBP + skillBP;
    }

    public int getRemainingBP() {
        return TOTAL_BP - getSpentBP();
    }

    // --- Karma & Nuyen Log ---

    public void addKarmaEntry(LogEntry entry) { karmaLog.add(entry); }
    public void addNuyenEntry(LogEntry entry) { nuyenLog.add(entry); }

    public List<LogEntry> getKarmaLog() { return Collections.unmodifiableList(karmaLog); }
    public List<LogEntry> getNuyenLog() { return Collections.unmodifiableList(nuyenLog); }

    public int getKarmaTotal() { return karmaLog.stream().mapToInt(LogEntry::amount).sum(); }
    public int getNuyenTotal() { return nuyenLog.stream().mapToInt(LogEntry::amount).sum(); }
}