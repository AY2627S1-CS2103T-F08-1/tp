package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all debtors in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all debtors.\n"
            + "Parameters: none\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_EMPTY_LIST =
            "No active debtors found (0 active). Add a debtor with the add command.";
    public static final String MESSAGE_SINGLE_DEBTOR = "1 active debtor listed.";
    public static final String MESSAGE_MULTIPLE_DEBTORS = "%1$d active debtors listed.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int debtorCount = model.getFilteredPersonList().size();
        return new CommandResult(getMessageForDebtorCount(debtorCount));
    }

    /**
     * Returns the list-result message for the given number of debtors.
     */
    public static String getMessageForDebtorCount(int debtorCount) {
        assert debtorCount >= 0;

        return switch (debtorCount) {
            case 0 -> MESSAGE_EMPTY_LIST;
            case 1 -> MESSAGE_SINGLE_DEBTOR;
            default -> String.format(MESSAGE_MULTIPLE_DEBTORS, debtorCount);
        };
    }
}
