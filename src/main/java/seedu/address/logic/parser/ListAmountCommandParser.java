package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MIN_AMOUNT;

import java.util.Optional;

import seedu.address.logic.commands.ListAmountCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.OutstandingAmountRange;

/** Parses arguments for {@link ListAmountCommand}. */
public final class ListAmountCommandParser implements Parser<ListAmountCommand> {

    @Override
    public ListAmountCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                args, PREFIX_MIN_AMOUNT, PREFIX_MAX_AMOUNT);

        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, ListAmountCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_MIN_AMOUNT, PREFIX_MAX_AMOUNT);

        Optional<OutstandingAmount> minimum = parseOptionalBound(arguments, PREFIX_MIN_AMOUNT);
        Optional<OutstandingAmount> maximum = parseOptionalBound(arguments, PREFIX_MAX_AMOUNT);
        if (minimum.isEmpty() && maximum.isEmpty()) {
            throw new ParseException(OutstandingAmountRange.MESSAGE_REQUIRES_BOUND
                    + "\n" + ListAmountCommand.MESSAGE_USAGE);
        }

        try {
            return new ListAmountCommand(new OutstandingAmountRange(minimum, maximum));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage());
        }
    }

    private Optional<OutstandingAmount> parseOptionalBound(
            ArgumentMultimap arguments, Prefix prefix) throws ParseException {
        Optional<String> rawValue = arguments.getValue(prefix);
        if (rawValue.isEmpty()) {
            return Optional.empty();
        }
        if (rawValue.get().chars().anyMatch(Character::isWhitespace)) {
            throw new ParseException(ListAmountCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
        }
        return Optional.of(ParserUtil.parseOutstandingAmount(rawValue.get()));
    }
}
