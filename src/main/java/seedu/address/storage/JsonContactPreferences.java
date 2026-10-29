package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.ContactPreferences;

/** JSON representation of optional contact settings. */
class JsonContactPreferences {
    private final String method;
    private final String hours;

    @JsonCreator
    JsonContactPreferences(@JsonProperty("method") String method, @JsonProperty("hours") String hours) {
        this.method = method;
        this.hours = hours;
    }

    JsonContactPreferences(ContactPreferences preferences) {
        this(preferences.getMethod(), preferences.getHours());
    }

    ContactPreferences toModelType() throws IllegalValueException {
        try {
            return new ContactPreferences(method, hours);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
