package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_OUTSTANDING_AMOUNT = new Prefix("o/");
    public static final Prefix PREFIX_MIN_AMOUNT = new Prefix("min/");
    public static final Prefix PREFIX_MAX_AMOUNT = new Prefix("max/");
    public static final Prefix PREFIX_DEBTOR_ID = new Prefix("--id");
    public static final Prefix PREFIX_OUTCOME = new Prefix("--outcome");
    public static final Prefix PREFIX_NOTE_TEXT = new Prefix("--text");
    public static final Prefix PREFIX_STAGE_VALUE = new Prefix("--value");
    public static final Prefix PREFIX_SORT_BY = new Prefix("--by");
    public static final Prefix PREFIX_SORT_ORDER = new Prefix("--order");

}
