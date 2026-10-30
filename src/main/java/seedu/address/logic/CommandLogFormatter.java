package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ContactCommand;
import seedu.address.logic.commands.ContactableCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListAmountCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListStageCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.PreviewCommand;
import seedu.address.logic.commands.PromiseCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.commands.UndoCommand;

/**
 * Formats command information for logs without including command arguments.
 */
public final class CommandLogFormatter {
    static final String EMPTY_COMMAND = "<empty>";
    static final String UNKNOWN_COMMAND = "<unknown>";

    private static final Pattern COMMAND_WORD_FORMAT = Pattern.compile("^\\s*(?<commandWord>\\S+)");
    private static final Set<String> KNOWN_COMMAND_WORDS = Set.of(
            AddCommand.COMMAND_WORD,
            ClearCommand.COMMAND_WORD,
            ContactCommand.COMMAND_WORD,
            ContactableCommand.COMMAND_WORD,
            DeleteCommand.COMMAND_WORD,
            DueCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD,
            ExitCommand.COMMAND_WORD,
            ExportCommand.COMMAND_WORD,
            FindCommand.COMMAND_WORD,
            FollowUpCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD,
            ListAmountCommand.COMMAND_WORD,
            ListStageCommand.COMMAND_WORD,
            NoteCommand.COMMAND_WORD,
            PreviewCommand.COMMAND_WORD,
            PromiseCommand.COMMAND_WORD,
            RedoCommand.COMMAND_WORD,
            ShowCommand.COMMAND_WORD,
            SortCommand.COMMAND_WORD,
            StageCommand.COMMAND_WORD,
            SummaryCommand.COMMAND_WORD,
            UndoCommand.COMMAND_WORD);

    private CommandLogFormatter() {
    }

    /**
     * Returns a safe label for the command in {@code commandText}.
     *
     * <p>Only known command words are returned. Arguments and unrecognised words are omitted because they may
     * contain personal data.
     */
    public static String getCommandWord(String commandText) {
        requireNonNull(commandText);
        Matcher matcher = COMMAND_WORD_FORMAT.matcher(commandText);
        if (!matcher.find()) {
            return EMPTY_COMMAND;
        }

        String commandWord = matcher.group("commandWord");
        return KNOWN_COMMAND_WORDS.contains(commandWord) ? commandWord : UNKNOWN_COMMAND;
    }
}
