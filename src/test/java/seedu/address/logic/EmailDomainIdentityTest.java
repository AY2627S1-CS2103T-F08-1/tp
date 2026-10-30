package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Covers contact identity without changing the original persisted email spelling. */
public class EmailDomainIdentityTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_domainCaseDuplicate_rejectsAddAndFilteredEditWithoutMutation() throws Exception {
        ModelManager model = new ModelManager();
        Logic logic = logic(model);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/First Street");
        logic.execute("add n/Bob p/82222222 e/bob@example.com a/Second Street");
        byte[] saved = Files.readAllBytes(file());

        assertThrows(CommandException.class, () ->
                logic.execute("add n/Carol p/83333333 e/alice@EXAMPLE.COM a/Third Street"));
        logic.execute("find Bob");
        assertThrows(CommandException.class, () -> logic.execute("edit 1 e/alice@EXAMPLE.COM"));

        assertArrayEquals(saved, Files.readAllBytes(file()));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals("Bob", model.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("undo");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals("Alice", model.getAddressBook().getPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_ownCaseEditAndDistinctLocalParts_preservesSpellingThroughReload() throws Exception {
        ModelManager model = new ModelManager();
        Logic logic = logic(model);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/First Street");
        logic.execute("edit 1 e/alice@EXAMPLE.COM");
        logic.execute("add n/Bob p/82222222 e/Alice@example.com a/Second Street");
        logic.execute("add n/Carol p/83333333 e/alice+work@example.com a/Third Street");
        logic.execute("add n/David p/84444444 e/alice@another.example a/Fourth Street");

        var loaded = new JsonAddressBookStorage(file()).readAddressBook().orElseThrow();

        assertEquals(model.getAddressBook(), loaded);
        assertEquals("alice@EXAMPLE.COM", loaded.getPersonList().getFirst().getEmail().value);
        assertEquals(4, loaded.getPersonList().size());
    }

    @Test
    public void read_existingDomainDuplicates_blocksSavingAndPreservesOriginalFile() throws Exception {
        ModelManager model = new ModelManager();
        Logic logic = logic(model);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/First Street");
        logic.execute("add n/Bob p/82222222 e/bob@example.com a/Second Street");
        String legacy = Files.readString(file()).replace("bob@example.com", "alice@EXAMPLE.COM");
        Files.writeString(file(), legacy);
        StorageManager storage = storage();

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        IOException error = assertThrows(IOException.class, () -> storage.saveAddressBook(model.getAddressBook()));

        assertTrue(error.getMessage().contains("restore valid JSON, and restart"));
        assertEquals(legacy, Files.readString(file()));
    }

    private Logic logic(ModelManager model) {
        return new LogicManager(model, storage());
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(file()),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
    }

    private Path file() {
        return folder.resolve("addressbook.json");
    }
}
