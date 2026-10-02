package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void startJavaFxToolkit() {
        Platform.startup(() -> { });
    }

    @AfterAll
    public static void stopJavaFxToolkit() {
        Platform.exit();
    }

    @Test
    public void constructor_displaysRemark() throws Exception {
        String remark = "Likes baseball";
        Person person = new PersonBuilder().withRemark(remark).build();
        FutureTask<String> task = new FutureTask<>(() -> {
            PersonCard card = new PersonCard(person, 1);
            Label remarkLabel = (Label) card.getRoot().lookup("#remark");
            assertNotNull(remarkLabel);
            return remarkLabel.getText();
        });
        Platform.runLater(task);

        assertEquals(remark, task.get(10, TimeUnit.SECONDS));
    }
}
