package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.DueCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class DueCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));

    private Person add(Model model, String name, String phone, String email, LocalDate date) throws Exception {
        Person added = model.addPerson(new PersonBuilder().withName(name).withPhone(phone).withEmail(email).build());
        if (date != null) {
            new FollowUpCommand(added.getDebtorId(), date).execute(model);
        }
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getDebtorId() == added.getDebtorId()).findFirst().orElseThrow();
    }

    @Test
    public void execute_defaultCutoffAndRange_sortByDateThenId() throws Exception {
        Model model = new ModelManager();
        Person today = add(model, "Alice", "91111111", "alice@example.com", LocalDate.of(2026, 10, 29));
        Person overdue = add(model, "Bob", "92222222", "bob@example.com", LocalDate.of(2026, 10, 28));
        Person tie = add(model, "Carl", "93333333", "carl@example.com", LocalDate.of(2026, 10, 29));
        Person future = add(model, "Dana", "94444444", "dana@example.com", LocalDate.of(2026, 11, 1));
        add(model, "Erin", "95555555", "erin@example.com", null);
        DueCommand command = new DueCommand(null, null, CLOCK);
        String result = command.execute(model).getFeedbackToUser();
        assertFalse(command.shouldSaveAddressBook());
        assertEquals(List.of(overdue, today, tie), List.copyOf(model.getFilteredPersonList()));
        assertTrue(result.contains("2026-10-28 | ID " + overdue.getDebtorId() + " | Bob | overdue"));
        assertTrue(result.contains("due today"));
        String range = new DueCommand(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 1), CLOCK)
                .execute(model).getFeedbackToUser();
        assertEquals(List.of(today, tie, future), List.copyOf(model.getFilteredPersonList()));
        assertTrue(range.contains("upcoming"));
        new ListCommand().execute(model);
        assertEquals(5, model.getFilteredPersonList().size());
        assertEquals(today.getDebtorId(), model.getFilteredPersonList().getFirst().getDebtorId());
    }

    @Test
    public void execute_emptyAndFutureCutoff_doesNotChangeData() throws Exception {
        Model model = new ModelManager();
        Person future = add(model, "Alice", "91111111", "alice@example.com", LocalDate.of(2026, 11, 1));
        assertTrue(new DueCommand(null, null, CLOCK).execute(model).getFeedbackToUser().contains("No matching"));
        String result = new DueCommand(null, LocalDate.of(2026, 11, 2), CLOCK).execute(model).getFeedbackToUser();
        assertTrue(result.contains("upcoming"));
        assertEquals(future, model.getAddressBook().getPersonList().getFirst());
        assertThrows(IllegalArgumentException.class, () ->
                new DueCommand(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new DueCommand(LocalDate.of(2026, 1, 1), null, CLOCK));
    }

    @Test
    public void parser_validAndInvalidDates() throws Exception {
        DueCommandParser parser = new DueCommandParser();
        parser.parse("");
        parser.parse("2024-02-29");
        parser.parse("--to 2027-01-01 --from 2026-12-31");
        for (String input : new String[] {"--invalid", "2026-02-29", "0000-01-01", "2026-1-01", "--from 2026-01-01",
            "--to 2026-01-01 --to 2026-01-02",
            "--from 2026-02-01 --to 2026-01-01", "--from 2026-01-01 --from 2026-01-02",
            "2026-01-01 --to 2026-01-02", "--to 2026-01-01 --other 2026-01-02"}) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void parser_parsedValuesAreUsedAsCutoffAndRange() throws Exception {
        DueCommandParser parser = new DueCommandParser();
        assertTrue(parser.parse("  2024-02-29 \t").execute(new ModelManager()).getFeedbackToUser()
                .startsWith("Follow-ups on or before 2024-02-29:"));
        assertTrue(parser.parse("--to 2027-01-01 --from 2026-12-31").execute(new ModelManager())
                .getFeedbackToUser().startsWith("Follow-ups from 2026-12-31 to 2027-01-01:"));
        assertTrue(parser.parse("9999-12-31").execute(new ModelManager()).getFeedbackToUser()
                .startsWith("Follow-ups on or before 9999-12-31:"));
    }

    @Test
    public void parser_invalidInput_usesSharedMessages() {
        DueCommandParser parser = new DueCommandParser();
        String format = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DueCommand.MESSAGE_USAGE);
        assertParseError(parser, "2026-02-30", FollowUpEvent.MESSAGE_INVALID_DATE);
        assertParseError(parser, "--from 2026-13-01 --to 2026-12-01", FollowUpEvent.MESSAGE_INVALID_DATE);
        assertParseError(parser, "2026-01-01 2026-01-02", format);
        assertParseError(parser, "--from 2026-01-01", format);
    }

    private static void assertParseError(DueCommandParser parser, String input, String message) {
        assertEquals(message, assertThrows(ParseException.class, () -> parser.parse(input)).getMessage());
    }

    @Test
    public void execute_singaporeMidnight_changesWhichDayIsToday() throws Exception {
        ZoneId singapore = ZoneId.of("Asia/Singapore");
        Model model = new ModelManager();
        add(model, "Alice", "91111111", "alice@example.com", LocalDate.of(2026, 10, 30));
        Clock lastSecondOfThe29th = Clock.fixed(Instant.parse("2026-10-29T15:59:59Z"), singapore);
        Clock firstSecondOfThe30th = Clock.fixed(Instant.parse("2026-10-29T16:00:00Z"), singapore);

        assertTrue(new DueCommand(null, null, lastSecondOfThe29th).execute(model).getFeedbackToUser()
                .contains("No matching follow-ups."));
        assertTrue(new DueCommand(null, null, firstSecondOfThe30th).execute(model).getFeedbackToUser()
                .contains("due today"));
    }

    @Test
    public void execute_sameDateAcrossTenOrMoreDebtors_ordersIdsNumerically() throws Exception {
        Model model = new ModelManager();
        for (int i = 1; i <= 11; i++) {
            String suffix = String.format("%02d", i);
            add(model, "Debtor " + suffix, "900000" + suffix, "debtor" + suffix + "@example.com",
                    LocalDate.of(2026, 10, 29));
        }

        new DueCommand(null, null, CLOCK).execute(model);

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                model.getFilteredPersonList().stream().map(Person::getDebtorId).toList());
    }

    @Test
    public void execute_afterReschedule_listsDebtorUnderItsNewDate() throws Exception {
        Model model = new ModelManager();
        Person alice = add(model, "Alice", "91111111", "alice@example.com", LocalDate.of(2026, 10, 29));
        new DueCommand(null, null, CLOCK).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());

        new FollowUpCommand(alice.getDebtorId(), LocalDate.of(2026, 11, 30)).execute(model);
        new DueCommand(null, null, CLOCK).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        new DueCommand(null, LocalDate.of(2026, 11, 30), CLOCK).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_replacesActiveFilterAndSort() throws Exception {
        Model model = new ModelManager();
        Person later = add(model, "Alice", "91111111", "alice@example.com", LocalDate.of(2026, 10, 29));
        Person earlier = add(model, "Bob", "92222222", "bob@example.com", LocalDate.of(2026, 10, 28));
        model.updatePersonView(person -> person.getName().fullName.equals("Alice"),
                Comparator.comparing((Person person) -> person.getName().fullName).reversed());
        assertEquals(1, model.getFilteredPersonList().size());

        new DueCommand(null, null, CLOCK).execute(model);

        assertEquals(List.of(earlier, later), List.copyOf(model.getFilteredPersonList()));
    }
}
