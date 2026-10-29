package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URL;
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
        assertEquals(workingDirectory.toAbsolutePath(), AppHome.homeOf((Path) null, workingDirectory));
    }

    @Test
    public void homeOf_folderNameWithSpacesPercentAndUnicode_returnsThatFolder() throws Exception {
        Path jarHome = Files.createDirectory(testFolder.resolve("my jar %41 é"));
        Path jar = Files.createFile(jarHome.resolve("osps.jar"));

        assertEquals(jarHome.toAbsolutePath(), AppHome.homeOf(jar, Path.of("elsewhere")));
    }

    @Test
    public void homeOf_regularFileThatIsNotAJar_returnsWorkingDirectory() throws Exception {
        Path notJar = Files.createFile(testFolder.resolve("osps.zip"));
        Path workingDirectory = Files.createDirectory(testFolder.resolve("work"));

        assertEquals(workingDirectory.toAbsolutePath(), AppHome.homeOf(notJar, workingDirectory));
    }

    @Test
    public void homeOf_jarLocationUrl_decodesSpacesPercentAndUnicode() throws Exception {
        Path jarHome = Files.createDirectory(testFolder.resolve("my jar %41 é"));
        Path jar = Files.createFile(jarHome.resolve("osps.jar"));

        assertEquals(jarHome.toAbsolutePath(), AppHome.homeOf(jar.toUri().toURL(), Path.of("elsewhere")));
    }

    @Test
    public void homeOf_missingOrNonFileLocationUrl_returnsWorkingDirectory() throws Exception {
        Path workingDirectory = Files.createDirectory(testFolder.resolve("work"));

        assertEquals(workingDirectory.toAbsolutePath(), AppHome.homeOf((URL) null, workingDirectory));
        assertEquals(workingDirectory.toAbsolutePath(),
                AppHome.homeOf(URI.create("http://example.com/osps.jar").toURL(), workingDirectory));
    }

    @Test
    public void resolve_whenRunningFromClasses_usesWorkingDirectory() {
        assertEquals(Path.of("").toAbsolutePath().resolve("x"), AppHome.resolve("x"));
    }

    @Test
    public void resolve_returnsAbsolutePathEndingWithRequestedParts() {
        Path resolved = AppHome.resolve("data", "addressbook.json");

        assertTrue(resolved.isAbsolute());
        assertEquals(Path.of("data", "addressbook.json"), resolved.subpath(resolved.getNameCount() - 2,
                resolved.getNameCount()));
    }
}
