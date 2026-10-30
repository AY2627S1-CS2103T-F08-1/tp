package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.ShowCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactOutcome;

/** Characterises shared ID boundaries through real command dispatch, including note-option interactions. */
class DebtorIdParserContractTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    void parse_validIdBoundaries_preservesCommandTargets() throws Exception {
        for (String input : List.of("1", "0007", "2147483647", "\uFF11")) {
            int id = Integer.parseInt(input);
            assertEquals(new ShowCommand(id), parser.parseCommand("show --id " + input));
            assertEquals(new NoteCommand(id, "Confirmed."),
                    parser.parseCommand("note --id " + input + " --text Confirmed."));
            Command stageCommand = parser.parseCommand("stage --id " + input + " --value new");
            CommandException missingDebtor = assertThrows(CommandException.class, () ->
                    stageCommand.execute(new ModelManager()));
            assertEquals(String.format(Messages.MESSAGE_DEBTOR_NOT_FOUND, id), missingDebtor.getMessage());
            assertEquals(new FollowUpCommand(id, LocalDate.of(2026, 11, 3)),
                    parser.parseCommand("followup --id " + input + " --date 2026-11-03"));
        }
    }

    @Test
    void parse_invalidIds_reportsBoundedIdErrorForEveryCommand() {
        for (String id : List.of("0", "-1", "+1", "1.5", "2147483648", "9".repeat(100_000))) {
            for (String command : List.of("show --id " + id,
                    "note --id " + id + " --text Confirmed.",
                    "stage --id " + id + " --value new",
                    "followup --id " + id + " --date 2026-11-03")) {
                ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
                assertEquals(ParserUtil.MESSAGE_INVALID_DEBTOR_ID, error.getMessage());
            }
        }
    }

    @Test
    void parse_outcomeBeforeTextAndIdAfterText_preservesLiteralOptions() throws Exception {
        assertEquals(new NoteCommand(7, "Use --portal; keep  two spaces. --outcome reached",
                        ContactOutcome.CALLBACK_REQUESTED),
                parser.parseCommand("note --outcome callback-requested --text "
                        + "Use --portal; keep  two spaces. --outcome reached --id 0007"));
    }
}
