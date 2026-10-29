package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.ui.HelpWindow;
import seedu.address.ui.MainWindow;

public class BrandingTest {

    private static final Map<String, String> RELEASED_COMMANDS = Map.ofEntries(
            Map.entry("add n/NAME p/PHONE e/EMAIL a/ADDRESS [o/AMOUNT] [t/TAG]...",
                    "add n/Alice p/91234567 e/alice@example.com a/Example Street"),
            Map.entry("list", "list"),
            Map.entry("find KEYWORD [MORE_KEYWORDS]...", "find Alice"),
            Map.entry("sort --by id|name|amount [--order asc|desc]", "sort --by id --order asc"),
            Map.entry("show --id DEBTOR_ID", "show --id 1"),
            Map.entry("note --id DEBTOR_ID [--outcome OUTCOME] --text NOTE_TEXT",
                    "note --id 1 --text Called debtor"),
            Map.entry("contact --id DEBTOR_ID [--method METHOD | --clear-method] "
                    + "[--hours HH:mm-HH:mm | --clear-hours]", "contact --id 1 --method sms"),
            Map.entry("edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/AMOUNT] [t/TAG]...",
                    "edit 1 n/Alice"),
            Map.entry("delete INDEX", "delete 1"),
            Map.entry("clear", "clear"),
            Map.entry("undo", "undo"),
            Map.entry("redo", "redo"),
            Map.entry("help", "help"),
            Map.entry("exit", "exit"));

    @Test
    public void releaseIdentity_matchesV14Branding() {
        assertEquals("OSPS", MainApp.APP_NAME);
        assertEquals("v1.5", MainApp.VERSION);
        assertEquals("OSPS v1.5", MainWindow.APP_TITLE);
        assertEquals("Exiting OSPS as requested ...", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void helpUrl_pointsToTeamUserGuide() {
        assertEquals("https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html",
                HelpWindow.USERGUIDE_URL);
    }

    @Test
    public void commandReference_listsEveryReleasedCommand() {
        Set<String> referenceLines = Set.copyOf(HelpWindow.COMMAND_REFERENCE.lines().toList());
        assertTrue(referenceLines.containsAll(RELEASED_COMMANDS.keySet()));
    }

    @Test
    public void commandReference_everyListedCommandIsAcceptedByParser() {
        AddressBookParser parser = new AddressBookParser();
        RELEASED_COMMANDS.forEach((syntax, validExample) -> {
            assertEquals(syntax.split(" ", 2)[0], validExample.split(" ", 2)[0]);
            assertDoesNotThrow(() -> parser.parseCommand(validExample));
        });
    }
}
