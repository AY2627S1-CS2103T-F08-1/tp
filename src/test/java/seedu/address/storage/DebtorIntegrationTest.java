package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class DebtorIntegrationTest {
    @TempDir
    public Path testFolder;

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void edit_saveAndRestart_preservesDebtorData() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street o/1250.50").execute(model);
        parser.parseCommand("note --id 1 --text Promised payment on Friday.").execute(model);
        Person original = model.getFilteredPersonList().getFirst();
        parser.parseCommand("edit 1 n/Alice Tan p/92345678").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("debtors.json"));
        storage.saveAddressBook(model.getAddressBook());
        Person restored = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("Alice Tan", restored.getName().fullName);
        assertEquals("92345678", restored.getPhone().value);
        assertEquals(original.getDebtorId(), restored.getDebtorId());
        assertEquals(original.getOutstandingAmount(), restored.getOutstandingAmount());
        assertEquals(original.getInteractionNotes(), restored.getInteractionNotes());
    }

    @Test
    public void deleteOnlyDebtor_saveAndRestart_doesNotReuseId() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street").execute(model);
        parser.parseCommand("delete 1").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("empty.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        parser.parseCommand("add n/Bob p/92345678 e/bob@example.com a/456 Street o/0").execute(restored);
        assertEquals(2, restored.getFilteredPersonList().getFirst().getDebtorId());
    }

    @Test
    public void duplicateContactWithWhitespace_rejectedWithoutChangingData() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street").execute(model);
        parser.parseCommand("add n/Bob p/92345678 e/bob@example.com a/456 Street").execute(model);
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        for (String command : List.of(
                "add n/Carol p/ 91234567  e/carol@example.com a/789 Street",
                "edit 2 e/ alice@example.com  ", "edit 2 p/ 91234567  ")) {
            assertThrows(CommandException.class, () -> parser.parseCommand(command).execute(model));
            assertEquals(original, model.getAddressBook().getPersonList());
            assertEquals(3, model.getAddressBook().getNextDebtorId());
        }
    }

    @Test
    public void add_emailCaseAndCountryCode_areComparedLiterally() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street").execute(model);
        parser.parseCommand("add n/Another Alice p/6591234567 e/Alice@example.com a/456 Street").execute(model);
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

}
