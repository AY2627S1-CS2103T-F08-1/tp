package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;

/** Updates contact preferences for a persistent debtor ID. */
public class ContactCommand extends Command {
    public static final String COMMAND_WORD = "contact";
    public static final String MESSAGE_USAGE = "contact --id ID [--method METHOD | --clear-method] "
            + "[--hours HH:mm-HH:mm | --clear-hours]";
    private final int id;
    private final boolean changeMethod;
    private final String method;
    private final boolean changeHours;
    private final String hours;
    // The first call classifies this as persistence-bound before execution. execute() then records whether it mutated.
    private boolean changed = true;

    /** Creates a command for the requested preference changes. */
    public ContactCommand(int id, boolean changeMethod, String method, boolean changeHours, String hours) {
        this.id = id;
        this.changeMethod = changeMethod;
        this.method = method;
        this.changeHours = changeHours;
        this.hours = hours;
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return changed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        changed = false;
        Optional<Person> match = model.findPersonById(id);
        if (match.isEmpty()) {
            throw new CommandException(String.format("No debtor with ID %d exists.", id));
        }
        Person target = match.get();
        ContactPreferences old = target.getContactPreferences();
        ContactPreferences updated = new ContactPreferences(changeMethod ? method : old.getMethod(),
                changeHours ? hours : old.getHours());
        if (old.equals(updated)) {
            return new CommandResult(String.format("Contact preferences for debtor ID %d are unchanged.", id));
        }
        model.setPerson(target, target.withContactPreferences(updated));
        changed = true;
        return new CommandResult(String.format("Updated contact preferences for debtor ID %d.", id));
    }
}
