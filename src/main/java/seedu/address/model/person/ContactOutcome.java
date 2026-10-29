package seedu.address.model.person;

import java.util.Locale;
import java.util.Optional;

/**
 * Describes the result of a recorded contact attempt.
 */
public enum ContactOutcome {
    REACHED("reached"),
    CALLBACK_REQUESTED("callback-requested"),
    UNREACHABLE("unreachable"),
    PAYMENT_PROMISED("payment-promised");

    public static final String MESSAGE_CONSTRAINTS = "Outcome must be one of: reached, callback-requested, "
            + "unreachable, payment-promised.";

    private final String token;

    ContactOutcome(String token) {
        this.token = token;
    }

    /**
     * Returns the outcome represented by {@code token}, ignoring ASCII letter case.
     */
    public static Optional<ContactOutcome> fromToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String normalizedToken = token.toLowerCase(Locale.ROOT);
        for (ContactOutcome outcome : values()) {
            if (outcome.token.equals(normalizedToken)) {
                return Optional.of(outcome);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return token;
    }
}
