package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.transformation.SortedList;
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
    public void execute_longEmailAddAndEdit_savedRecordsReload() throws Exception {
        String addedEmail = "a.".repeat(5000) + "a@example.com";
        logic.execute("add n/Long Email Tester p/96661111 e/" + addedEmail + " a/Test Street o/10");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(addedEmail, reloadedModel.getAddressBook().getPersonList().getFirst().getEmail().value);

        Logic reloadedLogic = new LogicManager(reloadedModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        String editedEmail = "person@" + "a".repeat(10000) + ".com";
        reloadedLogic.execute("edit 1 e/" + editedEmail);
        Person restored = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(editedEmail, restored.getEmail().value);
        assertEquals(reloadedModel.getAddressBook().getPersonList().getFirst(), restored);
    }

    @Test
    public void execute_punycodeEmailAddAndEdit_savedRecordsReload() throws Exception {
        logic.execute("add n/International Tester p/96663332 e/person@xn--bcher-kva.de a/Test Street o/50");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals("person@xn--bcher-kva.de", storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value);
        logic.execute("edit 1 e/updated@example.xn--p1ai");
        assertEquals("updated@example.xn--p1ai", storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void execute_unicodeBlankAddressAddAndEdit_rejectedWithoutMutation() throws Exception {
        logic.execute("add n/Address Tester p/96662222 e/address@example.com a/Test Street o/10");
        Path filePath = temporaryFolder.resolve("addressBook.json");
        String originalSavedData = Files.readString(filePath);
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        for (String blank : List.of("\u2003", "\u00a0", "\u202f", "\u3000")) {
            assertThrows(ParseException.class, () -> logic.execute(
                    "add n/Blank Tester p/96662223 e/blank@example.com a/" + blank + " o/10"));
            assertThrows(ParseException.class, () -> logic.execute("edit 1 a/" + blank));
            assertEquals(originalPersons, model.getAddressBook().getPersonList());
            assertEquals(originalSavedData, Files.readString(filePath));
        }
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
    public void execute_noteStorageThrowsIoException_keepsModelAndDiskUnchanged() throws Exception {
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

        Person unchangedDebtor = model.getAddressBook().getPersonList().getFirst();
        assertTrue(unchangedDebtor.getInteractionNotes().isEmpty());
        assertEquals(2, model.getAddressBook().getNextDebtorId());
        assertEquals(List.of(unchangedDebtor), model.getFilteredPersonList());
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
                "sort --by amount --order desc",
                "list",
                "list-amount min/0",
                "show --id " + debtor.getDebtorId(),
                "help",
                "exit")) {
            readOnlyLogic.execute(command);
        }

        assertEquals(0, addressBookStorage.getSaveAttemptCount());
    }

    @Test
    public void execute_listAmountThenEditByDisplayedIndex_editsVisibleDebtor() throws Exception {
        Person hiddenDebtor = model.addPerson(new PersonBuilder(AMY)
                .withOutstandingAmount("10").build());
        Person visibleDebtor = model.addPerson(new PersonBuilder(BOB)
                .withOutstandingAmount("500").build());

        logic.execute("list-amount min/500");
        assertEquals(List.of(visibleDebtor), logic.getFilteredPersonList());

        logic.execute("edit 1 n/Edited Visible Debtor");

        Person unchangedHiddenDebtor = findPersonById(hiddenDebtor.getDebtorId());
        Person editedVisibleDebtor = findPersonById(visibleDebtor.getDebtorId());
        assertEquals(hiddenDebtor.getName(), unchangedHiddenDebtor.getName());
        assertEquals("Edited Visible Debtor", editedVisibleDebtor.getName().fullName);
        assertEquals(List.of(editedVisibleDebtor), logic.getFilteredPersonList());
    }

    @Test
    public void execute_listAmountThenDeleteByDisplayedIndex_deletesVisibleDebtor() throws Exception {
        Person hiddenDebtor = model.addPerson(new PersonBuilder(AMY)
                .withOutstandingAmount("10").build());
        Person visibleDebtor = model.addPerson(new PersonBuilder(BOB)
                .withOutstandingAmount("500").build());

        logic.execute("list-amount min/500");
        assertEquals(List.of(visibleDebtor), logic.getFilteredPersonList());

        logic.execute("delete 1");

        assertEquals(List.of(hiddenDebtor), model.getAddressBook().getPersonList());
        assertTrue(logic.getFilteredPersonList().isEmpty());
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
    public void execute_saveFailsThenStorageRecovers_reissueRetriesCommand() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("recoveringAddressBook.json");
        FailsOnceAddressBookStorage addressBookStorage = new FailsOnceAddressBookStorage(addressBookPath);
        ModelManager retryModel = new ModelManager();
        Logic retryingLogic = createLogic(retryModel, addressBookStorage);
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        assertThrows(CommandException.class, () -> retryingLogic.execute(addCommand));
        assertEquals(1, addressBookStorage.getSaveAttemptCount());
        assertTrue(retryModel.getAddressBook().getPersonList().isEmpty());
        assertEquals(1, retryModel.getAddressBook().getNextDebtorId());
        assertFalse(Files.exists(addressBookPath));

        retryingLogic.execute("list");
        assertEquals(1, addressBookStorage.getSaveAttemptCount());

        CommandResult exitResult = retryingLogic.execute("exit");
        assertTrue(exitResult.isExit());
        assertEquals(1, addressBookStorage.getSaveAttemptCount());

        retryingLogic.execute(addCommand);
        assertEquals(2, addressBookStorage.getSaveAttemptCount());
        assertEquals(List.of(new PersonBuilder(AMY).withTags().build()),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
        retryingLogic.execute("undo");
        assertEquals(0, retryModel.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_undoRedo_persistsAndKeepsSessionHistory() throws Exception {
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        logic.execute(addCommand);
        assertTrue(model.canUndoAddressBook());
        logic.execute("undo");
        assertEquals(0, model.getAddressBook().getPersonList().size());
        assertEquals(0, new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().orElseThrow().getPersonList().size());
        logic.execute("redo");
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_undoRedoRestoresEditNoteAndDeletedProfile() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        int debtorId = model.getAddressBook().getPersonList().get(0).getDebtorId();
        logic.execute("note --id " + debtorId + " --text Promised payment on Friday.");
        Person withNote = model.getAddressBook().getPersonList().get(0);
        logic.execute("edit 1 n/Amy Updated");
        Person editedWithNote = model.getAddressBook().getPersonList().get(0);
        logic.execute("delete 1");
        assertEquals(0, model.getAddressBook().getPersonList().size());

        logic.execute("undo");
        Person restoredEdited = model.getAddressBook().getPersonList().get(0);
        assertEquals(editedWithNote.getName(), restoredEdited.getName());
        assertEquals(withNote.getInteractionNotes(), restoredEdited.getInteractionNotes());
        logic.execute("undo");
        assertEquals("Amy Smith", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals(withNote.getInteractionNotes(),
                model.getAddressBook().getPersonList().get(0).getInteractionNotes());
        logic.execute("undo");
        assertEquals(0, model.getAddressBook().getPersonList().get(0).getInteractionNotes().size());
        logic.execute("redo");
        logic.execute("redo");
        assertEquals(editedWithNote.getName(), model.getAddressBook().getPersonList().get(0).getName());
        assertEquals(withNote.getInteractionNotes(),
                model.getAddressBook().getPersonList().get(0).getInteractionNotes());
    }

    @Test
    public void execute_clearIsUndoableAndRedoable() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        logic.execute("clear");
        assertEquals(0, model.getAddressBook().getPersonList().size());
        logic.execute("undo");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        logic.execute("redo");
        assertEquals(0, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_findDeleteUsesDisplayedIndexAndUndoRedoShowAllInIdOrder() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        logic.execute("add n/Bob Jones p/90222222 e/bob@example.com a/2 Main Street");
        int bobId = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals("Bob Jones"))
                .findFirst().orElseThrow().getDebtorId();
        logic.execute("find Bob");
        assertEquals(List.of(bobId), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());

        logic.execute("delete 1");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(0, model.getFilteredPersonList().size());
        logic.execute("undo");
        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream().anyMatch(person -> person.getDebtorId() == bobId));
        assertDisplayedInIdOrder();
        logic.execute("find Bob");
        logic.execute("redo");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Amy Smith", model.getFilteredPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_undoRedoAfterFindShowsAllDebtorsAndDisplayedIndicesFollowIdOrder() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        logic.execute("add n/Bob Jones p/90222222 e/bob@example.com a/2 Main Street");
        logic.execute("add n/Carol Lee p/90333333 e/carol@example.com a/3 Main Street");

        logic.execute("find Carol");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("undo");
        assertEquals(List.of("Amy Smith", "Bob Jones"), displayedNames());
        assertDisplayedInIdOrder();

        logic.execute("find Amy");
        logic.execute("redo");
        assertEquals(List.of("Amy Smith", "Bob Jones", "Carol Lee"), displayedNames());
        assertDisplayedInIdOrder();

        // The displayed index after undo/redo addresses the full list, not the earlier search result.
        logic.execute("delete 2");
        assertEquals(List.of("Amy Smith", "Carol Lee"),
                model.getAddressBook().getPersonList().stream().map(p -> p.getName().fullName).toList());
    }

    @Test
    public void execute_undoRedoAfterNonDefaultSortRestoresIdOrder() throws Exception {
        logic.execute("add n/Zed Tan p/90111111 e/zed@example.com a/1 Main Street");
        logic.execute("add n/Amy Smith p/90222222 e/amy@example.com a/2 Main Street");
        // The displayed list is a SortedList; no command changes its order yet, so sort it directly.
        SortedList<Person> displayedList = (SortedList<Person>) model.getFilteredPersonList();
        displayedList.setComparator(Comparator.comparing(person -> person.getName().fullName));
        assertEquals(List.of("Amy Smith", "Zed Tan"), displayedNames());

        logic.execute("undo");
        assertEquals(List.of("Zed Tan"), displayedNames());
        logic.execute("redo");
        assertEquals(List.of("Zed Tan", "Amy Smith"), displayedNames());
        assertDisplayedInIdOrder();
    }

    private List<String> displayedNames() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    private void assertDisplayedInIdOrder() {
        List<Integer> ids = model.getFilteredPersonList().stream().map(Person::getDebtorId).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    @Test
    public void execute_restartStartsWithEmptyUndoHistory() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("restartAddressBook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookPath);
        Logic savedLogic = createLogic(model, addressBookStorage);
        savedLogic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");

        Model restartedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic restartedLogic = createLogic(restartedModel, addressBookStorage);
        assertFalse(restartedModel.canUndoAddressBook());
        assertThrows(CommandException.class, () -> restartedLogic.execute("undo"));
        assertEquals(1, restartedModel.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_undoAddDoesNotReuseIdAfterSaveAndReload() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        int firstId = model.getAddressBook().getPersonList().get(0).getDebtorId();
        logic.execute("undo");
        assertEquals(0, model.getAddressBook().getPersonList().size());

        logic.execute("add n/Bob Jones p/90222222 e/bob@example.com a/2 Main Street");
        int secondId = model.getAddressBook().getPersonList().get(0).getDebtorId();
        assertTrue(secondId > firstId);

        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloadedModel = new ModelManager(savedStorage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloadedModel,
                new StorageManager(savedStorage,
                        new JsonUserPrefsStorage(temporaryFolder.resolve("reloadedUserPrefs.json"))));
        reloadedLogic.execute("add n/Carol Lee p/90333333 e/carol@example.com a/3 Main Street");
        int thirdId = reloadedModel.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals("Carol Lee"))
                .findFirst().orElseThrow().getDebtorId();
        assertTrue(thirdId > secondId);
    }

    @Test
    public void execute_failedMutationIsNotHistoryAndReissueIs() throws Exception {
        FailsOnceAddressBookStorage addressBookStorage = new FailsOnceAddressBookStorage(
                temporaryFolder.resolve("failedThenSaved.json"));
        Logic retryingLogic = createLogic(model, addressBookStorage);
        String addAmy = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        assertThrows(CommandException.class, () -> retryingLogic.execute(addAmy));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());

        retryingLogic.execute("exit");
        assertFalse(model.canUndoAddressBook());

        retryingLogic.execute(addAmy);
        assertTrue(model.canUndoAddressBook());
        retryingLogic.execute("undo");
        assertEquals(0, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_failedMutationThenSuccessfulMutation_addsOnlyTheSuccessfulState() throws Exception {
        FailsOnceAddressBookStorage addressBookStorage = new FailsOnceAddressBookStorage(
                temporaryFolder.resolve("failedThenMutated.json"));
        Logic retryingLogic = createLogic(model, addressBookStorage);
        String addAmy = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        assertThrows(CommandException.class, () -> retryingLogic.execute(addAmy));
        assertFalse(model.canUndoAddressBook());

        retryingLogic.execute("add n/Bob Smith p/88888888 e/bobsmith@example.com a/123 Main Street");
        assertEquals(1, addressBookStorage.readAddressBook().orElseThrow().getPersonList().size());
        assertTrue(model.canUndoAddressBook());
        retryingLogic.execute("undo");
        assertEquals(0, model.getAddressBook().getPersonList().size());
        assertFalse(model.canUndoAddressBook());
        retryingLogic.execute("redo");
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_failedMutationAfterSuccess_leavesHistoryMemoryAndDiskUnchanged() throws Exception {
        Model failingModel = new ModelManager();
        Path path = temporaryFolder.resolve("failedSecondMutation.json");
        FailsOnAttemptAddressBookStorage storage = new FailsOnAttemptAddressBookStorage(path, 2);
        Logic failingLogic = createLogic(failingModel, storage);
        String addAmy = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        failingLogic.execute(addAmy);
        String savedBytes = Files.readString(path);

        assertThrows(CommandException.class, () -> failingLogic.execute(
                "add n/Bob Smith p/88888888 e/bobsmith@example.com a/123 Main Street"));
        assertEquals(1, failingModel.getAddressBook().getPersonList().size());
        assertEquals(savedBytes, Files.readString(path));
        assertTrue(failingModel.canUndoAddressBook());
        assertFalse(failingModel.canRedoAddressBook());

        failingLogic.execute("undo");
        assertEquals(0, failingModel.getAddressBook().getPersonList().size());
        assertFalse(failingModel.canUndoAddressBook());
    }

    @Test
    public void execute_branchAfterUndoDropsRedo() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        logic.execute("undo");
        assertTrue(model.canRedoAddressBook());
        logic.execute("add n/Bob Jones p/90222222 e/bob@example.com a/2 Main Street");
        assertFalse(model.canRedoAddressBook());
        assertThrows(CommandException.class, () -> logic.execute("redo"));
    }

    @Test
    public void execute_readOnlyAndFailedCommandsAddNoHistory() throws Exception {
        logic.execute("add n/Amy Smith p/90111111 e/amy@example.com a/1 Main Street");
        logic.execute("undo");
        logic.execute("redo");
        for (String readOnly : List.of("list", "find Amy", "help")) {
            logic.execute(readOnly);
        }
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        assertThrows(ParseException.class, () -> logic.execute("edit"));
        assertFalse(model.canRedoAddressBook());
        logic.execute("undo");
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_historyIsBounded() throws Exception {
        for (int i = 0; i < 105; i++) {
            String name = "Person " + (char) ('a' + i / 26) + (char) ('a' + i % 26);
            logic.execute("add n/" + name + " p/9" + String.format("%07d", i)
                    + " e/p" + i + "@example.com a/" + i + " Main Street");
        }
        int undoCount = 0;
        while (model.canUndoAddressBook()) {
            logic.execute("undo");
            undoCount++;
        }
        // 100 retained states, including the initial one, allow 99 undo steps.
        assertEquals(99, undoCount);
    }

    @Test
    public void execute_undoSaveFails_leavesMemoryHistoryAndDiskUnchanged() throws Exception {
        Model undoModel = new ModelManager();
        Path path = temporaryFolder.resolve("undoRetry.json");
        FailsOnAttemptAddressBookStorage storage = new FailsOnAttemptAddressBookStorage(path, 2);
        Logic undoLogic = createLogic(undoModel, storage);
        String addAmy = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        undoLogic.execute(addAmy);
        String savedBytes = Files.readString(path);

        assertThrows(CommandException.class, () -> undoLogic.execute("undo"));
        assertEquals(1, undoModel.getAddressBook().getPersonList().size());
        assertTrue(undoModel.canUndoAddressBook());
        assertFalse(undoModel.canRedoAddressBook());
        assertEquals(savedBytes, Files.readString(path));

        undoLogic.execute("undo");
        assertEquals(0, undoModel.getAddressBook().getPersonList().size());
        assertEquals(0, storage.readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void execute_redoSaveFails_leavesMemoryHistoryAndDiskUnchanged() throws Exception {
        Model redoModel = new ModelManager();
        Path path = temporaryFolder.resolve("redoRetry.json");
        FailsOnAttemptAddressBookStorage storage = new FailsOnAttemptAddressBookStorage(path, 3);
        Logic redoLogic = createLogic(redoModel, storage);
        String addAmy = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        redoLogic.execute(addAmy);
        redoLogic.execute("undo");
        String savedBytes = Files.readString(path);

        assertThrows(CommandException.class, () -> redoLogic.execute("redo"));
        assertEquals(0, redoModel.getAddressBook().getPersonList().size());
        assertTrue(redoModel.canRedoAddressBook());
        assertFalse(redoModel.canUndoAddressBook());
        assertEquals(savedBytes, Files.readString(path));

        redoLogic.execute("redo");
        assertEquals(1, redoModel.getAddressBook().getPersonList().size());
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void execute_savingCommandWithNoChange_doesNotSave() throws Exception {
        SaveTrackingAddressBookStorage addressBookStorage = new SaveTrackingAddressBookStorage(
                temporaryFolder.resolve("noChangeAddressBook.json"));
        Logic noChangeLogic = createLogic(new ModelManager(), addressBookStorage);

        noChangeLogic.execute("clear");

        assertEquals(0, addressBookStorage.getSaveAttemptCount());
    }

    @Test
    public void execute_onlyNextDebtorIdChanges_attemptsSave() {
        ModelManager idOnlyModel = new ModelManager() {
            @Override
            public Model createTransactionCopy() {
                Model candidate = super.createTransactionCopy();
                candidate.deletePerson(candidate.addPerson(AMY));
                return candidate;
            }
        };
        SaveTrackingAddressBookStorage addressBookStorage = new SaveTrackingAddressBookStorage(
                temporaryFolder.resolve("idOnlyAddressBook.json"));
        Logic idOnlyLogic = createLogic(idOnlyModel, addressBookStorage);

        assertThrows(CommandException.class, () -> idOnlyLogic.execute("clear"));

        assertEquals(1, addressBookStorage.getSaveAttemptCount());
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
    public void execute_deleteStorageThrowsIoException_leavesModelAndViewUnchanged() {
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

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure("delete 1", CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), expectedModel);
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        assertEquals(2, model.getAddressBook().getNextDebtorId());
        assertFalse(Files.exists(failingAddressBookPath));
    }

    @Test
    public void execute_failedDelete_keepsActiveFilteredView() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("find Amy");
        List<Person> originalView = List.copyOf(model.getFilteredPersonList());
        int originalNextId = model.getAddressBook().getNextDebtorId();
        Path path = temporaryFolder.resolve("filteredFailure.json");
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(path) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = createLogic(model, failingStorage);

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));

        assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList());
        assertEquals(originalView, model.getFilteredPersonList());
        assertEquals(originalNextId, model.getAddressBook().getNextDebtorId());
        assertFalse(Files.exists(path));
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

    private Person findPersonById(int debtorId) {
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst()
                .orElseThrow();
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
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
        assertEquals(model.getAddressBook().getNextDebtorId(), expectedModel.getAddressBook().getNextDebtorId());
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

    private static class FailsOnAttemptAddressBookStorage extends JsonAddressBookStorage {
        private final int failingAttempt;
        private int saveAttemptCount;

        FailsOnAttemptAddressBookStorage(Path filePath, int failingAttempt) {
            super(filePath);
            this.failingAttempt = failingAttempt;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttemptCount++;
            if (saveAttemptCount == failingAttempt) {
                throw DUMMY_IO_EXCEPTION;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
