package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppHomeTest {

    @TempDir
    public Path testFolder;

    @Test
    public void homeOf_jarFile_returnsFolderContainingJar() throws Exception {
        Path jarHome = Files.createDirectory(testFolder.resolve("jar-home"));
        Path jar = Files.createFile(jarHome.resolve("osps.jar"));
        Path launchFolder = Files.createDirectory(testFolder.resolve("launch-folder"));

        assertEquals(jarHome.toAbsolutePath(), AppHome.homeOf(jar, launchFolder));
    }

    @Test
    public void homeOf_upperCaseJarExtension_returnsFolderContainingJar() throws Exception {
        Path jar = Files.createFile(testFolder.resolve("OSPS.JAR"));

        assertEquals(testFolder.toAbsolutePath(), AppHome.homeOf(jar, Path.of("elsewhere")));
    }

    @Test
    public void homeOf_classesFolder_returnsWorkingDirectory() throws Exception {
        Path classes = Files.createDirectory(testFolder.resolve("classes"));
        Path workingDirectory = Files.createDirectory(testFolder.resolve("work"));

        assertEquals(workingDirectory.toAbsolutePath(), AppHome.homeOf(classes, workingDirectory));
    }

    @Test
    public void homeOf_missingJarOrNoCodeSource_returnsWorkingDirectory() throws Exception {
        Path workingDirectory = Files.createDirectory(testFolder.resolve("work"));

        assertEquals(workingDirectory.toAbsolutePath(),
                AppHome.homeOf(testFolder.resolve("missing.jar"), workingDirectory));
        assertEquals(workingDirectory.toAbsolutePath(), AppHome.homeOf(null, workingDirectory));
    }

    @Test
    public void resolve_returnsAbsolutePathEndingWithRequestedParts() {
        Path resolved = AppHome.resolve("data", "addressbook.json");

        assertTrue(resolved.isAbsolute());
        assertEquals(Path.of("data", "addressbook.json"), resolved.subpath(resolved.getNameCount() - 2,
                resolved.getNameCount()));
    }
}
