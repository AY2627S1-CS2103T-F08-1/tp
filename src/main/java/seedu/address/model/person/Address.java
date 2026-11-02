package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's address in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address {

    public static final String MESSAGE_CONSTRAINTS = "Addresses can take any values, and should not be blank";

    /*
     * The first character of the address must not be Unicode whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "(?U)[^\\s].*";

    public final String value;

    /**
     * Constructs an {@code Address}.
     *
     * @param address A valid address.
     */
    public Address(String address) {
        requireNonNull(address);
        checkArgument(isValidAddress(address), MESSAGE_CONSTRAINTS);
        value = address;
    }

    /**
     * Returns true if a given string is a valid address.
     */
    public static boolean isValidAddress(String test) {
        requireNonNull(test);
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if a new address has valid syntax and at least one visible base character.
     * This is stricter than {@link #isValidAddress(String)} so legacy stored addresses remain loadable.
     */
    public static boolean isValidUserInput(String test) {
        requireNonNull(test);
        return isValidAddress(test) && test.codePoints().anyMatch(Address::isVisibleBaseCharacter);
    }

    private static boolean isVisibleBaseCharacter(int character) {
        int type = Character.getType(character);
        return !Character.isWhitespace(character) && !Character.isSpaceChar(character)
                && type != Character.CONTROL && type != Character.FORMAT
                && type != Character.NON_SPACING_MARK && type != Character.COMBINING_SPACING_MARK
                && type != Character.ENCLOSING_MARK && character != '\u115f' && character != '\u1160'
                && character != '\u3164' && character != '\uffa0';
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Address otherAddress)) {
            return false;
        }

        return value.equals(otherAddress.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
