package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBTOR_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE_VALUE;

import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.RecoveryStage;

/** Parses a stage command with one persistent debtor ID and one stage value. */
public class StageCommandParser implements Parser<StageCommand> {
    @Override
    public StageCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_DEBTOR_ID, PREFIX_STAGE_VALUE);
        if (!values.getPreamble().isEmpty() || values.getValue(PREFIX_DEBTOR_ID).isEmpty()
                || values.getValue(PREFIX_STAGE_VALUE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }
        values.verifyNoDuplicatePrefixesFor(PREFIX_DEBTOR_ID, PREFIX_STAGE_VALUE);
        int debtorId = ParserUtil.parseDebtorId(values.getValue(PREFIX_DEBTOR_ID).get());
        try {
            RecoveryStage stage = RecoveryStage.parse(values.getValue(PREFIX_STAGE_VALUE).get());
            return new StageCommand(debtorId, stage);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(RecoveryStage.MESSAGE_CONSTRAINTS);
        }
    }
}
