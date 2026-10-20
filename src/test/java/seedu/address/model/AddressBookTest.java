package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void addPerson_duplicate_doesNotConsumeId() {
        addressBook.addPerson(ALICE);
        int nextId = addressBook.getNextDebtorId();
        assertThrows(DuplicatePersonException.class, () -> addressBook.addPerson(ALICE));
        assertEquals(nextId, addressBook.getNextDebtorId());
    }

    @Test
    public void setPersons_duplicate_doesNotResetIdCounter() {
        addressBook.addPerson(ALICE);
        addressBook.setNextDebtorId(20);
        assertThrows(DuplicatePersonException.class, () -> addressBook.setPersons(List.of(ALICE, ALICE)));
        assertEquals(20, addressBook.getNextDebtorId());
        assertEquals(1, addressBook.getPersonList().size());
    }

    @Test
    public void addPerson_idOverflow_rejectedWithoutChangingData() {
        assertThrows(IllegalArgumentException.class, () ->
                addressBook.addPerson(ALICE.withDebtorId(Integer.MAX_VALUE)));
        assertEquals(1, addressBook.getNextDebtorId());
        addressBook.setNextDebtorId(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> addressBook.addPerson(ALICE));
        assertEquals(Integer.MAX_VALUE, addressBook.getNextDebtorId());
        assertTrue(addressBook.getPersonList().isEmpty());
    }

    @Test
    public void setPersons_invalidIds_preservesDataAndCounter() {
        addressBook.addPerson(ALICE);
        addressBook.setNextDebtorId(20);
        assertThrows(IllegalArgumentException.class, () ->
                addressBook.setPersons(List.of(ALICE.withDebtorId(Integer.MAX_VALUE))));
        assertThrows(IllegalArgumentException.class, () ->
                addressBook.setPersons(List.of(ALICE.withDebtorId(1), ALICE.withDebtorId(1))));
        assertEquals(20, addressBook.getNextDebtorId());
        assertEquals(1, addressBook.getPersonList().size());
    }

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void resetData_withDuplicateDebtorIds_throwsIllegalArgumentException() {
        Person firstPerson = new PersonBuilder(ALICE).withDebtorId(1).build();
        Person secondPerson = new PersonBuilder().withName("Bob").withPhone("91234567")
                .withEmail("bob@example.com").withDebtorId(1).build();
        AddressBookStub newData = new AddressBookStub(List.of(firstPerson, secondPerson));

        assertThrows(IllegalArgumentException.class, AddressBook.MESSAGE_DUPLICATE_DEBTOR_ID, () ->
                addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void addPerson_assignsIncreasingIdsWithoutReuse() {
        Person firstPerson = new PersonBuilder().withName("Alice").build();
        Person secondPerson = new PersonBuilder().withName("Bob").withPhone("91234567")
                .withEmail("bob@example.com").build();
        Person thirdPerson = new PersonBuilder().withName("Carol").withPhone("92345678")
                .withEmail("carol@example.com").build();

        Person addedFirstPerson = addressBook.addPerson(firstPerson);
        Person addedSecondPerson = addressBook.addPerson(secondPerson);
        addressBook.removePerson(addedSecondPerson);
        Person addedThirdPerson = addressBook.addPerson(thirdPerson);

        assertEquals(1, addedFirstPerson.getDebtorId());
        assertEquals(2, addedSecondPerson.getDebtorId());
        assertEquals(3, addedThirdPerson.getDebtorId());
    }

    @Test
    public void addPerson_withDuplicateDebtorId_throwsIllegalArgumentException() {
        addressBook.addPerson(new PersonBuilder(ALICE).withDebtorId(1).build());
        Person secondPerson = new PersonBuilder().withName("Bob").withPhone("91234567")
                .withEmail("bob@example.com").withDebtorId(1).build();

        assertThrows(IllegalArgumentException.class, AddressBook.MESSAGE_DUPLICATE_DEBTOR_ID, () ->
                addressBook.addPerson(secondPerson));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
