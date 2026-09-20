package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents the amount currently owed by a debtor.
 * The value is non-negative and is stored to two decimal places.
 */
public final class OutstandingAmount {

    public static final String MESSAGE_CONSTRAINTS =
            "Amount must be a positive number with at most two decimal places.";

    private final BigDecimal value;

    /** Creates an amount from a decimal value. */
    public OutstandingAmount(String amount) {
        requireNonNull(amount);
        String trimmedAmount = amount.trim().replace(",", "");
        if (trimmedAmount.startsWith("S$")) {
            trimmedAmount = trimmedAmount.substring(2);
        } else if (trimmedAmount.startsWith("$")) {
            trimmedAmount = trimmedAmount.substring(1);
        }
        try {
            BigDecimal parsed = new BigDecimal(trimmedAmount);
            if (parsed.signum() <= 0 || parsed.scale() > 2) {
                throw new NumberFormatException();
            }
            value = parsed.setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, exception);
        }
    }

    /** Creates a zero amount. */
    public OutstandingAmount() {
        value = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OutstandingAmount otherAmount)) {
            return false;
        }
        return value.compareTo(otherAmount.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }
}
