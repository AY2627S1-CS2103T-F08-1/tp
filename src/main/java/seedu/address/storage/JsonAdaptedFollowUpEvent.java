package seedu.address.storage;

import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUpEvent;

/** Jackson-friendly version of a follow-up history event. */
class JsonAdaptedFollowUpEvent {
    static final String MESSAGE_MISSING_REQUIRED_FIELDS =
            "Follow-up event timestamp and action are required.";

    private final String timestamp;
    private final String action;
    private final String previousDate;
    private final String newDate;
    @JsonProperty(value = "date", access = JsonProperty.Access.WRITE_ONLY)
    private final String legacyDate;

    @JsonCreator
    public JsonAdaptedFollowUpEvent(@JsonProperty("timestamp") String timestamp,
            @JsonProperty("action") String action, @JsonProperty("date") String date,
            @JsonProperty("previousDate") String previousDate, @JsonProperty("newDate") String newDate) {
        this.timestamp = timestamp;
        this.action = action;
        this.legacyDate = date;
        this.previousDate = previousDate;
        this.newDate = newDate;
    }

    public JsonAdaptedFollowUpEvent(FollowUpEvent event) {
        timestamp = event.getStorageTimestamp();
        action = event.getAction().name();
        previousDate = event.getPreviousDate().map(Object::toString).orElse(null);
        newDate = event.getNewDate().map(Object::toString).orElse(null);
        legacyDate = null;
    }

    public FollowUpEvent toModelType(Optional<LocalDate> currentDate) throws IllegalValueException {
        if (timestamp == null || action == null) {
            throw new IllegalValueException(MESSAGE_MISSING_REQUIRED_FIELDS);
        }
        try {
            // Prefer the current date pair whenever it is present. This lets a partially migrated
            // file retain a legacy date without overriding its explicit transition history.
            if (legacyDate != null && previousDate == null && newDate == null) {
                return FollowUpEvent.fromLegacyStorage(timestamp, action, legacyDate, currentDate);
            }
            return FollowUpEvent.fromStorage(timestamp, action, previousDate, newDate);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
