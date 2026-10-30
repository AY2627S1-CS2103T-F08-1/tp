package seedu.address.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Finds an active debtor by persistent ID across all records, regardless of the displayed filter or order.
     * Returns empty when that ID is absent, including after deletion. This query does not change the view or data.
     */
    default Optional<Person> findPersonById(int debtorId) {
        return getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst();
    }

    /**
     * Returns true if a person with the same phone number or email address as {@code person} exists in the
     * address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    Person addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /** Records the current address-book state when it differs from the latest history entry. */
    default void commitAddressBook() {
    }

    /** Returns whether an earlier address-book state can be restored. */
    default boolean canUndoAddressBook() {
        return false;
    }

    /** Restores the preceding address-book state and shows all debtors in persistent-ID order. */
    default void undoAddressBook() {
        throw new IllegalStateException("Undo is not supported.");
    }

    /** Returns whether a later address-book state can be restored. */
    default boolean canRedoAddressBook() {
        return false;
    }

    /** Restores the next address-book state and shows all debtors in persistent-ID order. */
    default void redoAddressBook() {
        throw new IllegalStateException("Redo is not supported.");
    }


    /**
     * Updates the comparator of the displayed person list without changing its active filter.
     * @throws NullPointerException if {@code comparator} is null.
     */
    void updateSortedPersonList(Comparator<Person> comparator);

    /**
     * Replaces both the predicate and comparator of the displayed person list.
     * @throws NullPointerException if {@code predicate} or {@code comparator} is null.
     */
    void updatePersonView(Predicate<Person> predicate, Comparator<Person> comparator);

    /** Returns an isolated model with the same address-book data, history and active view for a save transaction. */
    default Model createTransactionCopy() {
        throw new UnsupportedOperationException("This model does not support save transactions.");
    }

    /** Publishes data, history and view state from a successfully persisted transaction candidate. */
    default void publishTransaction(Model candidate) {
        throw new UnsupportedOperationException("This model does not support save transactions.");
    }
}
