package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;

class SuspiciousAddressWarningTest {
    @Test
    void detectsCommonMistypedPrefixesWithoutRejectingOrdinarySlashes() {
        for (String value : new String[] {"12 Road 0/500", "12 Road O/500", "12 Road o /250",
                "12 Road p /93333333", "12 Road t /urgent"}) {
            assertEquals(Messages.SUSPICIOUS_ADDRESS_WARNING, Messages.suspiciousAddressWarning(value));
        }
        assertEquals("", Messages.suspiciousAddressWarning("Block 4/6 Unit 12"));
        assertEquals("", Messages.suspiciousAddressWarning("12 Road"));
    }

    @Test
    void addAndEdit_warnWhileKeepingEnteredAddress() throws Exception {
        var parser = new AddressBookParser();
        var model = new ModelManager();
        var add = (AddCommand) parser.parseCommand(
                "add n/Alice p/91111111 e/alice@example.com a/12 Road 0/500");
        assertTrue(add.execute(model).getFeedbackToUser().contains(Messages.SUSPICIOUS_ADDRESS_WARNING));
        assertEquals("12 Road 0/500", model.getFilteredPersonList().getFirst().getAddress().value);
        assertTrue(parser.parseCommand("edit 1 a/14 Road o /250").execute(model).getFeedbackToUser()
                .contains(Messages.SUSPICIOUS_ADDRESS_WARNING));
        assertEquals("14 Road o /250", model.getFilteredPersonList().getFirst().getAddress().value);
    }
}
