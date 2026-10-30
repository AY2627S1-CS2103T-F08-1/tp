package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the optional all-records scope for a workload summary. */
public class SummaryCommandParser implements Parser<SummaryCommand> {
    @Override
    public SummaryCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            return new SummaryCommand(false);
        }
        if (trimmed.equals("--all")) {
            return new SummaryCommand(true);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SummaryCommand.MESSAGE_USAGE));
    }
}
