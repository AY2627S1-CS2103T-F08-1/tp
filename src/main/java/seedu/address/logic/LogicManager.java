package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_PENDING_SAVE_BLOCKS_HISTORY_NAVIGATION = "Cannot undo or redo while a previous "
            + "change is waiting to be saved. Run a successful change or exit first.";
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private boolean hasUnsavedAddressBookChanges;
    private boolean hasUncommittedHistoryState;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + CommandLogFormatter.getCommandWord(commandText) + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (hasUncommittedHistoryState && command.isHistoryNavigation()) {
            throw new CommandException(MESSAGE_PENDING_SAVE_BLOCKS_HISTORY_NAVIGATION);
        }
        commandResult = command.execute(model);

        boolean shouldSaveAddressBook = command.shouldSaveAddressBook();
        if (shouldSaveAddressBook) {
            hasUnsavedAddressBookChanges = true;
        }

        if (hasUnsavedAddressBookChanges && (shouldSaveAddressBook || commandResult.isExit())) {
            try {
                storage.saveAddressBook(model.getAddressBook());
                hasUnsavedAddressBookChanges = false;
                if (hasUncommittedHistoryState) {
                    model.commitAddressBook();
                    hasUncommittedHistoryState = false;
                }
                if (shouldSaveAddressBook && command.shouldCommitAddressBook()) {
                    model.commitAddressBook();
                }
            } catch (AccessDeniedException e) {
                rememberFailedSaveState(command);
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
            } catch (IOException ioe) {
                rememberFailedSaveState(command);
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
        }

        return commandResult;
    }

    private void rememberFailedSaveState(Command command) {
        if (command.shouldSaveAddressBook() && command.shouldCommitAddressBook()) {
            hasUncommittedHistoryState = true;
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
