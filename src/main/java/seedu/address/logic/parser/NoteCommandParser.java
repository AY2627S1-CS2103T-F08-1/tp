package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE_TEXT;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

/**
 * Parses input arguments and creates a new {@code NoteCommand} object.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    public static final String MESSAGE_INVALID_DEBTOR_ID = "Debtor ID must be a positive integer.";

    @Override
    public NoteCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DEBTOR_ID, PREFIX_NOTE_TEXT);
        if (!argMultimap.getPreamble().isEmpty()
                || argMultimap.getValue(PREFIX_DEBTOR_ID).isEmpty()
                || argMultimap.getValue(PREFIX_NOTE_TEXT).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_NOTE_TEXT);
        int debtorId = parseDebtorId(argMultimap.getValue(PREFIX_DEBTOR_ID).get());
        String noteText = argMultimap.getValue(PREFIX_NOTE_TEXT).get();
        if (!InteractionNote.isValidText(noteText)) {
            throw new ParseException(InteractionNote.MESSAGE_CONSTRAINTS);
        }

        return new NoteCommand(debtorId, noteText);
    }

    private int parseDebtorId(String idText) throws ParseException {
        if (!StringUtil.isNonZeroUnsignedInteger(idText)) {
            throw new ParseException(MESSAGE_INVALID_DEBTOR_ID);
        }
        return Integer.parseInt(idText);
    }
}
