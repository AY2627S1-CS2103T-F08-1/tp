package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns the next persistent debtor ID. Implementations without ID state derive a safe value from the list.
     */
    default int getNextDebtorId() {
        return getPersonList().stream().mapToInt(Person::getDebtorId).max().orElse(0) + 1;
    }

}
