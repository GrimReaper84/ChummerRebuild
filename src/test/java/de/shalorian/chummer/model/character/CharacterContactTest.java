package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterContactTest {

    private static Character mensch(String name) {
        return new Character(name, MetatypeDataLoader.find("Mensch"));
    }

    @Test
    void addContactStartsAtMinimumRatings() {
        Character c = mensch("Test");
        c.addContact("Sam", "Fixer im Barrens");

        assertEquals(1, c.getContacts().size());
        Contact contact = c.getContacts().get(0);
        assertEquals("Sam", contact.getName());
        assertEquals("Fixer im Barrens", contact.getDescription());
        assertEquals(Contact.MIN_RATING, contact.getConnection());
        assertEquals(Contact.MIN_RATING, contact.getLoyalty());
    }

    @Test
    void duplicateNamesAreAllowed() {
        Character c = mensch("Test");
        c.addContact("Sam", null);
        c.addContact("Sam", "Anderer Sam");

        assertEquals(2, c.getContacts().size());
    }

    @Test
    void removeContactWorksByReferenceNotByName() {
        Character c = mensch("Test");
        c.addContact("Sam", null);
        c.addContact("Sam", "Anderer Sam");
        Contact first = c.getContacts().get(0);

        c.removeContact(first);

        assertEquals(1, c.getContacts().size());
        assertEquals("Anderer Sam", c.getContacts().get(0).getDescription());
    }

    @Test
    void contactBpIsZeroWithoutContacts() {
        Character c = mensch("Test");

        assertEquals(0, c.getContactBP());
    }

    @Test
    void contactBpIsSumOfConnectionAndLoyalty() {
        Character c = mensch("Test");
        c.addContact("Sam", null);
        Contact contact = c.getContacts().get(0);
        contact.setConnection(4);
        contact.setLoyalty(3);

        assertEquals(7, c.getContactBP()); // (4 + 3) * 1
    }

    @Test
    void contactBpSumsAcrossMultipleContacts() {
        Character c = mensch("Test");
        c.addContact("Sam", null);
        c.addContact("Johnson", null);
        c.getContacts().get(0).setConnection(2); // + Loyalty 1 = 3
        c.getContacts().get(1).setConnection(5);
        c.getContacts().get(1).setLoyalty(5); // = 10

        assertEquals(13, c.getContactBP());
    }

    @Test
    void contactBpIsIncludedInSpentBP() {
        Character c = mensch("Test");
        int baseline = c.getSpentBP();
        c.addContact("Sam", null);
        c.getContacts().get(0).setConnection(3);

        assertEquals(baseline + c.getContactBP(), c.getSpentBP());
    }

    @Test
    void removingContactReducesSpentBP() {
        Character c = mensch("Test");
        c.addContact("Sam", null);
        Contact contact = c.getContacts().get(0);
        contact.setConnection(5);
        contact.setLoyalty(5);
        int spentWithContact = c.getSpentBP();

        c.removeContact(contact);

        assertTrue(c.getSpentBP() < spentWithContact);
        assertEquals(0, c.getContactBP());
    }

    @Test
    void ratingChangesDoNotAffectKarmaInCareerMode() {
        Character c = mensch("Test");
        c.setCreated(true);
        c.addContact("Sam", null);
        Contact contact = c.getContacts().get(0);
        int karmaBefore = c.getKarmaTotal();

        contact.setConnection(6);
        contact.setLoyalty(6);

        assertEquals(karmaBefore, c.getKarmaTotal());
    }
}
