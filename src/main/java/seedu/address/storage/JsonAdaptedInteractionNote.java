package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

/**
 * Jackson-friendly version of {@link InteractionNote}.
 */
class JsonAdaptedInteractionNote {

    static final String MISSING_TIMESTAMP_MESSAGE = "Interaction note's timestamp field is missing!";
    static final String MISSING_TEXT_MESSAGE = "Interaction note's text field is missing!";

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
        if (timestamp == null) {
            throw new IllegalValueException(MISSING_TIMESTAMP_MESSAGE);
        }
        if (text == null) {
            throw new IllegalValueException(MISSING_TEXT_MESSAGE);
        }
        try {
            return InteractionNote.fromStorage(timestamp, text);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
