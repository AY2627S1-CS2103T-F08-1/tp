package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path DUPLICATE_DEBTOR_ID_FILE =
            TEST_DATA_FOLDER.resolve("duplicateDebtorIdAddressBook.json");
    private static final Path LEGACY_AND_EXPLICIT_DUPLICATE_DEBTOR_ID_FILE =
            TEST_DATA_FOLDER.resolve("legacyAndExplicitDuplicateDebtorIdAddressBook.json");
    private static final Path LEGACY_ZERO_DEBTOR_ID_FILE =
            TEST_DATA_FOLDER.resolve("legacyZeroDebtorIdAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_exhaustedNextDebtorId_roundTrips() throws Exception {
        AddressBook source = new AddressBook();
        source.setNextDebtorId(Integer.MAX_VALUE - 1);
        source.addPerson(TypicalPersons.ALICE.withDebtorId(0));
        assertEquals(Integer.MAX_VALUE, source.getNextDebtorId());

        AddressBook reloaded = new JsonSerializableAddressBook(source).toModelType();

        assertEquals(source, reloaded);
        assertEquals(Integer.MAX_VALUE, reloaded.getNextDebtorId());
    }

    @Test
    public void toModelType_legacyZeroAndMissingDebtorIds_assignsUniqueIds() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(LEGACY_ZERO_DEBTOR_ID_FILE,
                JsonSerializableAddressBook.class).get();

        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(List.of(1, 2), addressBookFromFile.getPersonList().stream()
                .map(Person::getDebtorId).toList());
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateDebtorIds_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_DEBTOR_ID_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, AddressBook.MESSAGE_DUPLICATE_DEBTOR_ID,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_legacyAndExplicitDuplicateDebtorIds_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(
                LEGACY_AND_EXPLICIT_DUPLICATE_DEBTOR_ID_FILE, JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, AddressBook.MESSAGE_DUPLICATE_DEBTOR_ID,
                dataFromFile::toModelType);
    }

}
