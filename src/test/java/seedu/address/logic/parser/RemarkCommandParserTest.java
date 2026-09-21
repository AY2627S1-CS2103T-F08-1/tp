package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_indexAndRemarkSpecified_success() {
        String remarkText = "Likes swimming.";
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + remarkText;

        assertParseSuccess(parser, userInput,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remarkText)));
    }

    @Test
    public void parse_indexAndEmptyRemarkSpecified_success() {
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK;

        assertParseSuccess(parser, userInput,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_missingIndex_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " " + PREFIX_REMARK + "Likes swimming.", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "zero " + PREFIX_REMARK + "Likes swimming.", expectedMessage);
    }
}
