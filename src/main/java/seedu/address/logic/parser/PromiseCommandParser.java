package seedu.address.logic.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.PromiseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the promise command's mutually exclusive set and clear operations. */
public class PromiseCommandParser implements Parser<PromiseCommand> {

    public static final String MESSAGE_INVALID_ID = "Debtor ID must be a positive integer.";
    public static final String MESSAGE_INVALID_DATE =
            "Promised payment date must be a real date in YYYY-MM-DD format, from 0001-01-01 to 9999-12-31.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown promise option: %s.";

    @Override
    public PromiseCommand parse(String args) throws ParseException {
        String[] tokens = args.trim().isEmpty() ? new String[0] : args.trim().split("\\s+");
        Integer id = null;
        String dateText = null;
        boolean clear = false;
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "--id" -> {
                    if (id != null || i + 1 >= tokens.length) {
                        throw invalidFormat();
                    }
                    id = parseId(tokens[++i]);
                }
                case "--date" -> {
                    if (dateText != null || i + 1 >= tokens.length) {
                        throw invalidFormat();
                    }
                    dateText = tokens[++i];
                }
                case "--clear" -> {
                    if (clear) {
                        throw invalidFormat();
                    }
                    clear = true;
                }
                default -> {
                    if (tokens[i].startsWith("--")) {
                        throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, tokens[i]));
                    }
                    throw invalidFormat();
                }
            }
        }
        if (id == null || (clear ? 1 : 0) + (dateText == null ? 0 : 1) != 1) {
            throw invalidFormat();
        }
        if (clear) {
            return new PromiseCommand(id, Optional.empty());
        }
        return new PromiseCommand(id, parseDate(dateText));
    }

    private int parseId(String idText) throws ParseException {
        if (!StringUtil.isNonZeroUnsignedInteger(idText)) {
            throw new ParseException(MESSAGE_INVALID_ID);
        }
        try {
            return Integer.parseInt(idText);
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_ID);
        }
    }

    private LocalDate parseDate(String dateText) throws ParseException {
        if (!dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
        try {
            LocalDate date = LocalDate.parse(dateText);
            if (date.getYear() < 1) {
                throw new ParseException(MESSAGE_INVALID_DATE);
            }
            return date;
        } catch (DateTimeParseException exception) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, PromiseCommand.MESSAGE_USAGE));
    }
}
