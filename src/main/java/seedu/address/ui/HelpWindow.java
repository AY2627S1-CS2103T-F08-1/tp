package seedu.address.ui;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL =
            "https://ay2627s1-cs2103t-f08-1.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide: " + USERGUIDE_URL;

    public static final String COMMAND_REFERENCE = """
            COMMAND REFERENCE

            Add a debtor
            add n/NAME p/PHONE e/EMAIL a/ADDRESS [o/AMOUNT] [t/TAG]...
            Example: add n/Olaf Go p/91234567 e/olaf@example.com a/Antarctica o/S$1250.00

            List all debtors
            list

            Find debtors by name or full phone number
            find KEYWORD [MORE_KEYWORDS]...
            Example: find Alice 91234567

            Sort the displayed worklist
            sort --by id|name|amount [--order asc|desc]
            Example: sort --by amount --order desc

            Show a debtor profile by persistent ID
            show --id DEBTOR_ID
            Example: show --id 3

            Add an interaction note by persistent ID
            note --id DEBTOR_ID [--outcome OUTCOME] --text NOTE_TEXT
            Example: note --id 3 --outcome reached --text Called debtor.

            Set contact preferences by persistent ID
            contact --id DEBTOR_ID [--method METHOD | --clear-method] [--hours HH:mm-HH:mm | --clear-hours]
            Example: contact --id 3 --method sms --hours 09:00-17:30

            Edit a debtor by displayed index
            edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/AMOUNT] [t/TAG]...
            Example: edit 2 o/S$500.00

            Delete a debtor by displayed index
            delete INDEX
            Example: delete 2

            Clear all debtors
            clear

            Undo or redo the most recent saved change in this session
            undo
            redo

            Open this command reference
            help

            Exit OSPS
            exit
            """;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    @FXML
    private Label commandReference;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        helpMessage.setText(HELP_MESSAGE);
        commandReference.setText(COMMAND_REFERENCE);
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
                event.consume();
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
