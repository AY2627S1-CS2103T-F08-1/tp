package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE_TEXT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OUTCOME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;

/**
 * Parses input arguments and creates a new {@code NoteCommand} object.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown note option: %s. Use --id, --outcome, and --text.";

    private static final Pattern LONG_OPTION_TOKEN = Pattern.compile("(?<!\\S)--\\S+");
    private static final Pattern NOTE_TEXT_PREFIX = Pattern.compile("(?<!\\S)--text(?=\\s|$)");

    @Override
    public NoteCommand parse(String args) throws ParseException {
        Matcher textPrefixMatcher = NOTE_TEXT_PREFIX.matcher(args);
        if (!textPrefixMatcher.find()) {
            rejectUnknownOptionsWithoutText(args);
            throw invalidFormat();
        }

        String optionArguments = args.substring(0, textPrefixMatcher.start());
        String textArguments = " " + args.substring(textPrefixMatcher.start());
        ArgumentMultimap optionMultimap = ArgumentTokenizer.tokenize(optionArguments,
                PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
        ArgumentMultimap textMultimap = ArgumentTokenizer.tokenize(textArguments,
                PREFIX_NOTE_TEXT, PREFIX_DEBTOR_ID);

        String idText = validateOptions(optionMultimap, textMultimap);
        int debtorId = ParserUtil.parseDebtorId(idText);
        String noteText = textMultimap.getValue(PREFIX_NOTE_TEXT).orElseThrow();
        if (!InteractionNote.isValidText(noteText)) {
            throw new ParseException(InteractionNote.MESSAGE_CONSTRAINTS);
        }
        return createCommand(debtorId, noteText, optionMultimap.getValue(PREFIX_OUTCOME));
    }

    /** Validates option structure without interpreting literal words inside the note text. */
    private String validateOptions(ArgumentMultimap optionMultimap, ArgumentMultimap textMultimap)
            throws ParseException {
        rejectUnknownOptions(optionMultimap.getPreamble());
        rejectUnknownOptions(textMultimap.getPreamble());
        List<String> idValues = new ArrayList<>(optionMultimap.getAllValues(PREFIX_DEBTOR_ID));
        idValues.addAll(textMultimap.getAllValues(PREFIX_DEBTOR_ID));
        rejectUnknownOptionsInValues(idValues);
        rejectUnknownOptionsInValues(optionMultimap.getAllValues(PREFIX_OUTCOME));
        optionMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
        textMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_NOTE_TEXT);
        if (idValues.size() > 1) {
            throw new ParseException(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
        }
        if (!optionMultimap.getPreamble().isEmpty() || !textMultimap.getPreamble().isEmpty()
                || idValues.size() != 1 || textMultimap.getValue(PREFIX_NOTE_TEXT).isEmpty()) {
            throw invalidFormat();
        }
        return idValues.getFirst();
    }

    private NoteCommand createCommand(int debtorId, String noteText, Optional<String> outcomeText)
            throws ParseException {
        if (outcomeText.isEmpty()) {
            return new NoteCommand(debtorId, noteText);
        }
        ContactOutcome outcome = ContactOutcome.fromToken(outcomeText.get())
                .orElseThrow(() -> new ParseException(ContactOutcome.MESSAGE_CONSTRAINTS));
        return new NoteCommand(debtorId, noteText, outcome);
    }

    private void rejectUnknownOptionsWithoutText(String args) throws ParseException {
        ArgumentMultimap options = ArgumentTokenizer.tokenize(args, PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
        rejectUnknownOptions(options.getPreamble());
        rejectUnknownOptionsInValues(options.getAllValues(PREFIX_DEBTOR_ID));
        rejectUnknownOptionsInValues(options.getAllValues(PREFIX_OUTCOME));
    }

    private void rejectUnknownOptionsInValues(List<String> values) throws ParseException {
        for (String value : values) {
            rejectUnknownOptions(value);
        }
    }

    private void rejectUnknownOptions(String parameterText) throws ParseException {
        Matcher matcher = LONG_OPTION_TOKEN.matcher(parameterText);
        if (matcher.find()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, matcher.group()));
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }
}
