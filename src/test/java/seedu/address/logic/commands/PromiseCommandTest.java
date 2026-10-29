package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class PromiseCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    @Test
    void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PromiseCommand(1, (LocalDate) null));
        assertThrows(NullPointerException.class, () -> new PromiseCommand(1, (Optional<LocalDate>) null));
    }

    @Test
    void execute_setAndClear_preservesIndependentFields() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withOutstandingAmount("123.45")
                .withFollowUpDate(Optional.of(LocalDate.of(2026, 10, 31))).build());

        CommandResult setResult = new PromiseCommand(debtor.getDebtorId(), DATE).execute(model);
        Person afterSet = model.getAddressBook().getPersonList().getFirst();
        assertEquals(Optional.of(DATE), afterSet.getPromisedPaymentDate());
        assertEquals(debtor.getOutstandingAmount(), afterSet.getOutstandingAmount());
        assertEquals(debtor.getFollowUpDate(), afterSet.getFollowUpDate());
        assertEquals(String.format(PromiseCommand.MESSAGE_SET_SUCCESS, DATE, debtor.getDebtorId()),
                setResult.getFeedbackToUser());

        new PromiseCommand(debtor.getDebtorId(), Optional.empty()).execute(model);
        assertTrue(model.getAddressBook().getPersonList().getFirst().getPromisedPaymentDate().isEmpty());
    }

    @Test
    void execute_hiddenDebtor_updatesByPersistentId() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        model.updateFilteredPersonList(person -> false);

        new PromiseCommand(debtor.getDebtorId(), DATE).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(Optional.of(DATE), model.getAddressBook().getPersonList()
                .getFirst().getPromisedPaymentDate());
    }

    @Test
    void execute_sameDateOrClearUnset_reportsNoChangeAndDoesNotRequestSave() throws Exception {
        Model model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().withPromisedPaymentDate(Optional.of(DATE)).build());
        PromiseCommand sameDate = new PromiseCommand(debtor.getDebtorId(), DATE);

        CommandResult sameDateResult = sameDate.execute(model);

        assertFalse(sameDate.shouldSaveAddressBook());
        assertEquals(String.format(PromiseCommand.MESSAGE_SAME_DATE, debtor.getDebtorId(), DATE),
                sameDateResult.getFeedbackToUser());

        Model unsetModel = new ModelManager();
        Person unsetDebtor = unsetModel.addPerson(new PersonBuilder().build());
        PromiseCommand clearUnset = new PromiseCommand(unsetDebtor.getDebtorId(), Optional.empty());
        CommandResult clearResult = clearUnset.execute(unsetModel);
        assertFalse(clearUnset.shouldSaveAddressBook());
        assertEquals(String.format(PromiseCommand.MESSAGE_ALREADY_UNSET, unsetDebtor.getDebtorId()),
                clearResult.getFeedbackToUser());
    }

    @Test
    void execute_unknownDebtor_throwsCommandException() {
        Model model = new ModelManager();
        PromiseCommand command = new PromiseCommand(999, DATE);

        assertCommandFailure(command, model, String.format(PromiseCommand.MESSAGE_DEBTOR_NOT_FOUND, 999));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PromiseCommand(1, DATE).execute(null));
    }
}
