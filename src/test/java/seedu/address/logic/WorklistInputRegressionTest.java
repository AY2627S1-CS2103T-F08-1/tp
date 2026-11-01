package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that input corrections preserve worklists, save bytes and history. */
public class WorklistInputRegressionTest {
    @TempDir
    private Path folder;
    private Path dataFile;
    private ModelManager model;
    private Logic logic;
    private StorageManager storage;

    @BeforeEach
    public void setUp() {
        dataFile = folder.resolve("debtors.json");
        model = new ModelManager();
        storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void find_unicodeSeparators_matchesOrdinarySpaceWithoutSaving() throws Exception {
        addTwoDebtors();
        byte[] original = Files.readAllBytes(dataFile);
        for (String separator : List.of("\u00a0", "\u2003", "\u202f", "\u3000")) {
            logic.execute("sort --by id --order desc");
            logic.execute("find " + separator + "liCe" + separator + "82222222" + separator);
            assertEquals(List.of(1, 2), ids());
            assertArrayEquals(original, Files.readAllBytes(dataFile));
        }
        logic.execute("find 8222");
        assertEquals(List.of(), ids());
        logic.execute("find .*");
        assertEquals(List.of(), ids());
        assertArrayEquals(original, Files.readAllBytes(dataFile));
    }

    @Test
    public void find_unicodeBlank_preservesPreviousViewBytesAndHistory() throws Exception {
        addTwoDebtors();
        logic.execute("find Bob");
        byte[] original = Files.readAllBytes(dataFile);
        for (String blank : List.of("\u00a0", "\u2003", "\u202f\u00a0\u3000")) {
            assertThrows(ParseException.class, () -> logic.execute("find " + blank));
            assertEquals(List.of(2), ids());
            assertArrayEquals(original, Files.readAllBytes(dataFile));
            assertTrue(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
        }
        logic.execute("undo");
        assertEquals(List.of(1), ids());
    }

    @Test
    public void decimalIndexes_targetDisplayedDebtorAndKeepPreviewReadOnly() throws Exception {
        addTwoDebtors();
        byte[] original = Files.readAllBytes(dataFile);
        for (String index : List.of("\uff11", "\u0661", "\u0967", "\uff10\u06601", "0001")) {
            logic.execute("find Bob");
            logic.execute("preview edit " + index + " n/Changed");
            logic.execute("preview delete " + index);
            assertEquals(List.of(2), ids());
            assertArrayEquals(original, Files.readAllBytes(dataFile));

            logic.execute("edit " + index + " n/Changed");
            assertEquals("Changed", model.getAddressBook().getPersonList().get(1).getName().fullName);
            assertEquals("Alice Tan", model.getAddressBook().getPersonList().getFirst().getName().fullName);
            logic.execute("undo");
            assertArrayEquals(original, Files.readAllBytes(dataFile));

            logic.execute("find Bob");
            logic.execute("delete " + index);
            assertEquals(List.of(1), model.getAddressBook().getPersonList().stream().map(Person::getDebtorId).toList());
            logic.execute("undo");
            assertArrayEquals(original, Files.readAllBytes(dataFile));
        }
    }

    @Test
    public void decimalPersistentIds_selectByIdDespiteDifferentDisplayedIndex() throws Exception {
        addTwoDebtors();
        logic.execute("find Bob");
        byte[] original = Files.readAllBytes(dataFile);
        String expected = logic.execute("show --id 2").getFeedbackToUser();
        for (String id : List.of("\uff12", "\u0662", "\u0968", "\uff10\u06602")) {
            assertEquals(expected, logic.execute("show --id " + id).getFeedbackToUser());
            assertEquals(List.of(2), ids());
            assertArrayEquals(original, Files.readAllBytes(dataFile));
        }
    }

    @Test
    public void invalidIndexesAndIds_preserveViewBytesAndHistory() throws Exception {
        addTwoDebtors();
        logic.execute("find Bob");
        byte[] original = Files.readAllBytes(dataFile);
        for (String invalid : List.of("\uff11x", "\u0661.0", "+\uff11", "\uff10\u0660", "\ud835\udfcf")) {
            for (String command : List.of("edit " + invalid + " n/Changed", "delete " + invalid,
                    "preview edit " + invalid + " n/Changed", "preview delete " + invalid,
                    "show --id " + invalid, "note --id " + invalid + " --text Called")) {
                assertThrows(ParseException.class, () -> logic.execute(command), command);
                assertEquals(List.of(2), ids());
                assertArrayEquals(original, Files.readAllBytes(dataFile));
                assertTrue(model.canUndoAddressBook());
                assertFalse(model.canRedoAddressBook());
            }
        }
        for (String command : List.of("delete " + "\uff19".repeat(100),
                "preview delete " + "\u0669".repeat(100))) {
            CommandException error = assertThrows(CommandException.class, () -> logic.execute(command));
            assertTrue(error.getMessage().contains("out of range"));
            assertEquals(List.of(2), ids());
            assertArrayEquals(original, Files.readAllBytes(dataFile));
        }
        logic.execute("undo");
        assertEquals(List.of(1), ids());
    }

    @Test
    public void list_afterFailedLoad_doesNotRecommendBlockedAddAndPreservesFile() throws Exception {
        byte[] damaged = "{broken".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(dataFile, damaged);
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        assertEquals("No debtors found in the current session (0 listed).",
                logic.execute("list").getFeedbackToUser());
        assertThrows(CommandException.class, () -> logic.execute(
                "add n/Alice Tan p/81111111 e/alice@example.com a/Hill Road"));
        assertArrayEquals(damaged, Files.readAllBytes(dataFile));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void list_genuineEmptyBook_remainsReadOnlyAndAllowsAdd() throws Exception {
        storage.saveAddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(dataFile);
        assertEquals("No debtors found in the current session (0 listed).",
                logic.execute("list").getFeedbackToUser());
        assertArrayEquals(original, Files.readAllBytes(dataFile));
        logic.execute("add n/Alice Tan p/81111111 e/alice@example.com a/Hill Road");
        assertEquals(List.of(1), ids());
        assertEquals(1, new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow().getPersonList().size());
    }

    private void addTwoDebtors() throws Exception {
        logic.execute("add n/Alice Tan p/81111111 e/alice@example.com a/Hill Road");
        logic.execute("add n/Bob Lim p/82222222 e/bob@example.com a/Oak Road");
    }

    private List<Integer> ids() {
        return model.getFilteredPersonList().stream().map(Person::getDebtorId).toList();
    }
}
