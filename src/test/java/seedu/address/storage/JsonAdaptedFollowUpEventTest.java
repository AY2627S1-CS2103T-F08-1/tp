package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUpEvent;

class JsonAdaptedFollowUpEventTest {

    private static final String TIMESTAMP = "2026-10-29T10:00:00";

    @Test
    void toModelType_currentFormat_returnsEvent() throws Exception {
        JsonAdaptedFollowUpEvent adapted = new JsonAdaptedFollowUpEvent(
                TIMESTAMP, "RESCHEDULED", null, "2026-11-03", "2026-11-05");

        FollowUpEvent expected = new FollowUpEvent(LocalDateTime.parse(TIMESTAMP),
                FollowUpEvent.Action.RESCHEDULED, Optional.of(LocalDate.parse("2026-11-03")),
                Optional.of(LocalDate.parse("2026-11-05")));
        assertEquals(expected, adapted.toModelType(Optional.empty()));
    }

    @Test
    void toModelType_legacyFormat_usesPriorState() throws Exception {
        JsonAdaptedFollowUpEvent adapted = new JsonAdaptedFollowUpEvent(
                TIMESTAMP, "RESCHEDULED", "2026-11-05", null, null);

        FollowUpEvent event = adapted.toModelType(Optional.of(LocalDate.parse("2026-11-03")));

        assertEquals(Optional.of(LocalDate.parse("2026-11-03")), event.getPreviousDate());
        assertEquals(Optional.of(LocalDate.parse("2026-11-05")), event.getNewDate());
    }

    @Test
    void toModelType_mixedLegacyAndCurrentFields_prefersCurrentFields() throws Exception {
        JsonAdaptedFollowUpEvent adapted = new JsonAdaptedFollowUpEvent(
                TIMESTAMP, "RESCHEDULED", "2099-12-31", "2026-11-03", "2026-11-05");

        FollowUpEvent event = adapted.toModelType(Optional.empty());

        assertEquals(Optional.of(LocalDate.parse("2026-11-03")), event.getPreviousDate());
        assertEquals(Optional.of(LocalDate.parse("2026-11-05")), event.getNewDate());
    }

    @Test
    void toModelType_missingTimestamp_throwsIllegalValueException() {
        JsonAdaptedFollowUpEvent adapted = new JsonAdaptedFollowUpEvent(
                null, "SCHEDULED", null, null, "2026-11-03");

        assertThrows(IllegalValueException.class, () -> adapted.toModelType(Optional.empty()),
                JsonAdaptedFollowUpEvent.MESSAGE_MISSING_REQUIRED_FIELDS);
    }

    @Test
    void toModelType_missingAction_throwsIllegalValueException() {
        JsonAdaptedFollowUpEvent adapted = new JsonAdaptedFollowUpEvent(
                TIMESTAMP, null, null, null, "2026-11-03");

        assertThrows(IllegalValueException.class, () -> adapted.toModelType(Optional.empty()),
                JsonAdaptedFollowUpEvent.MESSAGE_MISSING_REQUIRED_FIELDS);
    }
}
