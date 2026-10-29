package seedu.address.logic.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the followup command's mutually exclusive date, clear, and done operations. */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {

    public static final String MESSAGE_INVALID_ID = "Debtor ID must be a positive integer.";
    public static final String MESSAGE_INVALID_DATE = "Follow-up date must use the valid YYYY-MM-DD format.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown followup option: %s.";

    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        String[] tokens = args.trim().isEmpty() ? new String[0] : args.trim().split("\\s+");
        Integer id = null;
        String dateText = null;
        boolean clear = false;
        boolean done = false;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            switch (token) {
                case "--id" -> {
                    if (id != null || i + 1 >= tokens.length) {
                        throw invalidFormat();
                    }
                    String value = tokens[++i];
                    if (!StringUtil.isNonZeroUnsignedInteger(value)) {
                        throw new ParseException(MESSAGE_INVALID_ID);
                    }
                    id = Integer.parseInt(value);
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
                case "--done" -> {
                    if (done) {
                        throw invalidFormat();
                    }
                    done = true;
                }
                default -> {
                    if (token.startsWith("--")) {
                        throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, token));
                    }
                    throw invalidFormat();
                }
            }
        }
        if (id == null || (clear ? 1 : 0) + (done ? 1 : 0) + (dateText == null ? 0 : 1) != 1) {
            throw invalidFormat();
        }
        if (clear) {
            return new FollowUpCommand(id, FollowUpCommand.Operation.CLEAR, Optional.empty());
        }
        if (done) {
            return new FollowUpCommand(id, FollowUpCommand.Operation.DONE, Optional.empty());
        }
        if (!dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
        try {
            LocalDate parsedDate = LocalDate.parse(dateText);
            if (parsedDate.getYear() < 1) {
                throw new ParseException(MESSAGE_INVALID_DATE);
            }
            return new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, Optional.of(parsedDate));
        } catch (DateTimeParseException exception) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                FollowUpCommand.MESSAGE_USAGE));
    }
}
