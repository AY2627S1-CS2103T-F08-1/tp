package seedu.address.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            return new String(input.readAllBytes(), CHARSET);
        }
    }

    /** Returns whether any existing component of a managed path is a link or a special reparse entry. */
    public static boolean hasRedirectedPath(Path file) throws IOException {
        for (Path current = file.toAbsolutePath(); current != null; current = current.getParent()) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(current, BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (attributes.isSymbolicLink() || attributes.isOther()) {
                    return true;
                }
            } catch (NoSuchFileException ignored) {
                // Continue upwards so existing parents are still checked for redirection.
            }
        }
        return false;
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
            output.write(content.getBytes(CHARSET));
        }
    }

}
