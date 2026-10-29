package seedu.address.commons.core;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

/**
 * Locates the folder that owns the application's files: preferences, data, logs and exports.
 * When the application runs from a JAR this is the folder containing the JAR, whatever the launch directory is.
 * When it runs from compiled classes (Gradle, an IDE or tests) it is the working directory.
 */
public final class AppHome {

    private static final String JAR_EXTENSION = ".jar";
    private static final Path HOME = locateHome();

    private AppHome() {}

    /** Returns the absolute path of {@code first} and any {@code more} elements inside the application home. */
    public static Path resolve(String first, String... more) {
        return HOME.resolve(Path.of(first, more));
    }

    /**
     * Returns the folder containing {@code codeSource} if it is a JAR file, otherwise {@code workingDirectory}.
     * Exposed so the choice can be tested without launching a JAR.
     */
    static Path homeOf(Path codeSource, Path workingDirectory) {
        boolean isJar = codeSource != null && Files.isRegularFile(codeSource)
                && codeSource.getFileName().toString().toLowerCase().endsWith(JAR_EXTENSION);
        Path parent = isJar ? codeSource.toAbsolutePath().getParent() : null;
        return parent != null ? parent : workingDirectory.toAbsolutePath();
    }

    private static Path locateHome() {
        Path workingDirectory = Path.of("");
        try {
            CodeSource source = AppHome.class.getProtectionDomain().getCodeSource();
            Path codeSource = source == null ? null : Path.of(source.getLocation().toURI());
            return homeOf(codeSource, workingDirectory);
        } catch (URISyntaxException | RuntimeException e) {
            // Without a usable code location, fall back to the working directory rather than failing to start.
            return homeOf(null, workingDirectory);
        }
    }
}
