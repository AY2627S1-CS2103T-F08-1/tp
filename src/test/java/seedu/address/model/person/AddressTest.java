package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void isValidAddress_unicodeWhitespace_rejectsBlankAddresses() {
        for (String blank : new String[] {"\u2003", "\u00a0", "\u202f", "\u3000", " \t\u2003\u00a0"}) {
            assertFalse(Address.isValidAddress(blank));
            assertThrows(IllegalArgumentException.class, () -> new Address(blank));
        }
        assertTrue(Address.isValidAddress("Block\u2003123"));
        assertTrue(Address.isValidAddress("裕廊西大街 123"));
    }

    @Test
    public void isValidUserInput_invisibleFormattingCharacters_rejectsBlankAddresses() {
        for (String blank : new String[] {"\u200b", "\ufeff", "\u0000", "\u200b\ufeff"}) {
            assertTrue(Address.isValidAddress(blank));
            assertFalse(Address.isValidUserInput(blank));
            assertEquals(blank, new Address(blank).toString());
        }
        assertTrue(Address.isValidUserInput("Block\u200b123"));
    }

    @Test
    public void isValidUserInput_standaloneMarksAndFillers_rejectsBlankAddresses() {
        for (String blank : new String[] {"\u0000", "\u200b", "\u034f", "\u0903", "\u20dd",
            "\ufe0f", "\udb40\udd00", "\u115f", "\u1160", "\u2800", "\u3164", "\uffa0", "\u200b\t",
            "\u200b\u00a0"}) {
            assertFalse(Address.isValidUserInput(blank));
        }
        assertTrue(Address.isValidUserInput("Cafe\u0301 Road"));
        assertTrue(Address.isValidUserInput("地址\ufe0f"));
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
