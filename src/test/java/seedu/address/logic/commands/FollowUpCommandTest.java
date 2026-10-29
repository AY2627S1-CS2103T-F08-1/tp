package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class FollowUpCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));

    private Person addDebtor(Model model) {
        return model.addPerson(new PersonBuilder().build());
    }

    private Person current(Model model) {
        return model.getAddressBook().getPersonList().getFirst();
    }

    @Test
    void execute_unknownDebtor_throwsAndLeavesDataUnchanged() {
        Model model = new ModelManager();
        Person debtor = addDebtor(model);
        FollowUpCommand command = new FollowUpCommand(debtor.getDebtorId() + 50, DATE);
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertTrue(exception.getMessage().contains(String.valueOf(debtor.getDebtorId() + 50)));
        assertEquals(debtor, current(model));
        assertFalse(command.shouldSaveAddressBook());
    }

    @Test
    void execute_clearOrDoneWithoutPendingFollowUp_throwsAndAddsNoEvent() {
        Model model = new ModelManager();
        int id = addDebtor(model).getDebtorId();
        FollowUpCommand clear = new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty(), CLOCK);
        FollowUpCommand done = new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty(), CLOCK);
        assertThrows(CommandException.class, () -> clear.execute(model));
        assertThrows(CommandException.class, () -> done.execute(model));
        assertTrue(current(model).getFollowUpHistory().isEmpty());
        assertFalse(clear.shouldSaveAddressBook());
    }

    @Test
    void execute_scheduleThenSameDate_isNoOp() throws Exception {
        Model model = new ModelManager();
        int id = addDebtor(model).getDebtorId();
        FollowUpCommand command = new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(DATE), CLOCK);
        assertTrue(command.execute(model).getFeedbackToUser().contains("scheduled"));
        assertTrue(command.shouldSaveAddressBook());
        Person afterFirst = current(model);
        assertTrue(command.execute(model).getFeedbackToUser().contains("already scheduled"));
        assertFalse(command.shouldSaveAddressBook());
        assertEquals(afterFirst, current(model));
        assertEquals(1, current(model).getFollowUpHistory().size());
    }

    @Test
    void execute_reschedule_recordsPreviousAndNewDate() throws Exception {
        Model model = new ModelManager();
        int id = addDebtor(model).getDebtorId();
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(DATE), CLOCK).execute(model);
        CommandResult result = new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE,
                Optional.of(DATE.plusDays(2)), CLOCK).execute(model);
        assertTrue(result.getFeedbackToUser().contains("rescheduled"));
        FollowUpEvent event = current(model).getFollowUpHistory().getLast();
        assertEquals(FollowUpEvent.Action.RESCHEDULED, event.getAction());
        assertEquals(Optional.of(DATE), event.getPreviousDate());
        assertEquals(Optional.of(DATE.plusDays(2)), event.getNewDate());
        assertEquals(Optional.of(DATE.plusDays(2)), current(model).getFollowUpDate());
    }

    @Test
    void execute_clearAndDone_recordClearedAndCompletedEvents() throws Exception {
        Model model = new ModelManager();
        int id = addDebtor(model).getDebtorId();
        new FollowUpCommand(id, DATE).execute(model);
        CommandResult cleared = new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty(), CLOCK)
                .execute(model);
        assertTrue(cleared.getFeedbackToUser().contains("cleared"));
        assertTrue(current(model).getFollowUpDate().isEmpty());
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(DATE.plusDays(1)), CLOCK)
                .execute(model);
        FollowUpCommand done = new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty(), CLOCK);
        assertTrue(done.execute(model).getFeedbackToUser().contains("completed"));
        assertTrue(done.shouldSaveAddressBook());
        FollowUpEvent event = current(model).getFollowUpHistory().getLast();
        assertEquals(FollowUpEvent.Action.COMPLETED, event.getAction());
        assertEquals(Optional.of(DATE.plusDays(1)), event.getPreviousDate());
        assertTrue(event.getNewDate().isEmpty());
    }

    @Test
    void equalsHashCodeAndToString_considerEveryField() {
        FollowUpCommand command = new FollowUpCommand(1, DATE);
        assertEquals(command, command);
        assertEquals(command, new FollowUpCommand(1, DATE));
        assertNotEquals(command, null);
        assertNotEquals(command, "followup");
        assertNotEquals(command, new FollowUpCommand(2, DATE));
        assertNotEquals(command, new FollowUpCommand(1, DATE.plusDays(1)));
        assertNotEquals(command, new FollowUpCommand(1, FollowUpCommand.Operation.CLEAR, Optional.of(DATE)));
        assertTrue(command.toString().contains("debtorId=1"));
        assertTrue(command.toString().contains("operation=SCHEDULE"));
    }

    @Test
    void person_equalsAndHashCode_considerFollowUpState() throws Exception {
        Model model = new ModelManager();
        int id = addDebtor(model).getDebtorId();
        Person before = current(model);
        new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(DATE), CLOCK).execute(model);
        Person scheduled = current(model);
        FollowUpEvent event = scheduled.getFollowUpHistory().getFirst();
        Person sameDateLongerHistory = scheduled.withFollowUpChange(scheduled.getFollowUpDate(), event);
        Person sameHistoryOtherDate = before.withFollowUpChange(Optional.of(DATE.plusDays(1)), event);
        assertNotEquals(before, scheduled);
        assertNotEquals(scheduled, sameDateLongerHistory);
        assertNotEquals(scheduled, sameHistoryOtherDate);
        assertEquals(scheduled.hashCode(), before.withFollowUpChange(Optional.of(DATE), event).hashCode());
        assertEquals(scheduled, before.withFollowUpChange(Optional.of(DATE), event));
    }
}
