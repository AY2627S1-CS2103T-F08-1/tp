package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Exercises ID allocation at the boundary between stored IDs and legacy records. */
public class JsonLegacyIdBoundaryTest {
    @TempDir
    public Path folder;

    @Test
    public void read_legacyRecordAfterIdExhaustion_reportsLoadingErrorAndPreservesFile() throws Exception {
        for (String legacyId : List.of("", "\"debtorId\":0,")) {
            Path file = folder.resolve("exhausted.json");
            String json = records("\"debtorId\":2147483646,", legacyId);
            Files.writeString(file, json);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

            DataLoadingException error = assertThrows(DataLoadingException.class, storage::readAddressBook);

            assertTrue(error.getMessage().contains(AddressBook.MESSAGE_DEBTOR_ID_OUT_OF_RANGE));
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void read_legacyRecordGetsLastAvailableId_roundTripsWithoutReusingIds() throws Exception {
        for (String legacyId : List.of("", "\"debtorId\":0,")) {
            Path file = folder.resolve("last-available.json");
            Files.writeString(file, records("\"debtorId\":2147483645,", legacyId));
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

            ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

            assertEquals(List.of(2147483645, 2147483646), ids(loaded));
            assertEquals(Integer.MAX_VALUE, loaded.getNextDebtorId());
            storage.saveAddressBook(loaded);
            ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
            assertEquals(loaded, reloaded);
            assertEquals(Integer.MAX_VALUE, reloaded.getNextDebtorId());
        }
    }

    @Test
    public void read_explicitLowerIdAfterExhaustedCounter_preservesExistingIds() throws Exception {
        Path file = folder.resolve("out-of-order.json");
        Files.writeString(file, records("\"debtorId\":2147483646,", "\"debtorId\":1,"));

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();

        assertEquals(List.of(2147483646, 1), ids(loaded));
        assertEquals(Integer.MAX_VALUE, loaded.getNextDebtorId());
    }

    private String records(String firstId, String secondId) {
        return "{\"persons\":[{" + firstId
                + "\"name\":\"Alice\",\"phone\":\"81111111\",\"email\":\"alice@example.com\","
                + "\"address\":\"First Street\"},{" + secondId
                + "\"name\":\"Bob\",\"phone\":\"82222222\",\"email\":\"bob@example.com\","
                + "\"address\":\"Second Street\"}]}";
    }

    private List<Integer> ids(ReadOnlyAddressBook book) {
        return book.getPersonList().stream().map(Person::getDebtorId).toList();
    }
}
