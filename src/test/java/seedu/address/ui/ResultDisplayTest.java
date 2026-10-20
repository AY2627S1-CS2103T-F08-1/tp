package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests dynamic row sizing for the result display.
 */
public class ResultDisplayTest {

    @Test
    public void getVisibleRowCount_shortFeedback_usesMinimumRows() {
        assertEquals(4, ResultDisplay.getVisibleRowCount("Debtor added."));
    }

    @Test
    public void getVisibleRowCount_multilineFeedback_usesLineCount() {
        String feedback = "Debtor profile:\nID: 7\nName: Alex\nPhone: 91234567\nEmail: alex@example.com\n"
                + "Address: 123 Example Street";

        assertEquals(6, ResultDisplay.getVisibleRowCount(feedback));
    }

    @Test
    public void getVisibleRowCount_longFeedback_usesMaximumRows() {
        String feedback = "Profile detail\n".repeat(20);

        assertEquals(12, ResultDisplay.getVisibleRowCount(feedback));
    }
}
