package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigInteger;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Tests delete input validation, displayed-range feedback, and bounded processing of large inputs.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "0", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "-1", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 extra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_veryLargePositiveIndex_returnsDeleteCommand() {
        String veryLargeIndex = "9".repeat(100);
        assertParseSuccess(parser, veryLargeIndex,
                DeleteCommand.fromOneBased(new BigInteger(veryLargeIndex)));
    }

    @Test
    public void parse_millionDigitIndex_reportsRangePromptlyWithoutMutation() {
        String input = "9".repeat(1_000_000);
        ModelManager model = new ModelManager();
        assertTimeout(Duration.ofSeconds(2), () -> {
            DeleteCommand command = parser.parse(input);
            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
            assertTrue(error.getMessage().contains("displayed list is empty"));
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
        });
    }

    @Test
    public void parse_leadingZeros_normalizesWithoutDigitLimit() {
        assertTimeout(Duration.ofSeconds(2), () -> {
            assertParseSuccess(parser, "0".repeat(1_000_000) + "1", new DeleteCommand(INDEX_FIRST_PERSON));
            assertParseFailure(parser, "0".repeat(1_000_000),
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
            assertParseFailure(parser, "0".repeat(1_000_000) + "1x",
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        });
    }

    @Test
    public void parse_integerBoundary_preservesRequestedIndexAndRange() throws Exception {
        for (String input : new String[] {"2147483647", "2147483648", "0002147483648"}) {
            ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            String normalized = input.replaceFirst("^0+", "");
            DeleteCommand command = parser.parse(input);
            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
            assertTrue(error.getMessage().startsWith("Index " + normalized + " is out of range."));
            assertTrue(error.getMessage().contains("valid range is 1 to " + model.getFilteredPersonList().size()));
            assertEquals(getTypicalAddressBook(), model.getAddressBook());
        }
    }

    @Test
    public void parse_millionDigitIndexNonEmptyList_reportsBoundedFeedbackWithoutMutation() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        String input = "9".repeat(1_000_000);
        assertTimeout(Duration.ofSeconds(2), () -> {
            DeleteCommand command = parser.parse(input);
            CommandException error = assertThrows(CommandException.class, () -> command.execute(model));
            assertTrue(error.getMessage().contains("1000000 digits"));
            assertTrue(error.getMessage().contains("valid range is 1 to " + model.getFilteredPersonList().size()));
            assertTrue(error.getMessage().length() < 200);
            assertTrue(command.toString().length() < 200);
            assertEquals(getTypicalAddressBook(), model.getAddressBook());
        });
    }

}
