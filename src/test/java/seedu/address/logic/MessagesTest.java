package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void formatProfile_allFieldsAndNotes_returnsDeterministicProfile() {
        InteractionNote olderNote = new InteractionNote(LocalDateTime.of(2026, 10, 9, 14, 30),
                "Called debtor, promised payment.");
        InteractionNote newerNote = new InteractionNote(LocalDateTime.of(2026, 10, 10, 9, 15),
                "Sent follow-up email.");
        Person debtor = new PersonBuilder().withDebtorId(17).withName("Alice Tan")
                .withPhone("91234567").withEmail("alice@example.com")
                .withAddress("1 Example Road, #02-03").withOutstandingAmount("1,250.50")
                .withTags("zeta", "alpha").withInteractionNotes(List.of(olderNote, newerNote)).build();

        String expected = """
                Debtor profile
                ID: 17
                Name: Alice Tan
                Phone: 91234567
                Email: alice@example.com
                Address: 1 Example Road, #02-03
                Outstanding: S$1,250.50
                Tags: [alpha], [zeta]
                Interaction history (newest first):
                - [2026-10-10 09:15] Sent follow-up email.
                - [2026-10-09 14:30] Called debtor, promised payment.""";

        assertEquals(expected, Messages.formatProfile(debtor));
    }

    @Test
    public void formatProfile_noTagsOrNotes_showsExplicitEmptyValues() {
        Person debtor = new PersonBuilder().withDebtorId(1).build();

        String profile = Messages.formatProfile(debtor);

        assertTrue(profile.contains("Tags: None"));
        assertTrue(profile.endsWith("Interaction history (newest first):\nNo interaction notes recorded."));
    }

    @Test
    public void formatProfile_longNote_doesNotTruncateText() {
        String longText = "Discussed a detailed repayment arrangement with punctuation; follow up? ".repeat(30);
        InteractionNote longNote = new InteractionNote(LocalDateTime.of(2026, 10, 10, 10, 0), longText);
        Person debtor = new PersonBuilder().withDebtorId(2).withInteractionNotes(List.of(longNote)).build();

        assertTrue(Messages.formatProfile(debtor).contains(longText.trim()));
    }
}
