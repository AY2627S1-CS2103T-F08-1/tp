package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/** Verifies that an amount-only edit survives the storage boundary. */
public class EditOutstandingAmountIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void amountOnlyEdit_saveAndReload_preservesIdAndNotes() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        Model model = new ModelManager();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/123 Street o/1250").execute(model);
        parser.parseCommand("note --id 1 --text Promised payment on Friday.").execute(model);
        List<InteractionNote> originalNotes = List.copyOf(
                model.getFilteredPersonList().getFirst().getInteractionNotes());
        parser.parseCommand("edit 1 o/S$500.50").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Person reloaded = reloadedModel.getFilteredPersonList().getFirst();

        assertEquals(1, reloaded.getDebtorId());
        assertEquals("500.50", reloaded.getOutstandingAmount().toString());
        assertEquals(originalNotes, reloaded.getInteractionNotes());
        assertEquals(2, reloadedModel.getAddressBook().getNextDebtorId());
    }
}
