package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ContactOutcomeTest {

    @Test
    public void fromToken_validTokens_returnsMatchingOutcomes() {
        assertEquals(Optional.of(ContactOutcome.REACHED), ContactOutcome.fromToken("reached"));
        assertEquals(Optional.of(ContactOutcome.CALLBACK_REQUESTED),
                ContactOutcome.fromToken("CALLBACK-REQUESTED"));
        assertEquals(Optional.of(ContactOutcome.UNREACHABLE), ContactOutcome.fromToken("Unreachable"));
        assertEquals(Optional.of(ContactOutcome.PAYMENT_PROMISED),
                ContactOutcome.fromToken("payment-promised"));
    }

    @Test
    public void fromToken_invalidToken_returnsEmptyOptional() {
        assertTrue(ContactOutcome.fromToken(null).isEmpty());
        assertTrue(ContactOutcome.fromToken("").isEmpty());
        assertTrue(ContactOutcome.fromToken("answered").isEmpty());
    }
}
