package seedu.address.logic.parser;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a persistent debtor ID for profile export. */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) throws ParseException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2 || !parts[0].equals("--id")
                || !StringUtil.isNonZeroUnsignedInteger(parts[1])) {
            throw new ParseException(ExportCommand.MESSAGE_USAGE);
        }
        return new ExportCommand(Integer.parseInt(parts[1]));
    }
}
