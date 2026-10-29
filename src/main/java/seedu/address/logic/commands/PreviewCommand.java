package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Previews an edit or deletion against an isolated copy of the current view. */
public class PreviewCommand extends Command {
    public static final String COMMAND_WORD = "preview";
    public static final String MESSAGE_USAGE = "preview edit INDEX [FIELDS] | preview delete INDEX";
    private final Command proposed;

    /** Creates a preview for an edit or delete command. */
    public PreviewCommand(Command proposed) {
        requireNonNull(proposed);
        if (!(proposed instanceof EditCommand) && !(proposed instanceof DeleteCommand)) {
            throw new IllegalArgumentException(MESSAGE_USAGE);
        }
        this.proposed = proposed;
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Model copy = model.createTransactionCopy();
        CommandResult result = proposed.execute(copy);
        String feedback = result.getFeedbackToUser().replaceFirst("^Edited debtor:", "Would edit debtor:")
                .replaceFirst("^Deleted debtor:", "Would delete debtor:");
        String original = model.getAddressBook().getPersonList().stream()
                .filter(person -> hasChanged(person, copy))
                .findFirst().map(person -> "Current debtor: " + Messages.format(person) + "\n")
                .orElse("No data would change.\n");
        return new CommandResult("Preview only; no changes were saved.\n" + original + feedback);
    }
    private boolean hasChanged(Person original, Model copy) {
        return copy.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == original.getDebtorId())
                .findFirst().map(person -> !person.equals(original)).orElse(true);
    }
}
