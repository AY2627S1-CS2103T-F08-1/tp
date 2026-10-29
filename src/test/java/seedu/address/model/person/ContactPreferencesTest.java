package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ContactPreferencesTest {
    @Test
    public void equalityAndHashCode_distinguishBothFields() {
        ContactPreferences baseline = new ContactPreferences("call", "09:00-10:00");
        ContactPreferences same = new ContactPreferences("CALL", "09:00-10:00");
        assertEquals(baseline, same);
        assertEquals(baseline.hashCode(), same.hashCode());
        assertFalse(baseline.equals(new ContactPreferences("sms", "09:00-10:00")));
        assertFalse(baseline.equals(new ContactPreferences("call", "10:00-11:00")));
        assertFalse(baseline.equals(null));
        assertFalse(baseline.equals("call"));
        for (String method : new String[] {"call", "sms", "email", "letter"}) {
            assertEquals(method, new ContactPreferences(method, null).getMethod());
        }
    }

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
