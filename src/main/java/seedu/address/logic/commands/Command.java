package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether executing this command can change address-book data that must be saved.
     *
     * <p>The default is {@code true} so that new commands persist safely unless they explicitly declare
     * themselves read-only.
     */
    public boolean shouldSaveAddressBook() {
        return true;
    }

    /** Returns whether a successful save should create a new undo history entry. */
    public boolean shouldCommitAddressBook() {
        return shouldSaveAddressBook();
    }

    /** Returns whether the command navigates existing undo history. */
    public boolean isHistoryNavigation() {
        return false;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
