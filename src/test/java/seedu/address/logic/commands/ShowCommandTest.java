package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ShowCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validDebtorIds_showsCompleteProfiles() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person firstDebtor = model.getAddressBook().getPersonList().getFirst();
        Person lastDebtor = model.getAddressBook().getPersonList().getLast();

        assertCommandSuccess(new ShowCommand(firstDebtor.getDebtorId()), model,
                Messages.formatProfile(firstDebtor), expectedModel);
        assertCommandSuccess(new ShowCommand(lastDebtor.getDebtorId()), model,
                Messages.formatProfile(lastDebtor), expectedModel);
    }

    @Test
    public void execute_debtorFilteredOut_showsProfileWithoutChangingFilter() {
        Person hiddenDebtor = model.getAddressBook().getPersonList().getFirst();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(new ShowCommand(hiddenDebtor.getDebtorId()), model,
                Messages.formatProfile(hiddenDebtor), expectedModel);
        assertFalse(model.getFilteredPersonList().contains(hiddenDebtor));
    }

    @Test
    public void execute_addressBookOrderDiffersFromDebtorId_selectsById() {
        AddressBook addressBook = new AddressBook();
        Person debtorWithIdNine = new PersonBuilder().withName("Debtor Nine")
                .withPhone("90000009").withEmail("nine@example.com").withDebtorId(9).build();
        Person debtorWithIdTwo = new PersonBuilder().withName("Debtor Two")
                .withPhone("90000002").withEmail("two@example.com").withDebtorId(2).build();
        addressBook.addPerson(debtorWithIdNine);
        addressBook.addPerson(debtorWithIdTwo);
        Model outOfOrderModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());

        assertCommandSuccess(new ShowCommand(2), outOfOrderModel,
                Messages.formatProfile(debtorWithIdTwo), expectedModel);
    }

    @Test
    public void execute_missingDebtorId_throwsCommandException() {
        ShowCommand showCommand = new ShowCommand(999);

        assertCommandFailure(showCommand, model, String.format(ShowCommand.MESSAGE_DEBTOR_NOT_FOUND, 999));
    }

    @Test
    public void execute_deletedDebtorId_throwsCommandException() {
        Person deletedDebtor = model.getAddressBook().getPersonList().getFirst();
        int deletedDebtorId = deletedDebtor.getDebtorId();
        model.deletePerson(deletedDebtor);

        assertCommandFailure(new ShowCommand(deletedDebtorId), model,
                String.format(ShowCommand.MESSAGE_DEBTOR_NOT_FOUND, deletedDebtorId));
    }

    @Test
    public void equals() {
        ShowCommand firstCommand = new ShowCommand(1);
        ShowCommand firstCommandCopy = new ShowCommand(1);
        ShowCommand differentIdCommand = new ShowCommand(2);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(firstCommandCopy));
        assertFalse(firstCommand.equals(differentIdCommand));
        assertFalse(firstCommand.equals(1));
        assertFalse(firstCommand.equals(null));
    }

    @Test
    public void toStringMethod() {
        ShowCommand showCommand = new ShowCommand(7);
        String expected = ShowCommand.class.getCanonicalName() + "{debtorId=7}";

        assertEquals(expected, showCommand.toString());
    }
}
