package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PromiseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class PromiseCommandParserTest {

    private final PromiseCommandParser parser = new PromiseCommandParser();

    @Test
    void parse_validSetAndClear_returnsCommands() throws Exception {
        assertEquals(new PromiseCommand(7, LocalDate.of(2026, 11, 3)),
                parser.parse(" --id 7 --date 2026-11-03"));
        assertEquals(new PromiseCommand(7, Optional.empty()), parser.parse(" --clear --id 7"));
    }

    @Test
    void parse_strictDateBoundaries_acceptsOnlyRealFourDigitPositiveYears() throws Exception {
        assertEquals(new PromiseCommand(7, LocalDate.of(1, 1, 1)),
                parser.parse(" --id 7 --date 0001-01-01"));
        assertEquals(new PromiseCommand(7, LocalDate.of(9999, 12, 31)),
                parser.parse(" --id 7 --date 9999-12-31"));
        assertInvalidDate("0000-01-01");
        assertInvalidDate("2026-2-03");
        assertInvalidDate("2026-02-30");
        assertInvalidDate("+10000-01-01");
    }

    @Test
    void parse_invalidIdOptionsAndActions_rejectsCommand() {
        assertThrows(ParseException.class, PromiseCommandParser.MESSAGE_INVALID_ID, () ->
                parser.parse(" --id 0 --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --id 8 --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date 2026-11-03 --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --clear --clear"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --done"));
    }

    @Test
    void parse_duplicateDanglingOrMissingOptions_rejectsCommand() {
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date 2026-11-03 --date 2026-11-04"));
        assertThrows(ParseException.class, () -> parser.parse(" --id"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 --date"));
        assertThrows(ParseException.class, () -> parser.parse(" --date 2026-11-03"));
        assertThrows(ParseException.class, () -> parser.parse(" "));
    }

    @Test
    void parse_unknownOptionOrStrayToken_rejectsCommand() {
        String unknownOptionMessage = String.format(PromiseCommandParser.MESSAGE_UNKNOWN_OPTION, "--nope");
        assertThrows(ParseException.class, unknownOptionMessage, () -> parser.parse(" --id 7 --nope"));
        assertThrows(ParseException.class, () -> parser.parse(" --id 7 stray"));
    }

    @Test
    void parse_idBeyondIntegerRange_rejectsCommand() {
        assertThrows(ParseException.class, PromiseCommandParser.MESSAGE_INVALID_ID, () ->
                parser.parse(" --id 99999999999 --clear"));
    }

    private void assertInvalidDate(String date) {
        assertThrows(ParseException.class, PromiseCommandParser.MESSAGE_INVALID_DATE, () ->
                parser.parse(" --id 7 --date " + date));
    }
}
