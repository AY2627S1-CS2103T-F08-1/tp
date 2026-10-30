package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.TypicalPersons;

/** Contract tests shared by commands selecting persistent IDs rather than displayed indexes. */
public class PersistentDebtorSelectionTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_targetHiddenByFilter_selectsPersistentIdAndPreservesOtherDebtor() throws Exception {
        for (Command command : commands(17)) {
            Model model = modelWithHiddenTarget();
            command.execute(model);
            assertEquals(List.of(9), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList(),
                    command.getClass().getSimpleName());
            assertEquals(TypicalPersons.BOB.withDebtorId(9), model.getAddressBook().getPersonList().get(1));
        }
        assertTrue(Files.readString(folder.resolve("debtor-17.txt")).contains(TypicalPersons.ALICE.getName().fullName));
    }

    @Test
    public void execute_deletedTarget_rejectsWithoutChangingDataViewOrHistory() throws Exception {
        for (Command command : commands(17)) {
            Model model = modelWithHiddenTarget();
            model.deletePerson(model.getAddressBook().getPersonList().getFirst());
            AddressBook before = new AddressBook(model.getAddressBook());
            String expected = String.format(command instanceof ShowCommand || command instanceof ExportCommand
                    ? ShowCommand.MESSAGE_DEBTOR_NOT_FOUND : Messages.MESSAGE_DEBTOR_NOT_FOUND, 17);

            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));

            assertEquals(expected, error.getMessage(), command.getClass().getSimpleName());
            assertEquals(before, model.getAddressBook());
            assertEquals(before.getNextDebtorId(), model.getAddressBook().getNextDebtorId());
            assertEquals(List.of(9), model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
            assertFalse(model.canUndoAddressBook());
        }
        assertFalse(Files.exists(folder.resolve("debtor-17.txt")));
    }

    private Model modelWithHiddenTarget() {
        Model model = new ModelManager();
        model.addPerson(TypicalPersons.ALICE.withDebtorId(17));
        model.addPerson(TypicalPersons.BOB.withDebtorId(9));
        model.updateFilteredPersonList(person -> person.getDebtorId() == 9);
        return model;
    }

    private List<Command> commands(int id) {
        return List.of(new ShowCommand(id), new NoteCommand(id, "Reached debtor"),
                new StageCommand(id, RecoveryStage.CONTACTED), new FollowUpCommand(id, LocalDate.of(2026, 11, 1)),
                new ContactCommand(id, true, "call", false, null), new ExportCommand(id, folder),
                new PromiseCommand(id, LocalDate.of(2026, 11, 2)));
    }
}
