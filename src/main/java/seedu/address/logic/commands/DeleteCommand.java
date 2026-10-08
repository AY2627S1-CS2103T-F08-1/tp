package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    private static final BigInteger MAX_INDEX = BigInteger.valueOf(Integer.MAX_VALUE);

    private final BigInteger targetIndex;

    public DeleteCommand(Index targetIndex) {
        this(BigInteger.valueOf(requireNonNull(targetIndex).getOneBased()));
    }

    private DeleteCommand(BigInteger targetIndex) {
        requireNonNull(targetIndex);
        if (targetIndex.signum() <= 0) {
            throw new IllegalArgumentException("Target index must be positive.");
        }
        this.targetIndex = targetIndex;
    }

    /**
     * Creates a command from a positive one-based index that may be larger than an {@code int}.
     */
    public static DeleteCommand fromOneBased(BigInteger targetIndex) {
        return new DeleteCommand(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.compareTo(BigInteger.valueOf(lastShownList.size())) > 0) {
            throw new CommandException(Messages.getInvalidPersonDisplayedIndexMessage(lastShownList.size()));
        }

        Person personToDelete = lastShownList.get(targetIndex.intValueExact() - 1);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex.compareTo(MAX_INDEX) <= 0
                        ? Index.fromOneBased(targetIndex.intValueExact()) : targetIndex)
                .toString();
    }
}
