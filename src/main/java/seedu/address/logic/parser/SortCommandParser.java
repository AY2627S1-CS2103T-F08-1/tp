package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_BY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_ORDER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;

/** Parses arguments for {@link SortCommand}. */
public class SortCommandParser implements Parser<SortCommand> {

    public static final String MESSAGE_INVALID_FIELD = "Sort field must be one of: id, name, amount.";
    public static final String MESSAGE_INVALID_ORDER = "Sort order must be asc or desc.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown sort option: %s. Use --by and --order.";

    private static final Pattern LONG_OPTION_TOKEN = Pattern.compile("(?<!\\S)--\\S+");

    @Override
    public SortCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_SORT_BY, PREFIX_SORT_ORDER);
        rejectUnknownOptions(argMultimap);

        if (!argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_SORT_BY).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_SORT_BY, PREFIX_SORT_ORDER);

        Field field = Field.fromToken(argMultimap.getValue(PREFIX_SORT_BY).orElseThrow())
                .orElseThrow(() -> new ParseException(MESSAGE_INVALID_FIELD));
        Direction direction = parseDirection(argMultimap);
        return new SortCommand(field, direction);
    }

    private Direction parseDirection(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_SORT_ORDER).isEmpty()) {
            return Direction.ASCENDING;
        }
        return Direction.fromToken(argMultimap.getValue(PREFIX_SORT_ORDER).orElseThrow())
                .orElseThrow(() -> new ParseException(MESSAGE_INVALID_ORDER));
    }

    private void rejectUnknownOptions(ArgumentMultimap argMultimap) throws ParseException {
        rejectUnknownOptionsIn(argMultimap.getPreamble());
        for (String value : argMultimap.getAllValues(PREFIX_SORT_BY)) {
            rejectUnknownOptionsIn(value);
        }
        for (String value : argMultimap.getAllValues(PREFIX_SORT_ORDER)) {
            rejectUnknownOptionsIn(value);
        }
    }

    private void rejectUnknownOptionsIn(String parameterText) throws ParseException {
        Matcher matcher = LONG_OPTION_TOKEN.matcher(parameterText);
        if (matcher.find()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, matcher.group()));
        }
    }
}
