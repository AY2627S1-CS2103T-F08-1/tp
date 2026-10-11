package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

/**
 * Parses input arguments and creates a new RemarkCommand object.
 */
public class RemarkCommandParser implements Parser<RemarkCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the RemarkCommand
     * and returns a RemarkCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public RemarkCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }

        // Split at the first "r/" prefix.
        int remarkIndex = trimmedArgs.indexOf("r/");
        if (remarkIndex == -1 || remarkIndex + 2 > trimmedArgs.length()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }

        String indexPart = trimmedArgs.substring(0, remarkIndex).trim();
        String remarkPart = trimmedArgs.substring(remarkIndex + 2);

        Index index;
        try {
            index = ParserUtil.parseIndex(indexPart);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE), pe);
        }

        return new RemarkCommand(index, new Remark(remarkPart));
    }
}
