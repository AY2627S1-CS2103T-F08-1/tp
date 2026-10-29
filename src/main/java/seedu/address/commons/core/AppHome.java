package seedu.address.commons.core;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Locale;
import java.util.Optional;

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
     * Returns the folder containing the JAR at {@code codeSource}, or {@code workingDirectory} if {@code codeSource}
     * is missing, is not a {@code file:} location or is not a JAR file.
     * Exposed so the choice can be tested without launching a JAR.
     */
    static Path homeOf(URL codeSource, Path workingDirectory) {
        Path location = null;
        if (codeSource != null) {
            try {
                location = Path.of(URI.create(codeSource.toString()));
            } catch (RuntimeException e) {
                // Not a local file location, so there is no JAR folder to use; fall back to the working directory.
                location = null;
            }
        }
        return homeOf(location, workingDirectory);
    }

    static Path homeOf(Path codeSource, Path workingDirectory) {
        boolean isJar = codeSource != null && Files.isRegularFile(codeSource)
                && codeSource.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(JAR_EXTENSION);
        return isJar ? codeSource.toAbsolutePath().getParent() : workingDirectory.toAbsolutePath();
    }

    private static Path locateHome() {
        URL location = Optional.ofNullable(AppHome.class.getProtectionDomain().getCodeSource())
                .map(CodeSource::getLocation).orElse(null);
        return homeOf(location, Path.of(""));
    }
}
