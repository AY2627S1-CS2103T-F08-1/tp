package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.testutil.PersonBuilder;

/** Checks native Windows glyph layout; portable label and binding checks also run in Linux UiRuntimeTest. */
@EnabledOnOs(OS.WINDOWS)
public class PackagedWorklistRenderingTest {
    @TempDir
    private Path folder;

    @Test
    public void packagedHelpAndCards_showCompleteTextAfterLayout() throws Exception {
        Path jar = Files.copy(Path.of("build", "libs", "osps.jar"), folder.resolve("osps.jar"));
        Path tests = Path.of(RenderingProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path java = Path.of(System.getProperty("java.home"), "bin", "java.exe");
        Path log = folder.resolve("worklist-layout.log");
        Process probe = new ProcessBuilder(java.toString(), "-cp", tests + File.pathSeparator + jar,
                RenderingProbe.class.getName()).directory(folder.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            assertTrue(probe.waitFor(60, TimeUnit.SECONDS), "Native worklist layout probe timed out");
            assertEquals(0, probe.exitValue(), Files.readString(log));
        } finally {
            if (probe.isAlive()) {
                probe.destroyForcibly();
                probe.waitFor(10, TimeUnit.SECONDS);
            }
        }
    }

    /** Isolates the native JavaFX lifecycle from the other test classes. */
    public static final class RenderingProbe {
        private RenderingProbe() {
        }

        /** Runs actual packaged Help and person-card rendering checks. */
        public static void main(String[] args) throws Exception {
            Platform.startup(() -> Platform.setImplicitExit(false));
            try {
                FutureTask<Void> checks = new FutureTask<>(() -> {
                    checkHelp();
                    checkCards();
                    return null;
                });
                Platform.runLater(checks);
                checks.get(45, TimeUnit.SECONDS);
            } finally {
                Platform.exit();
            }
        }

        private static void checkHelp() {
            HelpWindow window = new HelpWindow();
            window.getRoot().setOpacity(0);
            try {
                window.show();
                Scene scene = window.getRoot().getScene();
                for (double width : new double[]{740, 1000}) {
                    window.getRoot().setWidth(width);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                    for (int page = 0; page < 30; page++) {
                        key(scene, KeyCode.PAGE_DOWN);
                    }
                    ScrollPane scroll = (ScrollPane) scene.lookup("#commandReferenceScroll");
                    require(scroll.getVvalue() == scroll.getVmax(), "Page Down must reach Help bottom");
                    Label reference = (Label) scene.lookup("#commandReference");
                    Text glyphs = (Text) reference.lookup(".text");
                    String tail = glyphs.getText().stripTrailing().lines().reduce("", (previous, last) -> last);
                    require(tail.strip().equals("exit"), "Help final command is clipped: " + tail);
                    require(reference.getText().contains("| preview delete INDEX"), "Ambiguous preview syntax");
                    key(scene, KeyCode.PAGE_UP);
                    require(scroll.getVvalue() < scroll.getVmax(), "Page Up must scroll back");
                }
                key(scene, KeyCode.ESCAPE);
                require(!window.isShowing(), "Escape must close Help");
                window.show();
                require(window.isShowing(), "Help must reopen after Escape");
            } finally {
                window.hide();
            }
        }

        private static void checkCards() {
            String[][] examples = {{"0", "Outstanding: S$0.00"}, {"10.50", "Outstanding: S$10.50"},
                {"1,250.00", "Outstanding: S$1,250.00"}};
            for (String[] example : examples) {
                PersonCard card = new PersonCard(new PersonBuilder().withOutstandingAmount(example[0]).build(), 1);
                SimpleDoubleProperty width = new SimpleDoubleProperty(413);
                card.bindContentWidth(width);
                Stage stage = new Stage();
                stage.setOpacity(0);
                Scene scene = new Scene(new StackPane(card.getRoot()), 413, 300);
                stage.setScene(scene);
                try {
                    stage.show();
                    for (double viewportWidth : new double[]{413, 300}) {
                        width.set(viewportWidth);
                        stage.setWidth(viewportWidth);
                        scene.getRoot().applyCss();
                        scene.getRoot().layout();
                        Label amount = (Label) card.getRoot().lookup("#outstandingAmount");
                        require(amount.isVisible(), "Amount label must be visible");
                        require(example[1].equals(amount.getText()), "Incorrect amount label");
                        Text glyphs = (Text) amount.lookup(".text");
                        require(example[1].equals(glyphs.getText()), "Amount text is clipped");
                    }
                } finally {
                    stage.hide();
                }
            }
        }

        private static void key(Scene scene, KeyCode code) {
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                    false, false, false, false));
        }

        private static void require(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }
    }
}
