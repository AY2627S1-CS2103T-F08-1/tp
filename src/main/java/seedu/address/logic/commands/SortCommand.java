package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonSort;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;

/**
 * Sorts the currently displayed debtor worklist without changing the stored debtor order or active filter.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the currently displayed debtors.\n"
            + "Parameters: --by id|name|amount [--order asc|desc]\n"
            + "Example: " + COMMAND_WORD + " --by amount --order desc";

    public static final String MESSAGE_SUCCESS =
            "Sorted %1$d displayed debtor(s) by %2$s in %3$s order.";

    private final Field field;
    private final Direction direction;

    /** Creates a command that sorts by {@code field} in {@code direction}. */
    public SortCommand(Field field, Direction direction) {
        this.field = requireNonNull(field);
        this.direction = requireNonNull(direction);
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateSortedPersonList(PersonSort.createComparator(field, direction));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getFilteredPersonList().size(), field.getToken(), direction.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }
        return field == otherSortCommand.field && direction == otherSortCommand.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, direction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .add("direction", direction)
                .toString();
    }
}
