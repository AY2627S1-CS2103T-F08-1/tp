package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import seedu.address.MainApp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final Path PERSON_CARD_SOURCE =
            Path.of("src/main/java/seedu/address/ui/PersonCard.java");
    private static final Pattern AMOUNT_BINDING = Pattern.compile(
            "outstandingAmount\\.setText\\(\\\"([^\\\"]*)\\\" \\+ person\\.getOutstandingAmount\\(\\)\\);");

    @Test
    public void cardText_zeroAmount_matchesExpected() throws IOException {
        Person debtor = new PersonBuilder().withOutstandingAmount("0").build();

        assertEquals("Outstanding: S$0.00", formatCardText(debtor));
    }

    @Test
    public void cardText_nonzeroAmount_matchesExpected() throws IOException {
        Person debtor = new PersonBuilder().withOutstandingAmount("10.50").build();

        assertEquals("Outstanding: S$10.50", formatCardText(debtor));
    }

    @Test
    public void cardText_groupedAmount_matchesExpected() throws IOException {
        Person debtor = new PersonBuilder().withOutstandingAmount("1,250.00").build();

        assertEquals("Outstanding: S$1,250.00", formatCardText(debtor));
    }

    @Test
    public void fxml_outstandingLabel_betweenPhoneAndAddress() throws IOException {
        String fxml = readPersonListCardFxml();
        int phoneIndex = fxml.indexOf("fx:id=\"phone\"");
        int outstandingAmountIndex = fxml.indexOf("fx:id=\"outstandingAmount\"");
        int addressIndex = fxml.indexOf("fx:id=\"address\"");

        assertTrue(phoneIndex >= 0);
        assertTrue(outstandingAmountIndex > phoneIndex);
        assertTrue(addressIndex > outstandingAmountIndex);
    }

    private String readPersonListCardFxml() throws IOException {
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/view/PersonListCard.fxml")) {
            assertNotNull(inputStream);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String formatCardText(Person person) throws IOException {
        Matcher matcher = AMOUNT_BINDING.matcher(Files.readString(PERSON_CARD_SOURCE));
        assertTrue(matcher.find());
        return matcher.group(1) + person.getOutstandingAmount();
    }
}
