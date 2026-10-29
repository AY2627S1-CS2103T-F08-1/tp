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

    /** Creates a command for the requested preference changes. */
    public ContactCommand(int id, boolean changeMethod, String method, boolean changeHours, String hours) {
        this.id = id;
        this.changeMethod = changeMethod;
        this.method = method;
        this.changeHours = changeHours;
        this.hours = hours;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Optional<Person> match = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == id).findFirst();
        if (match.isEmpty()) {
            throw new CommandException(String.format("No debtor with ID %d exists.", id));
        }
        Person target = match.get();
        ContactPreferences old = target.getContactPreferences();
        ContactPreferences updated = new ContactPreferences(changeMethod ? method : old.getMethod(),
                changeHours ? hours : old.getHours());
        model.setPerson(target, target.withContactPreferences(updated));
        return new CommandResult(String.format("Updated contact preferences for debtor ID %d.", id));
    }
}
