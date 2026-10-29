package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.PackagedJarTestAgent;

/** Launches the distributable in separate JVMs, including its real JavaFX entry point and resources. */
public class PackagedJarSmokeTest {
    private static final String AGENT_CLASS_FILE =
            "seedu/address/testutil/PackagedJarTestAgent.class";

    @TempDir
    private Path temporaryFolder;

    @Test
    @Timeout(150)
    public void launchElsewhereThenRestart_packagedJar_retainsFilesInJarHome() throws Exception {
        Path source = Path.of("build", "libs", "osps.jar").toAbsolutePath();
        assertTrue(Files.isRegularFile(source), "Build shadowJar before running this test");
        try (JarFile archive = new JarFile(source.toFile())) {
            assertNull(archive.getEntry(AGENT_CLASS_FILE), "Test driver must not be shipped with OSPS");
        }
        Path appFolder = Files.createDirectory(temporaryFolder.resolve("app %t with spaces"));
        Path launchFolder = Files.createDirectory(temporaryFolder.resolve("launch"));
        Path appJar = Files.copy(source, appFolder.resolve("osps.jar"));
        Files.writeString(appFolder.resolve("AUDIT_DISPOSABLE"), "Owned temporary test fixture");
        Files.writeString(launchFolder.resolve("AUDIT_DISPOSABLE"), "Owned temporary test fixture");
        Path agentJar = createAgentJar();

        runPackagedJar(launchFolder, appJar, agentJar, "save");
        Path data = appFolder.resolve("data").resolve("addressbook.json");
        assertTrue(Files.isRegularFile(data), "Saved data must be in JAR home, independent of launch folder");
        String savedData = Files.readString(data);
        assertTrue(savedData.contains("Packaged Smoke"), "First process must save its fixture");
        assertApplicationFilesStayInHome(appFolder, launchFolder);
        runPackagedJar(appFolder, appJar, agentJar, "reload");
        assertEquals(savedData, Files.readString(data), "Read-only restart must retain the exact saved records");
        assertEquals(-1, Files.mismatch(source, appJar), "Testing must not modify the application JAR");
    }

    private void assertApplicationFilesStayInHome(Path appFolder, Path launchFolder) throws Exception {
        assertTrue(Files.isRegularFile(appFolder.resolve("preferences.json")), "Preferences must stay in JAR home");
        assertTrue(Files.isRegularFile(appFolder.resolve("osps.log.0")), "Logs must stay in JAR home");
        try (var exports = Files.list(appFolder.resolve("exports"))) {
            List<Path> profiles = exports.toList();
            assertEquals(1, profiles.size(), "Exactly one profile must be exported in JAR home");
            assertTrue(Files.readString(profiles.getFirst()).contains("Packaged Smoke"));
        }
        for (String name : List.of("data", "preferences.json", "osps.log.0", "exports")) {
            assertFalse(Files.exists(launchFolder.resolve(name)), "App output escaped into launch folder: " + name);
        }
    }

    private Path createAgentJar() throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Premain-Class", PackagedJarTestAgent.class.getName());
        Path agentJar = temporaryFolder.resolve("test-driver.jar");
        try (InputStream source = getClass().getClassLoader().getResourceAsStream(AGENT_CLASS_FILE);
                JarOutputStream archive = new JarOutputStream(Files.newOutputStream(agentJar), manifest)) {
            assertNotNull(source, "Compiled test driver must exist");
            archive.putNextEntry(new JarEntry(AGENT_CLASS_FILE));
            source.transferTo(archive);
            archive.closeEntry();
        }
        return agentJar;
    }

    private void runPackagedJar(Path folder, Path appJar, Path agentJar, String mode) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Path output = folder.resolve("startup-" + mode + ".log");
        Process process = new ProcessBuilder(java.toString(), "-javaagent:" + agentJar + "=" + mode,
                "-jar", appJar.toString()).directory(folder.toFile()).redirectErrorStream(true)
                .redirectOutput(output.toFile()).start();
        try {
            boolean exited = process.waitFor(60, TimeUnit.SECONDS);
            String log = Files.readString(output);
            assertTrue(exited, "Packaged application did not exit normally:\n" + log);
            assertEquals(0, process.exitValue(), log);
            assertTrue(log.contains("PACKAGED_SMOKE_PASS " + mode), log);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
            }
        }
    }
}
