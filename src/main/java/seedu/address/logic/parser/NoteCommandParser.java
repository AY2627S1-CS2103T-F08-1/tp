package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

/**
 * Parses input arguments and creates a new {@code NoteCommand} object.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    public static final String MESSAGE_INVALID_DEBTOR_ID = "Debtor ID must be a positive integer.";

    private static final Pattern NOTE_COMMAND_FORMAT =
            Pattern.compile("\\s*--id\\s+(?<debtorId>\\S+)\\s+--text\\s*(?<noteText>.*)", Pattern.DOTALL);

    @Override
    public NoteCommand parse(String args) throws ParseException {
        Matcher matcher = NOTE_COMMAND_FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        int debtorId = parseDebtorId(matcher.group("debtorId"));
        String noteText = matcher.group("noteText").trim();
        if (!InteractionNote.isValidText(noteText)) {
            throw new ParseException(InteractionNote.MESSAGE_CONSTRAINTS);
        }

        return new NoteCommand(debtorId, noteText);
    }

    private int parseDebtorId(String idText) throws ParseException {
        try {
            int debtorId = Integer.parseInt(idText);
            if (debtorId <= 0) {
                throw new NumberFormatException();
            }
            return debtorId;
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_DEBTOR_ID);
        }
    }
}
