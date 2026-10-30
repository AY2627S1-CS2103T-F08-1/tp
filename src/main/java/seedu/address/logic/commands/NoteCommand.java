package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/**
 * Adds an interaction note to a debtor.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a timestamped interaction note to the debtor identified by debtor ID.\n"
            + "Parameters: --id DEBTOR_ID [--outcome OUTCOME] --text NOTE_TEXT\n"
            + "Example: " + COMMAND_WORD
            + " --id 3 --outcome reached --text Called debtor, promised payment on Friday.";

    public static final String MESSAGE_ADD_NOTE_SUCCESS = "Added interaction note to debtor ID %1$d: %2$s";

    private final int debtorId;
    private final String noteText;
    private final Optional<ContactOutcome> outcome;
    private final Clock clock;

    /**
     * Creates a command to add {@code noteText} to the debtor identified by {@code debtorId}.
     */
    public NoteCommand(int debtorId, String noteText) {
        this(debtorId, noteText, Optional.empty(), Clock.systemDefaultZone());
    }

    /**
     * Creates a command with an injectable clock for deterministic tests.
     */
    public NoteCommand(int debtorId, String noteText, Clock clock) {
        this(debtorId, noteText, Optional.empty(), clock);
    }

    /**
     * Creates a command that records a structured contact outcome.
     */
    public NoteCommand(int debtorId, String noteText, ContactOutcome outcome) {
        this(debtorId, noteText, Optional.of(requireNonNull(outcome)), Clock.systemDefaultZone());
    }

    /**
     * Creates a command that records a structured contact outcome with an injectable clock.
     */
    public NoteCommand(int debtorId, String noteText, ContactOutcome outcome, Clock clock) {
        this(debtorId, noteText, Optional.of(requireNonNull(outcome)), clock);
    }

    private NoteCommand(int debtorId, String noteText, Optional<ContactOutcome> outcome, Clock clock) {
        requireNonNull(noteText);
        requireNonNull(outcome);
        requireNonNull(clock);
        this.debtorId = debtorId;
        this.noteText = noteText.trim();
        this.outcome = outcome;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Optional<Person> debtor = model.findPersonById(debtorId);
        if (debtor.isEmpty()) {
            throw new CommandException(String.format(Messages.MESSAGE_DEBTOR_NOT_FOUND, debtorId));
        }

        InteractionNote note = new InteractionNote(LocalDateTime.now(clock), noteText, outcome);
        Person target = debtor.get();
        Person updatedDebtor = target.withAddedInteractionNote(note);
        model.setPerson(target, updatedDebtor);

        return new CommandResult(String.format(MESSAGE_ADD_NOTE_SUCCESS, debtorId, note));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NoteCommand otherNoteCommand)) {
            return false;
        }
        return debtorId == otherNoteCommand.debtorId && noteText.equals(otherNoteCommand.noteText)
                && outcome.equals(otherNoteCommand.outcome);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("debtorId", debtorId)
                .add("noteText", noteText)
                .add("outcome", outcome.map(ContactOutcome::toString).orElse("unspecified"))
                .toString();
    }
}
