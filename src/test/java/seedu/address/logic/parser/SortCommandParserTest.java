package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_BY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_ORDER;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.person.PersonSort.Direction;
import seedu.address.model.person.PersonSort.Field;

public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_eachFieldWithoutOrder_returnsAscendingCommand() {
        assertParseSuccess(parser, " --by id", new SortCommand(Field.ID, Direction.ASCENDING));
        assertParseSuccess(parser, " --by name", new SortCommand(Field.NAME, Direction.ASCENDING));
        assertParseSuccess(parser, " --by amount", new SortCommand(Field.AMOUNT, Direction.ASCENDING));
    }

    @Test
    public void parse_eachDirection_returnsExpectedCommand() {
        assertParseSuccess(parser, " --by name --order asc",
                new SortCommand(Field.NAME, Direction.ASCENDING));
        assertParseSuccess(parser, " --by amount --order desc",
                new SortCommand(Field.AMOUNT, Direction.DESCENDING));
    }

    @Test
    public void parse_optionsInEitherOrder_returnsExpectedCommand() {
        SortCommand expectedCommand = new SortCommand(Field.AMOUNT, Direction.DESCENDING);

        assertParseSuccess(parser, " --by amount --order desc", expectedCommand);
        assertParseSuccess(parser, " --order desc --by amount", expectedCommand);
    }

    @Test
    public void parse_uppercaseTokens_returnsExpectedCommand() {
        assertParseSuccess(parser, " --by NAME --order DESC",
                new SortCommand(Field.NAME, Direction.DESCENDING));
    }

    @Test
    public void parse_whitespaceAroundOptions_returnsExpectedCommand() {
        assertParseSuccess(parser, "\t--by\tamount\r\n\t--order\tdesc  ",
                new SortCommand(Field.AMOUNT, Direction.DESCENDING));
    }

    @Test
    public void parse_missingRequiredByOption_throwsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " --order asc", expectedMessage);
        assertParseFailure(parser, " amount", expectedMessage);
    }

    @Test
    public void parse_blankByValue_throwsInvalidField() {
        assertParseFailure(parser, " --by", SortCommandParser.MESSAGE_INVALID_FIELD);
        assertParseFailure(parser, " --by --order asc", SortCommandParser.MESSAGE_INVALID_FIELD);
    }

    @Test
    public void parse_invalidField_throwsSpecificMessage() {
        assertParseFailure(parser, " --by phone", SortCommandParser.MESSAGE_INVALID_FIELD);
        assertParseFailure(parser, " --by names", SortCommandParser.MESSAGE_INVALID_FIELD);
        assertParseFailure(parser, " --by name extra", SortCommandParser.MESSAGE_INVALID_FIELD);
    }

    @Test
    public void parse_blankOrInvalidOrder_throwsSpecificMessage() {
        assertParseFailure(parser, " --by id --order", SortCommandParser.MESSAGE_INVALID_ORDER);
        assertParseFailure(parser, " --by id --order ascending", SortCommandParser.MESSAGE_INVALID_ORDER);
        assertParseFailure(parser, " --by id --order desc extra", SortCommandParser.MESSAGE_INVALID_ORDER);
    }

    @Test
    public void parse_duplicateOptions_throwsDuplicatePrefixMessage() {
        assertParseFailure(parser, " --by id --by name",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SORT_BY));
        assertParseFailure(parser, " --by id --order asc --order desc",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SORT_ORDER));
        assertParseFailure(parser, " --by id --order asc --by name --order desc",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SORT_BY, PREFIX_SORT_ORDER));
    }

    @Test
    public void parse_unknownLongOption_throwsSpecificMessage() {
        assertUnknownOption(" --descending", "--descending");
        assertUnknownOption(" --by name --reverse", "--reverse");
        assertUnknownOption(" --by name --order asc --then id", "--then");
    }

    @Test
    public void parse_longOptionWithoutBoundary_throwsUnknownOption() {
        assertUnknownOption(" --byname", "--byname");
    }

    @Test
    public void commandValueObjects_exposeCanonicalTokens() {
        assertEquals("name", Field.NAME.getToken());
        assertEquals("desc", Direction.DESCENDING.getToken());
        assertEquals("descending", Direction.DESCENDING.getDescription());
    }

    private void assertUnknownOption(String arguments, String option) {
        assertParseFailure(parser, arguments,
                String.format(SortCommandParser.MESSAGE_UNKNOWN_OPTION, option));
    }
}
