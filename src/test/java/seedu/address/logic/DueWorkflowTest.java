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

public class DueWorkflowTest {
    @TempDir
    public Path directory;

    @Test
    public void due_sortedViewControlsDisplayedIndexAndDoesNotSave() throws Exception {
        Model model = new ModelManager();
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/Alice p/91111111 e/alice@example.com a/Main Street");
        logic.execute("add n/Bob p/92222222 e/bob@example.com a/Main Street");
        logic.execute("followup --id 1 --date 2026-11-03");
        logic.execute("followup --id 2 --date 2026-10-30");
        int saves = storage.saves;
        logic.execute("due 2026-11-03");
        assertEquals(saves, storage.saves);
        assertEquals(List.of(2, 1), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
        assertThrows(ParseException.class, () -> logic.execute("due --from 2026-11-04 --to 2026-11-03"));
        assertEquals(List.of(2, 1), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
        logic.execute("delete 1");
        assertEquals(List.of(1), model.getAddressBook().getPersonList().stream().map(Person::getDebtorId).toList());
        logic.execute("list");
        assertEquals(1, model.getFilteredPersonList().getFirst().getDebtorId());
    }

    @Test
    public void due_afterFailedSave_doesNotRetryWrite() throws Exception {
        Model model = new ModelManager();
        CountingStorage storage = new CountingStorage(directory.resolve("book.json"));
        Logic logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        storage.fail = true;
        assertThrows(CommandException.class, () ->
                logic.execute("add n/Alice p/91111111 e/alice@example.com a/Main Street"));
        storage.fail = false;
        logic.execute("due 2026-11-03");
        assertEquals(1, storage.saves);
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("exit");
        assertEquals(2, storage.saves);
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
}
