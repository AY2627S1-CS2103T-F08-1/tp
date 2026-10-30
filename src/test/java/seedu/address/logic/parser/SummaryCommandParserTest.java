package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SummaryCommand;
import seedu.address.model.ModelManager;

class SummaryCommandParserTest {
    private final SummaryCommandParser parser = new SummaryCommandParser();

    @Test
    void parse_emptyAndAllArgs_selectExpectedScope() throws Exception {
        assertTrue(parser.parse("   ").execute(new ModelManager()).getFeedbackToUser()
                .contains("displayed debtors"));
        assertTrue(parser.parse(" --all ").execute(new ModelManager()).getFeedbackToUser()
                .contains("all active debtors"));
    }

    @Test
    void parse_otherArgs_rejectsInput() {
        assertParseFailure(parser, " --all extra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SummaryCommand.MESSAGE_USAGE));
    }
}
