package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MIN_AMOUNT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListAmountCommand;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.OutstandingAmountRange;

public class ListAmountCommandParserTest {

    private final ListAmountCommandParser parser = new ListAmountCommandParser();

    @Test
    public void parse_lowerBoundOnly_success() {
        assertParseSuccess(parser, " min/S$1,000.50 ", command("1000.50", null));
    }

    @Test
    public void parse_upperBoundOnly_success() {
        assertParseSuccess(parser, " max/$500 ", command(null, "500"));
    }

    @Test
    public void parse_bothBounds_success() {
        assertParseSuccess(parser, " min/0 max/2,000.00 ", command("0", "2000"));
    }

    @Test
    public void parse_noBounds_failure() {
        assertParseFailure(parser, " ", OutstandingAmountRange.MESSAGE_REQUIRES_BOUND
                + "\n" + ListAmountCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_minimumExceedsMaximum_failure() {
        assertParseFailure(parser, " min/501 max/500", OutstandingAmountRange.MESSAGE_INVALID_ORDER);
    }

    @Test
    public void parse_malformedOrEmptyBound_failure() {
        assertParseFailure(parser, " min/-1", OutstandingAmount.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " max/10.001", OutstandingAmount.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " min/", OutstandingAmount.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateBound_failure() {
        assertParseFailure(parser, " min/1 min/2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MIN_AMOUNT));
    }

    @Test
    public void parse_extraPreamble_failure() {
        assertParseFailure(parser, " unexpected min/1",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListAmountCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_trailingArgumentAfterMinimum_failure() {
        assertParseFailure(parser, " min/1 unexpected",
                ListAmountCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
        assertParseFailure(parser, " min/1 unknown/value",
                ListAmountCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
    }

    @Test
    public void parse_trailingArgumentAfterMaximum_failure() {
        assertParseFailure(parser, " max/500 unexpected",
                ListAmountCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
        assertParseFailure(parser, " min/1 max/500 unknown/value",
                ListAmountCommand.MESSAGE_UNEXPECTED_ARGUMENTS);
    }

    private ListAmountCommand command(String minimum, String maximum) {
        Optional<OutstandingAmount> minimumAmount = minimum == null
                ? Optional.empty() : Optional.of(new OutstandingAmount(minimum));
        Optional<OutstandingAmount> maximumAmount = maximum == null
                ? Optional.empty() : Optional.of(new OutstandingAmount(maximum));
        return new ListAmountCommand(new OutstandingAmountRange(minimumAmount, maximumAmount));
    }
}
