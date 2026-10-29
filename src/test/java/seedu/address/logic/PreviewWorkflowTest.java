package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class PreviewWorkflowTest {
    @TempDir
    public Path directory;

    @Test
    public void preview_sortedAndFilteredView_doesNotMutateOrSave() throws Exception {
        Model model = new ModelManager();
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/Alice p/91111111 e/alice@example.com a/Main Street");
        logic.execute("add n/Bob p/92222222 e/bob@example.com a/Main Street");
        logic.execute("sort --by name --order desc");
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
        int saves = storage.saves;
        String edit = logic.execute("preview edit 1 n/Bob Updated").getFeedbackToUser();
        assertTrue(edit.contains("Would edit debtor:"));
        assertTrue(edit.contains("Bob Updated"));
        assertTrue(edit.contains("Current debtor: ID: 2; Bob"));
        assertTrue(logic.execute("preview edit 1 n/Bob").getFeedbackToUser().contains("No data would change"));
        assertEquals(before, model.getAddressBook().getPersonList());
        assertEquals("Bob", model.getFilteredPersonList().getFirst().getName().toString());
        assertEquals(saves, storage.saves);
        String delete = logic.execute("preview delete 1").getFeedbackToUser();
        assertTrue(delete.contains("Would delete debtor:"));
        assertTrue(delete.contains("Bob"));
        assertEquals(before, model.getAddressBook().getPersonList());
        assertEquals(saves, storage.saves);
        assertThrows(CommandException.class, () -> logic.execute("preview delete 99"));
        assertThrows(ParseException.class, () -> logic.execute("preview clear"));
        assertThrows(ParseException.class, () -> logic.execute("preview add n/Alice"));
        assertThrows(IllegalArgumentException.class, () ->
                new seedu.address.logic.commands.PreviewCommand(new seedu.address.logic.commands.ListCommand()));
        assertThrows(ParseException.class, () -> logic.execute("preview edit"));
        assertEquals(saves, storage.saves);
    }

    private static class CountingStorage extends JsonAddressBookStorage {
        private int saves;

        CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            saves++;
            super.saveAddressBook(book);
        }
    }
}
