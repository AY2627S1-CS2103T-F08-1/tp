package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Represents a timestamped note about an interaction with a debtor.
 */
public final class InteractionNote {

    public static final String MESSAGE_CONSTRAINTS = "Interaction note text must not be blank.";
    public static final DateTimeFormatter STORAGE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalDateTime timestamp;
    private final String text;

    /**
     * Creates an interaction note with the supplied timestamp and text.
     */
    public InteractionNote(LocalDateTime timestamp, String text) {
        requireNonNull(timestamp);
        requireNonNull(text);
        String trimmedText = text.trim();
        if (!isValidText(trimmedText)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.timestamp = timestamp;
        this.text = trimmedText;
    }

    /**
     * Creates an interaction note from a timestamp stored in ISO local date-time format.
     */
    public static InteractionNote fromStorage(String timestamp, String text) {
        requireNonNull(timestamp);
        try {
            return new InteractionNote(LocalDateTime.parse(timestamp, STORAGE_FORMATTER), text);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Interaction note timestamp must be a valid ISO local date-time.",
                    exception);
        }
    }

    public static boolean isValidText(String text) {
        return text != null && !text.isBlank();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public String getStorageTimestamp() {
        return timestamp.format(STORAGE_FORMATTER);
    }

    @Override
    public String toString() {
        return "[" + timestamp.format(DISPLAY_FORMATTER) + "] " + text;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof InteractionNote otherNote)) {
            return false;
        }
        return timestamp.equals(otherNote.timestamp) && text.equals(otherNote.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, text);
    }
}
