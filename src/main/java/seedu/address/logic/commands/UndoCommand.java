package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Restores the previous saved address-book state for this session. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_SUCCESS = "Undid the most recent change.";
    public static final String MESSAGE_FAILURE = "There is no change to undo.";

    @Override
    public boolean shouldCommitAddressBook() {
        return false;
    }

    @Override
    public boolean isHistoryNavigation() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        if (!model.canUndoAddressBook()) {
            throw new CommandException(MESSAGE_FAILURE);
        }
        model.undoAddressBook();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
