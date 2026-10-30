package seedu.address.model.person;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Optional contact method and same-day local contact window. */
public final class ContactPreferences {
    public static final String METHOD_CONSTRAINTS = "Method must be call, sms, email or letter.";
    public static final String HOURS_CONSTRAINTS = "Hours must be HH:mm-HH:mm with start before end.";
    private static final Set<String> METHODS = Set.of("call", "sms", "email", "letter");
    private final String method;
    private final String hours;

    /** Creates preferences, using null for an unset field. */
    public ContactPreferences(String method, String hours) {
        if (method != null && !METHODS.contains(method.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(METHOD_CONSTRAINTS);
        }
        if (hours != null && !isValidHours(hours)) {
            throw new IllegalArgumentException(HOURS_CONSTRAINTS);
        }
        this.method = method == null ? null : method.toLowerCase(Locale.ROOT);
        this.hours = hours;
    }

    /** Checks strict 24-hour same-day window syntax. */
    public static boolean isValidHours(String value) {
        if (!value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]-(?:[01][0-9]|2[0-3]):[0-5][0-9]")) {
            return false;
        }
        return LocalTime.parse(value.substring(0, 5)).isBefore(LocalTime.parse(value.substring(6)));
    }

    public String getMethod() {
        return method;
    }
    public String getHours() {
        return hours;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ContactPreferences that
                && Objects.equals(method, that.method) && Objects.equals(hours, that.hours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, hours);
    }
}
