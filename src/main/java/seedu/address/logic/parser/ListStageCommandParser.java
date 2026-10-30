package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListStageCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.RecoveryStage;

/** Parses exactly one recovery stage, rejecting unknown stages and trailing arguments. */
public final class ListStageCommandParser implements Parser<ListStageCommand> {
    @Override
    public ListStageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            return new ListStageCommand(RecoveryStage.parse(args));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListStageCommand.MESSAGE_USAGE),
                    exception);
        }
    }
}
