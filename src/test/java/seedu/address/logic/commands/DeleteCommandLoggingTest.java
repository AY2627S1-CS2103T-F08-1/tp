package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Checks that delete diagnostics distinguish outcomes without including debtor data or raw indexes.
 */
public class DeleteCommandLoggingTest {
    @Test
    public void execute_success_logsModelRemovalWithoutDebtorDetails() throws Exception {
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON);
        try (CapturedLogs logs = new CapturedLogs()) {
            command.execute(new ModelManager(getTypicalAddressBook(), new UserPrefs()));
            assertEquals(List.of("Delete applied to model; persistence is handled by LogicManager."), logs.messages);
        }
    }

    @Test
    public void execute_oversizedIndex_logsRejectionWithoutRawIndex() {
        DeleteCommand command = DeleteCommand.fromOneBasedDecimal("9".repeat(1000));
        try (CapturedLogs logs = new CapturedLogs()) {
            assertThrows(CommandException.class, () ->
                    command.execute(new ModelManager(getTypicalAddressBook(), new UserPrefs())));
            assertEquals(List.of("Delete rejected: index outside displayed list."), logs.messages);
        }
    }

    @Test
    public void execute_modelRemovalFails_doesNotLogCompletedRemoval() {
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON);
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs()) {
            @Override
            public void deletePerson(Person person) {
                throw new IllegalStateException("Simulated model failure");
            }
        };
        try (CapturedLogs logs = new CapturedLogs()) {
            assertThrows(IllegalStateException.class, () -> command.execute(model));
            assertEquals(List.of(), logs.messages);
        }
    }

    private static class CapturedLogs extends Handler implements AutoCloseable {
        private final Logger logger = Logger.getLogger("osps.DeleteCommand");
        private final Level previousLevel = logger.getLevel();
        private final boolean previousUseParentHandlers = logger.getUseParentHandlers();
        private final List<String> messages = new ArrayList<>();

        CapturedLogs() {
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.addHandler(this);
        }

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            logger.removeHandler(this);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParentHandlers);
        }
    }
}
