package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code RemarkCommand}.
 */
public class RemarkCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = getTypicalAddressBook();
        Person firstPerson = addressBook.getPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        addressBook.setPerson(firstPerson, new PersonBuilder(firstPerson).withRemark("Existing remark").build());
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_add_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRemark("Likes baseball").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"));
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_remove_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person personToEdit = model.getFilteredPersonList().getFirst();
        Person editedPerson = new PersonBuilder(personToEdit).withRemark("").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_usesIndexInFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToEdit = model.getFilteredPersonList().getFirst();
        Person editedPerson = new PersonBuilder(personToEdit).withRemark("Filtered-list remark").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Filtered-list remark"));
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand command = new RemarkCommand(outOfBoundsIndex, new Remark("Likes baseball"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes baseball"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes football"))));
        RemarkCommand equivalentCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"));
        assertEquals(command.hashCode(), equivalentCommand.hashCode());
    }
}
