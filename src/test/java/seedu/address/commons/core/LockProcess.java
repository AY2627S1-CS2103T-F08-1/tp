package seedu.address.commons.core;

import java.nio.file.Path;

/** Child process used to verify the OS-level data folder lock. */
public class LockProcess {
    public static void main(String[] args) throws Exception {
        try (AppHomeLock ignored = AppHomeLock.acquire(Path.of(args[0]))) {
            System.out.print("READY");
            System.out.flush();
            System.in.read();
        }
    }
}
