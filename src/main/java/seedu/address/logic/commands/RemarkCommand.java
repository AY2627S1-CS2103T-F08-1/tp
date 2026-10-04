package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/** Changes the remark of an existing person. */
public class RemarkCommand extends Command {
    public static final String COMMAND_WORD = "remark";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a person's remark.\n"
            + "Parameters: INDEX [" + CliSyntax.PREFIX_REMARK + "REMARK]";
    public static final String MESSAGE_ADD_REMARK_SUCCESS = "Added remark to Person: %1$s";
    public static final String MESSAGE_DELETE_REMARK_SUCCESS = "Removed remark from Person: %1$s";

    private final Index index;
    private final Remark remark;

    /**
     * Creates a command to replace the remark of the person at {@code index}.
     *
     * @param index index of the person in the filtered list
     * @param remark replacement remark
     */
    public RemarkCommand(Index index, Remark remark) {
        this.index = requireNonNull(index);
        this.remark = requireNonNull(remark);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = persons.get(index.getZeroBased());
        Person edited = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getAddress(),
                original.getDebtorId(), original.getOutstandingAmount(), remark, original.getTags());
        model.setPerson(original, edited);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String message = remark.value.isEmpty() ? MESSAGE_DELETE_REMARK_SUCCESS : MESSAGE_ADD_REMARK_SUCCESS;
        return new CommandResult(String.format(message, Messages.format(edited)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RemarkCommand command
                && index.equals(command.index) && remark.equals(command.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, remark);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("remark", remark).toString();
    }
}
