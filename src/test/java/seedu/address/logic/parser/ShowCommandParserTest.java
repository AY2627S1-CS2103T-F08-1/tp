package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ShowCommand;

public class ShowCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ShowCommand.MESSAGE_USAGE);

    private final ShowCommandParser parser = new ShowCommandParser();

    @Test
    public void parse_validDebtorId_returnsShowCommand() {
        assertParseSuccess(parser, " --id 3", new ShowCommand(3));
        assertParseSuccess(parser, "   --id    2147483647   ", new ShowCommand(Integer.MAX_VALUE));
    }

    @Test
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, " unexpected", INVALID_FORMAT);
    }

    @Test
    public void parse_blankId_throwsParseException() {
        assertParseFailure(parser, " --id   ", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        assertParseFailure(parser, " --id zero", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 0", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id -1", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 1.5", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 2147483648", ShowCommandParser.MESSAGE_INVALID_DEBTOR_ID);
    }

    @Test
    public void parse_duplicateId_throwsParseException() {
        assertParseFailure(parser, " --id 1 --id 2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
    }

    @Test
    public void parse_trailingText_throwsParseException() {
        assertParseFailure(parser, " --id 3 extra", INVALID_FORMAT);
    }

    @Test
    public void parse_unsupportedOption_throwsParseException() {
        assertParseFailure(parser, " --option value --id 3", INVALID_FORMAT);
        assertParseFailure(parser, " --id 3 --option value", INVALID_FORMAT);
    }

    @Test
    public void parse_malformedIdPrefix_throwsParseException() {
        assertParseFailure(parser, " --id3", INVALID_FORMAT);
    }
}
