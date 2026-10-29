package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUpEvent;

/** Jackson-friendly version of a follow-up history event. */
class JsonAdaptedFollowUpEvent {
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

    public FollowUpEvent toModelType(java.util.Optional<java.time.LocalDate> currentDate)
            throws IllegalValueException {
        if (timestamp == null || action == null) {
            throw new IllegalValueException("Follow-up event timestamp and action are required.");
        }
        try {
            if (legacyDate != null && previousDate == null && newDate == null) {
                return FollowUpEvent.fromLegacyStorage(timestamp, action, legacyDate, currentDate);
            }
            return FollowUpEvent.fromStorage(timestamp, action, previousDate, newDate);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
