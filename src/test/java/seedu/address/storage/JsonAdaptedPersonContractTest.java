package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.PersonBuilder;

/** Characterizes stored-data contracts before reorganising conversion responsibilities. */
public class JsonAdaptedPersonContractTest {
    @Test
    public void toModelType_multipleInvalidFields_preservesDiagnosticPrecedence() throws Exception {
        Map<String, Object> record = validRecord();
        List<Object> nullEntry = new ArrayList<>();
        nullEntry.add(null);
        record.put("tags", nullEntry);
        record.put("name", null);
        assertInvalid(record, JsonAdaptedPerson.NULL_TAG_MESSAGE);

        record = validRecord();
        record.put("phone", "bad phone");
        record.put("followUpDate", "bad date");
        assertInvalid(record, Phone.MESSAGE_CONSTRAINTS);

        record = validRecord();
        record.put("interactionNotes", nullEntry);
        record.put("outstandingAmount", "-1");
        assertInvalid(record, JsonAdaptedPerson.NULL_INTERACTION_NOTE_MESSAGE);

        record = validRecord();
        record.put("followUpHistory", nullEntry);
        record.put("promisedPaymentDate", "bad date");
        assertInvalid(record, "Follow-up history entry cannot be null.");

        record = validRecord();
        record.put("promisedPaymentDate", "2026-02-29");
        record.put("debtorId", -1);
        assertInvalid(record, "Promised payment date must be a valid ISO date from year 0001 to 9999.");

        record = validRecord();
        record.put("debtorId", -1);
        record.put("contactPreferences", Map.of("method", "invalid"));
        assertInvalid(record, Person.MESSAGE_INVALID_DEBTOR_ID);
    }

    @Test
    public void toModelType_allCurrentFields_roundTripThroughJson() throws Exception {
        LocalDate firstDate = LocalDate.of(2026, 11, 1);
        LocalDate secondDate = LocalDate.of(2026, 11, 2);
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 30, 8, 0);
        Person source = new PersonBuilder().withOutstandingAmount("1,250.50").withTags("friend", "client").build()
                .withDebtorId(25)
                .withAddedInteractionNote(InteractionNote.fromStorage("2026-10-30T08:00:00.123", "Called; agreed."))
                .withContactPreferences(new ContactPreferences("sms", "09:00-17:00"))
                .withRecoveryStage(RecoveryStage.NEGOTIATING)
                .withPromisedPaymentDate(Optional.of(LocalDate.of(2026, 11, 3)));
        source = source.withFollowUpChange(Optional.of(firstDate), new FollowUpEvent(timestamp,
                FollowUpEvent.Action.SCHEDULED, Optional.empty(), Optional.of(firstDate)));
        source = source.withFollowUpChange(Optional.of(secondDate), new FollowUpEvent(timestamp.plusMinutes(1),
                FollowUpEvent.Action.RESCHEDULED, Optional.of(firstDate), Optional.of(secondDate)));

        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(source));
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertEquals(source, restored);
        assertEquals(json, JsonUtil.toJsonString(new JsonAdaptedPerson(restored)));
    }

    private Map<String, Object> validRecord() {
        Map<String, Object> record = new HashMap<>();
        record.put("name", "Alice");
        record.put("phone", "81111111");
        record.put("email", "alice@example.com");
        record.put("address", "Main Street");
        return record;
    }

    private void assertInvalid(Map<String, Object> record, String expected) throws Exception {
        JsonAdaptedPerson adapted = JsonUtil.fromJsonString(JsonUtil.toJsonString(record), JsonAdaptedPerson.class);
        IllegalValueException error = assertThrows(IllegalValueException.class, adapted::toModelType);
        assertEquals(expected, error.getMessage());
    }
}
