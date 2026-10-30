package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Displays scheduled follow-ups in date order without changing stored data. */
public class DueCommand extends Command {
    public static final String COMMAND_WORD = "due";
    public static final String MESSAGE_USAGE = "due [YYYY-MM-DD] | due --from YYYY-MM-DD --to YYYY-MM-DD";
    private static final Comparator<Person> FOLLOW_UP_ORDER = Comparator
            .comparing((Person person) -> person.getFollowUpDate().orElse(LocalDate.MAX))
            .thenComparingInt(Person::getDebtorId);
    private final LocalDate from;
    private final LocalDate to;
    private final Clock clock;

    /** Creates a cutoff view, or an inclusive range if a start is supplied. */
    public DueCommand(LocalDate from, LocalDate to, Clock clock) {
        this.from = from;
        this.to = to;
        this.clock = requireNonNull(clock);
        if (from != null && (to == null || from.isAfter(to))) {
            throw new IllegalArgumentException("The start date must not be after the end date.");
        }
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        LocalDate cutoff = to == null ? today : to;
        model.updatePersonView(person -> person.getFollowUpDate().filter(date ->
                !date.isAfter(cutoff) && (from == null || !date.isBefore(from))).isPresent(), FOLLOW_UP_ORDER);
        String heading = from == null ? "Follow-ups on or before " + cutoff + ":"
                : "Follow-ups from " + from + " to " + cutoff + ":";
        if (model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(heading + "\nNo matching follow-ups.");
        }
        String lines = model.getFilteredPersonList().stream().map(person -> formatPerson(person, today))
                .collect(Collectors.joining("\n"));
        return new CommandResult(heading + "\n" + lines);
    }

    private String formatPerson(Person person, LocalDate today) {
        LocalDate date = person.getFollowUpDate().orElseThrow();
        String status = date.isBefore(today) ? "overdue" : date.equals(today) ? "due today" : "upcoming";
        return date + " | ID " + person.getDebtorId() + " | " + person.getName() + " | " + status;
    }
}
