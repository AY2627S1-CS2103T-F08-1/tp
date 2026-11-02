package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("-peter")); // starts with a non-alphanumeric character
        assertFalse(Name.isValidName("peter-")); // ends with a non-alphanumeric character
        assertFalse(Name.isValidName("O'Neil!")); // contains an invalid character
        for (String filler : new String[] {"\u115f", "\u1160", "\u3164", "\uffa0"}) {
            assertFalse(Name.isValidName(filler));
            assertFalse(Name.isValidName(filler + "Alice"));
            assertFalse(Name.isValidName("Alice" + filler));
        }
        assertFalse(Name.isValidName("A \u0301B")); // combining mark is not attached to an alphanumeric character

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Anne-Marie")); // contains a hyphen
        assertTrue(Name.isValidName("O'Neil")); // contains an apostrophe
        assertTrue(Name.isValidName("O’Connor")); // contains a typographic apostrophe
        assertTrue(Name.isValidName("José Tan")); // accented Latin characters
        assertTrue(Name.isValidName("Jose\u0301 Tan")); // decomposed accented Latin characters
        assertTrue(Name.isValidName("陈小明")); // non-Latin characters
        assertTrue(Name.isValidName("குமார்")); // non-Latin characters with combining marks
    }

    @Test
    public void constructor_decomposedCharacters_normalizesToCanonicalForm() {
        assertEquals(new Name("José Tan"), new Name("Jose\u0301 Tan"));
        assertEquals("José Tan", new Name("Jose\u0301 Tan").toString());
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
