package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

class SummaryCommandTest {
    @Test
    void execute_emptyWorkloadReportsZeroValues() {
        String summary = new SummaryCommand(false).execute(new ModelManager()).getFeedbackToUser();

        assertTrue(summary.contains("Debtors: 0"));
        assertTrue(summary.contains("Outstanding total: S$0.00"));
        assertTrue(summary.contains("new: 0"));
    }
}
