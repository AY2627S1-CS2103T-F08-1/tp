package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;

/** Changes the recovery stage of a debtor selected by persistent ID. */
public class StageCommand extends Command {
    public static final String COMMAND_WORD = "stage";
    public static final String MESSAGE_USAGE = "stage: Changes a debtor's recovery stage. "
            + "Parameters: --id DEBTOR_ID --value STAGE\nExample: stage --id 3 --value contacted";
    public static final String MESSAGE_DEBTOR_NOT_FOUND = "No debtor with ID %1$d exists.";
    public static final String MESSAGE_SUCCESS = "Recovery stage for debtor ID %1$d set to %2$s.";
    public static final String MESSAGE_UNCHANGED = "Recovery stage for debtor ID %1$d is already %2$s.";

    private final int debtorId;
    private final RecoveryStage stage;
    private boolean changed;

    /** Creates a command to set the stage of the debtor identified by {@code debtorId}. */
    public StageCommand(int debtorId, RecoveryStage stage) {
        this.debtorId = debtorId;
        this.stage = requireNonNull(stage);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        changed = false;
        Person target = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_DEBTOR_NOT_FOUND, debtorId)));
        if (target.getRecoveryStage() == stage) {
            return new CommandResult(String.format(MESSAGE_UNCHANGED, debtorId, stage));
        }
        model.setPerson(target, target.withRecoveryStage(stage));
        changed = true;
        return new CommandResult(String.format(MESSAGE_SUCCESS, debtorId, stage));
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return changed;
    }
}
