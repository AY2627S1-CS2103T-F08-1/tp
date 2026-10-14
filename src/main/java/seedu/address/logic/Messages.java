package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The debtor index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d debtor(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    private static final String MESSAGE_NO_PERSONS_DISPLAYED_FOR_DELETE =
            "Index %1$s is invalid because the displayed list is empty (0 debtors; no valid index range). "
                    + "Run the list command or adjust your find query before deleting.";
    private static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX_RANGE =
            "Index %1$s is out of range. The displayed list has %2$d %3$s; valid range is 1 to %2$d.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an actionable error message for an index outside the currently displayed person list.
     */
    public static String getInvalidPersonDisplayedIndexMessage(BigInteger requestedIndex, int displayedPersonCount) {
        if (displayedPersonCount == 0) {
            return String.format(MESSAGE_NO_PERSONS_DISPLAYED_FOR_DELETE, requestedIndex);
        }

        String personNoun = displayedPersonCount == 1 ? "debtor" : "debtors";
        return String.format(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX_RANGE,
                requestedIndex, displayedPersonCount, personNoun);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append("ID: ")
                .append(person.getDebtorId())
                .append("; ")
                .append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Outstanding: S$")
                .append(person.getOutstandingAmount())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

    /**
     * Formats a complete debtor profile for display in the result panel.
     */
    public static String formatProfile(Person person) {
        requireNonNull(person);
        String formattedTags = person.getTags().stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));
        if (formattedTags.isEmpty()) {
            formattedTags = "None";
        }

        StringBuilder builder = new StringBuilder()
                .append("Debtor profile\n")
                .append("ID: ").append(person.getDebtorId()).append('\n')
                .append("Name: ").append(person.getName()).append('\n')
                .append("Phone: ").append(person.getPhone()).append('\n')
                .append("Email: ").append(person.getEmail()).append('\n')
                .append("Address: ").append(person.getAddress()).append('\n')
                .append("Outstanding: S$").append(person.getOutstandingAmount()).append('\n')
                .append("Tags: ").append(formattedTags).append('\n')
                .append("Interaction history (newest first):");

        if (person.getInteractionNotes().isEmpty()) {
            return builder.append("\nNo interaction notes recorded.").toString();
        }

        List<InteractionNote> newestFirstNotes = new ArrayList<>(person.getInteractionNotes());
        Collections.reverse(newestFirstNotes);
        newestFirstNotes.stream()
                .sorted(Comparator.comparing(InteractionNote::getTimestamp).reversed())
                .forEach(note -> builder.append("\n- ").append(note));
        return builder.toString();
    }

}
