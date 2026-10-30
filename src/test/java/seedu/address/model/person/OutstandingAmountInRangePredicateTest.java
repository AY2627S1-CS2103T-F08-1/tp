package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class OutstandingAmountInRangePredicateTest {

    private static final OutstandingAmountRange RANGE = new OutstandingAmountRange(
            Optional.of(new OutstandingAmount("100")),
            Optional.of(new OutstandingAmount("200")));

    @Test
    public void test_amountWithinRange_returnsTrue() {
        OutstandingAmountInRangePredicate predicate = new OutstandingAmountInRangePredicate(RANGE);

        assertTrue(predicate.test(new PersonBuilder().withOutstandingAmount("100").build()));
        assertTrue(predicate.test(new PersonBuilder().withOutstandingAmount("150").build()));
        assertTrue(predicate.test(new PersonBuilder().withOutstandingAmount("200").build()));
    }

    @Test
    public void test_amountOutsideRange_returnsFalse() {
        OutstandingAmountInRangePredicate predicate = new OutstandingAmountInRangePredicate(RANGE);

        assertFalse(predicate.test(new PersonBuilder().withOutstandingAmount("99.99").build()));
        assertFalse(predicate.test(new PersonBuilder().withOutstandingAmount("200.01").build()));
    }

    @Test
    public void constructorAndTest_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OutstandingAmountInRangePredicate(null));
        OutstandingAmountInRangePredicate predicate = new OutstandingAmountInRangePredicate(RANGE);
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void equalsHashCodeAndToString() {
        OutstandingAmountInRangePredicate first = new OutstandingAmountInRangePredicate(RANGE);
        OutstandingAmountInRangePredicate copy = new OutstandingAmountInRangePredicate(RANGE);
        OutstandingAmountRange otherRange = new OutstandingAmountRange(
                Optional.of(new OutstandingAmount("200")), Optional.empty());
        OutstandingAmountInRangePredicate different = new OutstandingAmountInRangePredicate(otherRange);

        assertEquals(first, first);
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "predicate");
        assertTrue(first.toString().contains("range"));
    }
}
