package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a person's outstanding amount lies within an inclusive range. */
public final class OutstandingAmountInRangePredicate implements Predicate<Person> {
    private final OutstandingAmountRange range;

    public OutstandingAmountInRangePredicate(OutstandingAmountRange range) {
        this.range = requireNonNull(range);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return range.contains(person.getOutstandingAmount());
    }

    public OutstandingAmountRange getRange() {
        return range;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof OutstandingAmountInRangePredicate otherPredicate
                && range.equals(otherPredicate.range);
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("range", range).toString();
    }
}
