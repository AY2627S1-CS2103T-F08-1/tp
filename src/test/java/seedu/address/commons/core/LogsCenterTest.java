package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {

    @TempDir
    public Path testFolder;

    @Test
    public void addFileHandler_unwritableLocation_keepsLoggingWithoutThrowing() {
        Logger target = Logger.getLogger("osps.test.unwritable");
        Path missingFolder = testFolder.resolve("missing").resolve("osps.log");

        assertDoesNotThrow(() -> LogsCenter.addFileHandler(target, missingFolder.toString().replace("%", "%%")));
        assertEquals(0, target.getHandlers().length);
    }

    @Test
    public void addFileHandler_writableLocation_addsHandler() {
        Logger target = Logger.getLogger("osps.test.writable");
        LogsCenter.addFileHandler(target, testFolder.resolve("osps.log").toString().replace("%", "%%"));

        assertEquals(1, target.getHandlers().length);
        for (var handler : target.getHandlers()) {
            handler.close();
            target.removeHandler(handler);
        }
    }

    @Test
    public void logFilePattern_escapesPercentAndPointsIntoAppHome() {
        String pattern = LogsCenter.logFilePattern();

        assertTrue(pattern.endsWith("osps.log"));
        assertEquals(AppHome.resolve("osps.log").toString().replace("%", "%%"), pattern);
    }
}
