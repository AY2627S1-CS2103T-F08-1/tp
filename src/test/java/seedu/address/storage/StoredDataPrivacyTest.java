package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.InteractionNote;
import seedu.address.testutil.PersonBuilder;

/** Checks that unsuccessful loading protects both saved records and diagnostic privacy. */
public class StoredDataPrivacyTest {
    private static final String PRIVATE_NAME = "Private Sentinel";
    private static final String PRIVATE_PHONE = "81112223";
    private static final String PRIVATE_EMAIL = "private.sentinel@example.com";
    private static final String PRIVATE_ADDRESS = "Confidential Sentinel Road";
    private static final String PRIVATE_NOTE = "Private repayment discussion sentinel";

    @TempDir
    public Path folder;

    @Test
    public void read_truncatedJson_logsSafeWarningAndProtectsOriginalFile() throws Exception {
        String validJson = validStoredBook();
        assertPrivateLoadFailure(validJson.substring(0, validJson.lastIndexOf('}')),
                "Could not read JSON file");
    }

    @Test
    public void read_wrongFieldType_logsSafeWarningAndProtectsOriginalFile() throws Exception {
        String invalidJson = validStoredBook().replace("\"" + PRIVATE_PHONE + "\"",
                "{\"private\":\"" + PRIVATE_NOTE + "\"}");
        assertPrivateLoadFailure(invalidJson, "Could not read JSON file");
    }

    @Test
    public void read_invalidContactValue_logsSafeWarningAndProtectsOriginalFile() throws Exception {
        String invalidJson = validStoredBook().replace(PRIVATE_PHONE, "invalid " + PRIVATE_PHONE);
        assertPrivateLoadFailure(invalidJson, "Invalid stored address book values");
    }

    private String validStoredBook() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName(PRIVATE_NAME).withPhone(PRIVATE_PHONE)
                .withEmail(PRIVATE_EMAIL).withAddress(PRIVATE_ADDRESS)
                .withInteractionNotes(List.of(new InteractionNote(LocalDateTime.of(2026, 10, 30, 8, 0),
                        PRIVATE_NOTE))).build());
        return JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
    }

    private void assertPrivateLoadFailure(String invalidJson, String expectedWarning) throws Exception {
        Path dataFile = folder.resolve("addressbook.json");
        byte[] original = invalidJson.getBytes(StandardCharsets.UTF_8);
        Files.write(dataFile, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        Logger jsonLogger = LogsCenter.getLogger(JsonUtil.class);
        Logger storageLogger = LogsCenter.getLogger(JsonAddressBookStorage.class);
        Level previousJsonLevel = jsonLogger.getLevel();
        Level previousStorageLevel = storageLogger.getLevel();
        RecordingHandler handler = new RecordingHandler();
        jsonLogger.addHandler(handler);
        storageLogger.addHandler(handler);
        jsonLogger.setLevel(Level.ALL);
        storageLogger.setLevel(Level.ALL);
        try {
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertArrayEquals(original, Files.readAllBytes(dataFile));
            Model model = new ModelManager();
            Logic logic = new LogicManager(model, storage);
            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("add n/New Debtor p/82223334 e/new@example.com a/New Road"));
            assertTrue(error.getMessage().contains("could not be loaded"));
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertFalse(model.canUndoAddressBook());
            assertFalse(model.canRedoAddressBook());
            assertArrayEquals(original, Files.readAllBytes(dataFile));

            String diagnostics = handler.messages();
            assertTrue(diagnostics.contains(expectedWarning));
            assertTrue(diagnostics.contains(dataFile.toString()));
            for (String sentinel : List.of(PRIVATE_NAME, PRIVATE_PHONE, PRIVATE_EMAIL, PRIVATE_ADDRESS, PRIVATE_NOTE)) {
                assertFalse(diagnostics.contains(sentinel), "Private data appeared in diagnostic output");
            }
            assertTrue(handler.records.stream().allMatch(record -> record.getThrown() == null));
            assertEquals(1, handler.records.stream().filter(record ->
                    record.getLevel().intValue() >= Level.WARNING.intValue()).count());
        } finally {
            jsonLogger.removeHandler(handler);
            storageLogger.removeHandler(handler);
            jsonLogger.setLevel(previousJsonLevel);
            storageLogger.setLevel(previousStorageLevel);
        }
    }

    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        String messages() {
            return String.join("\n", records.stream().map(LogRecord::getMessage).toList());
        }
    }
}
