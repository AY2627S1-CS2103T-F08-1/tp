package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;
import seedu.address.testutil.PersonBuilder;

public class PersonSortTest {

    private static final Person ALPHA_HIGH_ID = person("alpha Tan", "10.00", 8, "81111111");
    private static final Person ALPHA_LOW_ID = person("Alpha Tan", "40.00", 2, "82222222");
    private static final Person BETA_LOW_AMOUNT = person("Beta Lim", "0", 5, "83333333");
    private static final Person GAMMA_HIGH_AMOUNT = person("Gamma Ong", "999999999999.99", 3, "84444444");
    private static final Person ZETA_TIED_AMOUNT = person("Zeta Lee", "40.00", 1, "85555555");

    @Test
    public void fieldFromToken_supportedTokensCaseInsensitive_returnsField() {
        assertEquals(Optional.of(Field.ID), Field.fromToken("id"));
        assertEquals(Optional.of(Field.NAME), Field.fromToken("NAME"));
        assertEquals(Optional.of(Field.AMOUNT), Field.fromToken("AmOuNt"));
    }

    @Test
    public void fieldFromToken_unknownOrUntrimmedToken_returnsEmpty() {
        assertEquals(Optional.empty(), Field.fromToken("phone"));
        assertEquals(Optional.empty(), Field.fromToken(" name "));
        assertEquals(Optional.empty(), Field.fromToken(""));
    }

    @Test
    public void directionFromToken_supportedTokensCaseInsensitive_returnsDirection() {
        assertEquals(Optional.of(Direction.ASCENDING), Direction.fromToken("asc"));
        assertEquals(Optional.of(Direction.DESCENDING), Direction.fromToken("DESC"));
    }

    @Test
    public void directionFromToken_unknownOrUntrimmedToken_returnsEmpty() {
        assertEquals(Optional.empty(), Direction.fromToken("ascending"));
        assertEquals(Optional.empty(), Direction.fromToken(" desc "));
        assertEquals(Optional.empty(), Direction.fromToken(""));
    }

    @Test
    public void tokenParsing_nullToken_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Field.fromToken(null));
        assertThrows(NullPointerException.class, () -> Direction.fromToken(null));
    }

    @Test
    public void createComparator_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                PersonSort.createComparator(null, Direction.ASCENDING));
        assertThrows(NullPointerException.class, () ->
                PersonSort.createComparator(Field.ID, null));
    }

    @Test
    public void createComparator_idAscending_ordersByPersistentId() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.ID, Direction.ASCENDING));

        assertEquals(List.of(1, 2, 3, 5, 8), debtorIds(people));
    }

    @Test
    public void createComparator_idDescending_ordersByPersistentId() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.ID, Direction.DESCENDING));

        assertEquals(List.of(8, 5, 3, 2, 1), debtorIds(people));
    }

    @Test
    public void createComparator_nameAscending_ignoresCaseAndBreaksTieByAscendingId() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.NAME, Direction.ASCENDING));

        assertEquals(List.of(2, 8, 5, 3, 1), debtorIds(people));
    }

    @Test
    public void createComparator_nameDescending_reversesOnlyNameAndKeepsTieBreakerAscending() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.NAME, Direction.DESCENDING));

        assertEquals(List.of(1, 3, 5, 2, 8), debtorIds(people));
    }

    @Test
    public void createComparator_amountAscending_comparesNumericallyAndBreaksTieByAscendingId() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.AMOUNT, Direction.ASCENDING));

        assertEquals(List.of(5, 8, 1, 2, 3), debtorIds(people));
    }

    @Test
    public void createComparator_amountDescending_reversesOnlyAmountAndKeepsTieBreakerAscending() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.createComparator(Field.AMOUNT, Direction.DESCENDING));

        assertEquals(List.of(3, 1, 2, 8, 5), debtorIds(people));
    }

    @Test
    public void defaultComparator_ordersByIdAscending() {
        List<Person> people = mutablePeople();
        people.sort(PersonSort.DEFAULT_COMPARATOR);

        assertEquals(List.of(1, 2, 3, 5, 8), debtorIds(people));
    }

    private static List<Person> mutablePeople() {
        return new java.util.ArrayList<>(List.of(
                ALPHA_HIGH_ID, ZETA_TIED_AMOUNT, BETA_LOW_AMOUNT, ALPHA_LOW_ID, GAMMA_HIGH_AMOUNT));
    }

    private static List<Integer> debtorIds(List<Person> people) {
        return people.stream().map(Person::getDebtorId).toList();
    }

    private static Person person(String name, String amount, int debtorId, String phone) {
        return new PersonBuilder()
                .withName(name)
                .withOutstandingAmount(amount)
                .withDebtorId(debtorId)
                .withPhone(phone)
                .withEmail("debtor" + debtorId + "@example.com")
                .build();
    }
}
