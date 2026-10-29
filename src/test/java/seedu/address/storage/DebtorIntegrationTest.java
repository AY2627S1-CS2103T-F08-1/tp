package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;

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
    public void editAndNote_saveAndRestart_preservesRecoveryStage() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street o/1250.50").execute(model);
        parser.parseCommand("stage --id 1 --value contacted").execute(model);
        parser.parseCommand("followup --id 1 --date 2026-11-03").execute(model);
        parser.parseCommand("contact --id 1 --method sms --hours 09:00-17:30").execute(model);
        parser.parseCommand("note --id 1 --text Promised payment on Friday.").execute(model);
        parser.parseCommand("edit 1 n/Alice Tan p/92345678").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("debtors.json"));
        storage.saveAddressBook(model.getAddressBook());
        Person restored = storage.readAddressBook().orElseThrow().getPersonList().getFirst();

        assertEquals(RecoveryStage.CONTACTED, restored.getRecoveryStage());
        assertEquals("Alice Tan", restored.getName().fullName);
        assertEquals(1, restored.getInteractionNotes().size());
        assertEquals("Promised payment on Friday.", restored.getInteractionNotes().getFirst().getText());
        assertEquals("2026-11-03", restored.getFollowUpDate().orElseThrow().toString());
        assertEquals(1, restored.getFollowUpHistory().size());
        assertEquals("sms", restored.getContactPreferences().getMethod());
        assertEquals("09:00-17:30", restored.getContactPreferences().getHours());
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

    @Test
    public void followUpDoneOnStoredDateWithoutHistory_saveAndRestart_remainsReadable() throws Exception {
        Person restored = followUpOnStoredDateWithoutHistory("followup --id 1 --done");
        assertTrue(restored.getFollowUpDate().isEmpty());
        assertEquals(1, restored.getFollowUpHistory().size());
        assertEquals(LocalDate.of(2026, 11, 3), restored.getFollowUpHistory().getFirst().getPreviousDate().get());
    }

    @Test
    public void followUpClearOnStoredDateWithoutHistory_saveAndRestart_remainsReadable() throws Exception {
        Person restored = followUpOnStoredDateWithoutHistory("followup --id 1 --clear");
        assertTrue(restored.getFollowUpDate().isEmpty());
        assertEquals(1, restored.getFollowUpHistory().size());
    }

    @Test
    public void followUpRescheduleOnStoredDateWithoutHistory_saveAndRestart_remainsReadable() throws Exception {
        Person restored = followUpOnStoredDateWithoutHistory("followup --id 1 --date 2026-11-09");
        assertEquals(LocalDate.of(2026, 11, 9), restored.getFollowUpDate().get());
        assertEquals(1, restored.getFollowUpHistory().size());
    }

    @Test
    public void followUpSameDateOnStoredDateWithoutHistory_saveAndRestart_addsNoHistory() throws Exception {
        Person restored = followUpOnStoredDateWithoutHistory("followup --id 1 --date 2026-11-03");
        assertEquals(LocalDate.of(2026, 11, 3), restored.getFollowUpDate().get());
        assertTrue(restored.getFollowUpHistory().isEmpty());
    }

    /** Loads an accepted record with a pending date and no history, runs a command, saves and reloads the file. */
    private Person followUpOnStoredDateWithoutHistory(String command) throws Exception {
        Path file = testFolder.resolve("stored.json");
        Files.writeString(file, "{\"persons\":[{\"name\":\"Alice Tan\",\"phone\":\"81234567\","
                + "\"email\":\"alice@example.com\",\"address\":\"1 Main Street\",\"debtorId\":1,"
                + "\"outstandingAmount\":\"50.00\",\"followUpDate\":\"2026-11-03\",\"followUpHistory\":[],"
                + "\"tags\":[]}],\"nextDebtorId\":2}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Model model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        parser.parseCommand(command).execute(model);
        storage.saveAddressBook(model.getAddressBook());
        return storage.readAddressBook().orElseThrow().getPersonList().getFirst();
    }

}
