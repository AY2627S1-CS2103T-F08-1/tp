package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

/**
 * Tests dynamic row sizing for the result display.
 */
public class ResultDisplayTest {

    private static final long TEST_TIMEOUT_SECONDS = 5;

    @BeforeAll
    public static void setUpJavaFxToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);

        assertTrue(latch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @AfterAll
    public static void tearDownJavaFxToolkit() {
        Platform.exit();
    }

    @Test
    public void setFeedbackToUser_updatesTextAndVisibleRows() throws InterruptedException {
        String feedback = "Debtor profile:\nID: 7\nName: Alex\nPhone: 91234567\nEmail: alex@example.com\n"
                + "Address: 123 Example Street";
        AtomicReference<TextArea> textArea = new AtomicReference<>();

        runOnJavaFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            resultDisplay.setFeedbackToUser(feedback);
            StackPane root = (StackPane) resultDisplay.getRoot();
            textArea.set((TextArea) root.getChildren().getFirst());
        });

        assertEquals(feedback, textArea.get().getText());
        assertEquals(6, textArea.get().getPrefRowCount());
    }

    @Test
    public void getVisibleRowCount_shortFeedback_usesMinimumRows() {
        assertEquals(4, ResultDisplay.getVisibleRowCount("Debtor added."));
    }

    @Test
    public void getVisibleRowCount_multilineFeedback_usesLineCount() {
        String feedback = "Debtor profile:\nID: 7\nName: Alex\nPhone: 91234567\nEmail: alex@example.com\n"
                + "Address: 123 Example Street";

        assertEquals(6, ResultDisplay.getVisibleRowCount(feedback));
    }

    @Test
    public void getVisibleRowCount_longFeedback_usesMaximumRows() {
        String feedback = "Profile detail\n".repeat(20);

        assertEquals(12, ResultDisplay.getVisibleRowCount(feedback));
    }

    private void runOnJavaFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable exception) {
                failure.set(exception);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNull(failure.get());
    }
}
