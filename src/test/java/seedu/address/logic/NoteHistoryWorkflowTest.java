package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies note history across command execution, editing, failed saves and application reloads.
 */
public class NoteHistoryWorkflowTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-09T06:30:00Z"),
            ZoneId.of("Asia/Singapore"));

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_equalTimestampAndRepeatedNotes_surviveEditReloadAndShow() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        int debtorId = debtor.getDebtorId();
        String firstText = "Called debtor; callback at 5pm?";
        String repeatedText = "Sent the same payment reminder.";

        // Direct commands inject a fixed clock; the parser uses the system clock.
        // The punctuation workflow below covers parsing and saving each note through LogicManager.
        new NoteCommand(debtorId, firstText, FIXED_CLOCK).execute(model);
        new NoteCommand(debtorId, repeatedText, FIXED_CLOCK).execute(model);
        new NoteCommand(debtorId, repeatedText, FIXED_CLOCK).execute(model);
        List<InteractionNote> originalNotes = model.getAddressBook().getPersonList().getFirst().getInteractionNotes();

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        // This first save deliberately persists all three fixed-clock notes together with the edit.
        createLogic(model, storage).execute("edit 1 n/Alice Updated o/0");

        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = createLogic(reloadedModel, storage);
        Person restoredDebtor = reloadedModel.getAddressBook().getPersonList().getFirst();
        assertEquals(debtorId, restoredDebtor.getDebtorId());
        assertEquals(originalNotes, restoredDebtor.getInteractionNotes());
        assertEquals(3, restoredDebtor.getInteractionNotes().size());

        String profile = reloadedLogic.execute("show --id " + debtorId).getFeedbackToUser();
        assertTrue(profile.contains("Name: Alice Updated"));
        assertTrue(profile.contains("Outstanding: S$0.00"));
        assertTrue(profile.endsWith("\n- " + originalNotes.get(2)
                + "\n- " + originalNotes.get(1) + "\n- " + originalNotes.getFirst()));

        reloadedLogic.execute("edit 1 p/92345678");
        assertEquals(originalNotes, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getInteractionNotes());
    }

    @Test
    public void execute_punctuationAndOptionLikeText_survivesEditAndReload() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Logic logic = createLogic(model, storage);
        String text = "Asked for $50.00; use the --identifier portal.  Follow-up? ".repeat(20).trim();

        logic.execute("note --text " + text + " --id " + debtor.getDebtorId());
        logic.execute("note --id " + debtor.getDebtorId()
                + " --outcome callback-requested --text " + text);
        List<InteractionNote> originalNotes = model.getAddressBook().getPersonList().getFirst().getInteractionNotes();
        // Each parsed note command must save its mutation before the later edit.
        assertEquals(originalNotes, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getInteractionNotes());
        logic.execute("edit 1 a/Updated Street");

        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        List<InteractionNote> restoredNotes = reloadedModel.getAddressBook().getPersonList()
                .getFirst().getInteractionNotes();
        assertEquals(originalNotes, restoredNotes);
        assertEquals(List.of(text, text), restoredNotes.stream().map(InteractionNote::getText).toList());
        assertTrue(restoredNotes.getFirst().getOutcome().isEmpty());
        assertEquals(ContactOutcome.CALLBACK_REQUESTED, restoredNotes.getLast().getOutcome().orElseThrow());
        String profile = createLogic(reloadedModel, storage)
                .execute("show --id " + debtor.getDebtorId()).getFeedbackToUser();
        assertTrue(profile.contains(text));
        assertTrue(profile.contains("[callback-requested]"));
    }

    @Test
    public void execute_invalidOutcome_doesNotAddNoteOrSaveMutation() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = createLogic(model, storage);

        assertThrows(ParseException.class, ContactOutcome.MESSAGE_CONSTRAINTS, () ->
                logic.execute("note --id " + debtor.getDebtorId()
                        + " --outcome answered --text Called debtor."));

        assertTrue(model.getAddressBook().getPersonList().getFirst().getInteractionNotes().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList()
                .getFirst().getInteractionNotes().isEmpty());
    }

    @Test
    public void execute_noteSaveFailsThenRecovers_reissueSavesOneNote() throws Exception {
        assertNoteSaveRecovery(new IOException("temporary write failure"));
    }

    @Test
    public void execute_noteSaveDeniedThenRecovers_reissueSavesOneNote() throws Exception {
        assertNoteSaveRecovery(new AccessDeniedException("temporary permission failure"));
    }

    /**
     * Checks that a failed save leaves the note out of memory and disk, then reissuing saves it once.
     */
    private void assertNoteSaveRecovery(IOException failure) throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path filePath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage diskStorage = new JsonAddressBookStorage(filePath);
        diskStorage.saveAddressBook(model.getAddressBook());
        String originalSavedData = Files.readString(filePath);
        FailOnceBeforeWriteAddressBookStorage storage = new FailOnceBeforeWriteAddressBookStorage(filePath, failure);
        Logic logic = createLogic(model, storage);
        String text = "Called debtor; promised payment.";
        String expectedMessage = failure instanceof AccessDeniedException
                ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage())
                : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());

        assertThrows(CommandException.class, expectedMessage, () ->
                logic.execute("note --id " + debtor.getDebtorId() + " --text " + text));
        List<InteractionNote> unchangedNotes = model.getAddressBook().getPersonList().getFirst().getInteractionNotes();
        assertTrue(unchangedNotes.isEmpty());
        assertEquals(2, model.getAddressBook().getNextDebtorId());
        assertEquals(originalSavedData, Files.readString(filePath));
        assertTrue(diskStorage.readAddressBook().orElseThrow().getPersonList()
                .getFirst().getInteractionNotes().isEmpty());

        String profile = logic.execute("show --id " + debtor.getDebtorId()).getFeedbackToUser();
        assertFalse(profile.contains(text));
        assertThrows(ParseException.class, () -> logic.execute("note --id " + debtor.getDebtorId() + " --text"));
        assertEquals(1, storage.saveAttempts);
        assertEquals(originalSavedData, Files.readString(filePath));

        assertTrue(logic.execute("exit").isExit());
        assertEquals(1, storage.saveAttempts);

        logic.execute("note --id " + debtor.getDebtorId() + " --text " + text);
        assertEquals(2, storage.saveAttempts);
        List<InteractionNote> savedNotes = diskStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getInteractionNotes();
        assertEquals(1, savedNotes.size());
        assertEquals(text, savedNotes.getFirst().getText());
    }

    /** Creates a logic instance using real JSON storage inside the temporary test directory. */
    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    /** Simulates a write failure before modifying the file, followed by restored write access. */
    private static class FailOnceBeforeWriteAddressBookStorage extends JsonAddressBookStorage {
        private final IOException failure;
        private int saveAttempts;

        FailOnceBeforeWriteAddressBookStorage(Path filePath, IOException failure) {
            super(filePath);
            this.failure = failure;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (saveAttempts == 1) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
