package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppHomeLockTest {
    @TempDir
    Path folder;

    @Test
    void sameHomeRejectedUntilReleased_differentHomeAllowed() throws Exception {
        Path first = folder.resolve("first.lock");
        try (AppHomeLock owner = AppHomeLock.acquire(first);
                AppHomeLock otherHome = AppHomeLock.acquire(folder.resolve("second.lock"))) {
            IOException failure = assertThrows(IOException.class, () -> AppHomeLock.acquire(first));
            assertTrue(failure.getMessage().contains("Another OSPS instance"));
        }
        try (AppHomeLock reopened = AppHomeLock.acquire(first)) {
            assertTrue(java.nio.file.Files.exists(first));
        }
    }

    @Test
    void unavailableLockPathFailsClosed() {
        assertThrows(IOException.class, () -> AppHomeLock.acquire(folder.resolve("missing").resolve("lock")));
    }

    @Test
    void anotherProcessCannotAcquireUntilOwnerExits() throws Exception {
        Path path = folder.resolve("process.lock");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String mainClasses = Path.of(AppHomeLock.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        String testClasses = Path.of(LockProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        Process process = new ProcessBuilder(javaExecutable, "-cp", mainClasses + java.io.File.pathSeparator
                + testClasses, LockProcess.class.getName(), path.toString()).start();
        try {
            String ready = new String(process.getInputStream().readNBytes(5), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(ready.equals("READY"));
            assertThrows(IOException.class, () -> AppHomeLock.acquire(path));
        } finally {
            process.getOutputStream().close();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
        }
        try (AppHomeLock reopened = AppHomeLock.acquire(path)) {
            assertTrue(java.nio.file.Files.exists(path));
        }
    }
}
