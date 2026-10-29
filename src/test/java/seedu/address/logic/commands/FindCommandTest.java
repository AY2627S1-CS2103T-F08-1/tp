package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSort;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_noMatchingKeyword_showsHelpfulEmptyResult() {
        NameContainsKeywordsPredicate predicate = preparePredicate("NotFound");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, FindCommand.MESSAGE_NO_DEBTORS_FOUND, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_exactPhoneKeyword_singlePersonFound() {
        NameContainsKeywordsPredicate predicate = preparePredicate(ALICE.getPhone().value);
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(command, model, FindCommand.MESSAGE_SINGLE_DEBTOR_FOUND, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(ALICE.getPhone(), model.getFilteredPersonList().get(0).getPhone());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = FindCommand.getMessageForMatchCount(3);
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameAndPhoneKeywords_returnsMatchesInAscendingDebtorIdOrder() {
        Person debtorWithIdNine = new PersonBuilder(CARL).withDebtorId(9).build();
        Person debtorWithIdTwo = new PersonBuilder(ALICE).withDebtorId(2).build();
        Person debtorWithIdFive = new PersonBuilder(BENSON).withDebtorId(5).build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(debtorWithIdNine)
                .withPerson(debtorWithIdFive).withPerson(debtorWithIdTwo).build();
        Model outOfOrderModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedOutOfOrderModel = new ModelManager(addressBook, new UserPrefs());
        NameContainsKeywordsPredicate predicate =
                new NameContainsKeywordsPredicate(List.of("Kurz", debtorWithIdTwo.getPhone().value));
        expectedOutOfOrderModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FindCommand(predicate), outOfOrderModel,
                FindCommand.getMessageForMatchCount(2), expectedOutOfOrderModel);
        assertEquals(List.of(2, 9), outOfOrderModel.getFilteredPersonList().stream()
                .map(Person::getDebtorId).toList());
    }

    @Test
    public void execute_afterDescendingSort_resetsMatchingResultsToAscendingIdOrder() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Meier"));
        model.updateSortedPersonList(PersonSort.createComparator(Field.ID, Direction.DESCENDING));

        new FindCommand(predicate).execute(model);

        assertEquals(List.of(2, 4), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}
