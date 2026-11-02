package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.util.Set;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with an alphanumeric character, and may contain Unicode letters, numbers, combining "
                    + "marks, spaces, hyphens, or apostrophes";

    /*
     * The name must start with an alphanumeric character. Combining marks may follow an alphanumeric character,
     * including at the end of a name. Spaces, hyphens, and apostrophes may separate alphanumeric sequences.
     */
    public static final String VALIDATION_REGEX =
            "(?U)\\p{Alnum}\\p{M}*(?:(?:\\p{Alnum}|[ '’-]+\\p{Alnum})\\p{M}*)*";

    private static final Set<Integer> INVISIBLE_FILLERS = Set.of(0x115f, 0x1160, 0x3164, 0xffa0);

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = Normalizer.normalize(name, Normalizer.Form.NFC);
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        String normalizedName = Normalizer.normalize(test, Normalizer.Form.NFC);
        return normalizedName.matches(VALIDATION_REGEX)
                && !INVISIBLE_FILLERS.contains(normalizedName.codePointAt(0));
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
