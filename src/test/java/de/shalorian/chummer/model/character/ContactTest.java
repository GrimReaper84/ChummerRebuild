package de.shalorian.chummer.model.character;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Contact("  ", null));
    }

    @Test
    void startsWithMinimumRatings() {
        Contact contact = new Contact("Sam", null);

        assertEquals(Contact.MIN_RATING, contact.getConnection());
        assertEquals(Contact.MIN_RATING, contact.getLoyalty());
    }

    @Test
    void connectionWithinBoundsIsAccepted() {
        Contact contact = new Contact("Sam", null);
        contact.setConnection(Contact.MAX_RATING);

        assertEquals(Contact.MAX_RATING, contact.getConnection());
    }

    @Test
    void connectionOutsideBoundsIsRejected() {
        Contact contact = new Contact("Sam", null);

        assertThrows(IllegalArgumentException.class,
                () -> contact.setConnection(Contact.MIN_RATING - 1));
        assertThrows(IllegalArgumentException.class,
                () -> contact.setConnection(Contact.MAX_RATING + 1));
    }

    @Test
    void loyaltyOutsideBoundsIsRejected() {
        Contact contact = new Contact("Sam", null);

        assertThrows(IllegalArgumentException.class,
                () -> contact.setLoyalty(Contact.MIN_RATING - 1));
        assertThrows(IllegalArgumentException.class,
                () -> contact.setLoyalty(Contact.MAX_RATING + 1));
    }

    @Test
    void descriptionIsInitiallyAbsentWhenNotProvided() {
        Contact contact = new Contact("Sam", null);

        assertNull(contact.getDescription());
    }

    @Test
    void blankDescriptionIsNormalizedToNull() {
        Contact contact = new Contact("Sam", "   ");

        assertNull(contact.getDescription());
    }

    @Test
    void descriptionIsTrimmedAndOverwritable() {
        Contact contact = new Contact("Sam", "  Fixer im Barrens  ");

        assertEquals("Fixer im Barrens", contact.getDescription());

        contact.setDescription("Anderer Text");
        assertEquals("Anderer Text", contact.getDescription());
    }

    @Test
    void nameCanBeChanged() {
        Contact contact = new Contact("Sam", null);
        contact.setName("Sam Two");

        assertEquals("Sam Two", contact.getName());
    }

    @Test
    void rejectsBlankNameOnSetter() {
        Contact contact = new Contact("Sam", null);

        assertThrows(IllegalArgumentException.class, () -> contact.setName("   "));
    }
}
