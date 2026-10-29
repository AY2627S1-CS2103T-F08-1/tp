package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.ContactableCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ContactableCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-29T01:00:45Z"),
            ZoneId.of("Asia/Singapore"));

    @Test
    public void execute_respectsBoundariesAndExplicitMethod() {
        Model model = new ModelManager();
        Person first = model.addPerson(new PersonBuilder().withName("Alice").withPhone("91111111")
                .withEmail("alice@example.com").build()
                .withContactPreferences(new ContactPreferences("sms", "09:00-17:00")));
        Person second = model.addPerson(new PersonBuilder().withName("Bob").withPhone("92222222")
                .withEmail("bob@example.com").build()
                .withContactPreferences(new ContactPreferences(null, "09:00-10:00")));
        model.addPerson(new PersonBuilder().withName("Carl").withPhone("93333333")
                .withEmail("carl@example.com").build());
        model.updateSortedPersonList(Comparator.comparingInt(Person::getDebtorId).reversed());
        ContactableCommand all = new ContactableCommand(null, null, CLOCK);
        String result = all.execute(model).getFeedbackToUser();
        assertFalse(all.shouldSaveAddressBook());
        assertEquals(List.of(first, second), List.copyOf(model.getFilteredPersonList()));
        assertTrue(result.contains("Contactable at 09:00"));
        assertTrue(result.contains("ID " + first.getDebtorId()));
        assertTrue(result.contains("method not set"));
        new ContactableCommand(LocalTime.of(8, 59), null, CLOCK).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        new ContactableCommand(LocalTime.of(9, 0), "sms", CLOCK).execute(model);
        assertEquals(List.of(first), List.copyOf(model.getFilteredPersonList()));
        new ContactableCommand(LocalTime.of(10, 0), null, CLOCK).execute(model);
        assertEquals(List.of(first), List.copyOf(model.getFilteredPersonList()));
        String empty = new ContactableCommand(LocalTime.of(17, 0), null, CLOCK)
                .execute(model).getFeedbackToUser();
        assertTrue(empty.contains("No matching debtors"));
        assertTrue(model.getFilteredPersonList().isEmpty());
        new ListCommand().execute(model);
        assertEquals(3, model.getFilteredPersonList().size());
    }

    @Test
    public void parser_validAndInvalidInputs() throws Exception {
        ContactableCommandParser parser = new ContactableCommandParser();
        parser.parse("");
        parser.parse("--method EMAIL --at 00:00");
        parser.parse("--at 23:59");
        for (String input : new String[] {"--at", "--method --at 09:00", "--at 24:00", "--at 9:00",
            "--at 12:60", "--method fax", "--at 10:00 --at 11:00", "--extra x", "text"}) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }
}
