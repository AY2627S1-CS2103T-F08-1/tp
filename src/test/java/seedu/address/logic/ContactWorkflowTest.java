package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ContactWorkflowTest {
    @TempDir
    public Path directory;

    @Test
    public void contactable_readOnlyViewDoesNotSaveAfterFailedMutation() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        storage.fail = true;
        assertThrows(CommandException.class, () ->
                logic.execute("contact --id " + debtor.getDebtorId() + " --hours 08:00-18:00"));
        storage.fail = false;
        logic.execute("contactable --at 09:00");
        assertEquals(1, storage.saves);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertThrows(ParseException.class, () -> logic.execute("contactable --at 24:00"));
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("list");
        assertEquals(1, storage.saves);
    }

    @Test
    public void contact_unchangedValues_doNotReplaceOrSave() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));

        logic.execute("contact --id " + debtor.getDebtorId() + " --clear-method --clear-hours");
        assertSame(debtor, model.getAddressBook().getPersonList().getFirst());
        assertEquals(0, storage.saves);
        assertTrue(Files.notExists(directory.resolve("book.json")));

        logic.execute("contact --id " + debtor.getDebtorId() + " --method sms");
        Person updated = model.getAddressBook().getPersonList().getFirst();
        assertEquals(1, storage.saves);
        logic.execute("contact --id " + debtor.getDebtorId() + " --method SMS --clear-hours");
        assertSame(updated, model.getAddressBook().getPersonList().getFirst());
        assertEquals(1, storage.saves);
    }

    @Test
    public void contact_noOpAfterFailedSave_leavesNoPendingWriteToRetry() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        storage.fail = true;
        assertThrows(CommandException.class, () ->
                logic.execute("contact --id " + debtor.getDebtorId() + " --method call"));
        Person updated = model.getAddressBook().getPersonList().getFirst();
        assertEquals(1, storage.saves);
        storage.fail = false;
        logic.execute("contact --id " + debtor.getDebtorId() + " --clear-hours");
        assertSame(updated, model.getAddressBook().getPersonList().getFirst());
        assertEquals(1, storage.saves);
        assertTrue(Files.notExists(directory.resolve("book.json")));
        logic.execute("exit");
        assertEquals(1, storage.saves);
    }

    private static class CountingStorage extends JsonAddressBookStorage {
        private int saves;
        private boolean fail;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            saves++;
            if (fail) {
                throw new IOException("simulated failure");
            }
            super.saveAddressBook(book);
        }
    }

    @Test
    public void contact_editNoteAndReload_preservesPreferences() throws Exception {
        Path data = directory.resolve("book.json");
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(data);
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street");
        logic.execute("add n/Bob Lim p/92345678 e/bob@example.com a/2 Main Street");
        logic.execute("find Bob");
        logic.execute("contact --id 1 --method email --hours 08:00-17:00");
        assertTrue(logic.execute("show --id 1").getFeedbackToUser().contains("Preferred contact method: email"));
        assertEquals("Bob Lim", logic.getFilteredPersonList().getFirst().getName().toString());
        logic.execute("list");
        logic.execute("edit 1 p/93345678");
        logic.execute("note --id 1 --text Called about account.");
        assertThrows(ParseException.class, () -> logic.execute("contact --id 1 --hours 17:00-08:00"));
        Person reloaded = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(new ContactPreferences("email", "08:00-17:00"), reloaded.getContactPreferences());
        assertEquals("93345678", reloaded.getPhone().toString());
        assertEquals("Called about account.", reloaded.getInteractionNotes().getFirst().getText());
    }
}
