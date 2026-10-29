package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE_TEXT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OUTCOME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.ContactOutcome;
import seedu.address.model.person.InteractionNote;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validArgs_returnsNoteCommand() {
        assertParseSuccess(parser, " --id 2 --text Called debtor, promised payment on Friday.",
                new NoteCommand(2, "Called debtor, promised payment on Friday."));
    }

    @Test
    public void parse_textWithPunctuation_returnsNoteCommand() {
        assertParseSuccess(parser, " --id 4 --text Left voicemail; callback at 5pm?",
                new NoteCommand(4, "Left voicemail; callback at 5pm?"));
    }

    @Test
    public void parse_validOutcome_returnsNoteCommandWithOutcome() {
        assertParseSuccess(parser, " --id 4 --outcome reached --text Spoke to debtor.",
                new NoteCommand(4, "Spoke to debtor.", ContactOutcome.REACHED));
        assertParseSuccess(parser, " --outcome CALLBACK-REQUESTED --id 4 --text Call again tomorrow.",
                new NoteCommand(4, "Call again tomorrow.", ContactOutcome.CALLBACK_REQUESTED));
        assertParseSuccess(parser, " --id 4 --outcome Unreachable --text No answer.",
                new NoteCommand(4, "No answer.", ContactOutcome.UNREACHABLE));
        assertParseSuccess(parser, " --id 4 --outcome payment-promised --text Payment due Friday.",
                new NoteCommand(4, "Payment due Friday.", ContactOutcome.PAYMENT_PROMISED));
    }

    @Test
    public void parse_textWithWhitespaceAndPunctuation_preservesInternalWhitespace() {
        assertParseSuccess(parser, " --id 4 --text  Called debtor,  left voicemail; follow-up?  ",
                new NoteCommand(4, "Called debtor,  left voicemail; follow-up?"));
    }

    @Test
    public void parse_longRealisticText_returnsNoteCommand() {
        String noteText = "Discussed instalment options; debtor requested a written summary. "
                + "Follow up after the next payday with the agreed figures and payment reference. ".repeat(8);

        assertParseSuccess(parser, " --id 4 --text " + noteText, new NoteCommand(4, noteText));
    }

    @Test
    public void parse_textBeyondTypicalLength_returnsNoteCommand() {
        String noteText = "Discussed payment options; follow up by email. ".repeat(256);

        assertParseSuccess(parser, " --id 4 --text " + noteText, new NoteCommand(4, noteText));
    }

    @Test
    public void parse_textBeforeId_returnsNoteCommand() {
        assertParseSuccess(parser, " --text Called debtor, promised payment on Friday. --id 2",
                new NoteCommand(2, "Called debtor, promised payment on Friday."));
    }

    @Test
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, " --text Called debtor.",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingText_throwsParseException() {
        assertParseFailure(parser, " --id 2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_malformedLongOptionName_throwsParseException() {
        assertParseFailure(parser, " --id 2 --textbook Called debtor.",
                String.format(NoteCommandParser.MESSAGE_UNKNOWN_OPTION, "--textbook"));
    }

    @Test
    public void parse_unknownOption_throwsParseException() {
        assertParseFailure(parser, " --unknown value --id 2 --text Called debtor.",
                String.format(NoteCommandParser.MESSAGE_UNKNOWN_OPTION, "--unknown"));
        assertParseFailure(parser, " --id 2 --unknown value --text Called debtor.",
                String.format(NoteCommandParser.MESSAGE_UNKNOWN_OPTION, "--unknown"));
        assertParseFailure(parser, " --text Called debtor. --id 2 --unknown value",
                String.format(NoteCommandParser.MESSAGE_UNKNOWN_OPTION, "--unknown"));
    }

    @Test
    public void parse_optionLikeWordsInText_preservesNoteText() {
        assertParseSuccess(parser, " --id 2 --text Asked debtor to use the --identifier portal.",
                new NoteCommand(2, "Asked debtor to use the --identifier portal."));
        assertParseSuccess(parser, " --id 2 --text Called debtor. --unknown value",
                new NoteCommand(2, "Called debtor. --unknown value"));
        assertParseSuccess(parser, " --text Use --identifier portal. --id 2",
                new NoteCommand(2, "Use --identifier portal."));
        assertParseSuccess(parser, " --id 2 --text Discussed --outcome reached with supervisor.",
                new NoteCommand(2, "Discussed --outcome reached with supervisor."));
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        assertParseFailure(parser, " --id zero --text Called debtor.", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 0 --text Called debtor.", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id -1 --text Called debtor.", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id +1 --text Called debtor.", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 2147483648 --text Called debtor.",
                ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
    }

    @Test
    public void parse_blankText_throwsParseException() {
        assertParseFailure(parser, " --id 1 --text    ", InteractionNote.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --id 1 --text", InteractionNote.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateParameters_throwsParseException() {
        assertParseFailure(parser, " --id 1 --id 2 --text Called debtor.",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
        assertParseFailure(parser, " --id 1 --text Called debtor. --text Promised payment.",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE_TEXT));
        assertParseFailure(parser, " --id 1 --outcome reached --outcome unreachable --text Called debtor.",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_OUTCOME));
        assertParseFailure(parser, " --id 1 --text Called debtor. --id 2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
    }

    @Test
    public void parse_invalidOutcome_throwsParseException() {
        assertParseFailure(parser, " --id 1 --outcome answered --text Called debtor.",
                ContactOutcome.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --id 1 --outcome --text Called debtor.",
                ContactOutcome.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unexpectedPreamble_throwsParseException() {
        assertParseFailure(parser, " unexpected --id 1 --text Called debtor.",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }
}
