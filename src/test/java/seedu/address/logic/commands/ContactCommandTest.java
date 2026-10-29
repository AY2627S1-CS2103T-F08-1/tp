package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ContactCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;

public class ContactCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final ContactCommandParser parser = new ContactCommandParser();

    @Test
    public void updatesAndClearsFieldsIndependently() throws Exception {
        Person original = model.getAddressBook().getPersonList().getFirst();
        int id = original.getDebtorId();
        parser.parse("--id " + id + " --method SMS --hours 09:00-17:30").execute(model);
        parser.parse("--id " + id + " --clear-method").execute(model);
        Person updated = model.getAddressBook().getPersonList().getFirst();
        assertEquals(new ContactPreferences(null, "09:00-17:30"), updated.getContactPreferences());
        assertEquals(original.getName(), updated.getName());
        assertEquals(id, updated.getDebtorId());
        parser.parse("--id " + id + " --method call").execute(model);
        parser.parse("--id " + id + " --clear-hours").execute(model);
        assertEquals(new ContactPreferences("call", null),
                model.getAddressBook().getPersonList().getFirst().getContactPreferences());
    }

    @Test
    public void rejectsMalformedOptionsAndHours() {
        for (String args : new String[] {"", "--method call", "--id", "--id --method call", "--id 1 --hours",
            "--id 1 --hours --clear-method", "--id 1 --clear-hours --hours 09:00-10:00",
            "--id 1", "--id 0 --method call", "--id 999999999999 --method call",
            "--id 1 --method call --method sms", "--id 1 --method call --clear-method",
            "--id 1 --hours 12:00-12:00", "--id 1 --hours 23:00-01:00",
            "--id 1 --hours 00:00-24:00", "--id 1 --method fax", "--id 1 --extra x"}) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }

    @Test
    public void missingDebtorDoesNotMutate() throws Exception {
        Person before = model.getAddressBook().getPersonList().getFirst();
        ContactCommand command = parser.parse("--id 999999 --method email");
        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(before, model.getAddressBook().getPersonList().getFirst());
    }
}
