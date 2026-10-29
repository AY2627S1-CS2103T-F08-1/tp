package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.FollowUpEvent.Action;

class FollowUpEventTest {

    private static final LocalDateTime TIME = LocalDateTime.of(2026, 10, 29, 10, 0);
    private static final LocalDate FIRST = LocalDate.of(2026, 11, 3);
    private static final LocalDate SECOND = LocalDate.of(2026, 11, 5);

    @Test
    void constructor_validTransitions_succeed() {
        FollowUpEvent scheduled = new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(FIRST));
        FollowUpEvent rescheduled = new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST),
                Optional.of(SECOND));
        FollowUpEvent cleared = new FollowUpEvent(TIME, Action.CLEARED, Optional.of(SECOND), Optional.empty());
        FollowUpEvent completed = new FollowUpEvent(TIME, Action.COMPLETED, Optional.of(SECOND), Optional.empty());
        assertEquals(TIME, scheduled.getTimestamp());
        assertEquals(Action.SCHEDULED, scheduled.getAction());
        assertEquals(Optional.of(FIRST), rescheduled.getPreviousDate());
        assertEquals(Optional.of(SECOND), rescheduled.getNewDate());
        assertEquals(Action.CLEARED, cleared.getAction());
        assertEquals(Action.COMPLETED, completed.getAction());
    }

    @Test
    void constructor_impossibleTransitions_throw() {
        Optional<LocalDate> none = Optional.empty();
        Optional<LocalDate> first = Optional.of(FIRST);
        Optional<LocalDate> second = Optional.of(SECOND);
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.SCHEDULED, first, second));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.SCHEDULED, none, none));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.RESCHEDULED, none, second));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.RESCHEDULED, first, none));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.CLEARED, none, none));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.CLEARED, first, second));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpEvent(TIME, Action.COMPLETED, none, second));
    }

    @Test
    void constructor_nullArguments_throw() {
        Optional<LocalDate> first = Optional.of(FIRST);
        assertThrows(NullPointerException.class, () ->
                new FollowUpEvent(null, Action.SCHEDULED, Optional.empty(), first));
        assertThrows(NullPointerException.class, () -> new FollowUpEvent(TIME, null, Optional.empty(), first));
        assertThrows(NullPointerException.class, () -> new FollowUpEvent(TIME, Action.SCHEDULED, null, first));
        assertThrows(NullPointerException.class, () ->
                new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), null));
    }

    @Test
    void getDate_prefersNewDateThenPreviousDate() {
        assertEquals(Optional.of(FIRST),
                new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(FIRST)).getDate());
        assertEquals(Optional.of(SECOND),
                new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND)).getDate());
        assertEquals(Optional.of(SECOND),
                new FollowUpEvent(TIME, Action.CLEARED, Optional.of(SECOND), Optional.empty()).getDate());
    }

    @Test
    void getStorageTimestamp_usesIsoLocalDateTime() {
        assertEquals("2026-10-29T10:00:00",
                new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(FIRST))
                        .getStorageTimestamp());
    }

    @Test
    void fromStorage_validPair_roundTrips() {
        FollowUpEvent event = FollowUpEvent.fromStorage("2026-10-29T10:00:00", "RESCHEDULED", "2026-11-03",
                "2026-11-05");
        assertEquals(new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND)), event);
    }

    @Test
    void fromStorage_malformedValues_throw() {
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("not-a-time", "SCHEDULED", null, "2026-11-03"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("0000-10-29T10:00:00", "SCHEDULED", null, "2026-11-03"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage(null, "SCHEDULED", null, "2026-11-03"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("2026-10-29T10:00:00", "SHELVED", null, "2026-11-03"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("2026-10-29T10:00:00", null, null, "2026-11-03"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("2026-10-29T10:00:00", "SCHEDULED", null, "2026-1-3"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("2026-10-29T10:00:00", "SCHEDULED", null, "2026-02-30"));
        assertThrows(IllegalArgumentException.class, () ->
                FollowUpEvent.fromStorage("2026-10-29T10:00:00", "SCHEDULED", null, "0000-01-01"));
    }

    @Test
    void fromLegacyStorage_infersDatesFromCurrentState() {
        assertEquals(new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(FIRST)),
                FollowUpEvent.fromLegacyStorage("2026-10-29T10:00:00", "SCHEDULED", "2026-11-03",
                        Optional.empty()));
        assertEquals(new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND)),
                FollowUpEvent.fromLegacyStorage("2026-10-29T10:00:00", "RESCHEDULED", "2026-11-05",
                        Optional.of(FIRST)));
        assertEquals(new FollowUpEvent(TIME, Action.CLEARED, Optional.of(SECOND), Optional.empty()),
                FollowUpEvent.fromLegacyStorage("2026-10-29T10:00:00", "CLEARED", "2026-11-05",
                        Optional.of(SECOND)));
        assertEquals(new FollowUpEvent(TIME, Action.COMPLETED, Optional.of(SECOND), Optional.empty()),
                FollowUpEvent.fromLegacyStorage("2026-10-29T10:00:00", "COMPLETED", "2026-11-05",
                        Optional.of(SECOND)));
    }

    @Test
    void fromLegacyStorage_rescheduleWithoutPriorDate_throws() {
        assertThrows(IllegalArgumentException.class, () -> FollowUpEvent.fromLegacyStorage("2026-10-29T10:00:00",
                "RESCHEDULED", "2026-11-05", Optional.empty()));
    }

    @Test
    void equalsAndHashCode_considerEveryField() {
        FollowUpEvent event = new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND));
        FollowUpEvent same = new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND));
        assertEquals(event, event);
        assertEquals(event, same);
        assertEquals(event.hashCode(), same.hashCode());
        assertNotEquals(event, null);
        assertNotEquals(event, "event");
        assertNotEquals(event, new FollowUpEvent(TIME.plusMinutes(1), Action.RESCHEDULED, Optional.of(FIRST),
                Optional.of(SECOND)));
        assertNotEquals(event, new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(SECOND)));
        assertNotEquals(event, new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(SECOND), Optional.of(FIRST)));
        assertNotEquals(event, new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST),
                Optional.of(SECOND.plusDays(1))));
    }

    @Test
    void toString_showsBothDatesForReschedule() {
        assertEquals("[2026-10-29 10:00] SCHEDULED 2026-11-03",
                new FollowUpEvent(TIME, Action.SCHEDULED, Optional.empty(), Optional.of(FIRST)).toString());
        assertEquals("[2026-10-29 10:00] RESCHEDULED 2026-11-03 -> 2026-11-05",
                new FollowUpEvent(TIME, Action.RESCHEDULED, Optional.of(FIRST), Optional.of(SECOND)).toString());
        assertEquals("[2026-10-29 10:00] CLEARED 2026-11-05",
                new FollowUpEvent(TIME, Action.CLEARED, Optional.of(SECOND), Optional.empty()).toString());
        assertEquals("[2026-10-29 10:00] COMPLETED 2026-11-05",
                new FollowUpEvent(TIME, Action.COMPLETED, Optional.of(SECOND), Optional.empty()).toString());
        assertFalse(new FollowUpEvent(TIME, Action.COMPLETED, Optional.of(SECOND), Optional.empty()).toString()
                .contains("->"));
        assertTrue(Action.valueOf("SCHEDULED") == Action.SCHEDULED);
    }
}
