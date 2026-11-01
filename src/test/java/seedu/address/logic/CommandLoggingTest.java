package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class CommandLoggingTest {
    private static final String PRIVATE_EMAIL = "alice.private@example.com";
    private static final String PRIVATE_PHONE = "91234567";
    private static final String PRIVATE_NOTE = "Confidential repayment discussion";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_commandsContainingPersonalData_logsOnlySafeCommandLabels() throws Exception {
        Logic logic = createLogic();
        Logger logicLogger = LogsCenter.getLogger(LogicManager.class);
        Logger parserLogger = LogsCenter.getLogger(AddressBookParser.class);
        RecordingHandler recordingHandler = new RecordingHandler();
        Level originalLogicLevel = logicLogger.getLevel();
        Level originalParserLevel = parserLogger.getLevel();
        logicLogger.setLevel(Level.ALL);
        parserLogger.setLevel(Level.ALL);
        logicLogger.addHandler(recordingHandler);
        parserLogger.addHandler(recordingHandler);

        try {
            logic.execute("find " + PRIVATE_EMAIL + " " + PRIVATE_PHONE + " " + PRIVATE_NOTE);
            assertThrows(CommandException.class, () ->
                    logic.execute("note --id 999 --text " + PRIVATE_NOTE));
            assertThrows(ParseException.class, () ->
                    logic.execute(PRIVATE_EMAIL + " " + PRIVATE_PHONE + " " + PRIVATE_NOTE));

            String loggedMessages = recordingHandler.getMessages();
            assertTrue(loggedMessages.contains("[find]"));
            assertTrue(loggedMessages.contains("[note]"));
            assertTrue(loggedMessages.contains(CommandLogFormatter.UNKNOWN_COMMAND));
            assertFalse(loggedMessages.contains(PRIVATE_EMAIL));
            assertFalse(loggedMessages.contains(PRIVATE_PHONE));
            assertFalse(loggedMessages.contains(PRIVATE_NOTE));
        } finally {
            logicLogger.removeHandler(recordingHandler);
            parserLogger.removeHandler(recordingHandler);
            logicLogger.setLevel(originalLogicLevel);
            parserLogger.setLevel(originalParserLevel);
        }
    }

    @Test
    public void createModel_addressBookContainsPersonalData_logsOnlyEntryCount() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder()
                .withName("Alice Private")
                .withPhone(PRIVATE_PHONE)
                .withEmail(PRIVATE_EMAIL)
                .build());
        Logger modelLogger = LogsCenter.getLogger(ModelManager.class);
        RecordingHandler recordingHandler = new RecordingHandler();
        Level originalModelLevel = modelLogger.getLevel();
        modelLogger.setLevel(Level.ALL);
        modelLogger.addHandler(recordingHandler);

        try {
            new ModelManager(addressBook, new UserPrefs());

            String loggedMessages = recordingHandler.getMessages();
            assertTrue(loggedMessages.contains("1 address book entries"));
            assertFalse(loggedMessages.contains("Alice Private"));
            assertFalse(loggedMessages.contains(PRIVATE_EMAIL));
            assertFalse(loggedMessages.contains(PRIVATE_PHONE));
        } finally {
            modelLogger.removeHandler(recordingHandler);
            modelLogger.setLevel(originalModelLevel);
        }
    }

    private Logic createLogic() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        return new LogicManager(new ModelManager(), new StorageManager(addressBookStorage, userPrefsStorage));
    }

    private static class RecordingHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        private String getMessages() {
            return String.join(System.lineSeparator(), messages);
        }
    }
}
