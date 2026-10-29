package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.OutstandingAmountRange;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ListAmountCommandTest {

    private Person zeroBalance;
    private Person mediumBalance;
    private Person highBalance;
    private AddressBook addressBook;
    private Model model;

    @BeforeEach
    public void setUp() {
        zeroBalance = new PersonBuilder(ALICE).withDebtorId(9).withOutstandingAmount("0").build();
        mediumBalance = new PersonBuilder(BENSON).withDebtorId(2).withOutstandingAmount("500").build();
        highBalance = new PersonBuilder(CARL).withDebtorId(5).withOutstandingAmount("1000").build();
        addressBook = new AddressBookBuilder()
                .withPerson(zeroBalance).withPerson(highBalance).withPerson(mediumBalance).build();
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_boundedRange_returnsMatchesInPersistentIdOrder() {
        OutstandingAmountRange range = range("500", "1000");
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> range.contains(person.getOutstandingAmount()));

        assertCommandSuccess(new ListAmountCommand(range), model,
                ListAmountCommand.getResultMessage(2, range), expectedModel);
        assertEquals(List.of(2, 5), model.getFilteredPersonList().stream()
                .map(Person::getDebtorId).toList());
    }

    @Test
    public void execute_exactBoundary_returnsOneMatch() {
        OutstandingAmountRange range = range("500", "500");
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> range.contains(person.getOutstandingAmount()));

        assertCommandSuccess(new ListAmountCommand(range), model,
                ListAmountCommand.getResultMessage(1, range), expectedModel);
        assertEquals(List.of(mediumBalance), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatches_returnsHelpfulMessage() {
        OutstandingAmountRange range = range("1000.01", null);
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> range.contains(person.getOutstandingAmount()));

        assertCommandSuccess(new ListAmountCommand(range), model,
                ListAmountCommand.getResultMessage(0, range), expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_replacesPreviousFilter() {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        OutstandingAmountRange range = range(null, "500");
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> range.contains(person.getOutstandingAmount()));

        assertCommandSuccess(new ListAmountCommand(range), model,
                ListAmountCommand.getResultMessage(2, range), expectedModel);
        assertEquals(List.of(2, 9), model.getFilteredPersonList().stream()
                .map(Person::getDebtorId).toList());
    }

    @Test
    public void shouldSaveAddressBook_returnsFalse() {
        assertFalse(new ListAmountCommand(range("0", null)).shouldSaveAddressBook());
    }

    @Test
    public void equalsHashCodeAndToString() {
        ListAmountCommand first = new ListAmountCommand(range("10", "20"));
        ListAmountCommand copy = new ListAmountCommand(range("10.00", "20.0"));
        ListAmountCommand different = new ListAmountCommand(range("20", null));

        assertEquals(first, first);
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "command");
        assertTrue(first.toString().contains("predicate"));
    }

    private OutstandingAmountRange range(String minimum, String maximum) {
        Optional<OutstandingAmount> minimumAmount = minimum == null
                ? Optional.empty() : Optional.of(new OutstandingAmount(minimum));
        Optional<OutstandingAmount> maximumAmount = maximum == null
                ? Optional.empty() : Optional.of(new OutstandingAmount(maximum));
        return new OutstandingAmountRange(minimumAmount, maximumAmount);
    }
}
