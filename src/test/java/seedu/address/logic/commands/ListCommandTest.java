package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

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
    public void execute_multipleDebtors_showsEveryDebtorInAscendingIdOrder() {
        Person debtorWithIdThree = new PersonBuilder(ALICE)
                .withDebtorId(3).withOutstandingAmount("30.00").build();
        Person debtorWithIdOne = new PersonBuilder(BENSON)
                .withDebtorId(1).withOutstandingAmount("10.00").build();
        Person debtorWithIdTwo = new PersonBuilder(CARL)
                .withDebtorId(2).withOutstandingAmount("20.00").build();
        Model outOfOrderModel = new ModelManager(
                new AddressBookBuilder().withPerson(debtorWithIdThree)
                        .withPerson(debtorWithIdOne).withPerson(debtorWithIdTwo).build(),
                new UserPrefs());
        Model expectedOutOfOrderModel = new ModelManager(outOfOrderModel.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ListCommand(), outOfOrderModel,
                ListCommand.getMessageForDebtorCount(3), expectedOutOfOrderModel);
        assertEquals(List.of(1, 2, 3), outOfOrderModel.getFilteredPersonList().stream()
                .map(Person::getDebtorId).toList());
        assertEquals(List.of(debtorWithIdOne.getName(), debtorWithIdTwo.getName(), debtorWithIdThree.getName()),
                outOfOrderModel.getFilteredPersonList().stream().map(Person::getName).toList());
        assertEquals(List.of(debtorWithIdOne.getPhone(), debtorWithIdTwo.getPhone(), debtorWithIdThree.getPhone()),
                outOfOrderModel.getFilteredPersonList().stream().map(Person::getPhone).toList());
        assertEquals(List.of(debtorWithIdOne.getOutstandingAmount(), debtorWithIdTwo.getOutstandingAmount(),
                        debtorWithIdThree.getOutstandingAmount()),
                outOfOrderModel.getFilteredPersonList().stream().map(Person::getOutstandingAmount).toList());
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model,
                ListCommand.getMessageForDebtorCount(getTypicalPersons().size()), expectedModel);
    }
}
