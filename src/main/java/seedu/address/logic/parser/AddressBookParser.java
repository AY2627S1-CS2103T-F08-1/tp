package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.CommandLogFormatter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.commands.ContactableCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListAmountCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.PreviewCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);
    private static final Map<String, CommandParser> COMMAND_PARSERS = Map.ofEntries(
            Map.entry(AddCommand.COMMAND_WORD, arguments -> new AddCommandParser().parse(arguments)),
            Map.entry(ClearCommand.COMMAND_WORD, arguments -> {
                if (!arguments.trim().isEmpty()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
                }
                return new ClearCommand();
            }),
            Map.entry(ContactCommand.COMMAND_WORD, arguments -> new ContactCommandParser().parse(arguments)),
            Map.entry(ContactableCommand.COMMAND_WORD, arguments -> new ContactableCommandParser().parse(arguments)),
            Map.entry(DeleteCommand.COMMAND_WORD, arguments -> new DeleteCommandParser().parse(arguments)),
            Map.entry(EditCommand.COMMAND_WORD, arguments -> new EditCommandParser().parse(arguments)),
            Map.entry(ExitCommand.COMMAND_WORD, arguments -> new ExitCommand()),
            Map.entry(ExportCommand.COMMAND_WORD, arguments -> new ExportCommandParser().parse(arguments)),
            Map.entry(FindCommand.COMMAND_WORD, arguments -> new FindCommandParser().parse(arguments)),
            Map.entry(FollowUpCommand.COMMAND_WORD, arguments -> new FollowUpCommandParser().parse(arguments)),
            Map.entry(HelpCommand.COMMAND_WORD, arguments -> new HelpCommand()),
            Map.entry(ListCommand.COMMAND_WORD, arguments -> new ListCommandParser().parse(arguments)),
            Map.entry(ListAmountCommand.COMMAND_WORD, arguments -> new ListAmountCommandParser().parse(arguments)),
            Map.entry(NoteCommand.COMMAND_WORD, arguments -> new NoteCommandParser().parse(arguments)),
            Map.entry(PreviewCommand.COMMAND_WORD, arguments -> new PreviewCommandParser().parse(arguments)),
            Map.entry(RedoCommand.COMMAND_WORD, arguments -> {
                if (!arguments.trim().isEmpty()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            RedoCommand.COMMAND_WORD + ": Redoes the most recently undone change.\nParameters: none"));
                }
                return new RedoCommand();
            }),
            Map.entry(ShowCommand.COMMAND_WORD, arguments -> new ShowCommandParser().parse(arguments)),
            Map.entry(SortCommand.COMMAND_WORD, arguments -> new SortCommandParser().parse(arguments)),
            Map.entry(StageCommand.COMMAND_WORD, arguments -> new StageCommandParser().parse(arguments)),
            Map.entry(SummaryCommand.COMMAND_WORD, arguments -> new SummaryCommandParser().parse(arguments)),
            Map.entry(UndoCommand.COMMAND_WORD, arguments -> {
                if (!arguments.trim().isEmpty()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            UndoCommand.COMMAND_WORD + ": Undoes the most recent change.\nParameters: none"));
                }
                return new UndoCommand();
            }));

    /** Parses one command's argument text into an executable command. */
    @FunctionalInterface
    private interface CommandParser {
        Command parse(String arguments) throws ParseException;
    }

    /** Exposes the immutable dispatch keys to the cross-package {@code BrandingTest}. */
    public static Set<String> getSupportedCommandWords() {
        return COMMAND_PARSERS.keySet();
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + CommandLogFormatter.getCommandWord(userInput));

        CommandParser commandParser = COMMAND_PARSERS.get(commandWord);
        if (commandParser == null) {
            logger.finer("Command parsing failed for command word: "
                    + CommandLogFormatter.getCommandWord(userInput));
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }
        return commandParser.parse(arguments);
    }

}
