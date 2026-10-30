package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUpEvent;

/** Parses due-date cutoff and inclusive range forms. */
public class DueCommandParser implements Parser<DueCommand> {
    public static final String MESSAGE_DATE = FollowUpEvent.MESSAGE_INVALID_DATE;

    @Override
    public DueCommand parse(String args) throws ParseException {
        String input = args.trim();
        if (input.isEmpty()) {
            return new DueCommand(null, null, Clock.systemDefaultZone());
        }
        String[] tokens = input.split("\\s+");
        if (tokens.length == 1 && !tokens[0].startsWith("--")) {
            return new DueCommand(null, parseDate(tokens[0]), Clock.systemDefaultZone());
        }
        if (tokens.length != 4) {
            throw invalidFormat();
        }
        LocalDate from = null;
        LocalDate to = null;
        for (int i = 0; i < tokens.length; i += 2) {
            if (tokens[i].equals("--from") && from == null) {
                from = parseDate(tokens[i + 1]);
            } else if (tokens[i].equals("--to") && to == null) {
                to = parseDate(tokens[i + 1]);
            } else {
                throw invalidFormat();
            }
        }
        if (from.isAfter(to)) {
            throw new ParseException("Provide an inclusive range with the start on or before the end.");
        }
        return new DueCommand(from, to, Clock.systemDefaultZone());
    }

    private LocalDate parseDate(String input) throws ParseException {
        return FollowUpEvent.tryParseDate(input).orElseThrow(() -> new ParseException(MESSAGE_DATE));
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DueCommand.MESSAGE_USAGE));
    }
}
