package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, Messages.getInvalidPersonDisplayedIndexMessage(BigInteger.valueOf(9), 0));
    }

    @Test
    public void execute_deleteCommand_persistsAndReloadsRemainingPersons() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);

        logic.execute("delete 1");

        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloadedAddressBook = savedStorage.readAddressBook().get();
        assertEquals(List.of(BOB), reloadedAddressBook.getPersonList());
    }

    @Test
    public void execute_noteEditReloadShow_preservesNotesNewestFirst() throws Exception {
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        int debtorId = debtor.getDebtorId();
        String firstNote = "Called debtor, promised payment.";
        String secondNote = "Sent follow-up email; reply requested?";

        logic.execute("note --id " + debtorId + " --text " + firstNote);
        logic.execute("note --text " + secondNote + " --id " + debtorId);
        logic.execute("edit 1 n/Alice Updated");

        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloadedAddressBook = savedStorage.readAddressBook().get();
        Model reloadedModel = new ModelManager(reloadedAddressBook, new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloadedModel,
                new StorageManager(savedStorage,
                        new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        String profile = reloadedLogic.execute("show --id " + debtorId).getFeedbackToUser();

        assertTrue(profile.contains("ID: " + debtorId));
        assertTrue(profile.contains("Name: Alice Updated"));
        assertTrue(profile.contains(firstNote));
        assertTrue(profile.contains(secondNote));
        assertTrue(profile.indexOf(secondNote) < profile.indexOf(firstNote));
        assertEquals(2, reloadedAddressBook.getPersonList().getFirst().getInteractionNotes().size());
    }

    @Test
    public void execute_invalidNoteCommands_doNotChangeModelOrSavedData() throws Exception {
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        logic.execute("note --id " + debtor.getDebtorId() + " --text Existing note.");

        Path savedAddressBookPath = temporaryFolder.resolve("addressBook.json");
        String originalSavedData = Files.readString(savedAddressBookPath);
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());

        for (String invalidCommand : List.of(
                "note --id " + debtor.getDebtorId() + " --text    ",
                "note --id " + debtor.getDebtorId() + " --text \u2003",
                "note --id 0 --text Should not be saved.",
                "note --id -1 --text Should not be saved.",
                "note --id 2147483648 --text Should not be saved.",
                "note --id " + debtor.getDebtorId() + " --id 2 --text Should not be saved.",
                "note --id " + debtor.getDebtorId() + " --text Use --id 2 to identify the debtor.",
                "note --id " + debtor.getDebtorId() + " --text Use --text to enter a note.",
                "note --id " + debtor.getDebtorId() + " --unknown value --text Should not be saved.")) {
            assertThrows(ParseException.class, () -> logic.execute(invalidCommand));
            assertEquals(originalPersons, model.getAddressBook().getPersonList());
            assertEquals(originalSavedData, Files.readString(savedAddressBookPath));
        }

        assertThrows(CommandException.class, () -> logic.execute("note --id 999 --text Should not be saved."));
        assertEquals(originalPersons, model.getAddressBook().getPersonList());
        assertEquals(originalSavedData, Files.readString(savedAddressBookPath));
    }

    @Test
    public void execute_noteStorageThrowsIoException_keepsNoteInMemoryButNotOnDisk() throws Exception {
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        Path savedAddressBookPath = temporaryFolder.resolve("addressBook.json");
        new JsonAddressBookStorage(savedAddressBookPath).saveAddressBook(model.getAddressBook());
        String originalSavedData = Files.readString(savedAddressBookPath);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(savedAddressBookPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        String noteText = "Called debtor; promised payment.";
        String noteCommand = "note --id " + debtor.getDebtorId() + " --text " + noteText;
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), () ->
                        logic.execute(noteCommand));

        Person updatedDebtor = model.getAddressBook().getPersonList().getFirst();
        assertEquals(1, updatedDebtor.getInteractionNotes().size());
        assertEquals(noteText, updatedDebtor.getInteractionNotes().getFirst().getText());
        assertEquals(originalSavedData, Files.readString(savedAddressBookPath));
        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(savedAddressBookPath);
        Person reloadedDebtor = savedStorage.readAddressBook().get().getPersonList().getFirst();
        assertTrue(reloadedDebtor.getInteractionNotes().isEmpty());
    }

    @Test
    public void execute_invalidDeleteCommands_doNotChangeModelOrSavedData() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);

        Path savedAddressBookPath = temporaryFolder.resolve("addressBook.json");
        new JsonAddressBookStorage(savedAddressBookPath).saveAddressBook(model.getAddressBook());
        String originalSavedData = Files.readString(savedAddressBookPath);
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        String invalidFormatMessage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                DeleteCommand.MESSAGE_USAGE);

        for (String invalidFormatCommand : List.of("delete", "delete a", "delete 0", "delete -1")) {
            assertInvalidDeleteDoesNotChangeState(invalidFormatCommand, ParseException.class, invalidFormatMessage,
                    originalPersons, savedAddressBookPath, originalSavedData);
        }

        BigInteger outOfRangeIndex = BigInteger.valueOf(3);
        assertInvalidDeleteDoesNotChangeState("delete " + outOfRangeIndex, CommandException.class,
                Messages.getInvalidPersonDisplayedIndexMessage(outOfRangeIndex, originalPersons.size()),
                originalPersons, savedAddressBookPath, originalSavedData);

        BigInteger veryLongIndex = new BigInteger("9".repeat(100));
        assertInvalidDeleteDoesNotChangeState("delete " + veryLongIndex, CommandException.class,
                Messages.getInvalidPersonDisplayedIndexMessage(veryLongIndex, originalPersons.size()),
                originalPersons, savedAddressBookPath, originalSavedData);
    }

    @Test
    public void executeReadOnlyCommands_storageUnavailable_doesNotSave() throws Exception {
        Model readOnlyModel = new ModelManager();
        Person debtor = readOnlyModel.addPerson(AMY);
        SaveTrackingAddressBookStorage addressBookStorage = new SaveTrackingAddressBookStorage(
                temporaryFolder.resolve("readOnlyAddressBook.json"));
        Logic readOnlyLogic = createLogic(readOnlyModel, addressBookStorage);

        for (String command : List.of(
                "find Amy",
                "list",
                "show --id " + debtor.getDebtorId(),
                "help",
                "exit")) {
            readOnlyLogic.execute(command);
        }

        assertEquals(0, addressBookStorage.getSaveAttemptCount());
    }

    @Test
    public void executeMutatingCommands_storageUnavailable_attemptsSave() {
        assertMutatingCommandAttemptsSave(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY, new ModelManager());

        Model editModel = new ModelManager();
        editModel.addPerson(AMY);
        assertMutatingCommandAttemptsSave("edit 1 n/Amy Edited", editModel);

        Model deleteModel = new ModelManager();
        deleteModel.addPerson(AMY);
        assertMutatingCommandAttemptsSave("delete 1", deleteModel);

        Model clearModel = new ModelManager();
        clearModel.addPerson(AMY);
        assertMutatingCommandAttemptsSave("clear", clearModel);

        Model noteModel = new ModelManager();
        Person debtor = noteModel.addPerson(AMY);
        assertMutatingCommandAttemptsSave(
                "note --id " + debtor.getDebtorId() + " --text Payment reminder sent.", noteModel);
    }

    @Test
    public void execute_saveFailsThenStorageRecovers_exitRetriesPendingChanges() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("recoveringAddressBook.json");
        FailsOnceAddressBookStorage addressBookStorage = new FailsOnceAddressBookStorage(addressBookPath);
        Logic retryingLogic = createLogic(new ModelManager(), addressBookStorage);
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        assertThrows(CommandException.class, () -> retryingLogic.execute(addCommand));
        assertEquals(1, addressBookStorage.getSaveAttemptCount());

        retryingLogic.execute("list");
        assertEquals(1, addressBookStorage.getSaveAttemptCount());

        CommandResult exitResult = retryingLogic.execute("exit");
        assertTrue(exitResult.isExit());
        assertEquals(2, addressBookStorage.getSaveAttemptCount());
        assertEquals(List.of(new PersonBuilder(AMY).withTags().build()),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand,
                ListCommand.getMessageForDebtorCount(model.getFilteredPersonList().size()), model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_deleteStorageThrowsIoException_reportsFailureWithoutRollback() {
        model.addPerson(AMY);
        Path failingAddressBookPath = temporaryFolder.resolve("failingAddressBook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(failingAddressBookPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("failingUserPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));

        ModelManager expectedModel = new ModelManager();
        assertCommandFailure("delete 1", CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), expectedModel);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    private void assertInvalidDeleteDoesNotChangeState(String inputCommand,
            Class<? extends Throwable> expectedException, String expectedMessage,
            List<Person> originalPersons, Path savedAddressBookPath, String originalSavedData) throws IOException {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(originalPersons, model.getAddressBook().getPersonList());
        assertEquals(originalPersons, logic.getFilteredPersonList());
        assertEquals(originalSavedData, Files.readString(savedAddressBookPath));
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private void assertMutatingCommandAttemptsSave(String commandText, Model testModel) {
        SaveTrackingAddressBookStorage addressBookStorage = new SaveTrackingAddressBookStorage(
                temporaryFolder.resolve("mutatingAddressBook.json"));
        Logic testLogic = createLogic(testModel, addressBookStorage);

        assertThrows(CommandException.class, () -> testLogic.execute(commandText));
        assertEquals(1, addressBookStorage.getSaveAttemptCount());
    }

    private Logic createLogic(Model testModel, JsonAddressBookStorage addressBookStorage) {
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("trackingUserPrefs.json"));
        return new LogicManager(testModel, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    private static class SaveTrackingAddressBookStorage extends JsonAddressBookStorage {
        private int saveAttemptCount;

        SaveTrackingAddressBookStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttemptCount++;
            throw DUMMY_IO_EXCEPTION;
        }

        int getSaveAttemptCount() {
            return saveAttemptCount;
        }
    }

    private static class FailsOnceAddressBookStorage extends JsonAddressBookStorage {
        private int saveAttemptCount;

        FailsOnceAddressBookStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttemptCount++;
            if (saveAttemptCount == 1) {
                throw DUMMY_IO_EXCEPTION;
            }
            super.saveAddressBook(addressBook);
        }

        int getSaveAttemptCount() {
            return saveAttemptCount;
        }
    }
}
