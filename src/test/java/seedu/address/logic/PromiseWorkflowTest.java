package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Verifies promised payment dates across other mutations, filtering, persistence, and profile display. */
class PromiseWorkflowTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    void execute_setOtherMutationsReloadAndClear_preservesIndependentDate() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withOutstandingAmount("123.45").build());
        int debtorId = debtor.getDebtorId();
        LocalDate promisedDate = LocalDate.of(2026, 11, 3);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Logic logic = createLogic(model, storage);

        logic.execute("promise --id " + debtorId + " --date " + promisedDate);
        logic.execute("edit 1 a/Updated Street");
        logic.execute("note --id " + debtorId + " --text Confirmed promised payment.");
        logic.execute("followup --id " + debtorId + " --date 2026-11-01");

        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person restored = reloadedModel.getAddressBook().getPersonList().getFirst();
        assertEquals(Optional.of(promisedDate), restored.getPromisedPaymentDate());
        assertEquals("123.45", restored.getOutstandingAmount().toString());
        assertEquals(Optional.of(LocalDate.of(2026, 11, 1)), restored.getFollowUpDate());
        Logic reloadedLogic = createLogic(reloadedModel, storage);
        assertTrue(reloadedLogic.execute("show --id " + debtorId).getFeedbackToUser()
                .contains("Promised payment: " + promisedDate));

        reloadedLogic.execute("find nobody-matches-this");
        reloadedLogic.execute("promise --id " + debtorId + " --clear");
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList()
                .getFirst().getPromisedPaymentDate().isEmpty());
    }

    @Test
    void execute_noChange_doesNotWriteAddressBook() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 3);
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder()
                .withPromisedPaymentDate(Optional.of(date)).build());
        CountingAddressBookStorage storage = new CountingAddressBookStorage(
                temporaryFolder.resolve("addressBook.json"));
        Logic logic = createLogic(model, storage);

        logic.execute("promise --id " + debtor.getDebtorId() + " --date " + date);

        assertEquals(0, storage.saveCount);
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    /** Counts attempted writes so no-change commands can be verified without relying on file timestamps. */
    private static class CountingAddressBookStorage extends JsonAddressBookStorage {
        private int saveCount;

        CountingAddressBookStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            super.saveAddressBook(addressBook);
        }
    }
}
