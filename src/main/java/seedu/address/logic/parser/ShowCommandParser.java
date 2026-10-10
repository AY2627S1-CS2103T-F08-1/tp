package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;

import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ShowCommand} object.
 */
public class ShowCommandParser implements Parser<ShowCommand> {

    public static final String MESSAGE_INVALID_DEBTOR_ID = "Debtor ID must be a positive integer.";

    private static final Pattern EXACT_ARGUMENT_FORMAT = Pattern.compile("--id\\s+\\S+");

    @Override
    public ShowCommand parse(String args) throws ParseException {
        String normalizedArgs = " " + args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_DEBTOR_ID);
        if (!argMultimap.getPreamble().isEmpty()
                || argMultimap.getValue(PREFIX_DEBTOR_ID).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ShowCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID);
        String idText = argMultimap.getValue(PREFIX_DEBTOR_ID).get();
        if (idText.isBlank()) {
            throw new ParseException(MESSAGE_INVALID_DEBTOR_ID);
        }
        if (!EXACT_ARGUMENT_FORMAT.matcher(args.trim()).matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ShowCommand.MESSAGE_USAGE));
        }

        return new ShowCommand(parseDebtorId(idText));
    }

    private int parseDebtorId(String idText) throws ParseException {
        if (!StringUtil.isNonZeroUnsignedInteger(idText)) {
            throw new ParseException(MESSAGE_INVALID_DEBTOR_ID);
        }
        return Integer.parseInt(idText);
    }
}
