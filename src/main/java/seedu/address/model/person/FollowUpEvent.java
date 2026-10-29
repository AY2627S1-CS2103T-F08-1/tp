package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/** A timestamped change to a debtor's scheduled follow-up. */
public final class FollowUpEvent {

    /** The kind of follow-up state change represented by an event. */
    public enum Action {
        SCHEDULED,
        RESCHEDULED,
        CLEARED,
        COMPLETED
    }

    public static final DateTimeFormatter STORAGE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LocalDateTime timestamp;
    private final Action action;
    private final Optional<LocalDate> date;

    /** Creates an event with its timestamp, action, and the date affected by that action. */
    public FollowUpEvent(LocalDateTime timestamp, Action action, Optional<LocalDate> date) {
        requireNonNull(timestamp);
        requireNonNull(action);
        requireNonNull(date);
        if (date.isEmpty()) {
            throw new IllegalArgumentException("Follow-up event date does not match its action.");
        }
        this.timestamp = timestamp;
        this.action = action;
        this.date = date;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Action getAction() {
        return action;
    }

    public Optional<LocalDate> getDate() {
        return date;
    }

    public String getStorageTimestamp() {
        return timestamp.format(STORAGE_FORMATTER);
    }

    /** Creates an event from its stored ISO timestamp, action name, and ISO date. */
    public static FollowUpEvent fromStorage(String timestamp, String action, String date) {
        try {
            return new FollowUpEvent(LocalDateTime.parse(timestamp, STORAGE_FORMATTER), Action.valueOf(action),
                    Optional.ofNullable(date).map(LocalDate::parse));
        } catch (DateTimeParseException | NullPointerException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Follow-up event contains an invalid timestamp, action or date.",
                    exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FollowUpEvent otherEvent)) {
            return false;
        }
        return timestamp.equals(otherEvent.timestamp) && action == otherEvent.action && date.equals(otherEvent.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, action, date);
    }

    @Override
    public String toString() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "[" + timestamp.format(displayFormatter) + "] " + action + date.map(value -> " " + value).orElse("");
    }
}
