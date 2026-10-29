package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSort;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void getFilteredPersonList_mutations_refreshInAscendingDebtorIdOrder() {
        Person debtorWithIdThree = new PersonBuilder(ALICE).withDebtorId(3).build();
        Person debtorWithIdOne = new PersonBuilder(BENSON).withDebtorId(1).build();
        Person debtorWithIdTwo = new PersonBuilder(CARL).withDebtorId(2).build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(debtorWithIdThree)
                .withPerson(debtorWithIdOne).withPerson(debtorWithIdTwo).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());

        assertEquals(List.of(1, 2, 3), getDisplayedDebtorIds(modelManager));

        Person debtorWithIdFour = new PersonBuilder()
                .withName("Dora Tan").withPhone("81234567").withEmail("dora@example.com")
                .withDebtorId(4).build();
        modelManager.addPerson(debtorWithIdFour);
        assertEquals(List.of(1, 2, 3, 4), getDisplayedDebtorIds(modelManager));

        Person editedDebtorWithIdTwo = new PersonBuilder(debtorWithIdTwo).withName("Carl Tan").build();
        modelManager.setPerson(debtorWithIdTwo, editedDebtorWithIdTwo);
        assertEquals(List.of(1, 2, 3, 4), getDisplayedDebtorIds(modelManager));
        assertEquals("Carl Tan", modelManager.getFilteredPersonList().get(1).getName().fullName);

        modelManager.deletePerson(debtorWithIdOne);
        assertEquals(List.of(2, 3, 4), getDisplayedDebtorIds(modelManager));
    }

    @Test
    public void undoRedo_restoresDataNeverReusesIdsAndResetsView() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        int firstId = modelManager.getAddressBook().getPersonList().get(0).getDebtorId();
        modelManager.updateFilteredPersonList(person -> person.getName().fullName.contains("Benson"));
        modelManager.deletePerson(modelManager.getAddressBook().getPersonList().get(0));
        modelManager.commitAddressBook();

        assertTrue(modelManager.canUndoAddressBook());
        modelManager.undoAddressBook();
        assertEquals(1, modelManager.getAddressBook().getPersonList().size());
        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertTrue(modelManager.canRedoAddressBook());
        modelManager.updateFilteredPersonList(person -> false);
        modelManager.redoAddressBook();
        assertEquals(0, modelManager.getAddressBook().getPersonList().size());
        modelManager.undoAddressBook();
        modelManager.deletePerson(modelManager.getAddressBook().getPersonList().get(0));
        modelManager.commitAddressBook();
        Person next = modelManager.addPerson(BENSON);
        assertTrue(next.getDebtorId() > firstId);
    }

    @Test
    public void undoRedo_historyIsBoundedToOneHundredStatesAndNewBranchClearsRedo() {
        for (int i = 0; i < 100; i++) {
            Person debtor = new PersonBuilder().withName("Debtor " + i)
                    .withPhone(String.format("8%07d", i))
                    .withEmail("debtor" + i + "@example.com").build();
            modelManager.addPerson(debtor);
            modelManager.commitAddressBook();
        }

        for (int i = 0; i < 99; i++) {
            assertTrue(modelManager.canUndoAddressBook());
            modelManager.undoAddressBook();
        }
        assertFalse(modelManager.canUndoAddressBook());
        assertEquals(1, modelManager.getAddressBook().getPersonList().size());

        for (int i = 0; i < 99; i++) {
            assertTrue(modelManager.canRedoAddressBook());
            modelManager.redoAddressBook();
        }
        assertFalse(modelManager.canRedoAddressBook());
        assertEquals(100, modelManager.getAddressBook().getPersonList().size());

        modelManager.undoAddressBook();
        modelManager.deletePerson(modelManager.getAddressBook().getPersonList().getLast());
        modelManager.commitAddressBook();
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void updateSortedPersonList_preservesFilterAndUsesComparatorForLaterMutations() {
        Person alpha = new PersonBuilder(ALICE).withName("Alpha Tan").withDebtorId(3)
                .withOutstandingAmount("10.00").build();
        Person beta = new PersonBuilder(BENSON).withName("Beta Lim").withDebtorId(1)
                .withOutstandingAmount("30.00").build();
        Person gamma = new PersonBuilder(CARL).withName("Gamma Ong").withDebtorId(2)
                .withOutstandingAmount("20.00").build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(alpha).withPerson(beta).withPerson(gamma).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        modelManager.updateFilteredPersonList(person -> person.getDebtorId() != gamma.getDebtorId());

        modelManager.updateSortedPersonList(PersonSort.createComparator(Field.AMOUNT, Direction.DESCENDING));

        assertEquals(List.of(1, 3), getDisplayedDebtorIds(modelManager));
        Person editedAlpha = new PersonBuilder(alpha).withOutstandingAmount("40.00").build();
        modelManager.setPerson(alpha, editedAlpha);
        assertEquals(List.of(3, 1), getDisplayedDebtorIds(modelManager));
    }

    @Test
    public void updatePersonView_replacesFilterAndComparatorTogether() {
        Person alpha = new PersonBuilder(ALICE).withName("Alpha Tan").withDebtorId(3).build();
        Person beta = new PersonBuilder(BENSON).withName("Beta Lim").withDebtorId(1).build();
        Person gamma = new PersonBuilder(CARL).withName("Gamma Ong").withDebtorId(2).build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(alpha).withPerson(beta).withPerson(gamma).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());

        modelManager.updatePersonView(person -> person.getDebtorId() != beta.getDebtorId(),
                PersonSort.createComparator(Field.NAME, Direction.DESCENDING));

        assertEquals(List.of(2, 3), getDisplayedDebtorIds(modelManager));
    }

    @Test
    public void updatePersonView_nullArguments_throwNullPointerExceptionWithoutChangingView() {
        modelManager = new ModelManager(getTypicalAddressBookForNullViewTest(), new UserPrefs());
        List<Person> originalView = List.copyOf(modelManager.getFilteredPersonList());

        assertThrows(NullPointerException.class, () ->
                modelManager.updatePersonView(null, PersonSort.DEFAULT_COMPARATOR));
        assertThrows(NullPointerException.class, () ->
                modelManager.updatePersonView(PREDICATE_SHOW_ALL_PERSONS, null));
        assertThrows(NullPointerException.class, () -> modelManager.updateSortedPersonList(null));
        assertEquals(originalView, modelManager.getFilteredPersonList());
    }

    private static AddressBook getTypicalAddressBookForNullViewTest() {
        return new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
    }

    private static List<Integer> getDisplayedDebtorIds(ModelManager modelManager) {
        return modelManager.getFilteredPersonList().stream().map(Person::getDebtorId).toList();
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void createTransactionCopy_isIsolatedUntilPublished() {
        modelManager.addPerson(ALICE);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        Model candidate = modelManager.createTransactionCopy();

        assertEquals(modelManager.getFilteredPersonList(), candidate.getFilteredPersonList());
        candidate.addPerson(BENSON);
        assertEquals(1, modelManager.getAddressBook().getPersonList().size());

        modelManager.publishTransaction(candidate);
        assertEquals(2, modelManager.getAddressBook().getPersonList().size());
        assertEquals(candidate.getFilteredPersonList(), modelManager.getFilteredPersonList());
    }

    @Test
    public void transactionCopy_carriesSortOrderThroughPublish() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        Comparator<Person> byNameDescending = Comparator.comparing((Person person) -> person.getName().fullName)
                .reversed();
        modelManager.updateSortedPersonList(byNameDescending);
        List<Person> sortedView = List.copyOf(modelManager.getFilteredPersonList());

        Model candidate = modelManager.createTransactionCopy();
        assertEquals(sortedView, candidate.getFilteredPersonList());
        candidate.updatePersonView(PREDICATE_SHOW_ALL_PERSONS, byNameDescending);
        modelManager.publishTransaction(candidate);
        assertEquals(sortedView, modelManager.getFilteredPersonList());
    }

    @Test
    public void publishTransaction_invalidCandidate_throws() {
        assertThrows(NullPointerException.class, () -> modelManager.publishTransaction(null));
        assertThrows(IllegalArgumentException.class, () -> modelManager.publishTransaction(unsupportedModel()));
    }

    @Test
    public void transactionDefaults_unsupportedModel_throwUnsupportedOperationException() {
        Model unsupported = unsupportedModel();
        assertThrows(UnsupportedOperationException.class, unsupported::createTransactionCopy);
        assertThrows(UnsupportedOperationException.class, () -> unsupported.publishTransaction(modelManager));
    }

    @Test
    public void historyDefaults_unsupportedModel_reportNoHistory() {
        Model unsupported = unsupportedModel();
        unsupported.commitAddressBook();
        assertFalse(unsupported.canUndoAddressBook());
        assertFalse(unsupported.canRedoAddressBook());
        assertThrows(IllegalStateException.class, unsupported::undoAddressBook);
        assertThrows(IllegalStateException.class, unsupported::redoAddressBook);
    }

    @Test
    public void undoRedo_noHistory_throwsIllegalStateException() {
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
        assertThrows(IllegalStateException.class, modelManager::undoAddressBook);
        assertThrows(IllegalStateException.class, modelManager::redoAddressBook);
    }

    @Test
    public void commitAddressBook_unchangedState_addsNoHistory() {
        modelManager.commitAddressBook();
        assertFalse(modelManager.canUndoAddressBook());

        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.commitAddressBook();
        modelManager.undoAddressBook();
        assertFalse(modelManager.canUndoAddressBook());
    }

    /** Returns a {@code Model} that only provides the interface's default methods. */
    private static Model unsupportedModel() {
        InvocationHandler defaultsOnly = (proxy, method, args) -> InvocationHandler.invokeDefault(proxy, method, args);
        return (Model) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class<?>[] {Model.class}, defaultsOnly);
    }
}
