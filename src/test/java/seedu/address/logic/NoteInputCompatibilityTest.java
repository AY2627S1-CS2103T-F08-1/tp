package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises stricter new-note validation without rejecting or rewriting legacy notes. */
public class NoteInputCompatibilityTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_formattingOnlyNewNotes_preservesSavedDataAndPendingRedo() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("new-notes.json"));
        Logic logic = logic(model, storage);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/Main Street");
        logic.execute("note --id 1 --text Kept as a pending redo");
        logic.execute("undo");
        byte[] original = Files.readAllBytes(storage.getAddressBookFilePath());
        assertTrue(model.canRedoAddressBook());

        for (String text : List.of("\u200b", "\ufeff", "\u200d", " \u00a0\u200b\ufeff ")) {
            ParseException error = assertThrows(ParseException.class, () ->
                    logic.execute("note --id 1 --text " + text));
            assertEquals(InteractionNote.MESSAGE_CONSTRAINTS, error.getMessage());
            assertTrue(model.getAddressBook().getPersonList().getFirst().getInteractionNotes().isEmpty());
            assertTrue(model.canUndoAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertArrayEquals(original, Files.readAllBytes(storage.getAddressBookFilePath()));
        }

        logic.execute("redo");
        assertEquals("Kept as a pending redo", storage.readAddressBook().orElseThrow().getPersonList()
                .getFirst().getInteractionNotes().getFirst().getText());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_legacyFormattingNotes_surviveEditSaveAndReloadWithMeaningfulNewNotes() throws Exception {
        List<InteractionNote> legacyNotes = List.of(
                InteractionNote.fromStorage("2026-10-30T08:00:00", "\u200b"),
                InteractionNote.fromStorage("2026-10-30T08:01:00", "\ufeff"),
                InteractionNote.fromStorage("2026-10-30T08:02:00", "\u00a0\u200d"));
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("Alice").withInteractionNotes(legacyNotes).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("legacy-notes.json"));
        storage.saveAddressBook(original);
        Model model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic logic = logic(model, storage);

        logic.execute("edit 1 a/New Address");
        List<String> validTexts = List.of("👩‍💻 已联系", "e\u0301", "\u0301", "Call\u200bback", "\ufeffPaid");
        for (String text : validTexts) {
            logic.execute("note --id 1 --text " + text);
        }

        List<InteractionNote> restored = storage.readAddressBook().orElseThrow().getPersonList()
                .getFirst().getInteractionNotes();
        assertEquals(legacyNotes, restored.subList(0, legacyNotes.size()));
        assertEquals(validTexts, restored.subList(legacyNotes.size(), restored.size()).stream()
                .map(InteractionNote::getText).toList());
        assertEquals("New Address", storage.readAddressBook().orElseThrow().getPersonList().getFirst()
                .getAddress().value);
    }

    private Logic logic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
