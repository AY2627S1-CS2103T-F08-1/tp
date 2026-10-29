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

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;

/**
 * Parses input arguments and creates a new {@code NoteCommand} object.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    public static final String MESSAGE_INVALID_DEBTOR_ID = "Debtor ID must be a positive integer.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown note option: %s. Use --id, --outcome, and --text.";

    private static final Pattern LONG_OPTION_TOKEN = Pattern.compile("(?<!\\S)--\\S+");
    private static final Pattern NOTE_TEXT_PREFIX = Pattern.compile("(?<!\\S)--text(?=\\s|$)");

    @Override
    public NoteCommand parse(String args) throws ParseException {
        Matcher textPrefixMatcher = NOTE_TEXT_PREFIX.matcher(args);
        if (!textPrefixMatcher.find()) {
            ArgumentMultimap incompleteMultimap = ArgumentTokenizer.tokenize(args,
                    PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
            rejectUnknownOptions(incompleteMultimap.getPreamble());
            for (String value : incompleteMultimap.getAllValues(PREFIX_DEBTOR_ID)) {
                rejectUnknownOptions(value);
            }
            for (String value : incompleteMultimap.getAllValues(PREFIX_OUTCOME)) {
                rejectUnknownOptions(value);
            }
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        String optionArguments = args.substring(0, textPrefixMatcher.start());
        String textArguments = " " + args.substring(textPrefixMatcher.start());
        ArgumentMultimap optionMultimap = ArgumentTokenizer.tokenize(optionArguments,
                PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
        ArgumentMultimap textMultimap = ArgumentTokenizer.tokenize(textArguments,
                PREFIX_NOTE_TEXT, PREFIX_DEBTOR_ID);

        rejectUnknownOptions(optionMultimap.getPreamble());
        rejectUnknownOptions(textMultimap.getPreamble());
        List<String> idValues = new ArrayList<>(optionMultimap.getAllValues(PREFIX_DEBTOR_ID));
        idValues.addAll(textMultimap.getAllValues(PREFIX_DEBTOR_ID));
        for (String idValue : idValues) {
            rejectUnknownOptions(idValue);
        }
        for (String outcomeValue : optionMultimap.getAllValues(PREFIX_OUTCOME)) {
            rejectUnknownOptions(outcomeValue);
        }
        optionMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_OUTCOME);
        textMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_NOTE_TEXT);
        if (idValues.size() > 1) {
            throw new ParseException(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
        }
        if (!optionMultimap.getPreamble().isEmpty() || !textMultimap.getPreamble().isEmpty()
                || idValues.size() != 1 || textMultimap.getValue(PREFIX_NOTE_TEXT).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        int debtorId = parseDebtorId(idValues.getFirst());
        String noteText = textMultimap.getValue(PREFIX_NOTE_TEXT).get();
        if (!InteractionNote.isValidText(noteText)) {
            throw new ParseException(InteractionNote.MESSAGE_CONSTRAINTS);
        }

        Optional<String> outcomeText = optionMultimap.getValue(PREFIX_OUTCOME);
        if (outcomeText.isEmpty()) {
            return new NoteCommand(debtorId, noteText);
        }
        ContactOutcome outcome = ContactOutcome.fromToken(outcomeText.get())
                .orElseThrow(() -> new ParseException(ContactOutcome.MESSAGE_CONSTRAINTS));
        return new NoteCommand(debtorId, noteText, outcome);
    }

    private void rejectUnknownOptions(String parameterText) throws ParseException {
        Matcher matcher = LONG_OPTION_TOKEN.matcher(parameterText);
        while (matcher.find()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, matcher.group()));
        }
    }

    private int parseDebtorId(String idText) throws ParseException {
        if (!StringUtil.isNonZeroUnsignedInteger(idText)) {
            throw new ParseException(MESSAGE_INVALID_DEBTOR_ID);
        }
        return Integer.parseInt(idText);
    }
}
