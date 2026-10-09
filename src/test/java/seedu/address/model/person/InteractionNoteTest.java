package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class InteractionNoteTest {

    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 10, 9, 14, 30);

    @Test
    public void constructor_blankText_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionNote(TIMESTAMP, "   "));
    }

    @Test
    public void constructor_validText_trimsText() {
        InteractionNote note = new InteractionNote(TIMESTAMP, "  Called debtor, promised payment.  ");

        assertEquals("Called debtor, promised payment.", note.getText());
    }

    @Test
    public void isValidText() {
        assertFalse(InteractionNote.isValidText(null));
        assertFalse(InteractionNote.isValidText(""));
        assertFalse(InteractionNote.isValidText("   "));
        assertTrue(InteractionNote.isValidText("Left voicemail; follow up tomorrow."));
    }

    @Test
    public void fromStorage_validTimestamp_returnsInteractionNote() {
        InteractionNote expected = new InteractionNote(TIMESTAMP, "Asked for callback.");

        assertEquals(expected, InteractionNote.fromStorage("2026-10-09T14:30:00", "Asked for callback."));
    }

    @Test
    public void toStringMethod() {
        InteractionNote note = new InteractionNote(TIMESTAMP, "Sent payment reminder.");

        assertEquals("[2026-10-09 14:30] Sent payment reminder.", note.toString());
    }
}
