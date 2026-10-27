package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import seedu.address.testutil.PersonBuilder;

/**
 * Runs JavaFX-backed UI regression checks on the Linux CI display.
 */
@EnabledOnOs(OS.LINUX)
public class UiRuntimeTest {

    private static final long TEST_TIMEOUT_SECONDS = 5;

    @BeforeAll
    public static void startJavaFxToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @AfterAll
    public static void stopJavaFxToolkit() {
        Platform.exit();
    }

    @Test
    public void resultDisplay_feedbackUpdatesTextAndVisibleRows() throws InterruptedException {
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
    public void personCard_contentWidthTracksListWidth() throws InterruptedException {
        runOnJavaFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            SimpleDoubleProperty listWidth = new SimpleDoubleProperty(413.0);
            card.bindContentWidth(listWidth);
            Label address = (Label) card.getRoot().lookup("#address");

            assertNotNull(address);
            assertEquals(363.0, address.getMaxWidth());
            listWidth.set(300.0);
            assertEquals(250.0, address.getMaxWidth());
        });
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
