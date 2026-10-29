package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class SortWorkflowIntegrationTest {

    private static final Person ALICE_HIGH = person("Alice Tan", "700.00", 4, "81111111");
    private static final Person ALICE_LOW = person("Alice Goh", "20.00", 2, "82222222");
    private static final Person BOB = person("Bob Lim", "40.00", 3, "83333333");
    private static final Person CAROL = person("Carol Ong", "0", 1, "84444444");

    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path addressBookPath;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(ALICE_HIGH)
                .withPerson(ALICE_LOW)
                .withPerson(BOB)
                .withPerson(CAROL)
                .build();
        model = new ModelManager(addressBook, new UserPrefs());
        addressBookPath = temporaryFolder.resolve("addressBook.json");
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void findThenSort_preservesFilteredMembershipAndDoesNotWrite() throws Exception {
        logic.execute("find Alice");
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());

        CommandResult result = logic.execute("sort --by amount --order desc");

        assertEquals(List.of(4, 2), displayedIds());
        assertEquals("Sorted 2 displayed debtor(s) by amount in descending order.", result.getFeedbackToUser());
        assertEquals(before, model.getAddressBook().getPersonList());
        assertFalse(Files.exists(addressBookPath));
    }

    @Test
    public void sortThenDelete_deletesExactlyFirstDisplayedDebtorAndPersists() throws Exception {
        logic.execute("sort --by amount --order desc");
        assertEquals(ALICE_HIGH.getDebtorId(), model.getFilteredPersonList().getFirst().getDebtorId());

        logic.execute("delete 1");

        assertEquals(List.of(3, 2, 1), displayedIds());
        assertFalse(model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getDebtorId() == ALICE_HIGH.getDebtorId()));
        List<Person> savedPeople = new JsonAddressBookStorage(addressBookPath)
                .readAddressBook().orElseThrow().getPersonList();
        assertFalse(savedPeople.stream().anyMatch(person -> person.getDebtorId() == ALICE_HIGH.getDebtorId()));
    }

    @Test
    public void sortThenEditName_reordersDisplayedListButKeepsPersistentId() throws Exception {
        logic.execute("sort --by name --order asc");
        assertEquals(List.of(2, 4, 3, 1), displayedIds());

        logic.execute("edit 1 n/Zelda Goh");

        assertEquals(List.of(4, 3, 1, 2), displayedIds());
        Person edited = model.getFilteredPersonList().getLast();
        assertEquals(ALICE_LOW.getDebtorId(), edited.getDebtorId());
        assertEquals("Zelda Goh", edited.getName().fullName);
    }

    @Test
    public void listAfterFilterAndSort_resetsMembershipAndIdOrder() throws Exception {
        logic.execute("find Alice");
        logic.execute("sort --by amount --order desc");

        logic.execute("list");

        assertEquals(List.of(1, 2, 3, 4), displayedIds());
    }

    @Test
    public void listAmountAfterSort_replacesFilterAndResetsToIdOrder() throws Exception {
        logic.execute("sort --by amount --order desc");
        assertEquals(List.of(4, 3, 2, 1), displayedIds());

        logic.execute("list-amount min/20");

        assertEquals(List.of(2, 3, 4), displayedIds());
    }

    @Test
    public void addAfterFilterAndSort_resetsMembershipAndIdOrder() throws Exception {
        logic.execute("find Alice");
        logic.execute("sort --by amount --order desc");

        logic.execute("add n/Dan Wong p/85555555 e/dan@example.com a/5 Hill Road o/100");

        assertEquals(List.of(1, 2, 3, 4, 5), displayedIds());
        assertTrue(model.getFilteredPersonList().stream()
                .anyMatch(person -> person.getName().fullName.equals("Dan Wong")));
    }

    @Test
    public void sortIsSessionOnly_reloadUsesDefaultIdOrder() throws Exception {
        logic.execute("sort --by amount --order desc");
        logic.execute("delete 4");
        assertEquals(List.of(4, 3, 2), displayedIds());

        Model reloadedModel = new ModelManager(new JsonAddressBookStorage(addressBookPath)
                .readAddressBook().orElseThrow(), new UserPrefs());

        assertEquals(List.of(2, 3, 4), reloadedModel.getFilteredPersonList().stream()
                .map(Person::getDebtorId).toList());
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
                .withEmail("workflow" + debtorId + "@example.com")
                .build();
    }
}
