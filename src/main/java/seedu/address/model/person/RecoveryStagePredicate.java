package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Matches debtors by their manually assigned recovery stage, independently of their balance. */
public final class RecoveryStagePredicate implements Predicate<Person> {
    private final RecoveryStage stage;

    /** Creates a predicate for one canonical recovery stage. */
    public RecoveryStagePredicate(RecoveryStage stage) {
        this.stage = requireNonNull(stage);
    }

    @Override
    public boolean test(Person person) {
        return requireNonNull(person).getRecoveryStage() == stage;
    }

    public RecoveryStage getStage() {
        return stage;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof RecoveryStagePredicate otherPredicate
                && stage == otherPredicate.stage;
    }

    @Override
    public int hashCode() {
        return stage.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("stage", stage).toString();
    }
}
