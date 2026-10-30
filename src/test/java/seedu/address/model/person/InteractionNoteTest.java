package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class InteractionNoteTest {

    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 10, 9, 14, 30);

    @Test
    public void constructor_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InteractionNote(null, "Called debtor."));
        assertThrows(NullPointerException.class, () -> new InteractionNote(TIMESTAMP, null));
        assertThrows(NullPointerException.class, () -> new InteractionNote(TIMESTAMP, "Called debtor.", null));
    }

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
    public void fromStorage_validOutcome_returnsInteractionNoteWithOutcome() {
        InteractionNote note = InteractionNote.fromStorage("2026-10-09T14:30:00", "Asked for callback.",
                "CALLBACK-REQUESTED");

        assertEquals(Optional.of(ContactOutcome.CALLBACK_REQUESTED), note.getOutcome());
    }

    @Test
    public void fromStorage_invalidOutcome_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            InteractionNote.fromStorage("2026-10-09T14:30:00", "Called debtor.", "answered"));
    }

    @Test
    public void fromStorage_invalidTimestamp_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            InteractionNote.fromStorage("2026-13-40T14:30:00", "Called debtor."));
    }

    @Test
    public void storageTimestampAndEquality() {
        InteractionNote note = new InteractionNote(TIMESTAMP, "Sent payment reminder.");
        InteractionNote sameNote = new InteractionNote(TIMESTAMP, "Sent payment reminder.");
        InteractionNote differentTimestamp = new InteractionNote(TIMESTAMP.plusMinutes(1), "Sent payment reminder.");
        InteractionNote differentText = new InteractionNote(TIMESTAMP, "Called debtor.");
        InteractionNote differentOutcome = new InteractionNote(TIMESTAMP, "Sent payment reminder.",
                Optional.of(ContactOutcome.REACHED));

        assertEquals("2026-10-09T14:30:00", note.getStorageTimestamp());
        assertTrue(note.equals(note));
        assertEquals(note, sameNote);
        assertNotEquals(note, differentTimestamp);
        assertNotEquals(note, differentText);
        assertNotEquals(note, differentOutcome);
        assertNotEquals(note, "not a note");
        assertEquals(note.hashCode(), sameNote.hashCode());
    }

    @Test
    public void toStringMethod() {
        InteractionNote note = new InteractionNote(TIMESTAMP, "Sent payment reminder.");

        assertEquals("[2026-10-09 14:30] [unspecified] Sent payment reminder.", note.toString());
    }

    @Test
    public void toString_withOutcome_displaysOutcome() {
        InteractionNote note = new InteractionNote(TIMESTAMP, "Reached debtor.", Optional.of(ContactOutcome.REACHED));

        assertEquals("[2026-10-09 14:30] [reached] Reached debtor.", note.toString());
    }
}
