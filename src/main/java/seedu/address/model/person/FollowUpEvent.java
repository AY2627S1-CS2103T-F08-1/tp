package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/** An immutable transition between two scheduled follow-up dates. */
public final class FollowUpEvent {

    public static final String MESSAGE_INVALID_DATE =
            "Follow-up date must use the valid YYYY-MM-DD format.";
    public static final String MESSAGE_INVALID_STORAGE =
            "Follow-up event contains an invalid timestamp, action or date.";
    public static final String MESSAGE_INVALID_LEGACY_RESCHEDULE =
            "Legacy rescheduled event has no prior scheduled date.";

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
    private final Optional<LocalDate> previousDate;
    private final Optional<LocalDate> newDate;

    /** Creates a lifecycle event with its previous and resulting dates. */
    public FollowUpEvent(LocalDateTime timestamp, Action action, Optional<LocalDate> previousDate,
            Optional<LocalDate> newDate) {
        requireNonNull(timestamp);
        requireNonNull(action);
        requireNonNull(previousDate);
        requireNonNull(newDate);
        if (!isValidTransition(action, previousDate, newDate)) {
            throw new IllegalArgumentException("Follow-up event dates do not match its action.");
        }
        this.timestamp = timestamp;
        this.action = action;
        this.previousDate = previousDate;
        this.newDate = newDate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Action getAction() {
        return action;
    }

    public Optional<LocalDate> getPreviousDate() {
        return previousDate;
    }

    public Optional<LocalDate> getNewDate() {
        return newDate;
    }

    /** Returns the date shown by legacy profile and client code. */
    public Optional<LocalDate> getDate() {
        return newDate.isPresent() ? newDate : previousDate;
    }

    public String getStorageTimestamp() {
        return timestamp.format(STORAGE_FORMATTER);
    }

    /** Creates an event from a stored timestamp, action and previous/new date pair. */
    public static FollowUpEvent fromStorage(String timestamp, String action, String previousDate, String newDate) {
        return new FollowUpEvent(parseTimestamp(timestamp), parseAction(action),
                Optional.ofNullable(previousDate).map(FollowUpEvent::parseDate),
                Optional.ofNullable(newDate).map(FollowUpEvent::parseDate));
    }

    /** Migrates an earlier date-only event using the state established by prior history entries. */
    public static FollowUpEvent fromLegacyStorage(String timestamp, String action, String date,
            Optional<LocalDate> currentDate) {
        Action parsedAction = parseAction(action);
        Optional<LocalDate> parsedDate = Optional.ofNullable(date).map(FollowUpEvent::parseDate);
        if (parsedAction == Action.RESCHEDULED && currentDate.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_INVALID_LEGACY_RESCHEDULE);
        }
        Optional<LocalDate> previousDate = parsedAction == Action.CLEARED || parsedAction == Action.COMPLETED
                ? parsedDate : parsedAction == Action.RESCHEDULED ? currentDate : Optional.empty();
        Optional<LocalDate> newDate = parsedAction == Action.CLEARED || parsedAction == Action.COMPLETED
                ? Optional.empty() : parsedDate;
        return new FollowUpEvent(parseTimestamp(timestamp), parsedAction, previousDate, newDate);
    }

    private static boolean isValidTransition(Action action, Optional<LocalDate> previousDate,
            Optional<LocalDate> newDate) {
        return switch (action) {
            case SCHEDULED -> previousDate.isEmpty() && newDate.isPresent();
            case RESCHEDULED -> previousDate.isPresent() && newDate.isPresent();
            case CLEARED, COMPLETED -> previousDate.isPresent() && newDate.isEmpty();
        };
    }

    private static LocalDateTime parseTimestamp(String timestamp) {
        final LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(timestamp, STORAGE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE, exception);
        }
        if (parsed.getYear() < 1 || parsed.getYear() > 9999) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE);
        }
        return parsed;
    }

    private static Action parseAction(String action) {
        requireNonNull(action);
        try {
            return Action.valueOf(action);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE, exception);
        }
    }

    private static LocalDate parseDate(String date) {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE);
        }
        final LocalDate parsed;
        try {
            parsed = LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE, exception);
        }
        if (parsed.getYear() < 1) {
            throw new IllegalArgumentException(MESSAGE_INVALID_STORAGE);
        }
        return parsed;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FollowUpEvent otherEvent)) {
            return false;
        }
        return timestamp.equals(otherEvent.timestamp) && action == otherEvent.action
                && previousDate.equals(otherEvent.previousDate) && newDate.equals(otherEvent.newDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, action, previousDate, newDate);
    }

    @Override
    public String toString() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String dates = switch (action) {
            case SCHEDULED -> newDate.map(value -> " " + value).orElse("");
            case RESCHEDULED -> previousDate.flatMap(from -> newDate.map(to -> " " + from + " -> " + to))
                    .orElse("");
            case CLEARED, COMPLETED -> previousDate.map(value -> " " + value).orElse("");
        };
        return "[" + timestamp.format(displayFormatter) + "] " + action + dates;
    }
}
