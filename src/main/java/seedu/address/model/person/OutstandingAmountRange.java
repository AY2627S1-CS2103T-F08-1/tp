package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An inclusive range of outstanding amounts with at least one bound.
 */
public final class OutstandingAmountRange {

    public static final String MESSAGE_REQUIRES_BOUND =
            "Provide at least one amount bound using min/AMOUNT or max/AMOUNT.";
    public static final String MESSAGE_INVALID_ORDER =
            "The minimum outstanding amount must not exceed the maximum outstanding amount.";

    private final Optional<OutstandingAmount> minimum;
    private final Optional<OutstandingAmount> maximum;

    /**
     * Creates an inclusive range from the supplied optional bounds.
     *
     * @throws IllegalArgumentException if both bounds are absent or the minimum exceeds the maximum
     */
    public OutstandingAmountRange(Optional<OutstandingAmount> minimum,
                                  Optional<OutstandingAmount> maximum) {
        this.minimum = requireNonNull(minimum);
        this.maximum = requireNonNull(maximum);

        if (minimum.isEmpty() && maximum.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_REQUIRES_BOUND);
        }
        if (minimum.isPresent() && maximum.isPresent()
                && minimum.get().getValue().compareTo(maximum.get().getValue()) > 0) {
            throw new IllegalArgumentException(MESSAGE_INVALID_ORDER);
        }
    }

    /** Returns whether {@code amount} lies within this range, including its boundaries. */
    public boolean contains(OutstandingAmount amount) {
        requireNonNull(amount);
        boolean satisfiesMinimum = minimum
                .map(bound -> amount.getValue().compareTo(bound.getValue()) >= 0)
                .orElse(true);
        boolean satisfiesMaximum = maximum
                .map(bound -> amount.getValue().compareTo(bound.getValue()) <= 0)
                .orElse(true);
        return satisfiesMinimum && satisfiesMaximum;
    }

    /** Returns a concise description suitable for a command result. */
    public String toUserDescription() {
        if (minimum.isPresent() && maximum.isPresent()) {
            return "from S$" + minimum.get() + " to S$" + maximum.get();
        }
        if (minimum.isPresent()) {
            return "at least S$" + minimum.get();
        }
        return "at most S$" + maximum.orElseThrow();
    }

    public Optional<OutstandingAmount> getMinimum() {
        return minimum;
    }

    public Optional<OutstandingAmount> getMaximum() {
        return maximum;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OutstandingAmountRange otherRange)) {
            return false;
        }
        return minimum.equals(otherRange.minimum) && maximum.equals(otherRange.maximum);
    }

    @Override
    public int hashCode() {
        return minimum.hashCode() * 31 + maximum.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("minimum", minimum)
                .add("maximum", maximum)
                .toString();
    }
}
