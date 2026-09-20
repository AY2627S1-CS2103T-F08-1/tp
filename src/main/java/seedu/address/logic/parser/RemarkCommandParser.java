package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

public class RemarkCommandParser implements Parser<RemarkCommand> {
    private static final Prefix PREFIX_REMARK = new Prefix("r/");


    public RemarkCommand parse(String args) throws ParseException {
        ArgumentMultimap mpp = ArgumentTokenizer.tokenize(args, PREFIX_REMARK);

        Index index;
        try {
            index = ParserUtil.parseIndex(mpp.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE), pe);
        }

        Remark remark = new Remark(mpp.getValue(PREFIX_REMARK).orElse(""));

        return new RemarkCommand(index, remark);
    }
}
