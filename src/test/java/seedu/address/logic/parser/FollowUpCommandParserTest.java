package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class FollowUpCommandParserTest {

    @Test
    void parse_validSchedule_returnsCommand() throws Exception {
        FollowUpCommand command = new FollowUpCommandParser().parse(" --id 7 --date 2026-11-03");
        assertEquals(new FollowUpCommand(7, LocalDate.of(2026, 11, 3)), command);
    }

    @Test
    void parse_nonStrictDate_rejectsCommand() {
        assertThrows(ParseException.class, () -> new FollowUpCommandParser().parse(" --id 7 --date 2026-2-3"));
        assertThrows(ParseException.class, () -> new FollowUpCommandParser().parse(" --id 7 --date 2026-02-30"));
        assertThrows(ParseException.class, () -> new FollowUpCommandParser().parse(" --id 7 --date 0000-01-01"));
    }

    @Test
    void parse_clearAndDone_returnCommands() throws Exception {
        FollowUpCommandParser parser = new FollowUpCommandParser();
        assertEquals(new FollowUpCommand(7, FollowUpCommand.Operation.CLEAR, java.util.Optional.empty()),
                parser.parse(" --id 7 --clear"));
        assertEquals(new FollowUpCommand(7, FollowUpCommand.Operation.DONE, java.util.Optional.empty()),
                parser.parse(" --done --id 7"));
    }

    @Test
    void parse_missingValuesAndBadIds_rejectCommand() {
        FollowUpCommandParser parser = new FollowUpCommandParser();
        assertThrows(ParseException.class, () -> parser.parse(""));
        assertThrows(ParseException.class, () -> parser.parse(" --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date"));
        assertThrows(ParseException.class, () -> parser.parse(" --id -3 --done"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 99999999999 --done"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date 2026-11-03 --date 2026-11-04"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --clear --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --done --done"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --clear --done"));
        assertThrows(ParseException.class, () -> parser.parse(" 7 --done"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date tomorrow"));
    }

    @Test
    void parse_unknownDuplicateAndConflictingOptions_rejectCommand() {
        FollowUpCommandParser parser = new FollowUpCommandParser();
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date 2026-11-03 --mystery"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --id 8 --date 2026-11-03"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date 2026-11-03 --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 0 --done"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --clear extra"));
    }
}
