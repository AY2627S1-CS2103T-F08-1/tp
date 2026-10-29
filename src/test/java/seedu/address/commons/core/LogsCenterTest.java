package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
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

        assertDoesNotThrow(() -> LogsCenter.addFileHandler(target, LogsCenter.toFileHandlerPattern(missingFolder)));
        assertEquals(0, target.getHandlers().length);
    }

    @Test
    public void addFileHandler_folderNameContainingPercent_writesLogInThatFolder() throws Exception {
        Path folder = Files.createDirectory(testFolder.resolve("my %t dir"));
        Logger target = Logger.getLogger("osps.test.percent");

        LogsCenter.addFileHandler(target, LogsCenter.toFileHandlerPattern(folder.resolve("osps.log")));

        assertEquals(1, target.getHandlers().length);
        for (var handler : target.getHandlers()) {
            handler.close();
            target.removeHandler(handler);
        }
        assertTrue(Files.exists(folder.resolve("osps.log.0")));
    }

    @Test
    public void toFileHandlerPattern_doublesPercentSigns() {
        String pattern = LogsCenter.toFileHandlerPattern(Path.of("a%b", "osps.log"));
        assertEquals(Path.of("a%%b", "osps.log").toString(), pattern);
    }

    @Test
    public void logFilePattern_pointsIntoAppHome() {
        assertEquals(LogsCenter.toFileHandlerPattern(AppHome.resolve("osps.log")), LogsCenter.logFilePattern());
    }
}
