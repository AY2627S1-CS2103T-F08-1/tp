package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class FollowUpHistoryWorkflowTest {
    private static final LocalDate FOLLOW_UP_DATE = LocalDate.of(2026, 11, 3);
    @TempDir
    Path temporaryFolder;

    @Test
    void execute_schedule_survivesEditReloadAndShow() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        Person otherDebtor = model.addPerson(new PersonBuilder().withName("Beth Lee")
                .withPhone("81234567").withEmail("beth@example.com").build());
        int id = debtor.getDebtorId();
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("addressBook.json"));
        Logic logic = createLogic(model, addressBookStorage);

        logic.execute("followup --id " + id + " --date 2026-11-03");
        logic.execute("followup --id " + otherDebtor.getDebtorId() + " --date 2026-11-05");
        logic.execute("edit 1 n/Alice Updated");
        Model reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        Person restored = reloaded.getAddressBook().getPersonList().getFirst();
        Person restoredOther = reloaded.getAddressBook().getPersonList().get(1);

        assertEquals(LocalDate.of(2026, 11, 3), restored.getFollowUpDate().orElseThrow());
        assertEquals(1, restored.getFollowUpHistory().size());
        assertEquals(otherDebtor.getDebtorId(), restoredOther.getDebtorId());
        assertEquals(LocalDate.of(2026, 11, 5), restoredOther.getFollowUpDate().orElseThrow());
        assertEquals(1, restoredOther.getFollowUpHistory().size());
        String profile = createLogic(reloaded, addressBookStorage).execute("show --id " + id).getFeedbackToUser();
        assertTrue(profile.contains("Follow-up: 2026-11-03"));
        assertTrue(profile.contains("SCHEDULED 2026-11-03"));
    }

    @Test
    void execute_rescheduleClearAndDone_recordDatesAndRejectMissingPendingDate() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        int id = debtor.getDebtorId();
        Clock late = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));
        Clock early = Clock.fixed(Instant.parse("2026-10-29T08:00:00Z"), ZoneId.of("UTC"));

        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE), late)
                .execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE.plusDays(2)),
                early).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, java.util.Optional.empty(), late).execute(model);
        Person afterClear = model.getAddressBook().getPersonList().getFirst();
        assertEquals(FOLLOW_UP_DATE.plusDays(2), afterClear.getFollowUpHistory().getLast().getDate().orElseThrow());
        assertTrue(afterClear.getFollowUpDate().isEmpty());

        assertThrows(CommandException.class, "No pending follow-up exists for debtor ID " + id + ".", () ->
                new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty(), late).execute(model));
        assertThrows(CommandException.class, "No pending follow-up exists for debtor ID " + id + ".", () ->
                new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty(), late).execute(model));
        assertEquals(3, model.getAddressBook().getPersonList().getFirst().getFollowUpHistory().size());
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE), late)
                .execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty(), late).execute(model);
        Person completed = model.getAddressBook().getPersonList().getFirst();
        assertEquals(FollowUpEvent.Action.COMPLETED, completed.getFollowUpHistory().getLast().getAction());
        assertEquals(FOLLOW_UP_DATE, completed.getFollowUpHistory().getLast().getDate().orElseThrow());
        assertTrue(completed.getFollowUpDate().isEmpty());
        String profile = Messages.formatProfile(completed);
        assertTrue(profile.indexOf("] RESCHEDULED") < profile.indexOf("] SCHEDULED"));
        assertTrue(profile.indexOf("2026-10-29 08:00") < profile.indexOf("2026-10-29 10:00"));
        assertTrue(profile.contains("] CLEARED 2026-11-05"));
        assertTrue(profile.contains("] COMPLETED 2026-11-03"));
        assertTrue(profile.indexOf("] SCHEDULED 2026-11-03") < profile.indexOf("] CLEARED 2026-11-05"));
        assertTrue(profile.indexOf("] CLEARED 2026-11-05") < profile.indexOf("] COMPLETED 2026-11-03"));
    }

    @Test
    void read_legacyJsonWithoutFollowUpFields_defaultsToEmpty() throws Exception {
        Path legacyPath = Path.of("src/test/data/JsonSerializableAddressBookTest/typicalPersonsAddressBook.json");
        var legacyBook = new JsonAddressBookStorage(legacyPath).readAddressBook().orElseThrow();
        assertTrue(legacyBook.getPersonList().stream().allMatch(person -> person.getFollowUpDate().isEmpty()));
        assertTrue(legacyBook.getPersonList().stream().allMatch(person -> person.getFollowUpHistory().isEmpty()));
    }

    @Test
    void read_invalidFollowUpDate_rejectsData() throws Exception {
        Path file = temporaryFolder.resolve("invalid.json");
        java.nio.file.Files.writeString(file, """
                {"persons":[{"name":"Alice Tan","phone":"81234567","email":"alice@example.com",
                "address":"1 Main Street","followUpDate":"2026-02-30","tags":[]}]}
                """);
        assertThrows(seedu.address.commons.exceptions.DataLoadingException.class, () ->
                new JsonAddressBookStorage(file).readAddressBook());
    }

    @Test
    void execute_equalTimestampHistory_survivesRestartAfterClearAndCompletion() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        int id = debtor.getDebtorId();
        Clock sameTime = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE), sameTime)
                .execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE,
                Optional.of(FOLLOW_UP_DATE.plusDays(2)), sameTime).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE,
                Optional.of(FOLLOW_UP_DATE.plusDays(4)), sameTime).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty(), sameTime).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE,
                Optional.of(FOLLOW_UP_DATE.plusDays(6)), sameTime).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty(), sameTime).execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("history.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person restored = reloaded.getAddressBook().getPersonList().getFirst();
        assertTrue(restored.getFollowUpDate().isEmpty());
        assertEquals(6, restored.getFollowUpHistory().size());
        assertEquals(List.of(FollowUpEvent.Action.SCHEDULED, FollowUpEvent.Action.RESCHEDULED,
                        FollowUpEvent.Action.RESCHEDULED, FollowUpEvent.Action.CLEARED,
                        FollowUpEvent.Action.SCHEDULED, FollowUpEvent.Action.COMPLETED),
                restored.getFollowUpHistory().stream().map(FollowUpEvent::getAction).toList());
        assertEquals(FOLLOW_UP_DATE.plusDays(4), restored.getFollowUpHistory().get(3).getDate().orElseThrow());
        assertEquals(FOLLOW_UP_DATE.plusDays(6), restored.getFollowUpHistory().getLast().getDate().orElseThrow());
        String profile = Messages.formatProfile(restored);
        String firstReschedule = "] RESCHEDULED 2026-11-03 -> 2026-11-05";
        String secondReschedule = "] RESCHEDULED 2026-11-05 -> 2026-11-07";
        assertTrue(profile.indexOf("] SCHEDULED 2026-11-03") < profile.indexOf(firstReschedule));
        assertTrue(profile.indexOf(firstReschedule) < profile.indexOf(secondReschedule));
        assertTrue(profile.indexOf(secondReschedule) < profile.indexOf("] CLEARED 2026-11-07"));
        assertTrue(profile.indexOf("] CLEARED 2026-11-07") < profile.indexOf("] COMPLETED 2026-11-09"));
    }

    @Test
    void execute_saveFails_thenExitPersistsFollowUpOnce() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path file = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage baseStorage = new JsonAddressBookStorage(file);
        baseStorage.saveAddressBook(model.getAddressBook());
        JsonAddressBookStorage failOnce = new JsonAddressBookStorage(file) {
            private int attempts;
            @Override
            public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook addressBook)
                    throws java.io.IOException {
                if (++attempts == 1) {
                    throw new java.io.IOException("temporary failure");
                }
                super.saveAddressBook(addressBook);
            }
        };
        Logic logic = createLogic(model, failOnce);

        String commandText = "followup --id " + debtor.getDebtorId() + " --date 2026-11-03";
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "temporary failure"), () ->
                        logic.execute(commandText));
        assertTrue(model.getAddressBook().getPersonList().getFirst().getFollowUpDate().isPresent());
        assertTrue(baseStorage.readAddressBook().orElseThrow().getPersonList().getFirst().getFollowUpDate().isEmpty());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(LocalDate.of(2026, 11, 3), baseStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getFollowUpDate().orElseThrow());
    }

    @Test
    void execute_accessDeniedSave_thenExitPersistsFollowUpOnce() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path file = temporaryFolder.resolve("deniedAddressBook.json");
        JsonAddressBookStorage baseStorage = new JsonAddressBookStorage(file);
        baseStorage.saveAddressBook(model.getAddressBook());
        JsonAddressBookStorage failOnce = new JsonAddressBookStorage(file) {
            private int attempts;
            @Override
            public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook addressBook)
                    throws java.io.IOException {
                if (++attempts == 1) {
                    throw new java.nio.file.AccessDeniedException("temporary denied path");
                }
                super.saveAddressBook(addressBook);
            }
        };
        Logic logic = createLogic(model, failOnce);

        String commandText = "followup --id " + debtor.getDebtorId() + " --date 2026-11-03";
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "temporary denied path"), () ->
                        logic.execute(commandText));
        assertTrue(model.getAddressBook().getPersonList().getFirst().getFollowUpDate().isPresent());
        assertTrue(baseStorage.readAddressBook().orElseThrow().getPersonList().getFirst().getFollowUpDate().isEmpty());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(LocalDate.of(2026, 11, 3), baseStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getFollowUpDate().orElseThrow());
    }

    @Test
    void execute_unknownOrDeletedId_doesNotChangeOtherDebtorHistory() throws Exception {
        Model model = new ModelManager();
        Person first = model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        Person second = model.addPerson(new PersonBuilder().withName("Beth Lee").withEmail("beth@example.com")
                .withPhone("81234567").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Logic logic = createLogic(model, storage);
        logic.execute("followup --id " + first.getDebtorId() + " --date 2026-11-03");
        logic.execute("followup --id " + second.getDebtorId() + " --date 2026-11-05");
        String beforeRejected = java.nio.file.Files.readString(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, "No debtor with ID 999 exists.", () ->
                logic.execute("followup --id 999 --date 2026-11-07"));
        assertEquals(beforeRejected, java.nio.file.Files.readString(storage.getAddressBookFilePath()));
        logic.execute("delete 1");
        String afterDelete = java.nio.file.Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, "No debtor with ID " + first.getDebtorId() + " exists.", () ->
                logic.execute("followup --id " + first.getDebtorId() + " --done"));
        assertEquals(afterDelete, java.nio.file.Files.readString(storage.getAddressBookFilePath()));
        Person surviving = model.getAddressBook().getPersonList().getFirst();
        assertEquals(second.getDebtorId(), surviving.getDebtorId());
        assertEquals(LocalDate.of(2026, 11, 5), surviving.getFollowUpDate().orElseThrow());
        assertEquals(1, surviving.getFollowUpHistory().size());
    }

    @Test
    void execute_sameDateIsNoOpWithoutEventOrSaveRetry() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        Path file = temporaryFolder.resolve("same-date.json");
        JsonAddressBookStorage baseStorage = new JsonAddressBookStorage(file);
        baseStorage.saveAddressBook(model.getAddressBook());
        JsonAddressBookStorage failOnce = new JsonAddressBookStorage(file) {
            private int attempts;
            @Override
            public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook book)
                    throws java.io.IOException {
                if (++attempts == 1) {
                    throw new java.io.IOException("temporary failure");
                }
                super.saveAddressBook(book);
            }
        };
        Logic logic = createLogic(model, failOnce);
        String command = "followup --id " + debtor.getDebtorId() + " --date 2026-11-03";
        assertThrows(CommandException.class, () -> logic.execute(command));
        String beforeNoOp = java.nio.file.Files.readString(file);
        logic.execute(command);
        assertEquals(beforeNoOp, java.nio.file.Files.readString(file));
        assertEquals(1, model.getAddressBook().getPersonList().getFirst().getFollowUpHistory().size());
    }
    @Test
    void execute_lifecycleEventsCapturePreviousAndNewDates() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        int id = debtor.getDebtorId();
        Clock clock = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE), clock).execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(FOLLOW_UP_DATE.plusDays(2)), clock)
                .execute(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty(), clock).execute(model);
        List<FollowUpEvent> events = model.getAddressBook().getPersonList().getFirst().getFollowUpHistory();
        assertTrue(events.get(0).getPreviousDate().isEmpty());
        assertEquals(Optional.of(FOLLOW_UP_DATE), events.get(0).getNewDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE), events.get(1).getPreviousDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE.plusDays(2)), events.get(1).getNewDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE.plusDays(2)), events.get(2).getPreviousDate());
        assertTrue(events.get(2).getNewDate().isEmpty());
    }

    @Test
    void execute_malformedSourceSurvivesMutatingCommand() throws Exception {
        Path file = temporaryFolder.resolve("malformed.json");
        String original = "{\"persons\":[{\"name\":\"Alice Tan\",\"phone\":\"81234567\","
                + "\"email\":\"alice@example.com\",\"address\":\"1 Main Street\","
                + "\"followUpHistory\":[null],\"tags\":[]}]}";
        java.nio.file.Files.writeString(file, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        assertThrows(seedu.address.commons.exceptions.DataLoadingException.class, storage::readAddressBook);
        Model model = new ModelManager();
        Logic logic = new LogicManager(model, storage);
        CommandException failure = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, () ->
                logic.execute("add n/Beth Lee p/81234568 e/beth@example.com a/2 Main Street"));
        assertTrue(failure.getMessage().contains("Preserve this file, restore valid JSON, and restart"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(original, java.nio.file.Files.readString(file));
    }
    @Test
    void execute_reusedCommand_marksNoOpAsNotNeedingSave() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        FollowUpCommand command = new FollowUpCommand(debtor.getDebtorId(), FOLLOW_UP_DATE);
        command.execute(model);
        assertTrue(command.shouldSaveAddressBook());
        command.execute(model);
        org.junit.jupiter.api.Assertions.assertFalse(command.shouldSaveAddressBook());
    }
    @Test
    void read_legacyDateOnlyEventsRemainReadable() throws Exception {
        Path file = temporaryFolder.resolve("legacy-events.json");
        java.nio.file.Files.writeString(file, """
                {"persons":[{"name":"Alice Tan","phone":"81234567","email":"alice@example.com",
                "address":"1 Main Street","followUpHistory":[
                {"timestamp":"2026-10-29T09:59:00","action":"SCHEDULED","date":"2026-11-03"},
                {"timestamp":"2026-10-29T10:00:00","action":"RESCHEDULED","date":"2026-11-05"},
                {"timestamp":"2026-10-29T10:01:00","action":"CLEARED","date":"2026-11-05"}],"tags":[]}]}
                """);
        Person restored = new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().getFirst();
        List<FollowUpEvent> events = restored.getFollowUpHistory();
        assertTrue(events.get(0).getPreviousDate().isEmpty());
        assertEquals(Optional.of(FOLLOW_UP_DATE), events.get(0).getNewDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE), events.get(1).getPreviousDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE.plusDays(2)), events.get(1).getNewDate());
        assertEquals(Optional.of(FOLLOW_UP_DATE.plusDays(2)), events.get(2).getPreviousDate());
        assertTrue(events.get(2).getNewDate().isEmpty());
    }

    @Test
    void read_yearZeroFollowUpDate_rejectsData() throws Exception {
        Path file = temporaryFolder.resolve("year-zero.json");
        java.nio.file.Files.writeString(file, """
                {"persons":[{"name":"Alice Tan","phone":"81234567","email":"alice@example.com",
                "address":"1 Main Street","followUpDate":"0000-01-01","tags":[]}]}
                """);
        assertThrows(seedu.address.commons.exceptions.DataLoadingException.class, () ->
                new JsonAddressBookStorage(file).readAddressBook());
    }

    @Test
    void storage_nextDebtorIdSurvivesRestartAndAllocatesExplicitly() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alice Tan").build());
        seedu.address.model.AddressBook book = (seedu.address.model.AddressBook) model.getAddressBook();
        book.setNextDebtorId(9);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("next-id.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(9, reloaded.getAddressBook().getNextDebtorId());
        Person added = reloaded.addPerson(new PersonBuilder().withName("Beth Lee").withPhone("81234568")
                .withEmail("beth@example.com").build());
        assertEquals(9, added.getDebtorId());
        assertEquals(10, reloaded.getAddressBook().getNextDebtorId());
    }
    @Test
    void read_impossibleLifecycleTransition_rejectsData() throws Exception {
        Path file = temporaryFolder.resolve("impossible-transition.json");
        java.nio.file.Files.writeString(file, """
                {"persons":[{"name":"Alice Tan","phone":"81234567","email":"alice@example.com",
                "address":"1 Main Street","followUpHistory":[{"timestamp":"2026-10-29T10:00:00",
                "action":"CLEARED","previousDate":null,"newDate":"2026-11-03"}],"tags":[]}]}
                """);
        assertThrows(seedu.address.commons.exceptions.DataLoadingException.class, () ->
                new JsonAddressBookStorage(file).readAddressBook());
    }
    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }
}
