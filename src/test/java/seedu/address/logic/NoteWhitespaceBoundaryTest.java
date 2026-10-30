package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.InteractionNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Regression coverage for Unicode spaces at the note validation boundary. */
public class NoteWhitespaceBoundaryTest {
    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 10, 30, 8, 0);
    private static final List<String> BLANK_TEXTS = List.of("\u00a0\u00a0", "\u2007", "\u202f", " \t\u00a0\u2003");

    @TempDir
    public Path folder;

    @Test
    public void note_blankUnicodeSpaces_rejectsWithoutChangingFileOrHistory() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("data.json"));
        Logic logic = logic(model, storage);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/Main Street");
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String blank : BLANK_TEXTS) {
            ParseException error = assertThrows(ParseException.class, () ->
                    logic.execute("note --id 1 --text " + blank));
            assertEquals(InteractionNote.MESSAGE_CONSTRAINTS, error.getMessage());
            assertTrue(model.getAddressBook().getPersonList().getFirst().getInteractionNotes().isEmpty());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("redo");
        assertTrue(model.getAddressBook().getPersonList().getFirst().getInteractionNotes().isEmpty());
    }

    @Test
    public void constructorAndStorage_blankUnicodeSpaces_rejectConsistently() {
        for (String blank : BLANK_TEXTS) {
            assertFalse(InteractionNote.isValidText(blank));
            assertThrows(IllegalArgumentException.class, () -> new InteractionNote(TIMESTAMP, blank));
            assertThrows(IllegalArgumentException.class, () ->
                    InteractionNote.fromStorage(TIMESTAMP.toString(), blank));
        }
    }

    @Test
    public void note_realUnicodeTextAndInternalSpaces_preservesTextAcrossSaveLoad() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("valid.json"));
        Logic logic = logic(model, storage);
        logic.execute("add n/Alice p/81111111 e/alice@example.com a/Main Street");
        String text = "\u00a0Called\u00a0Alice; 已联系, agreed ✅.\u202f";

        logic.execute("note --id 1 --text " + text);

        assertEquals(text, model.getAddressBook().getPersonList().getFirst()
                .getInteractionNotes().getFirst().getText());
        assertEquals(text, storage.readAddressBook().orElseThrow().getPersonList().getFirst()
                .getInteractionNotes().getFirst().getText());
    }

    @Test
    public void read_blankUnicodeStoredNote_reportsLoadingErrorWithoutChangingFile() throws Exception {
        Path file = folder.resolve("invalid.json");
        String json = JsonUtil.toJsonString(Map.of("persons", List.of(Map.of("name", "Alice", "phone", "81111111",
                "email", "alice@example.com", "address", "Main Street", "interactionNotes",
                List.of(Map.of("timestamp", TIMESTAMP.toString(), "text", "\u00a0\u00a0"))))));
        Files.writeString(file, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        DataLoadingException error = assertThrows(DataLoadingException.class, storage::readAddressBook);

        assertTrue(error.getMessage().contains(InteractionNote.MESSAGE_CONSTRAINTS));
        assertEquals(json, Files.readString(file));
    }

    private Logic logic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }
}
