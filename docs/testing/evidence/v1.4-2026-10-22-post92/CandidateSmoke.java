import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.model.person.Person;

/** External smoke driver: all production classes are loaded from the unmodified packaged JAR. */
public class CandidateSmoke {
    static class App extends MainApp {
        List<Person> people() { return List.copyOf(model.getAddressBook().getPersonList()); }
        List<Person> shown() { return List.copyOf(model.getFilteredPersonList()); }
        int nextId() { return model.getAddressBook().getNextDebtorId(); }
    }
    static App app;
    static Stage stage;
    static int checks;
    static final List<String> report = new ArrayList<>();
    static String mode;
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
        report.add("PASS " + message);
    }
    static String command(String text, boolean rejected) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        long start = System.nanoTime();
        input.setText(text);
        input.fireEvent(new ActionEvent());
        long ms = (System.nanoTime() - start) / 1000000;
        String feedback = ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
        check(input.getStyleClass().contains("error") == rejected, "command "
                + (text.length() > 200 ? "delete <" + (text.length() - 7) + " digits>" : text)
                + " rejected=" + rejected + " elapsedMs=" + ms);
        report.add("FEEDBACK " + feedback);
        if (text.length() > 100000) check(ms < 2000, "million-digit UI command completes within 2 seconds");
        return feedback;
    }
    static void shot(String name) throws Exception {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        WritableImage image = stage.getScene().snapshot(null);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++)
            output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        ImageIO.write(output, "png", Path.of(name + ".png").toFile());
    }
    static void unchanged(List<Person> expected) {
        List<Person> actual = app.people();
        check(actual.size() == expected.size(), "survivor count unchanged");
        for (int i = 0; i < expected.size(); i++) {
            check(expected.get(i).getDebtorId() == actual.get(i).getDebtorId()
                    && expected.get(i).equals(actual.get(i)), "surviving ID/fields/amount/notes "
                    + expected.get(i).getDebtorId() + " unchanged");
        }
    }
    static Person byName(String name) {
        return app.people().stream().filter(p -> p.getName().fullName.equals(name)).findFirst().orElseThrow();
    }
    static void run() throws Exception {
        command("list", false);
        if (mode.equals("fixture")) {
            command("add n/Release QA Cedar p/81110001 e/release.qa.cedar@example.com a/1 Test Lane o/125.00", false);
            command("add n/Release QA Maple p/81110002 e/release.qa.maple@example.com a/2 Test Lane o/900.25", false);
            command("add n/Release QA Birch p/81110003 e/release.qa.birch@example.com a/3 Test Lane o/0", false);
            command("note --id " + byName("Release QA Cedar").getDebtorId() + " --text v1.3 fixture Cedar note", false);
            command("note --id 1 --text v1.3 control note to preserve", false);
            command("show --id 1", false);
            shot("v13-fixture-control");
        } else if (mode.equals("clean")) {
            check(app.people().size() > 0, "first-run sample data loaded");
            shot("clean-folder-startup");
        } else if (mode.equals("restart")) {
            check(app.people().stream().noneMatch(p -> p.getName().fullName.equals("Release Smoke Target")),
                    "deleted target absent in fresh JVM");
            command("show --id 1", false);
            check(app.people().get(0).getInteractionNotes().stream()
                    .anyMatch(n -> n.getText().equals("v1.3 control note to preserve")), "control note survives restart");
            command("show --id " + (app.nextId() - 1), true);
            shot("workflow-after-restart");
        } else if (mode.equals("workflow")) {
            List<Person> baseline = app.people();
            command("show --id 1", false);
            check(baseline.get(0).getInteractionNotes().size() > 0, "v1.3 notes loaded");
            command("add n/Release Smoke Target p/81110004 e/release.smoke.target@example.com a/4 Test Lane o/1250.00", false);
            Person target = byName("Release Smoke Target");
            int id = target.getDebtorId();
            command("list", false);
            command("find Target", false);
            check(app.shown().size() == 1 && app.shown().get(0).getDebtorId() == id,
                    "filtered displayed index differs from persistent ID");
            command("show --id " + id, false);
            command("note --id " + id + " --text Candidate smoke note before deletion", false);
            command("edit 1 a/4 Updated Test Lane", false);
            target = byName("Release Smoke Target");
            check(target.getDebtorId() == id && target.getInteractionNotes().size() == 1
                    && target.getOutstandingAmount().getValue().toPlainString().equals("1250.00"),
                    "edit preserves ID, amount, and note");
            command("show --id " + id, false);
            command("find Target", false);
            command("show --id " + id, false);
            shot("workflow-profile-before-delete");
            String result = command("delete 1", false);
            check(result.contains("Release Smoke Target") && result.contains("4 Updated Test Lane"),
                    "delete success identifies edited target");
            check(app.shown().isEmpty(), "filter remains active after deletion");
            shot("workflow-filtered-delete");
            unchanged(baseline);
            command("show --id " + id, true);
            command("list", false);
        } else if (mode.equals("invalid")) {
            List<Person> baseline = app.people();
            for (String text : List.of("delete", "delete abc", "delete -1", "delete 0", "delete 0000",
                    "delete 1.5", "delete " + (baseline.size() + 1), "delete " + "9".repeat(101),
                    "delete " + "9".repeat(1000000))) {
                command(text, true);
                unchanged(baseline);
            }
            // Clear the rejected million-digit input before capturing a readable error.
            command("delete 0", true);
            shot("invalid-delete");
            command("find NoSuchReleaseSmokeDebtor", false);
            command("delete 1", true);
            command("delete " + "9".repeat(1000000), true);
            unchanged(baseline);
            command("delete 1", true);
            shot("empty-filter-delete");
        } else {
            if (mode.equals("only")) {
                command("clear", false);
                command("add n/Only Debtor p/82220001 e/only@example.com a/Only Lane o/75.50", false);
            }
            List<Person> before = app.people();
            int index = mode.equals("last") ? before.size() : mode.equals("middle") ? (before.size() + 1) / 2 : 1;
            Person target = before.get(index - 1);
            command("show --id " + target.getDebtorId(), false);
            String feedback = command("delete " + (mode.equals("first") ? "0001" : index), false);
            check(feedback.contains(target.getName().fullName), "success names intended " + mode + " debtor");
            List<Person> survivors = new ArrayList<>(before);
            survivors.remove(index - 1);
            unchanged(survivors);
            command("show --id " + target.getDebtorId(), true);
            shot(mode + "-delete");
        }
        report.add("PASS TOTAL " + checks);
    }
    public static void main(String[] args) throws Exception {
        mode = args[0];
        CountDownLatch done = new CountDownLatch(1);
        Platform.startup(() -> {});
        Platform.runLater(() -> {
            try {
                app = new App();
                app.init();
                stage = new Stage();
                app.start(stage);
                check(stage.getTitle().equals("OSPS v1.4"), "loaded Stage title is OSPS v1.4");
                check(MainApp.VERSION.equals("v1.4"), "packaged MainApp version is v1.4");
                stage.setWidth(1100);
                stage.setHeight(800);
                run();
                app.stop();
                stage.close();
            } catch (Throwable e) {
                report.add("FAIL " + e);
                e.printStackTrace();
            } finally {
                try { Files.write(Path.of("result-" + mode + ".txt"), report); }
                catch (Exception e) { e.printStackTrace(); }
                done.countDown();
                Platform.exit();
            }
        });
        done.await();
        if (report.stream().anyMatch(s -> s.startsWith("FAIL"))) System.exit(1);
    }
}
