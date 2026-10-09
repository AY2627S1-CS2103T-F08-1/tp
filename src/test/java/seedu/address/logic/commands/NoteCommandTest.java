package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

public class NoteCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-09T06:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String VALID_NOTE = "Called debtor, promised payment on Friday.";

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validDebtorIdUnfilteredList_success() {
        Person debtor = model.getAddressBook().getPersonList().getFirst();
        NoteCommand noteCommand = new NoteCommand(debtor.getDebtorId(), VALID_NOTE, FIXED_CLOCK);
        InteractionNote expectedNote = new InteractionNote(java.time.LocalDateTime.of(2026, 10, 9, 14, 30),
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
        new NoteCommand(debtor.getDebtorId(), "First note.", FIXED_CLOCK).execute(model);
        new NoteCommand(debtor.getDebtorId(), "Second note.", FIXED_CLOCK).execute(model);

        List<InteractionNote> notes = model.getAddressBook().getPersonList().getFirst().getInteractionNotes();
        assertEquals("First note.", notes.get(0).getText());
        assertEquals("Second note.", notes.get(1).getText());
    }

    @Test
    public void execute_invalidDebtorId_throwsCommandException() {
        NoteCommand noteCommand = new NoteCommand(999, VALID_NOTE, FIXED_CLOCK);

        assertCommandFailure(noteCommand, model, String.format(NoteCommand.MESSAGE_DEBTOR_NOT_FOUND, 999));
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
