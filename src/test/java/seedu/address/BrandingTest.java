package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.ui.HelpWindow;
import seedu.address.ui.MainWindow;

public class BrandingTest {

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
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("add n/NAME"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("list\n"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("find KEYWORD"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("sort --by id|name|amount"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("show --id DEBTOR_ID"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("note --id DEBTOR_ID [--outcome OUTCOME]"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("contact --id DEBTOR_ID"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("edit INDEX"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("delete INDEX"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("clear\n"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("undo\n"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("redo\n"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("help\n"));
        assertTrue(HelpWindow.COMMAND_REFERENCE.contains("exit\n"));
    }
}
