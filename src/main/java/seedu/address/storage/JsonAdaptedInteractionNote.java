package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

/**
 * Jackson-friendly version of {@link InteractionNote}.
 */
class JsonAdaptedInteractionNote {

    private final String timestamp;
    private final String text;

    /**
     * Constructs a {@code JsonAdaptedInteractionNote} with the given note details.
     */
    @JsonCreator
    public JsonAdaptedInteractionNote(@JsonProperty("timestamp") String timestamp,
            @JsonProperty("text") String text) {
        this.timestamp = timestamp;
        this.text = text;
    }

    /**
     * Converts a given {@code InteractionNote} into this class for Jackson use.
     */
    public JsonAdaptedInteractionNote(InteractionNote source) {
        timestamp = source.getStorageTimestamp();
        text = source.getText();
    }

    /**
     * Converts this Jackson-friendly adapted note object into the model's {@code InteractionNote} object.
     */
    public InteractionNote toModelType() throws IllegalValueException {
        try {
            return InteractionNote.fromStorage(timestamp, text);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
