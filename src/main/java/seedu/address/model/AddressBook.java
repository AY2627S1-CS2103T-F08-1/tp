package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private int nextDebtorId = 1;

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        nextDebtorId = 1;
        List<Person> assignedPersons = new ArrayList<>();
        Set<Integer> usedDebtorIds = new HashSet<>();
        for (Person person : persons) {
            Person assignedPerson = person.getDebtorId() == 0 ? person.withDebtorId(nextDebtorId++) : person;
            if (!usedDebtorIds.add(assignedPerson.getDebtorId())) {
                throw new IllegalArgumentException("Debtor ID must be unique.");
            }
            nextDebtorId = Math.max(nextDebtorId, assignedPerson.getDebtorId() + 1);
            assignedPersons.add(assignedPerson);
        }
        this.persons.setPersons(assignedPersons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        nextDebtorId = Math.max(nextDebtorId, newData.getNextDebtorId());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same phone number or email address as {@code person} exists in the
     * address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public Person addPerson(Person p) {
        Person assignedPerson = assignDebtorId(p);
        persons.add(assignedPerson);
        return assignedPerson;
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public int getNextDebtorId() {
        return nextDebtorId;
    }

    /** Restores the next persistent debtor ID after loading saved data. */
    public void setNextDebtorId(int nextDebtorId) {
        if (nextDebtorId < this.nextDebtorId) {
            throw new IllegalArgumentException("Next debtor ID cannot be reused.");
        }
        this.nextDebtorId = nextDebtorId;
    }

    private Person assignDebtorId(Person person) {
        if (person.getDebtorId() == 0) {
            Person assignedPerson = person.withDebtorId(nextDebtorId);
            nextDebtorId++;
            return assignedPerson;
        }
        if (persons.asUnmodifiableObservableList().stream()
                .anyMatch(existingPerson -> existingPerson.getDebtorId() == person.getDebtorId())) {
            throw new IllegalArgumentException("Debtor ID must be unique.");
        }
        nextDebtorId = Math.max(nextDebtorId, person.getDebtorId() + 1);
        return person;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
