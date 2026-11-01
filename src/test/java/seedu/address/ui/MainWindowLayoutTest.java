package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import seedu.address.MainApp;

/**
 * Tests the FXML layout constraints of the main window.
 */
public class MainWindowLayoutTest {

    @Test
    public void fxml_resultDisplayPlaceholder_growsWhenWindowGrows() throws IOException {
        String mainWindowFxml = readFxml("MainWindow.fxml");
        String resultDisplayPlaceholder = getElement(mainWindowFxml, "resultDisplayPlaceholder");

        assertTrue(resultDisplayPlaceholder.contains("minHeight=\"100\""));
        assertTrue(resultDisplayPlaceholder.contains("VBox.vgrow=\"ALWAYS\""));
        assertFalse(resultDisplayPlaceholder.contains("prefHeight="));
        assertFalse(resultDisplayPlaceholder.contains("maxHeight="));
    }

    @Test
    public void fxml_resultDisplay_startsAtMinimumVisibleRows() throws IOException {
        String resultDisplayFxml = readFxml("ResultDisplay.fxml");
        String resultDisplay = getElement(resultDisplayFxml, "resultDisplay");

        assertTrue(resultDisplay.contains("prefRowCount=\"4\""));
        assertTrue(resultDisplay.contains("wrapText=\"true\""));
    }

    @Test
    public void fxml_windowTitle_matchesApplicationTitle() throws IOException {
        String mainWindowFxml = readFxml("MainWindow.fxml");
        int rootStart = mainWindowFxml.indexOf("<fx:root");
        int rootEnd = mainWindowFxml.indexOf('>', rootStart);

        assertTrue(rootStart >= 0);
        assertTrue(rootEnd > rootStart);
        String rootElement = mainWindowFxml.substring(rootStart, rootEnd + 1);

        assertTrue(rootElement.contains("title=\"" + MainWindow.APP_TITLE + "\""));
    }

    private String readFxml(String fxmlFileName) throws IOException {
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/view/" + fxmlFileName)) {
            assertNotNull(inputStream);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String getElement(String fxml, String elementId) {
        int startIndex = fxml.indexOf("fx:id=\"" + elementId + "\"");
        assertTrue(startIndex >= 0);

        int openingElementStart = fxml.lastIndexOf('<', startIndex);
        int openingElementEnd = fxml.indexOf('>', startIndex);
        assertTrue(openingElementStart >= 0);
        assertTrue(openingElementEnd > startIndex);

        return fxml.substring(openingElementStart, openingElementEnd + 1);
    }
}
