package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StageCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.PersonBuilder;

class StageCommandParserTest {
    private final StageCommandParser parser = new StageCommandParser();

    @Test
    void parse_validArgs_updatesRequestedStage() throws Exception {
        ModelManager model = new ModelManager();
        Person debtor = model.addPerson(new PersonBuilder().build());

        parser.parse(" --id " + debtor.getDebtorId() + " --value CONTACTED").execute(model);

        assertEquals(RecoveryStage.CONTACTED,
                model.getAddressBook().getPersonList().getFirst().getRecoveryStage());
    }

    @Test
    void parse_missingOrUnexpectedArguments_rejectsInput() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " --id 1", usage);
        assertParseFailure(parser, " --value contacted", usage);
        assertParseFailure(parser, " unexpected --id 1 --value contacted", usage);
    }

    @Test
    void parse_duplicateInvalidIdAndInvalidStage_rejectsInput() {
        assertParseFailure(parser, " --id 1 --id 2 --value contacted",
                getErrorMessageForDuplicatePrefixes(PREFIX_DEBTOR_ID));
        assertParseFailure(parser, " --id 0 --value contacted", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 2147483648 --value contacted", ParserUtil.MESSAGE_INVALID_DEBTOR_ID);
        assertParseFailure(parser, " --id 1 --value unknown", RecoveryStage.MESSAGE_CONSTRAINTS);
    }
}
