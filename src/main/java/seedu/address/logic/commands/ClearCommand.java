package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All debtor records have been cleared from OSPS.";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Clears all debtor records.\n"
            + "Parameters: none\n"
            + "Example: " + COMMAND_WORD;

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        AddressBook clearedAddressBook = new AddressBook();
        clearedAddressBook.setNextDebtorId(model.getAddressBook().getNextDebtorId());
        model.setAddressBook(clearedAddressBook);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
