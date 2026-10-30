package seedu.address.testutil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.model.person.Person;

/** Test-only Java agent that drives the packaged app without altering its classes or successful shutdown path. */
public final class PackagedJarTestAgent {
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final AtomicBoolean FINISHED = new AtomicBoolean();

    private PackagedJarTestAgent() {
    }

    /**
     * Starts a daemon driver while allowing the actual application entry point to launch normally.
     * @param mode {@code save} creates a fixture; {@code reload} verifies it; {@code corrupt} checks data protection.
     */
    public static void premain(String mode) {
        boolean knownMode = "save".equals(mode) || "reload".equals(mode) || "corrupt".equals(mode);
        if (!Files.exists(Path.of("AUDIT_DISPOSABLE")) || !knownMode) {
            throw new IllegalStateException("Refusing to run without a marked test folder and known mode");
        }
        Thread driver = new Thread(() -> awaitApplication(mode), "packaged-jar-test-driver");
        driver.setDaemon(true);
        driver.start();
    }

    private static void awaitApplication(String mode) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(40);
        while (!FINISHED.get() && System.nanoTime() < deadline) {
            try {
                Platform.runLater(() -> runWhenReady(mode));
            } catch (IllegalStateException notStartedYet) {
                // The real entry point has not initialized JavaFX yet.
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                fail(exception);
            }
        }
        if (!FINISHED.get()) {
            fail(new AssertionError("Application startup/commands exceeded 40 seconds"));
        }
    }

    private static void runWhenReady(String mode) {
        if (STARTED.get()) {
            return;
        }
        Stage main = Window.getWindows().stream().filter(Window::isShowing)
                .filter(window -> window instanceof Stage).map(window -> (Stage) window)
                .filter(window -> window.getScene().lookup("#commandTextField") != null)
                .findFirst().orElse(null);
        if (main == null || !STARTED.compareAndSet(false, true)) {
            return;
        }
        try {
            exerciseApplication(main, mode);
            FINISHED.set(true);
            System.out.println("PACKAGED_SMOKE_PASS " + mode);
        } catch (Throwable error) {
            fail(error);
        }
    }

    private static void exerciseApplication(Stage main, String mode) {
        check(main.getTitle().startsWith("OSPS "), "Expected OSPS main window");
        if ("corrupt".equals(mode)) {
            exerciseUnreadableData(main);
        } else {
            exerciseSavedData(main, mode);
        }
        execute(main, "help");
        check(Window.getWindows().stream().filter(Window::isShowing).count() == 2, "Help did not open");
        execute(main, "exit");
        check(Window.getWindows().stream().noneMatch(Window::isShowing), "Exit left an application window open");
    }

    private static void exerciseSavedData(Stage main, String mode) {
        if ("save".equals(mode)) {
            execute(main, "clear");
            execute(main, "add n/Packaged Smoke p/89990000 e/smoke@example.com a/Test Address o/25");
        }
        execute(main, "list");
        ListView<?> list = (ListView<?>) main.getScene().lookup("#personListView");
        check(list != null && list.getItems().size() == 1, "Expected exactly one saved debtor");
        Person saved = (Person) list.getItems().getFirst();
        check(saved.getName().fullName.equals("Packaged Smoke"), "Saved debtor was not restored");
        check(saved.getOutstandingAmount().toString().equals("25.00"), "Saved amount was not restored");
        if ("save".equals(mode)) {
            execute(main, "export --id " + saved.getDebtorId());
        }
    }

    private static void exerciseUnreadableData(Stage main) {
        execute(main, "list");
        ListView<?> list = (ListView<?>) main.getScene().lookup("#personListView");
        check(list != null && list.getItems().isEmpty(), "Unreadable data must not be replaced with sample debtors");
        TextField input = submit(main, "add n/Packaged Smoke p/89990000 e/smoke@example.com a/Test Address o/25");
        check(input.getStyleClass().contains("error"), "Add must fail while unreadable data is protected");
        TextArea result = (TextArea) main.getScene().lookup("#resultDisplay");
        check(result.getText().contains("Preserve this file")
                && result.getText().contains("restart before saving changes"), "Missing actionable recovery guidance");
        check(list.getItems().isEmpty(), "Rejected save must not publish an in-memory debtor");
        execute(main, "clear");
        execute(main, "list");
        execute(main, "summary");
        check(list.getItems().isEmpty(), "Safe read-only/no-op commands changed the protected view");
    }

    private static TextField submit(Stage main, String command) {
        TextField input = (TextField) main.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        return input;
    }

    private static void execute(Stage main, String command) {
        TextField input = submit(main, command);
        check(!input.getStyleClass().contains("error"), "Valid fixture command failed: " + command);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void fail(Throwable error) {
        error.printStackTrace(System.err);
        System.exit(2);
    }
}
