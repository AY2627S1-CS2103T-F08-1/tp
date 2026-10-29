package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSort;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final int MAX_HISTORY_SIZE = 100;
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final List<AddressBook> addressBookHistory = new ArrayList<>();
    private int historyPointer;
    private Predicate<Person> personListPredicate = PREDICATE_SHOW_ALL_PERSONS;
    private Comparator<Person> personListComparator = PersonSort.DEFAULT_COMPARATOR;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing model with " + addressBook.getPersonList().size() + " address book entries");

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons, PersonSort.DEFAULT_COMPARATOR);
        addressBookHistory.add(new AddressBook(this.addressBook));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public Person addPerson(Person person) {
        Person addedPerson = addressBook.addPerson(person);
        updatePersonView(PREDICATE_SHOW_ALL_PERSONS, PersonSort.DEFAULT_COMPARATOR);
        return addedPerson;
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        personListPredicate = predicate;
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void commitAddressBook() {
        AddressBook current = new AddressBook(addressBook);
        AddressBook latest = addressBookHistory.get(historyPointer);
        if (latest.equals(current) && latest.getNextDebtorId() == current.getNextDebtorId()) {
            return;
        }
        while (addressBookHistory.size() > historyPointer + 1) {
            addressBookHistory.removeLast();
        }
        addressBookHistory.add(current);
        historyPointer++;
        if (addressBookHistory.size() > MAX_HISTORY_SIZE) {
            addressBookHistory.removeFirst();
            historyPointer--;
        }
    }

    @Override
    public boolean canUndoAddressBook() {
        return historyPointer > 0;
    }

    @Override
    public void undoAddressBook() {
        if (!canUndoAddressBook()) {
            throw new IllegalStateException("No address-book state to undo.");
        }
        addressBook.resetData(addressBookHistory.get(--historyPointer));
        resetView();
    }

    @Override
    public boolean canRedoAddressBook() {
        return historyPointer < addressBookHistory.size() - 1;
    }

    @Override
    public void redoAddressBook() {
        if (!canRedoAddressBook()) {
            throw new IllegalStateException("No address-book state to redo.");
        }
        addressBook.resetData(addressBookHistory.get(++historyPointer));
        resetView();
    }

    /** Shows every debtor in persistent-ID order, discarding any active filter. */
    private void resetView() {
        updatePersonView(PREDICATE_SHOW_ALL_PERSONS, PersonSort.DEFAULT_COMPARATOR);
    }

    @Override
    public void updateSortedPersonList(Comparator<Person> comparator) {
        requireNonNull(comparator);
        personListComparator = comparator;
        sortedPersons.setComparator(comparator);
    }

    @Override
    public void updatePersonView(Predicate<Person> predicate, Comparator<Person> comparator) {
        requireAllNonNull(predicate, comparator);
        personListPredicate = predicate;
        personListComparator = comparator;
        filteredPersons.setPredicate(predicate);
        sortedPersons.setComparator(comparator);
    }

    @Override
    public Model createTransactionCopy() {
        ModelManager candidate = new ModelManager(addressBook, userPrefs);
        // Snapshots are never mutated after they are recorded, so the candidate can share them.
        candidate.addressBookHistory.clear();
        candidate.addressBookHistory.addAll(addressBookHistory);
        candidate.historyPointer = historyPointer;
        candidate.updatePersonView(personListPredicate, personListComparator);
        return candidate;
    }

    @Override
    public void publishTransaction(Model candidate) {
        requireNonNull(candidate);
        if (!(candidate instanceof ModelManager candidateManager)) {
            throw new IllegalArgumentException("Transaction candidate must be a ModelManager.");
        }
        addressBook.resetData(candidateManager.addressBook);
        addressBookHistory.clear();
        addressBookHistory.addAll(candidateManager.addressBookHistory);
        historyPointer = candidateManager.historyPointer;
        updatePersonView(candidateManager.personListPredicate, candidateManager.personListComparator);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && sortedPersons.equals(otherModelManager.sortedPersons);
    }

}
