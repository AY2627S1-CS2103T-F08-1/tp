package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmountInRangePredicate;
import seedu.address.model.person.OutstandingAmountRange;
import seedu.address.model.person.PersonSort;

/** Lists debtors whose outstanding amounts lie within an inclusive range. */
public final class ListAmountCommand extends Command {

    public static final String COMMAND_WORD = "list-amount";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists debtors within an inclusive outstanding-amount range.\n"
            + "Provide at least one bound.\n"
            + "Parameters: [min/AMOUNT] [max/AMOUNT]\n"
            + "Example: " + COMMAND_WORD + " min/250 max/2,000.00";

    public static final String MESSAGE_NO_MATCHES =
            "No debtors have an outstanding amount %s.";
    public static final String MESSAGE_ONE_MATCH =
            "1 debtor has an outstanding amount %s.";
    public static final String MESSAGE_MULTIPLE_MATCHES =
            "%d debtors have outstanding amounts %s.";
    public static final String MESSAGE_UNEXPECTED_ARGUMENTS =
            "Unexpected argument(s). Use only min/AMOUNT and max/AMOUNT.";

    private final OutstandingAmountInRangePredicate predicate;

    public ListAmountCommand(OutstandingAmountRange range) {
        predicate = new OutstandingAmountInRangePredicate(requireNonNull(range));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updatePersonView(predicate, PersonSort.DEFAULT_COMPARATOR);
        int matchCount = model.getFilteredPersonList().size();
        return new CommandResult(getResultMessage(matchCount, predicate.getRange()));
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    /** Returns the result message for {@code matchCount} and {@code range}. */
    public static String getResultMessage(int matchCount, OutstandingAmountRange range) {
        assert matchCount >= 0;
        requireNonNull(range);
        String rangeDescription = range.toUserDescription();
        return switch (matchCount) {
            case 0 -> String.format(MESSAGE_NO_MATCHES, rangeDescription);
            case 1 -> String.format(MESSAGE_ONE_MATCH, rangeDescription);
            default -> String.format(MESSAGE_MULTIPLE_MATCHES, matchCount, rangeDescription);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof ListAmountCommand otherCommand
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
