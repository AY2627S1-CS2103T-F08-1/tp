package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class OutstandingAmountRangeTest {

    private static final OutstandingAmount ZERO = new OutstandingAmount("0");
    private static final OutstandingAmount HUNDRED = new OutstandingAmount("100");
    private static final OutstandingAmount TWO_HUNDRED = new OutstandingAmount("200");

    @Test
    public void constructor_noBounds_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new OutstandingAmountRange(Optional.empty(), Optional.empty()));
        assertEquals(OutstandingAmountRange.MESSAGE_REQUIRES_BOUND, exception.getMessage());
    }

    @Test
    public void constructor_minimumExceedsMaximum_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                range(TWO_HUNDRED, HUNDRED));
        assertEquals(OutstandingAmountRange.MESSAGE_INVALID_ORDER, exception.getMessage());
    }

    @Test
    public void constructor_nullOptionals_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new OutstandingAmountRange(null, Optional.of(HUNDRED)));
        assertThrows(NullPointerException.class, () ->
                new OutstandingAmountRange(Optional.of(HUNDRED), null));
    }

    @Test
    public void contains_lowerBoundOnly_isInclusive() {
        OutstandingAmountRange range = new OutstandingAmountRange(
                Optional.of(HUNDRED), Optional.empty());

        assertFalse(range.contains(ZERO));
        assertTrue(range.contains(HUNDRED));
        assertTrue(range.contains(TWO_HUNDRED));
    }

    @Test
    public void contains_upperBoundOnly_isInclusive() {
        OutstandingAmountRange range = new OutstandingAmountRange(
                Optional.empty(), Optional.of(HUNDRED));

        assertTrue(range.contains(ZERO));
        assertTrue(range.contains(HUNDRED));
        assertFalse(range.contains(TWO_HUNDRED));
    }

    @Test
    public void contains_twoBounds_isInclusive() {
        OutstandingAmountRange range = range(HUNDRED, TWO_HUNDRED);

        assertFalse(range.contains(ZERO));
        assertTrue(range.contains(HUNDRED));
        assertTrue(range.contains(new OutstandingAmount("150")));
        assertTrue(range.contains(TWO_HUNDRED));
        assertFalse(range.contains(new OutstandingAmount("200.01")));
    }

    @Test
    public void toUserDescription_allBoundCombinations_areReadable() {
        assertEquals("at least S$100.00", new OutstandingAmountRange(
                Optional.of(HUNDRED), Optional.empty()).toUserDescription());
        assertEquals("at most S$100.00", new OutstandingAmountRange(
                Optional.empty(), Optional.of(HUNDRED)).toUserDescription());
        assertEquals("from S$100.00 to S$200.00", range(HUNDRED, TWO_HUNDRED).toUserDescription());
    }

    @Test
    public void equalsHashCodeAndToString() {
        OutstandingAmountRange first = range(HUNDRED, TWO_HUNDRED);
        OutstandingAmountRange copy = range(new OutstandingAmount("100.00"),
                new OutstandingAmount("200.0"));
        OutstandingAmountRange different = new OutstandingAmountRange(
                Optional.of(HUNDRED), Optional.empty());

        assertEquals(first, first);
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "range");
        assertTrue(first.toString().contains("minimum"));
        assertTrue(first.toString().contains("maximum"));
    }

    private OutstandingAmountRange range(OutstandingAmount minimum, OutstandingAmount maximum) {
        return new OutstandingAmountRange(Optional.of(minimum), Optional.of(maximum));
    }
}
