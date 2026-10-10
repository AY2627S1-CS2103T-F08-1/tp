package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Shows the complete profile of the debtor identified by persistent debtor ID.
 */
public class ShowCommand extends Command {

    public static final String COMMAND_WORD = "show";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the complete profile of the debtor identified by debtor ID.\n"
            + "Parameters: --id DEBTOR_ID\n"
            + "Example: " + COMMAND_WORD + " --id 3";

    public static final String MESSAGE_DEBTOR_NOT_FOUND =
            "No active debtor with ID %1$d exists. It may have been deleted.";

    private final int debtorId;

    /**
     * Creates a command to show the debtor identified by {@code debtorId}.
     */
    public ShowCommand(int debtorId) {
        this.debtorId = debtorId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Optional<Person> debtor = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst();
        if (debtor.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_DEBTOR_NOT_FOUND, debtorId));
        }

        return new CommandResult(Messages.formatProfile(debtor.get()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ShowCommand otherShowCommand)) {
            return false;
        }
        return debtorId == otherShowCommand.debtorId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("debtorId", debtorId)
                .toString();
    }
}
