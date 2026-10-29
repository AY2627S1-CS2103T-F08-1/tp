package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUpEvent;

/** Jackson-friendly version of a follow-up history event. */
class JsonAdaptedFollowUpEvent {
    private final String timestamp;
    private final String action;
    private final String date;

    @JsonCreator
    public JsonAdaptedFollowUpEvent(@JsonProperty("timestamp") String timestamp,
            @JsonProperty("action") String action, @JsonProperty("date") String date) {
        this.timestamp = timestamp;
        this.action = action;
        this.date = date;
    }

    public JsonAdaptedFollowUpEvent(FollowUpEvent event) {
        timestamp = event.getStorageTimestamp();
        action = event.getAction().name();
        date = event.getDate().map(Object::toString).orElse(null);
    }

    public FollowUpEvent toModelType() throws IllegalValueException {
        if (timestamp == null || action == null) {
            throw new IllegalValueException("Follow-up event timestamp and action are required.");
        }
        try {
            return FollowUpEvent.fromStorage(timestamp, action, date);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
