package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Sets or clears a debtor's promised payment date. */
public class PromiseCommand extends Command {

    public static final String COMMAND_WORD = "promise";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets or clears a promised payment date. Parameters: --id ID (--date YYYY-MM-DD | --clear)";
    public static final String MESSAGE_DEBTOR_NOT_FOUND = "No debtor with ID %1$d exists.";
    public static final String MESSAGE_SET_SUCCESS = "Promised payment date set to %1$s for debtor ID %2$d.";
    public static final String MESSAGE_CLEAR_SUCCESS = "Promised payment date cleared for debtor ID %1$d.";
    public static final String MESSAGE_SAME_DATE =
            "Promised payment date for debtor ID %1$d is already %2$s; no change made.";
    public static final String MESSAGE_ALREADY_UNSET =
            "No promised payment date is set for debtor ID %1$d; no change made.";

    private final int debtorId;
    private final Optional<LocalDate> date;
    private boolean hasChanges;

    /** Creates a command that sets the promised payment date. */
    public PromiseCommand(int debtorId, LocalDate date) {
        this(debtorId, Optional.of(requireNonNull(date)));
    }

    /** Creates a command that sets the supplied date, or clears it when empty. */
    public PromiseCommand(int debtorId, Optional<LocalDate> date) {
        requireNonNull(date);
        this.debtorId = debtorId;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person target = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_DEBTOR_NOT_FOUND, debtorId)));
        if (target.getPromisedPaymentDate().equals(date)) {
            hasChanges = false;
            String message = date.map(value -> String.format(MESSAGE_SAME_DATE, debtorId, value))
                    .orElseGet(() -> String.format(MESSAGE_ALREADY_UNSET, debtorId));
            return new CommandResult(message);
        }

        hasChanges = true;
        model.setPerson(target, target.withPromisedPaymentDate(date));
        String message = date.map(value -> String.format(MESSAGE_SET_SUCCESS, value, debtorId))
                .orElseGet(() -> String.format(MESSAGE_CLEAR_SUCCESS, debtorId));
        return new CommandResult(message);
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return hasChanges;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PromiseCommand otherCommand
                && debtorId == otherCommand.debtorId && date.equals(otherCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("debtorId", debtorId).add("date", date).toString();
    }
}
