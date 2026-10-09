package seedu.address.logic.parser;

import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    void parse_validRemark_success() {
        assertParseSuccess(parser, "1 r/Likes swimming",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")));
    }

    @Test
    void parse_duplicateRemarkPrefix_failure() {
        assertParseFailure(parser, "1 r/first r/second", getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
