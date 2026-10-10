package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import seedu.address.MainApp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @Test
    public void formatOutstandingAmount_zeroAmount_returnsCurrencyText() {
        Person debtor = new PersonBuilder().withOutstandingAmount("0").build();

        assertEquals("Outstanding: S$0.00", PersonCard.formatOutstandingAmount(debtor));
    }

    @Test
    public void formatOutstandingAmount_nonzeroAmount_returnsCurrencyText() {
        Person debtor = new PersonBuilder().withOutstandingAmount("10.50").build();

        assertEquals("Outstanding: S$10.50", PersonCard.formatOutstandingAmount(debtor));
    }

    @Test
    public void formatOutstandingAmount_groupedAmount_returnsCurrencyText() {
        Person debtor = new PersonBuilder().withOutstandingAmount("1,250.00").build();

        assertEquals("Outstanding: S$1,250.00", PersonCard.formatOutstandingAmount(debtor));
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
}
