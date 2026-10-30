package seedu.address.logic.parser;

import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactPreferences;

/** Parses the contact command's fixed options. */
public class ContactCommandParser implements Parser<ContactCommand> {
    @Override
    public ContactCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException(ContactCommand.MESSAGE_USAGE);
        }
        String[] tokens = trimmed.split("\\s+");
        Set<String> seen = new HashSet<>();
        String id = null;
        String method = null;
        String hours = null;
        for (int i = 0; i < tokens.length; i++) {
            String option = tokens[i];
            if (!seen.add(option)) {
                throw new ParseException("Duplicate option: " + option);
            }
            switch (option) {
                case "--id":
                case "--method":
                case "--hours":
                    if (++i == tokens.length || tokens[i].startsWith("--")) {
                        throw new ParseException("Missing value for " + option);
                    }
                    if (option.equals("--id")) {
                        id = tokens[i];
                    } else if (option.equals("--method")) {
                        method = tokens[i];
                    } else {
                        hours = tokens[i];
                    }
                    break;
                case "--clear-method":
                case "--clear-hours":
                    break;
                default:
                    throw new ParseException("Unknown option: " + option);
            }
        }
        boolean changeMethod = seen.contains("--method") || seen.contains("--clear-method");
        boolean changeHours = seen.contains("--hours") || seen.contains("--clear-hours");
        if (id == null || !StringUtil.isNonZeroUnsignedInteger(id) || (!changeMethod && !changeHours)
                || (seen.contains("--method") && seen.contains("--clear-method"))
                || (seen.contains("--hours") && seen.contains("--clear-hours"))) {
            throw new ParseException(ContactCommand.MESSAGE_USAGE);
        }
        try {
            ContactPreferences preferences = new ContactPreferences(method, hours);
            return new ContactCommand(Integer.parseInt(id), changeMethod, preferences.getMethod(),
                    changeHours, preferences.getHours());
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage());
        }
    }
}
