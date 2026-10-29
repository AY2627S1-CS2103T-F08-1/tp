package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Restores the next undone address-book state for this session. */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_SUCCESS = "Redid the most recently undone change.";
    public static final String MESSAGE_FAILURE = "There is no change to redo.";

    @Override
    public boolean shouldCommitAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        if (!model.canRedoAddressBook()) {
            throw new CommandException(MESSAGE_FAILURE);
        }
        model.redoAddressBook();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
