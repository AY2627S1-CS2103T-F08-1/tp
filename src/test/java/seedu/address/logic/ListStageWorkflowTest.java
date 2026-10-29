package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ListStageWorkflowTest {
    private static final Person ALICE = person("Alice Tan", "900", 7, RecoveryStage.NEW);
    private static final Person BOB = person("Bob Lim", "50", 2, RecoveryStage.NEW);
    private static final Person CAROL = person("Carol Ong", "0", 5, RecoveryStage.CONTACTED);
    private static final Person DAVID = person("David Goh", "100", 1, RecoveryStage.SETTLED);

    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private RecordingStorage storage;
    private Path dataPath;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BOB)
                .withPerson(CAROL).withPerson(DAVID).build();
        model = new ModelManager(addressBook, new UserPrefs());
        dataPath = temporaryFolder.resolve("addressbook.json");
        storage = new RecordingStorage(dataPath, temporaryFolder.resolve("preferences.json"));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void filter_replacesSearchAndSortInIdOrder_withoutSaving() throws Exception {
        logic.execute("find Alice");
        logic.execute("sort --by amount --order desc");

        assertEquals("Listed 2 debtors in recovery stage new.",
                logic.execute("list-stage new").getFeedbackToUser());

        assertEquals(List.of(2, 7), displayedIds());
        assertEquals(4, model.getAddressBook().getPersonList().size());
        assertEquals(0, storage.saveAttempts);
        assertFalse(Files.exists(dataPath));
        logic.execute("list");
        assertEquals(List.of(1, 2, 5, 7), displayedIds());
    }

    @Test
    public void filter_allStagesMatchOnlyCanonicalStage_includingNonzeroSettledBalance() throws Exception {
        for (RecoveryStage stage : RecoveryStage.values()) {
            model.setPerson(model.getAddressBook().getPersonList().getFirst(), ALICE.withRecoveryStage(stage));
            logic.execute("list-stage " + stage);
            assertTrue(model.getFilteredPersonList().stream().anyMatch(person -> person.getDebtorId() == 7));
            assertTrue(model.getFilteredPersonList().stream().allMatch(person -> person.getRecoveryStage() == stage));
        }
        assertEquals(ALICE.getOutstandingAmount(),
                model.getAddressBook().getPersonList().getFirst().getOutstandingAmount());
        assertEquals(0, storage.saveAttempts);
    }

    @Test
    public void filter_emptyAndSingleResult_haveHelpfulFeedback() throws Exception {
        assertEquals("No debtors are in recovery stage disputed. Use list to see all debtors.",
                logic.execute("list-stage disputed").getFeedbackToUser());
        assertTrue(displayedIds().isEmpty());
        assertEquals("Listed 1 debtor in recovery stage contacted.",
                logic.execute("list-stage contacted").getFeedbackToUser());
        assertEquals(List.of(5), displayedIds());
        logic.execute("clear");
        assertEquals("No debtors are in recovery stage new. Use list to see all debtors.",
                logic.execute("list-stage new").getFeedbackToUser());
    }

    @Test
    public void filter_invalidInput_preservesDataAndSortedView() throws Exception {
        logic.execute("find Alice Bob");
        logic.execute("sort --by amount --order desc");
        List<Person> originalData = List.copyOf(model.getAddressBook().getPersonList());
        for (String input : List.of("list-stage", "list-stage   ", "list-stage unknown", "list-stage new settled",
                "list-stage new new", "list-stage --value new", "list-stage new --all", "list-stage 新",
                "list-stage " + "x".repeat(10000))) {
            assertThrows(ParseException.class, () -> logic.execute(input), input);
            assertEquals(List.of(7, 2), displayedIds(), input);
            assertEquals(originalData, model.getAddressBook().getPersonList(), input);
        }
        assertEquals(0, storage.saveAttempts);
    }

    @Test
    public void filter_mixedCaseAndWhitespace_usesCanonicalStage() throws Exception {
        logic.execute("  list-stage\tNeW \t");
        assertEquals(List.of(2, 7), displayedIds());
        assertEquals(0, storage.saveAttempts);
    }

    @Test
    public void filteredStageChanges_refreshMembershipByPersistentId() throws Exception {
        logic.execute("list-stage new");
        logic.execute("stage --id 7 --value contacted");
        assertEquals(List.of(2), displayedIds());
        logic.execute("stage --id 5 --value new");
        assertEquals(List.of(2, 5), displayedIds());
        assertEquals(RecoveryStage.CONTACTED, storedPerson(7).getRecoveryStage());
        assertEquals(RecoveryStage.NEW, storedPerson(5).getRecoveryStage());
    }

    @Test
    public void filteredEditAndDelete_targetDisplayedIndex_andPersistRemainingRecords() throws Exception {
        logic.execute("list-stage new");
        logic.execute("edit 1 n/Updated Bob o/75");
        assertEquals("Updated Bob", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals(RecoveryStage.NEW, model.getFilteredPersonList().getFirst().getRecoveryStage());
        logic.execute("delete 1");
        assertEquals(List.of(7), displayedIds());
        List<Person> saved = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow().getPersonList();
        assertEquals(List.of(7, 5, 1), saved.stream().map(Person::getDebtorId).toList());
        assertFalse(saved.stream().anyMatch(person -> person.getDebtorId() == 2));
        assertEquals(RecoveryStage.SETTLED, storedPerson(1).getRecoveryStage());
    }

    @Test
    public void filterAfterFailedSave_doesNotRetryWrite_orChangeStoredFile() throws Exception {
        storage.saveAddressBook(model.getAddressBook());
        String savedData = Files.readString(dataPath);
        storage.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("clear"));
        int failedWriteCount = storage.saveAttempts;

        logic.execute("list-stage new");

        assertEquals(List.of(2, 7), displayedIds());
        assertEquals(failedWriteCount, storage.saveAttempts);
        assertEquals(savedData, Files.readString(dataPath));
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void filteredMutationUndoRedo_andReload_preserveStages() throws Exception {
        logic.execute("list-stage new");
        logic.execute("stage --id 2 --value disputed");
        assertEquals(List.of(7), displayedIds());
        logic.execute("undo");
        assertEquals(RecoveryStage.NEW, storedPerson(2).getRecoveryStage());
        logic.execute("redo");
        assertEquals(RecoveryStage.DISPUTED, storedPerson(2).getRecoveryStage());
        Model reloaded = new ModelManager(new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow(),
                new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloaded, storage);
        reloadedLogic.execute("list-stage disputed");
        assertEquals(List.of(2), reloaded.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
        reloadedLogic.execute("list");
        assertEquals(List.of(1, 2, 5, 7), reloaded.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
    }

    @Test
    public void filterAndSummary_preserveUndoRedoHistory_withoutSaving() throws Exception {
        logic.execute("list-stage new");
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(0, storage.saveAttempts);
        logic.execute("stage --id 2 --value disputed");
        logic.execute("undo");
        int savesBeforeReadCommands = storage.saveAttempts;

        logic.execute("list-stage new");
        String displayedSummary = logic.execute("summary").getFeedbackToUser();
        assertTrue(displayedSummary.contains("Debtors: 2\nOutstanding total: S$950.00"));
        String wholeSummary = logic.execute("summary --all").getFeedbackToUser();
        assertTrue(wholeSummary.contains("Debtors: 4\nOutstanding total: S$1,050.00"));
        assertEquals(List.of(2, 7), displayedIds());
        assertEquals(savesBeforeReadCommands, storage.saveAttempts);
        logic.execute("redo");
        assertEquals(RecoveryStage.DISPUTED, storedPerson(2).getRecoveryStage());
    }

    @Test
    public void stageChangesWhileFiltered_preserveOtherProfileFieldsThroughStorage() throws Exception {
        logic.execute("contact --id 2 --method sms --hours 09:00-17:30");
        logic.execute("note --id 2 --outcome reached --text Call confirmed, follow up next week.");
        logic.execute("followup --id 2 --date 2026-11-03");
        Person before = storedPerson(2);

        logic.execute("list-stage new");
        logic.execute("stage --id 2 --value contacted");

        assertEquals(List.of(7), displayedIds());
        Person after = storedPerson(2);
        assertEquals(before.withRecoveryStage(RecoveryStage.CONTACTED), after);
        assertEquals(before.getDebtorId(), after.getDebtorId());
        assertEquals(before.getContactPreferences(), after.getContactPreferences());
        assertEquals(before.getInteractionNotes(), after.getInteractionNotes());
        assertEquals(before.getFollowUpDate(), after.getFollowUpDate());
        assertEquals(before.getFollowUpHistory(), after.getFollowUpHistory());
        assertTrue(logic.execute("show --id 2").getFeedbackToUser().contains("Recovery stage: contacted"));
        assertEquals(List.of(7), displayedIds());
    }

    private List<Integer> displayedIds() {
        return model.getFilteredPersonList().stream().map(Person::getDebtorId).toList();
    }

    private Person storedPerson(int debtorId) throws Exception {
        return new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId).findFirst().orElseThrow();
    }

    private static Person person(String name, String amount, int id, RecoveryStage stage) {
        return new PersonBuilder().withName(name).withDebtorId(id).withPhone("8000000" + id)
                .withEmail("debtor" + id + "@example.com").withOutstandingAmount(amount).build()
                .withRecoveryStage(stage);
    }

    private static class RecordingStorage extends StorageManager {
        private int saveAttempts;
        private boolean shouldFail;

        RecordingStorage(Path dataPath, Path prefsPath) {
            super(new JsonAddressBookStorage(dataPath), new JsonUserPrefsStorage(prefsPath));
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (shouldFail) {
                throw new IOException("Simulated disk failure");
            }
            super.saveAddressBook(addressBook);
        }
    }
}
