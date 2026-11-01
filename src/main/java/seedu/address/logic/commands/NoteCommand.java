package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/**
 * Adds an interaction note to a debtor.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a timestamped interaction note to the debtor identified by debtor ID.\n"
            + "Parameters: --id DEBTOR_ID --text NOTE_TEXT\n"
            + "Example: " + COMMAND_WORD + " --id 3 --text Called debtor, promised payment on Friday.";

    public static final String MESSAGE_ADD_NOTE_SUCCESS = "Added interaction note to debtor ID %1$d: %2$s";
    public static final String MESSAGE_DEBTOR_NOT_FOUND = "No debtor with ID %1$d exists.";

    private final int debtorId;
    private final String noteText;
    private final Clock clock;

    /**
     * Creates a command to add {@code noteText} to the debtor identified by {@code debtorId}.
     */
    public NoteCommand(int debtorId, String noteText) {
        this(debtorId, noteText, Clock.systemDefaultZone());
    }

    /**
     * Creates a command with an injectable clock for deterministic tests.
     */
    public NoteCommand(int debtorId, String noteText, Clock clock) {
        requireNonNull(noteText);
        requireNonNull(clock);
        this.debtorId = debtorId;
        this.noteText = noteText.trim();
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Optional<Person> debtor = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == debtorId)
                .findFirst();
        if (debtor.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_DEBTOR_NOT_FOUND, debtorId));
        }

        InteractionNote note = new InteractionNote(LocalDateTime.now(clock), noteText);
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
        return debtorId == otherNoteCommand.debtorId && noteText.equals(otherNoteCommand.noteText);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("debtorId", debtorId)
                .add("noteText", noteText)
                .toString();
    }
}
