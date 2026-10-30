package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.RecoveryStage;
import seedu.address.model.person.RecoveryStagePredicate;

public class ListStageCommandTest {
    @Test
    public void equals_sameStage_hasSameValueAndHash() {
        ListStageCommand command = new ListStageCommand(RecoveryStage.NEW);
        ListStageCommand sameStage = new ListStageCommand(RecoveryStage.NEW);
        assertEquals(command, command);
        assertEquals(command, sameStage);
        assertEquals(command.hashCode(), sameStage.hashCode());
        assertNotEquals(command, new ListStageCommand(RecoveryStage.SETTLED));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
        assertEquals(ListStageCommand.class.getCanonicalName() + "{predicate="
                + new RecoveryStagePredicate(RecoveryStage.NEW) + "}", command.toString());
    }

    @Test
    public void readOnlyCommand_neverRequestsSaving() {
        for (RecoveryStage stage : RecoveryStage.values()) {
            assertFalse(new ListStageCommand(stage).shouldSaveAddressBook());
        }
    }

    @Test
    public void nullInputs_rejectProgrammingErrors() {
        assertThrows(NullPointerException.class, () -> new ListStageCommand(null));
        assertThrows(NullPointerException.class, () -> new ListStageCommand(RecoveryStage.NEW).execute(null));
    }
}
