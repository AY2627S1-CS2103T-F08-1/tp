package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.Person;

/** Covers how follow-up fields are validated when an address book is loaded from JSON. */
class JsonFollowUpStorageTest {

    @TempDir
    public Path testFolder;

    private Person read(String followUpFields) throws Exception {
        Path file = testFolder.resolve("data.json");
        Files.writeString(file, "{\"persons\":[{\"name\":\"Alice Tan\",\"phone\":\"81234567\","
                + "\"email\":\"alice@example.com\",\"address\":\"1 Main Street\"," + followUpFields
                + "\"tags\":[]}]}");
        return new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().getFirst();
    }

    private void assertRejected(String followUpFields) {
        assertThrows(DataLoadingException.class, () -> read(followUpFields));
    }

    private static String event(String action, String previous, String next) {
        return "{\"timestamp\":\"2026-10-29T10:00:00\",\"action\":\"" + action + "\",\"previousDate\":"
                + previous + ",\"newDate\":" + next + "}";
    }

    @Test
    void read_noFollowUpFields_loadsEmptyFollowUp() throws Exception {
        Person person = read("");
        assertTrue(person.getFollowUpDate().isEmpty());
        assertTrue(person.getFollowUpHistory().isEmpty());
    }

    @Test
    void read_nullHistory_loadsEmptyHistory() throws Exception {
        assertTrue(read("\"followUpHistory\":null,").getFollowUpHistory().isEmpty());
    }

    @Test
    void read_previousAndNewDates_loadExactPair() throws Exception {
        Person person = read("\"followUpDate\":\"2026-11-05\",\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"2026-11-03\"") + ","
                + event("RESCHEDULED", "\"2026-11-03\"", "\"2026-11-05\"") + "],");
        List<FollowUpEvent> history = person.getFollowUpHistory();
        assertEquals(Optional.of(LocalDate.of(2026, 11, 3)), history.get(1).getPreviousDate());
        assertEquals(Optional.of(LocalDate.of(2026, 11, 5)), history.get(1).getNewDate());
        assertEquals(Optional.of(LocalDate.of(2026, 11, 5)), person.getFollowUpDate());
    }

    @Test
    void read_legacyDateOnlyEvents_areMigrated() throws Exception {
        Person person = read("\"followUpDate\":\"2026-11-05\",\"followUpHistory\":["
                + "{\"timestamp\":\"2026-10-29T10:00:00\",\"action\":\"SCHEDULED\",\"date\":\"2026-11-03\"},"
                + "{\"timestamp\":\"2026-10-29T10:01:00\",\"action\":\"RESCHEDULED\",\"date\":\"2026-11-05\"}],");
        assertEquals(Optional.of(LocalDate.of(2026, 11, 3)), person.getFollowUpHistory().get(1).getPreviousDate());
    }

    @Test
    void read_eventWithBothLegacyAndNewFields_usesNewFields() throws Exception {
        Person withNewDate = read("\"followUpDate\":\"2026-11-03\",\"followUpHistory\":["
                + "{\"timestamp\":\"2026-10-29T10:00:00\",\"action\":\"SCHEDULED\",\"date\":\"2026-11-09\","
                + "\"newDate\":\"2026-11-03\"}],");
        assertEquals(Optional.of(LocalDate.of(2026, 11, 3)), withNewDate.getFollowUpHistory().get(0).getNewDate());
        Person withPreviousDate = read("\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"2026-11-03\"") + ","
                + "{\"timestamp\":\"2026-10-29T10:01:00\",\"action\":\"CLEARED\",\"date\":\"2026-11-09\","
                + "\"previousDate\":\"2026-11-03\"}],");
        assertEquals(Optional.of(LocalDate.of(2026, 11, 3)),
                withPreviousDate.getFollowUpHistory().get(1).getPreviousDate());
    }

    @Test
    void read_legacyRescheduleWithoutEarlierSchedule_isRejected() {
        assertRejected("\"followUpDate\":\"2026-11-05\",\"followUpHistory\":["
                + "{\"timestamp\":\"2026-10-29T10:00:00\",\"action\":\"RESCHEDULED\",\"date\":\"2026-11-05\"}],");
    }

    @Test
    void read_malformedEvents_areRejected() {
        assertRejected("\"followUpHistory\":[null],");
        assertRejected("\"followUpHistory\":[{\"action\":\"SCHEDULED\",\"newDate\":\"2026-11-03\"}],");
        assertRejected("\"followUpHistory\":[{\"timestamp\":\"2026-10-29T10:00:00\",\"newDate\":\"2026-11-03\"}],");
        assertRejected("\"followUpDate\":\"2026-11-03\",\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"2026-1-3\"") + "],");
        assertRejected("\"followUpDate\":\"0000-01-01\",\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"0000-01-01\"") + "],");
        assertRejected("\"followUpHistory\":[" + event("CLEARED", "null", "\"2026-11-03\"") + "],");
    }

    @Test
    void read_invalidCurrentDate_isRejected() {
        assertRejected("\"followUpDate\":\"2026-1-3\",");
        assertRejected("\"followUpDate\":\"2026-02-30\",");
        assertRejected("\"followUpDate\":\"0000-01-01\",");
    }

    @Test
    void read_discontinuousHistory_isRejected() {
        assertRejected("\"followUpDate\":\"2026-11-05\",\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"2026-11-03\"") + ","
                + event("SCHEDULED", "null", "\"2026-11-05\"") + "],");
    }

    @Test
    void read_historyNotMatchingCurrentDate_isRejected() {
        assertRejected("\"followUpHistory\":[" + event("SCHEDULED", "null", "\"2026-11-03\"") + "],");
        assertRejected("\"followUpDate\":\"2026-11-09\",\"followUpHistory\":["
                + event("SCHEDULED", "null", "\"2026-11-03\"") + "],");
    }
}
