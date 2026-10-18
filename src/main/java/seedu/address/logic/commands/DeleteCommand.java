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
            + ": Deletes the debtor identified by the index number used in the displayed debtor list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted debtor: %1$s";

    private static final String MAX_INDEX = Integer.toString(Integer.MAX_VALUE);

    private static final int MAX_DISPLAYED_INDEX_DIGITS = 100;
    private static final int INDEX_PREVIEW_DIGITS = 20;

    private final String targetIndex;

    public DeleteCommand(Index targetIndex) {
        this(Integer.toString(requireNonNull(targetIndex).getOneBased()));
    }

    private DeleteCommand(String targetIndex) {
        requireNonNull(targetIndex);
        if (!targetIndex.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException("Target index must be positive.");
        }
        this.targetIndex = targetIndex;
    }

    /**
     * Creates a command from a positive one-based index that may be larger than an {@code int}.
     */
    public static DeleteCommand fromOneBased(BigInteger targetIndex) {
        return new DeleteCommand(targetIndex.toString());
    }

    /**
     * Creates a command from a normalized positive decimal index without numeric conversion.
     */
    public static DeleteCommand fromOneBasedDecimal(String targetIndex) {
        return new DeleteCommand(targetIndex);
    }

    private boolean exceeds(String limit) {
        return targetIndex.length() > limit.length()
                || (targetIndex.length() == limit.length() && targetIndex.compareTo(limit) > 0);
    }

    private String displayIndex() {
        return targetIndex.length() <= MAX_DISPLAYED_INDEX_DIGITS ? targetIndex
                : targetIndex.substring(0, INDEX_PREVIEW_DIGITS) + "... (" + targetIndex.length() + " digits)";
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (exceeds(Integer.toString(lastShownList.size()))) {
            throw new CommandException(Messages.getInvalidPersonDisplayedIndexMessage(displayIndex(),
                    lastShownList.size()));
        }

        Person personToDelete = lastShownList.get(Integer.parseInt(targetIndex) - 1);
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
                .add("targetIndex", !exceeds(MAX_INDEX)
                        ? Index.fromOneBased(Integer.parseInt(targetIndex)) : displayIndex())
                .toString();
    }
}
