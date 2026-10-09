package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexMiddleOfUnfilteredList_success() {
        Index middleIndex = Index.fromOneBased(model.getFilteredPersonList().size() / 2 + 1);
        Person personToDelete = model.getFilteredPersonList().get(middleIndex.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(middleIndex);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexLastInUnfilteredList_success() {
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person personToDelete = model.getFilteredPersonList().get(lastIndex.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(lastIndex);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexOnlyPerson_success() {
        ModelManager onePersonModel = new ModelManager();
        onePersonModel.addPerson(AMY);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(AMY));
        ModelManager expectedModel = new ModelManager();

        assertCommandSuccess(deleteCommand, onePersonModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexSimilarPeople_deletesOnlyPersonAtDisplayedIndex() {
        Person firstAmy = AMY;
        Person secondAmy = new PersonBuilder(AMY).withName("Amy Bees").withPhone("82223333").build();
        ModelManager similarPeopleModel = new ModelManager();
        similarPeopleModel.addPerson(firstAmy);
        similarPeopleModel.addPerson(secondAmy);
        assertTrue(secondAmy.getName().toString().startsWith(firstAmy.getName().toString()));
        assertEquals(2, similarPeopleModel.getFilteredPersonList().size());

        Person personToDelete = similarPeopleModel.getFilteredPersonList().get(1);
        Person personToKeep = similarPeopleModel.getFilteredPersonList().get(0);
        DeleteCommand deleteCommand = new DeleteCommand(Index.fromOneBased(2));
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));
        ModelManager expectedModel = new ModelManager(similarPeopleModel.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, similarPeopleModel, expectedMessage, expectedModel);
        assertEquals(List.of(personToKeep), similarPeopleModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);
        int displayedPersonCount = model.getFilteredPersonList().size();
        String expectedMessage = String.format(
                "Index %d is out of range. The displayed list has %d people; valid range is 1 to %d.",
                outOfBoundIndex.getOneBased(), displayedPersonCount, displayedPersonCount);

        assertCommandFailure(deleteCommand, model, expectedMessage);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredListWithRemainingMatches_success() {
        NameContainsKeywordsPredicate meierPredicate = new NameContainsKeywordsPredicate(List.of("Meier"));
        model.updateFilteredPersonList(meierPredicate);
        assertEquals(2, model.getFilteredPersonList().size());

        Person personToDelete = model.getFilteredPersonList().get(1);
        DeleteCommand deleteCommand = new DeleteCommand(Index.fromOneBased(2));
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        expectedModel.updateFilteredPersonList(meierPredicate);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model,
                Messages.getInvalidPersonDisplayedIndexMessage(
                        BigInteger.valueOf(outOfBoundIndex.getOneBased()), model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_invalidIndexEmptyList_showsListHint() {
        Model emptyModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        emptyModel.updateFilteredPersonList(person -> false);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        assertCommandFailure(deleteCommand, emptyModel, "Index 1 is invalid because the displayed list is empty "
                + "(0 people; no valid index range). Run the list command or adjust your find query before deleting.");
    }

    @Test
    public void execute_positiveIndexBeyondIntegerRange_showsDisplayedRange() {
        DeleteCommand deleteCommand = DeleteCommand.fromOneBased(new BigInteger("9".repeat(100)));

        assertCommandFailure(deleteCommand, model,
                Messages.getInvalidPersonDisplayedIndexMessage(
                        new BigInteger("9".repeat(100)), model.getFilteredPersonList().size()));
    }

    @Test
    public void fromOneBased_nonPositiveIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DeleteCommand.fromOneBased(BigInteger.ZERO));
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());

        BigInteger largeIndex = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        DeleteCommand largeIndexCommand = DeleteCommand.fromOneBased(largeIndex);
        String expectedForLargeIndex = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + largeIndex + "}";
        assertEquals(expectedForLargeIndex, largeIndexCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
