package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSort;

/** Shows debtors with a matching local contact window. */
public class ContactableCommand extends Command {
    public static final String COMMAND_WORD = "contactable";
    public static final String MESSAGE_USAGE = "contactable [--at HH:mm] [--method call|sms|email|letter]";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime requestedTime;
    private final String method;
    private final Clock clock;

    /** Creates a contactability view, using the clock when no time is supplied. */
    public ContactableCommand(LocalTime requestedTime, String method, Clock clock) {
        this.requestedTime = requestedTime;
        this.method = method;
        this.clock = requireNonNull(clock);
    }

    @Override
    public boolean shouldSaveAddressBook() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalTime reference = requestedTime == null ? LocalTime.now(clock).withSecond(0).withNano(0) : requestedTime;
        model.updatePersonView(person -> matches(person, reference), PersonSort.DEFAULT_COMPARATOR);
        String heading = "Contactable at " + reference.format(TIME_FORMAT)
                + (method == null ? "" : " by " + method) + ":";
        if (model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(heading + "\nNo matching debtors.");
        }
        String lines = model.getFilteredPersonList().stream().map(this::formatPerson)
                .collect(Collectors.joining("\n"));
        return new CommandResult(heading + "\n" + lines);
    }

    private boolean matches(Person person, LocalTime reference) {
        ContactPreferences preferences = person.getContactPreferences();
        String hours = preferences.getHours();
        if (hours == null || (method != null && !method.equals(preferences.getMethod()))) {
            return false;
        }
        LocalTime start = LocalTime.parse(hours.substring(0, 5));
        LocalTime end = LocalTime.parse(hours.substring(6));
        return !reference.isBefore(start) && reference.isBefore(end);
    }

    private String formatPerson(Person person) {
        ContactPreferences preferences = person.getContactPreferences();
        return "ID " + person.getDebtorId() + " - " + person.getName() + " | "
                + (preferences.getMethod() == null ? "method not set" : preferences.getMethod())
                + " | " + preferences.getHours();
    }
}
