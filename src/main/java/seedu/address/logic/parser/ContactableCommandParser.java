package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import seedu.address.logic.commands.ContactableCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactPreferences;

/** Parses optional time and method filters for the contactability view. */
public class ContactableCommandParser implements Parser<ContactableCommand> {
    @Override
    public ContactableCommand parse(String args) throws ParseException {
        String text = args.trim();
        String time = null;
        String method = null;
        Set<String> seen = new HashSet<>();
        if (!text.isEmpty()) {
            String[] tokens = text.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                String option = tokens[i];
                if ((!option.equals("--at") && !option.equals("--method")) || !seen.add(option)) {
                    throw new ParseException(ContactableCommand.MESSAGE_USAGE);
                }
                if (++i == tokens.length || tokens[i].startsWith("--")) {
                    throw new ParseException("Missing value for " + option);
                }
                if (option.equals("--at")) {
                    time = tokens[i];
                } else {
                    method = tokens[i];
                }
            }
        }
        if (time != null && !time.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new ParseException("Time must be HH:mm in 24-hour local time.");
        }
        try {
            String canonicalMethod = new ContactPreferences(method, null).getMethod();
            return new ContactableCommand(time == null ? null : LocalTime.parse(time), canonicalMethod,
                    Clock.systemDefaultZone());
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage());
        }
    }
}
