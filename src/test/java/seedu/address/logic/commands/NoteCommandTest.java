package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

public class NoteCommandTest {

    private static final Clock EARLIER_CLOCK = Clock.fixed(Instant.parse("2026-10-09T06:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Clock LATER_CLOCK = Clock.fixed(Instant.parse("2026-10-09T06:35:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String VALID_NOTE = "Called debtor, promised payment on Friday.";

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(1, null));
        assertThrows(NullPointerException.class, () -> new NoteCommand(1, VALID_NOTE, null));
    }

    @Test
    public void execute_validDebtorIdUnfilteredList_success() {
        Person debtor = model.getAddressBook().getPersonList().getFirst();
        NoteCommand noteCommand = new NoteCommand(debtor.getDebtorId(), VALID_NOTE, EARLIER_CLOCK);
        InteractionNote expectedNote = new InteractionNote(LocalDateTime.of(2026, 10, 9, 14, 30),
                VALID_NOTE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedDebtor = expectedModel.getAddressBook().getPersonList().getFirst();
        expectedModel.setPerson(expectedDebtor, expectedDebtor.withAddedInteractionNote(expectedNote));
        String expectedMessage = String.format(NoteCommand.MESSAGE_ADD_NOTE_SUCCESS, debtor.getDebtorId(),
                expectedNote);

        assertCommandSuccess(noteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_notesAreAppendedChronologically_success() throws Exception {
        Person debtor = model.getAddressBook().getPersonList().getFirst();
        new NoteCommand(debtor.getDebtorId(), "First note.", EARLIER_CLOCK).execute(model);
        new NoteCommand(debtor.getDebtorId(), "Second note.", LATER_CLOCK).execute(model);

        List<InteractionNote> notes = model.getAddressBook().getPersonList().getFirst().getInteractionNotes();
        assertEquals(List.of(
                new InteractionNote(LocalDateTime.of(2026, 10, 9, 14, 30), "First note."),
                new InteractionNote(LocalDateTime.of(2026, 10, 9, 14, 35), "Second note.")), notes);
        assertTrue(notes.get(0).getTimestamp().isBefore(notes.get(1).getTimestamp()));
    }

    @Test
    public void execute_invalidDebtorId_throwsCommandException() {
        NoteCommand noteCommand = new NoteCommand(999, VALID_NOTE, EARLIER_CLOCK);

        assertCommandFailure(noteCommand, model, String.format(NoteCommand.MESSAGE_DEBTOR_NOT_FOUND, 999));
    }

    @Test
    public void execute_debtorFilteredOut_addsNoteByPersistentId() throws Exception {
        Person debtor = model.getAddressBook().getPersonList().getFirst();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new NoteCommand(debtor.getDebtorId(), VALID_NOTE, EARLIER_CLOCK).execute(model);

        Person updatedDebtor = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtor.getDebtorId())
                .findFirst()
                .orElseThrow();
        assertEquals(VALID_NOTE, updatedDebtor.getInteractionNotes().getFirst().getText());
        assertFalse(model.getFilteredPersonList().contains(updatedDebtor));
    }

    @Test
    public void toStringMethod() {
        NoteCommand noteCommand = new NoteCommand(7, "  Called debtor.  ");
        String expected = NoteCommand.class.getCanonicalName()
                + "{debtorId=7, noteText=Called debtor.}";

        assertEquals(expected, noteCommand.toString());
    }

    @Test
    public void equals() {
        NoteCommand firstCommand = new NoteCommand(1, "First note.");
        NoteCommand firstCommandCopy = new NoteCommand(1, "First note.");
        NoteCommand differentIdCommand = new NoteCommand(2, "First note.");
        NoteCommand differentTextCommand = new NoteCommand(1, "Second note.");

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(firstCommandCopy));
        assertFalse(firstCommand.equals(differentIdCommand));
        assertFalse(firstCommand.equals(differentTextCommand));
        assertFalse(firstCommand.equals(1));
        assertFalse(firstCommand.equals(null));
    }
}
