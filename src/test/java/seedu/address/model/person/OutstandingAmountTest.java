package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class OutstandingAmountTest {

    @Test
    public void constructor_validAmounts_createsNormalisedAmount() {
        assertEquals("0.00", new OutstandingAmount().toString());
        assertEquals("1,250.00", new OutstandingAmount("S$1,250.00").toString());
        assertEquals("10.50", new OutstandingAmount("$10.50").toString());
        assertEquals("42.00", new OutstandingAmount("42").toString());
    }

    @Test
    public void constructor_invalidAmounts_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, OutstandingAmount.MESSAGE_CONSTRAINTS,
                () -> new OutstandingAmount("-1.00"));
        assertThrows(IllegalArgumentException.class, OutstandingAmount.MESSAGE_CONSTRAINTS,
                () -> new OutstandingAmount("10.123"));
        assertThrows(IllegalArgumentException.class, OutstandingAmount.MESSAGE_CONSTRAINTS,
                () -> new OutstandingAmount("not an amount"));
    }

    @Test
    public void equals() {
        OutstandingAmount amount = new OutstandingAmount("10.00");

        assertTrue(amount.equals(amount));
        assertTrue(amount.equals(new OutstandingAmount("10")));
        assertFalse(amount.equals(new OutstandingAmount("20")));
        assertFalse(amount.equals("10.00"));
        assertFalse(amount.equals(null));
        assertEquals(amount.hashCode(), new OutstandingAmount("10.00").hashCode());
    }
}
