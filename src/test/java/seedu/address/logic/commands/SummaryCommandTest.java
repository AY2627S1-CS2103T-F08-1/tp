package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.PersonBuilder;

class SummaryCommandTest {
    @Test
    void execute_emptyWorkloadReportsZeroValues() {
        String summary = new SummaryCommand(false).execute(new ModelManager()).getFeedbackToUser();

        assertTrue(summary.contains("Debtors: 0"));
        assertTrue(summary.contains("Outstanding total: S$0.00"));
        assertTrue(summary.contains("new: 0"));
    }

    @Test
    void execute_allWorkloadReportsBalancesAndEveryStage() {
        ModelManager model = new ModelManager();
        Person first = model.addPerson(new PersonBuilder().withOutstandingAmount("1,250.50").build());
        Person second = model.addPerson(new PersonBuilder().withName("Bob").withPhone("91234567")
                .withEmail("bob@example.com").withOutstandingAmount("0").build());
        model.setPerson(first, first.withRecoveryStage(RecoveryStage.CONTACTED));
        model.setPerson(second, second.withRecoveryStage(RecoveryStage.SETTLED));

        String summary = new SummaryCommand(true).execute(model).getFeedbackToUser();

        assertTrue(summary.contains("Case workload summary (all active debtors)"));
        assertTrue(summary.contains("Debtors: 2"));
        assertTrue(summary.contains("Outstanding total: S$1,250.50"));
        assertTrue(summary.contains("Zero-balance debtors: 1"));
        assertTrue(summary.contains("contacted: 1"));
        assertTrue(summary.contains("settled: 1"));
    }
}
