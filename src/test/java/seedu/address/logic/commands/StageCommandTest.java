package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.InteractionNote;
import seedu.address.testutil.PersonBuilder;

class StageCommandTest {
    @Test
    void execute_updatesPersistentIdTargetAndPreservesOtherData() throws Exception {
        ModelManager model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        CommandResult result = new StageCommand(debtor.getDebtorId(), RecoveryStage.CONTACTED).execute(model);

        Person updated = model.getAddressBook().getPersonList().getFirst();
        assertEquals(RecoveryStage.CONTACTED, updated.getRecoveryStage());
        assertEquals(debtor.getOutstandingAmount(), updated.getOutstandingAmount());
        assertEquals("Recovery stage for debtor ID 1 set to contacted.", result.getFeedbackToUser());
    }

    @Test
    void execute_sameStageIsReadOnly() throws Exception {
        ModelManager model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        StageCommand command = new StageCommand(debtor.getDebtorId(), RecoveryStage.NEW);
        CommandResult result = command.execute(model);

        assertEquals("Recovery stage for debtor ID 1 is already new.", result.getFeedbackToUser());
        assertEquals(false, command.shouldSaveAddressBook());
    }

    @Test
    void execute_stageSurvivesAddingAnInteractionNote() throws CommandException {
        ModelManager model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());
        new StageCommand(debtor.getDebtorId(), RecoveryStage.NEGOTIATING).execute(model);

        Person staged = model.getAddressBook().getPersonList().getFirst();
        model.setPerson(staged, staged.withAddedInteractionNote(
                InteractionNote.fromStorage("2026-10-29T12:00:00", "Called debtor.")));

        assertEquals(RecoveryStage.NEGOTIATING,
                model.getAddressBook().getPersonList().getFirst().getRecoveryStage());
    }
}
