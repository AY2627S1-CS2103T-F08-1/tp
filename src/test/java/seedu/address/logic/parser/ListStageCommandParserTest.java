package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListStageCommand;
import seedu.address.model.person.RecoveryStage;

public class ListStageCommandParserTest {
    private final ListStageCommandParser parser = new ListStageCommandParser();

    @Test
    public void parse_eachCanonicalStage_acceptsCaseAndSurroundingWhitespace() {
        for (RecoveryStage stage : RecoveryStage.values()) {
            assertParseSuccess(parser, stage.toString(), new ListStageCommand(stage));
            assertParseSuccess(parser, " \t" + stage.toString().toUpperCase(Locale.ROOT) + " \n",
                    new ListStageCommand(stage));
        }
    }

    @Test
    public void parse_invalidOrExtraArgument_explainsAcceptedStagesAndSyntax() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListStageCommand.MESSAGE_USAGE);
        for (String args : List.of("", " \t", "unknown", "new new", "new\tsettled", "new\nsettled", "1",
                "--value new", "new --all", "broken_promise", "broken promise", "new extra", "新")) {
            assertParseFailure(parser, args, expected);
        }
    }

    @Test
    public void parse_null_rejectsProgrammingError() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
