package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.CommandLogFormatter.EMPTY_COMMAND;
import static seedu.address.logic.CommandLogFormatter.UNKNOWN_COMMAND;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandLogFormatterTest {

    @Test
    public void getCommandWord_knownCommandWithPersonalData_returnsCommandWordOnly() {
        String command = "add n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street";

        assertEquals("add", CommandLogFormatter.getCommandWord(command));
    }

    @Test
    public void getCommandWord_allKnownCommands_returnsCommandWord() {
        List<String> commandWords = List.of("add", "clear", "contact", "delete", "edit", "exit",
                "find", "followup", "help", "list", "list-amount", "note", "preview", "redo", "show", "sort", "stage",
                "summary", "undo", "list-stage");

        commandWords.forEach(commandWord ->
                assertEquals(commandWord, CommandLogFormatter.getCommandWord("  " + commandWord + " secret")));
    }

    @Test
    public void getCommandWord_unrecognisedCommand_returnsUnknownLabel() {
        assertEquals(UNKNOWN_COMMAND, CommandLogFormatter.getCommandWord("alice@example.com private details"));
    }

    @Test
    public void getCommandWord_blankCommand_returnsEmptyLabel() {
        assertEquals(EMPTY_COMMAND, CommandLogFormatter.getCommandWord("   "));
    }
}
