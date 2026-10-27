package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Integration tests for debtor-profile behavior across commands and persistence.
 */
public class ProfileWorkflowIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path addressBookPath;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        addressBookPath = temporaryFolder.resolve("addressBook.json");
        logic = createLogic(model, new JsonAddressBookStorage(addressBookPath));
    }

    @Test
    public void execute_crossFeatureProfileWorkflow_alwaysTargetsPersistentId() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street o/S$100.00");
        logic.execute("add n/Bob Lim p/92345678 e/bob@example.com a/2 Main Street o/S$200.00");
        Person alice = model.getAddressBook().getPersonList().getFirst();
        Person bob = model.getAddressBook().getPersonList().getLast();

        logic.execute("find Bob");
        assertEquals(List.of(bob), logic.getFilteredPersonList());
        String filteredProfile = logic.execute("show --id " + alice.getDebtorId()).getFeedbackToUser();
        assertTrue(filteredProfile.contains("Name: Alice Tan"));
        assertEquals(List.of(bob), logic.getFilteredPersonList());

        logic.execute("list");
        logic.execute("edit 1 n/Alice Updated o/S$75.00");
        String editedProfile = logic.execute("show --id " + alice.getDebtorId()).getFeedbackToUser();
        assertTrue(editedProfile.contains("Name: Alice Updated"));
        assertTrue(editedProfile.contains("Outstanding: S$75.00"));

        logic.execute("note --id " + alice.getDebtorId() + " --text Called debtor; payment promised.");
        String profileWithNote = logic.execute("show --id " + alice.getDebtorId()).getFeedbackToUser();
        assertTrue(profileWithNote.contains("Called debtor; payment promised."));

        logic.execute("delete 1");
        String expectedMessage = String.format(ShowCommand.MESSAGE_DEBTOR_NOT_FOUND, alice.getDebtorId());
        CommandException exception = assertThrows(CommandException.class, (
        ) -> logic.execute("show --id " + alice.getDebtorId()));
        assertEquals(expectedMessage, exception.getMessage());

        String remainingProfile = logic.execute("show --id " + bob.getDebtorId()).getFeedbackToUser();
        assertTrue(remainingProfile.contains("Name: Bob Lim"));

        ReadOnlyAddressBook reloadedAddressBook = new JsonAddressBookStorage(addressBookPath)
                .readAddressBook().orElseThrow();
        assertEquals(List.of(bob), reloadedAddressBook.getPersonList());
    }

    @Test
    public void execute_invalidShowTargets_doNotChangeModel() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street");
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());

        assertThrows(ParseException.class, () -> logic.execute("show --id 0"));
        assertThrows(ParseException.class, () -> logic.execute("show --id -1"));
        assertThrows(CommandException.class, () -> logic.execute("show --id 999999"));

        assertEquals(originalPersons, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_longProfileContent_preservedAcrossSaveAndReload() throws Exception {
        String longName = ("Alexandra Very Long Debtor Name ").repeat(20).trim();
        String longAddress = ("123 Long Address Building, ").repeat(20) + "Singapore";
        String longNote = ("Discussed payment arrangements and documented the agreed follow-up. ").repeat(100).trim();
        Person debtor = model.addPerson(new PersonBuilder()
                .withName(longName)
                .withAddress(longAddress)
                .build());

        logic.execute("note --id " + debtor.getDebtorId() + " --text " + longNote);
        String profile = logic.execute("show --id " + debtor.getDebtorId()).getFeedbackToUser();

        assertTrue(profile.contains(longName));
        assertTrue(profile.contains(longAddress));
        assertTrue(profile.contains(longNote));

        ReadOnlyAddressBook reloadedAddressBook = new JsonAddressBookStorage(addressBookPath)
                .readAddressBook().orElseThrow();
        Person reloadedDebtor = reloadedAddressBook.getPersonList().getFirst();
        assertEquals(longName, reloadedDebtor.getName().toString());
        assertEquals(longAddress, reloadedDebtor.getAddress().toString());
        assertEquals(longNote, reloadedDebtor.getInteractionNotes().getFirst().getText());
    }

    private Logic createLogic(Model testModel, JsonAddressBookStorage addressBookStorage) {
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        return new LogicManager(testModel, new StorageManager(addressBookStorage, userPrefsStorage));
    }
}
