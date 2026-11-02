package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        StringBuilder decimal = new StringBuilder(trimmedArgs.length());
        int firstNonZero = -1;
        for (int index = 0; index < trimmedArgs.length(); index++) {
            int digit = Character.digit(trimmedArgs.charAt(index), 10);
            if (digit < 0) {
                throw invalidIndex();
            }
            decimal.append((char) ('0' + digit));
            if (firstNonZero < 0 && digit != 0) {
                firstNonZero = index;
            }
        }
        if (firstNonZero < 0) {
            throw invalidIndex();
        }
        return DeleteCommand.fromOneBasedDecimal(decimal.substring(firstNonZero));
    }

    private ParseException invalidIndex() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

}
