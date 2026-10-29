package seedu.address.testing;

import seedu.address.MainApp;

/**
 * Runs a released JAR's startup and command/storage paths without opening the JavaFX window.
 * Run each phase in a separate Java process to check loading from the saved file.
 */
public class ReleaseStartupProbe extends MainApp {

    /**
     * Initializes the released application and executes the supplied commands in order.
     *
     * @param args Commands to execute through the released application's logic.
     * @throws Exception If startup or a command fails.
     */
    public static void main(String[] args) throws Exception {
        ReleaseStartupProbe app = new ReleaseStartupProbe();
        app.init();
        System.out.println("RELEASE DATA PATH: " + app.storage.getAddressBookFilePath().toAbsolutePath());
        for (String command : args) {
            System.out.println("> " + command);
            System.out.println(app.logic.execute(command).getFeedbackToUser());
        }
    }
}
