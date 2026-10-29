package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ContactPreferencesTest {
    @Test
    public void validatesBoundariesAndCanonicalisesMethod() {
        assertEquals("sms", new ContactPreferences("SMS", "00:00-23:59").getMethod());
        assertTrue(ContactPreferences.isValidHours("00:00-23:59"));
        for (String hours : new String[] {"9:00-10:00", "10:00-10:00", "23:59-00:00", "00:00-24:00"}) {
            assertFalse(ContactPreferences.isValidHours(hours));
            assertThrows(IllegalArgumentException.class, () -> new ContactPreferences(null, hours));
        }
    }
}
