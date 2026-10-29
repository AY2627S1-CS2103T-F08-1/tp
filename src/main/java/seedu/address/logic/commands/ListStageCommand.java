package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonSort;
import seedu.address.model.person.RecoveryStage;
import seedu.address.model.person.RecoveryStagePredicate;

/** Replaces the worklist with debtors in one recovery stage, ordered by persistent ID. */
public final class ListStageCommand extends Command {
    public static final String COMMAND_WORD = "list-stage";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists debtors in one recovery stage.\nParameters: STAGE\n"
            + RecoveryStage.MESSAGE_CONSTRAINTS + "\nExample: " + COMMAND_WORD + " contacted";
    public static final String MESSAGE_NO_MATCHES =
            "No debtors are in recovery stage %s. Use list to see all debtors.";
    public static final String MESSAGE_ONE_MATCH = "Listed 1 debtor in recovery stage %s.";
    public static final String MESSAGE_MULTIPLE_MATCHES = "Listed %d debtors in recovery stage %s.";

    private final RecoveryStagePredicate predicate;

    /** Creates a read-only worklist command for {@code stage}. */
    public ListStageCommand(RecoveryStage stage) {
        predicate = new RecoveryStagePredicate(stage);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updatePersonView(predicate, PersonSort.DEFAULT_COMPARATOR);
        int count = model.getFilteredPersonList().size();
        RecoveryStage stage = predicate.getStage();
        String feedback = switch (count) {
            case 0 -> String.format(MESSAGE_NO_MATCHES, stage);
            case 1 -> String.format(MESSAGE_ONE_MATCH, stage);
            default -> String.format(MESSAGE_MULTIPLE_MATCHES, count, stage);
        };
        return new CommandResult(feedback);
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ListStageCommand otherCommand
                && predicate.equals(otherCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
