package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_emptyList_showsHelpfulMessage() {
        Model emptyModel = new ModelManager();
        Model expectedEmptyModel = new ModelManager();

        assertCommandSuccess(new ListCommand(), emptyModel,
                ListCommand.MESSAGE_EMPTY_LIST, expectedEmptyModel);
    }

    @Test
    public void execute_singleDebtor_showsDebtorAndCount() {
        Model singleDebtorModel = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());
        Model expectedSingleDebtorModel = new ModelManager(
                singleDebtorModel.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ListCommand(), singleDebtorModel,
                ListCommand.MESSAGE_SINGLE_DEBTOR, expectedSingleDebtorModel);
        assertEquals(ALICE, singleDebtorModel.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_multipleDebtors_showsEveryDebtorInStoredOrder() {
        assertCommandSuccess(new ListCommand(), model,
                ListCommand.getMessageForDebtorCount(getTypicalPersons().size()), expectedModel);
        assertEquals(getTypicalPersons(), model.getFilteredPersonList());
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model,
                ListCommand.getMessageForDebtorCount(getTypicalPersons().size()), expectedModel);
    }
}
