package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonContactPreferencesTest {
    @TempDir
    public Path tempDir;

    @Test
    public void saveAndReload_preservesPreferences() throws Exception {
        AddressBook book = new AddressBook();
        Person added = book.addPerson(new PersonBuilder().build()
                .withContactPreferences(new ContactPreferences("sms", "09:00-17:00")));
        Path path = tempDir.resolve("book.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        storage.saveAddressBook(book, path);
        Person restored = storage.readAddressBook(path).orElseThrow().getPersonList().getFirst();
        assertEquals(added.getDebtorId(), restored.getDebtorId());
        assertEquals(new ContactPreferences("sms", "09:00-17:00"), restored.getContactPreferences());
    }
    @Test
    public void personAdapter_roundTripAndLegacyDefaults() throws Exception {
        Person original = new PersonBuilder().withDebtorId(7).build()
                .withContactPreferences(new ContactPreferences("email", "08:00-17:00"));
        Person restored = new JsonAdaptedPerson(original).toModelType();
        assertEquals(7, restored.getDebtorId());
        assertEquals(original.getContactPreferences(), restored.getContactPreferences());
        Person legacy = new JsonAdaptedPersonBuilder(
                original.getName().toString(), original.getPhone().toString(),
                original.getEmail().toString(), original.getAddress().toString(), null)
                .build()
                .toModelType();
        assertEquals(new ContactPreferences(null, null), legacy.getContactPreferences());
    }

    @Test
    public void malformedStoredValues_areRejected() {
        assertThrows(IllegalValueException.class, () -> new JsonContactPreferences("fax", null).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonContactPreferences(null, "18:00-09:00").toModelType());
    }
}
