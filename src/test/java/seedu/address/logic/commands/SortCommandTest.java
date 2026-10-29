package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {

    private static final Person ALICE = person("Alice Tan", "500.00", 4, "81111111");
    private static final Person ALICE_TIED = person("alice Tan", "20.00", 2, "82222222");
    private static final Person BOB = person("Bob Lim", "20.00", 3, "83333333");
    private static final Person CAROL = person("Carol Ong", "0", 1, "84444444");

    private Model model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(ALICE)
                .withPerson(ALICE_TIED)
                .withPerson(BOB)
                .withPerson(CAROL)
                .build();
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_eachSupportedSort_producesExpectedOrder() {
        assertSort(Field.ID, Direction.ASCENDING, List.of(1, 2, 3, 4));
        assertSort(Field.ID, Direction.DESCENDING, List.of(4, 3, 2, 1));
        assertSort(Field.NAME, Direction.ASCENDING, List.of(2, 4, 3, 1));
        assertSort(Field.NAME, Direction.DESCENDING, List.of(1, 3, 2, 4));
        assertSort(Field.AMOUNT, Direction.ASCENDING, List.of(1, 2, 3, 4));
        assertSort(Field.AMOUNT, Direction.DESCENDING, List.of(4, 2, 3, 1));
    }

    @Test
    public void execute_filteredList_preservesMembershipAndSortsOnlyVisibleDebtors() {
        model.updateFilteredPersonList(person -> person.getName().fullName.toLowerCase().contains("alice"));

        SortCommand command = new SortCommand(Field.AMOUNT, Direction.ASCENDING);
        CommandResult result = command.execute(model);

        assertEquals(List.of(2, 4), displayedIds());
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 2, "amount", "ascending"),
                result.getFeedbackToUser());
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_emptyFilteredList_reportsZeroAndKeepsData() {
        model.updateFilteredPersonList(person -> false);

        CommandResult result = new SortCommand(Field.NAME, Direction.DESCENDING).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 0, "name", "descending"),
                result.getFeedbackToUser());
        assertEquals(4, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_singleVisibleDebtor_keepsDebtorVisible() {
        model.updateFilteredPersonList(person -> person.getDebtorId() == BOB.getDebtorId());

        CommandResult result = new SortCommand(Field.AMOUNT, Direction.DESCENDING).execute(model);

        assertEquals(List.of(BOB.getDebtorId()), displayedIds());
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 1, "amount", "descending"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_thenEditSortField_refreshesPositionWithoutResettingSort() {
        new SortCommand(Field.AMOUNT, Direction.DESCENDING).execute(model);
        Person editedCarol = new PersonBuilder(CAROL).withOutstandingAmount("600.00").build();

        model.setPerson(CAROL, editedCarol);

        assertEquals(List.of(1, 4, 2, 3), displayedIds());
    }

    @Test
    public void execute_thenDeleteDisplayedDebtor_keepsRemainingSort() {
        new SortCommand(Field.NAME, Direction.DESCENDING).execute(model);
        Person firstDisplayed = model.getFilteredPersonList().getFirst();

        model.deletePerson(firstDisplayed);

        assertEquals(CAROL, firstDisplayed);
        assertEquals(List.of(3, 2, 4), displayedIds());
    }

    @Test
    public void execute_isReadOnly() {
        SortCommand command = new SortCommand(Field.AMOUNT, Direction.DESCENDING);
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        command.execute(model);

        assertFalse(command.shouldSaveAddressBook());
        assertEquals(before, model.getAddressBook().getPersonList());
    }

    @Test
    public void equals() {
        SortCommand command = new SortCommand(Field.AMOUNT, Direction.DESCENDING);
        SortCommand sameCommand = new SortCommand(Field.AMOUNT, Direction.DESCENDING);

        assertEquals(command, command);
        assertEquals(command, sameCommand);
        assertEquals(command.hashCode(), sameCommand.hashCode());
        assertNotEquals(command, new SortCommand(Field.AMOUNT, Direction.ASCENDING));
        assertNotEquals(command, new SortCommand(Field.NAME, Direction.DESCENDING));
        assertNotEquals(command, null);
        assertNotEquals(command, "sort");
        assertTrue(command.toString().contains("field=AMOUNT"));
        assertTrue(command.toString().contains("direction=DESCENDING"));
    }

    private void assertSort(Field field, Direction direction, List<Integer> expectedIds) {
        SortCommand command = new SortCommand(field, direction);
        CommandResult result = command.execute(model);

        assertEquals(expectedIds, displayedIds());
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS,
                expectedIds.size(), field.getToken(), direction.getDescription()), result.getFeedbackToUser());
    }

    private List<Integer> displayedIds() {
        return model.getFilteredPersonList().stream().map(Person::getDebtorId).toList();
    }

    private static Person person(String name, String amount, int debtorId, String phone) {
        return new PersonBuilder()
                .withName(name)
                .withOutstandingAmount(amount)
                .withDebtorId(debtorId)
                .withPhone(phone)
                .withEmail("sort" + debtorId + "@example.com")
                .build();
    }
}
