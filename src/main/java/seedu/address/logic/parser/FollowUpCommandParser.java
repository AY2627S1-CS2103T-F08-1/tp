package seedu.address.logic.parser;

import java.time.LocalDate;
import java.util.Optional;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUpEvent;

/** Parses the followup command's mutually exclusive date, clear, and done operations. */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {

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
                    id = ParserUtil.parseDebtorId(tokens[++i]);
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
        Optional<LocalDate> parsedDate = FollowUpEvent.tryParseDate(dateText);
        if (parsedDate.isEmpty()) {
            throw new ParseException(FollowUpEvent.MESSAGE_INVALID_DATE);
        }
        return new FollowUpCommand(id, FollowUpCommand.Operation.SCHEDULE, parsedDate);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                FollowUpCommand.MESSAGE_USAGE));
    }
}
