package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.AppHome;

public class MainAppTest {

    @Test
    public void defaultFileLocations_areInsideAppHome() {
        assertEquals(AppHome.resolve("preferences.json"), MainApp.USER_PREFS_FILE_PATH);
        assertEquals(AppHome.resolve("data", "addressbook.json"), MainApp.ADDRESS_BOOK_FILE_PATH);
    }
}
