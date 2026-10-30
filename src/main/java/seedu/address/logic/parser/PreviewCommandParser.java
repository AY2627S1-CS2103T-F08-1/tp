package seedu.address.logic.parser;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.PreviewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses only edit or delete commands for a preview. */
public class PreviewCommandParser implements Parser<PreviewCommand> {
    @Override
    public PreviewCommand parse(String args) throws ParseException {
        String text = args.trim();
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2 || (!parts[0].equals(EditCommand.COMMAND_WORD)
                && !parts[0].equals(DeleteCommand.COMMAND_WORD))) {
            throw new ParseException(PreviewCommand.MESSAGE_USAGE);
        }
        Command command = parts[0].equals(EditCommand.COMMAND_WORD)
                ? new EditCommandParser().parse(parts[1]) : new DeleteCommandParser().parse(parts[1]);
        return new PreviewCommand(command);
    }
}
