package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteCommand;
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
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, " --text Called debtor.",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        assertParseFailure(parser, " --id zero --text Called debtor.", NoteCommandParser.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 0 --text Called debtor.", NoteCommandParser.MESSAGE_INVALID_DEBTOR_ID);
    }

    @Test
    public void parse_blankText_throwsParseException() {
        assertParseFailure(parser, " --id 1 --text    ", InteractionNote.MESSAGE_CONSTRAINTS);
    }
}
