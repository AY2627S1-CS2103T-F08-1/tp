package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PersonSort;

/**
 * Finds and lists all persons whose name contains a keyword or whose phone number exactly matches a keyword.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds debtors whose names contain any specified "
            + "keyword (case-insensitive, literal substring) or whose phone number exactly matches one.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice 91234567";

    public static final String MESSAGE_NO_DEBTORS_FOUND =
            "No debtors found matching the supplied name or phone keyword(s).";
    public static final String MESSAGE_SINGLE_DEBTOR_FOUND = "1 matching debtor found.";
    public static final String MESSAGE_MULTIPLE_DEBTORS_FOUND = "%1$d matching debtors found.";

    private final NameContainsKeywordsPredicate predicate;

    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updatePersonView(predicate, PersonSort.DEFAULT_COMPARATOR);
        return new CommandResult(getMessageForMatchCount(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the find-result message for the given number of matching debtors.
     */
    public static String getMessageForMatchCount(int matchCount) {
        assert matchCount >= 0;

        return switch (matchCount) {
            case 0 -> MESSAGE_NO_DEBTORS_FOUND;
            case 1 -> MESSAGE_SINGLE_DEBTOR_FOUND;
            default -> String.format(MESSAGE_MULTIPLE_DEBTORS_FOUND, matchCount);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
