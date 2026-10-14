package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.ui.HelpWindow;
import seedu.address.ui.MainWindow;

public class BrandingTest {

    @Test
    public void releaseIdentity_matchesV13Branding() {
        assertEquals("OSPS", MainApp.APP_NAME);
        assertEquals("v1.3", MainApp.VERSION);
        assertEquals("OSPS v1.3", MainWindow.APP_TITLE);
        assertEquals("Exiting OSPS as requested ...", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void helpUrl_pointsToTeamUserGuide() {
        assertEquals("https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html",
                HelpWindow.USERGUIDE_URL);
    }
}
