package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import seedu.address.MainApp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final Path PERSON_CARD_SOURCE =
            Path.of("src/main/java/seedu/address/ui/PersonCard.java");
    private static final Path PERSON_LIST_PANEL_SOURCE =
            Path.of("src/main/java/seedu/address/ui/PersonListPanel.java");
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

    @Test
    public void fxml_longTextLabels_wrapWithinCardWidth() throws IOException {
        String fxml = readPersonListCardFxml();

        for (String labelId : List.of("name", "phone", "outstandingAmount", "address", "email")) {
            String labelElement = getLabelElement(fxml, labelId);
            assertTrue(labelElement.contains("minWidth=\"0.0\""));
            assertTrue(labelElement.contains("maxWidth=\"1.7976931348623157E308\""));
            assertTrue(labelElement.contains("wrapText=\"true\""));
            if (!labelId.equals("name")) {
                assertTrue(labelElement.contains("prefWidth=\"0.0\""));
            }
        }
        assertTrue(getLabelElement(fxml, "name").contains("HBox.hgrow=\"ALWAYS\""));
    }

    @Test
    public void listCell_cardWidthTracksListViewport() throws IOException {
        String source = Files.readString(PERSON_LIST_PANEL_SOURCE);
        String panelFxml;
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/view/PersonListPanel.fxml")) {
            assertNotNull(inputStream);
            panelFxml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(source.contains(
                "card.prefWidthProperty().bind(personListView.widthProperty().subtract(20));"));
        assertTrue(source.contains("personCard.bindContentWidth(personListView.widthProperty());"));
        assertTrue(panelFxml.contains("fx:id=\"personListView\" minWidth=\"0.0\""));
    }

    private String readPersonListCardFxml() throws IOException {
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/view/PersonListCard.fxml")) {
            assertNotNull(inputStream);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String getLabelElement(String fxml, String labelId) {
        int startIndex = fxml.indexOf("<Label fx:id=\"" + labelId + "\"");
        assertTrue(startIndex >= 0);
        int endIndex = fxml.indexOf("/>", startIndex);
        assertTrue(endIndex > startIndex);
        return fxml.substring(startIndex, endIndex);
    }

    private String formatCardText(Person person) throws IOException {
        Matcher matcher = AMOUNT_BINDING.matcher(Files.readString(PERSON_CARD_SOURCE));
        assertTrue(matcher.find());
        return matcher.group(1) + person.getOutstandingAmount();
    }
}
