package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.Person;

/** Schedules, clears, or completes a debtor follow-up. */
public class FollowUpCommand extends Command {

    public static final String COMMAND_WORD = "followup";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Schedules, clears, or completes a follow-up. Parameters: --id ID --date YYYY-MM-DD | --clear | --done";
    public static final String MESSAGE_NO_PENDING_FOLLOW_UP = "No pending follow-up exists for debtor ID %1$d.";
    public static final String MESSAGE_SUCCESS = "Follow-up %1$s for debtor ID %2$d%3$s.";

    /** The requested follow-up state change. */
    public enum Operation {
        SCHEDULE,
        CLEAR,
        DONE
    }

    private final int debtorId;
    private final Operation operation;
    private final Optional<LocalDate> date;
    private final Clock clock;
    private boolean hasChanges;

    /** Creates a command to schedule a follow-up date. */
    public FollowUpCommand(int debtorId, LocalDate date) {
        this(debtorId, Operation.SCHEDULE, Optional.of(date), Clock.systemDefaultZone());
    }

    /** Creates a command with the selected operation and system clock. */
    public FollowUpCommand(int debtorId, Operation operation, Optional<LocalDate> date) {
        this(debtorId, operation, date, Clock.systemDefaultZone());
    }

    /** Creates a command with an injectable clock for deterministic use. */
    public FollowUpCommand(int debtorId, Operation operation, Optional<LocalDate> date, Clock clock) {
        requireNonNull(operation);
        requireNonNull(date);
        requireNonNull(clock);
        this.debtorId = debtorId;
        this.operation = operation;
        this.date = date;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        hasChanges = false;
        Person target = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId).findFirst()
                .orElseThrow(() -> new CommandException(String.format(Messages.MESSAGE_DEBTOR_NOT_FOUND, debtorId)));
        if (operation != Operation.SCHEDULE && target.getFollowUpDate().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_PENDING_FOLLOW_UP, debtorId));
        }

        if (operation == Operation.SCHEDULE && target.getFollowUpDate().equals(date)) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, "already scheduled", debtorId,
                    date.map(value -> " for " + value).orElse("")));
        }

        FollowUpEvent.Action action;
        Optional<LocalDate> updatedDate;
        String verb;
        switch (operation) {
            case SCHEDULE -> {
                boolean rescheduling = target.getFollowUpDate().isPresent();
                action = rescheduling ? FollowUpEvent.Action.RESCHEDULED : FollowUpEvent.Action.SCHEDULED;
                updatedDate = date;
                verb = rescheduling ? "rescheduled" : "scheduled";
            }
            case CLEAR -> {
                action = FollowUpEvent.Action.CLEARED;
                updatedDate = Optional.empty();
                verb = "cleared";
            }
            default -> {
                action = FollowUpEvent.Action.COMPLETED;
                updatedDate = Optional.empty();
                verb = "completed";
            }
        }

        FollowUpEvent event = new FollowUpEvent(LocalDateTime.now(clock), action, target.getFollowUpDate(),
                updatedDate);
        model.setPerson(target, target.withFollowUpChange(updatedDate, event));
        hasChanges = true;
        String dateText = updatedDate.map(value -> " for " + value).orElse("");
        return new CommandResult(String.format(MESSAGE_SUCCESS, verb, debtorId, dateText));
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
        if (!(other instanceof FollowUpCommand otherCommand)) {
            return false;
        }
        return debtorId == otherCommand.debtorId && operation == otherCommand.operation
                && date.equals(otherCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("debtorId", debtorId)
                .add("operation", operation)
                .add("date", date)
                .toString();
    }
}
