package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedInteractionNote.MISSING_TEXT_MESSAGE;
import static seedu.address.storage.JsonAdaptedInteractionNote.MISSING_TIMESTAMP_MESSAGE;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

public class JsonAdaptedInteractionNoteTest {

    @Test
    public void toModelType_validNote_returnsInteractionNote() throws Exception {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("2026-10-09T14:30:00", "Called debtor.");

        InteractionNote modelNote = note.toModelType();

        assertEquals("Called debtor.", modelNote.getText());
    }

    @Test
    public void toModelType_missingTimestamp_throwsIllegalValueException() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote(null, "Called debtor.");

        assertThrows(IllegalValueException.class, MISSING_TIMESTAMP_MESSAGE, note::toModelType);
    }

    @Test
    public void toModelType_missingText_throwsIllegalValueException() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("2026-10-09T14:30:00", null);

        assertThrows(IllegalValueException.class, MISSING_TEXT_MESSAGE, note::toModelType);
    }

    @Test
    public void toModelType_malformedTimestamp_throwsIllegalValueException() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("not-a-timestamp", "Called debtor.");

        assertThrows(IllegalValueException.class, note::toModelType);
    }

    @Test
    public void toModelType_blankText_throwsIllegalValueException() {
        JsonAdaptedInteractionNote note = new JsonAdaptedInteractionNote("2026-10-09T14:30:00", "   ");

        assertThrows(IllegalValueException.class, note::toModelType);
    }
}
