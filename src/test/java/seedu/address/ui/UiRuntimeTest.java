package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Runs JavaFX-backed UI regression checks on the Linux CI display.
 */
@EnabledOnOs(OS.LINUX)
public class UiRuntimeTest {

    // Shared Linux runners can spend several seconds loading JavaFX fonts and CSS on a cold cache.
    private static final long TEST_TIMEOUT_SECONDS = 30;
    private HelpWindow helpWindow;

    private static class RecordingHelpWindow extends HelpWindow {
        private boolean hideCalled;

        @Override
        public void hide() {
            hideCalled = true;
            super.hide();
        }
    }

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

    @AfterEach
    public void closeHelpWindow() throws InterruptedException {
        if (helpWindow != null) {
            runOnJavaFxThread(helpWindow::hide);
            helpWindow = null;
        }
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

    @Test
    public void personCard_longTagWrapsWithinViewport() throws InterruptedException {
        runOnJavaFxThread(() -> {
            String longTag = "A".repeat(120);
            Person person = new PersonBuilder().withTags(longTag).build();
            PersonCard card = new PersonCard(person, 1);
            card.bindContentWidth(new SimpleDoubleProperty(413));
            Region root = card.getRoot();
            FlowPane tags = (FlowPane) root.lookup("#tags");
            Label tag = (Label) tags.getChildren().getFirst();
            // The app-owned wrapping contract is the label flag plus the viewport width binding.
            // Running a native CSS/layout pulse here made headless Linux CI deadlock in JavaFX itself.
            assertEquals(longTag, tag.getText());
            assertTrue(tag.isWrapText());
            assertEquals(0.0, tag.getMinWidth());
            assertEquals(363.0, tag.getMaxWidth());
        });
    }

    @Test
    public void personListPanel_cellWidthTracksListViewport() throws InterruptedException {
        runOnJavaFxThread(() -> {
            Person person = new PersonBuilder().build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(person));
            @SuppressWarnings("unchecked")
            ListView<Person> listView = (ListView<Person>) panel.getRoot().lookup("#personListView");
            listView.resize(413.0, 600.0);

            PersonListPanel.PersonListViewCell cell =
                    (PersonListPanel.PersonListViewCell) listView.getCellFactory().call(listView);
            cell.updateIndex(0);
            cell.updateItem(person, false);
            Region card = (Region) cell.getGraphic();
            Label address = (Label) card.lookup("#address");

            assertNotNull(card);
            assertNotNull(address);
            assertEquals(393.0, card.getPrefWidth());
            assertEquals(363.0, address.getMaxWidth());

            listView.resize(300.0, 600.0);
            assertEquals(280.0, card.getPrefWidth());
            assertEquals(250.0, address.getMaxWidth());
        });
    }

    @Test
    public void helpWindow_commandReferenceIsScrollableAndUsable() throws InterruptedException {
        runOnJavaFxThread(() -> {
            RecordingHelpWindow recordingHelpWindow = new RecordingHelpWindow();
            helpWindow = recordingHelpWindow;
            ScrollPane reference = (ScrollPane) helpWindow.getRoot().getScene().lookup(".scroll-pane");

            assertNotNull(reference);
            assertTrue(reference.isFitToWidth());
            assertTrue(reference.isFocusTraversable());
            helpWindow.getRoot().getScene().getRoot().applyCss();
            helpWindow.getRoot().getScene().getRoot().layout();
            assertTrue(reference.getVmax() > reference.getVmin());
            reference.setVvalue(reference.getVmin());
            KeyEvent enter = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER,
                    false, false, false, false);
            Event.fireEvent(reference, enter);
            assertFalse(recordingHelpWindow.hideCalled);
            KeyEvent pageDown = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PAGE_DOWN,
                    false, false, false, false);
            double viewportHeight = reference.getViewportBounds().getHeight();
            double contentHeight = reference.getContent().getBoundsInLocal().getHeight();
            double expectedPage = Math.clamp(viewportHeight / (contentHeight - viewportHeight),
                    reference.getVmin(), reference.getVmax());
            Event.fireEvent(reference, pageDown);
            assertEquals(expectedPage, reference.getVvalue(), 0.001);
            assertFalse(recordingHelpWindow.hideCalled);
            KeyEvent pageUp = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PAGE_UP,
                    false, false, false, false);
            Event.fireEvent(reference, pageUp);
            assertEquals(reference.getVmin(), reference.getVvalue());
            assertFalse(recordingHelpWindow.hideCalled);
            KeyEvent escape = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false);
            Event.fireEvent(reference, escape);
            assertTrue(recordingHelpWindow.hideCalled);
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
        if (failure.get() != null) {
            throw new AssertionError("JavaFX action failed", failure.get());
        }
    }
}
