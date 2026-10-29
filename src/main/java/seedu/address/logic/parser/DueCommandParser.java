package seedu.address.logic.parser;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;

import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses due-date cutoff and inclusive range forms. */
public class DueCommandParser implements Parser<DueCommand> {
    public static final String MESSAGE_DATE = "Date must be a valid YYYY-MM-DD from year 0001 to 9999.";

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
            throw new ParseException(DueCommand.MESSAGE_USAGE);
        }
        LocalDate from = null;
        LocalDate to = null;
        for (int i = 0; i < tokens.length; i += 2) {
            if (tokens[i].equals("--from") && from == null) {
                from = parseDate(tokens[i + 1]);
            } else if (tokens[i].equals("--to") && to == null) {
                to = parseDate(tokens[i + 1]);
            } else {
                throw new ParseException(DueCommand.MESSAGE_USAGE);
            }
        }
        if (from.isAfter(to)) {
            throw new ParseException("Provide an inclusive range with the start on or before the end.");
        }
        return new DueCommand(from, to, Clock.systemDefaultZone());
    }

    private LocalDate parseDate(String input) throws ParseException {
        if (!input.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(MESSAGE_DATE);
        }
        try {
            LocalDate date = LocalDate.parse(input);
            if (date.getYear() == 0) {
                throw new ParseException(MESSAGE_DATE);
            }
            return date;
        } catch (DateTimeException exception) {
            throw new ParseException(MESSAGE_DATE);
        }
    }
}
