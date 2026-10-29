package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.SortCommand;

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
            DeleteCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD,
            ExitCommand.COMMAND_WORD,
            FindCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD,
            NoteCommand.COMMAND_WORD,
            ShowCommand.COMMAND_WORD,
            SortCommand.COMMAND_WORD);

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
