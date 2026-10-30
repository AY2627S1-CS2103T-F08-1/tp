package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    private static final String SPECIAL_CHARACTERS = "+_.-'";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters, excluding "
            + "the parentheses, (" + SPECIAL_CHARACTERS + "). The local-part may not start or end with any special "
            + "characters.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - have each domain label consist of alphanumeric characters, separated only by hyphens, if any.";
    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        int atIndex = test.indexOf('@');
        return atIndex > 0 && isValidLocalPart(test, atIndex) && isValidDomain(test, atIndex + 1);
    }

    /** Checks the local part iteratively so long accepted values cannot exhaust the regex stack. */
    private static boolean isValidLocalPart(String email, int endIndex) {
        boolean previousIsAlphanumeric = false;
        for (int i = 0; i < endIndex; i++) {
            char character = email.charAt(i);
            if (isAsciiAlphanumeric(character)) {
                previousIsAlphanumeric = true;
            } else if (SPECIAL_CHARACTERS.indexOf(character) >= 0 && previousIsAlphanumeric) {
                previousIsAlphanumeric = false;
            } else {
                return false;
            }
        }
        return previousIsAlphanumeric;
    }

    /** Checks domain labels, allowing internal hyphen runs such as the ASCII Punycode prefix. */
    private static boolean isValidDomain(String email, int startIndex) {
        int labelStart = startIndex;
        for (int i = startIndex; i < email.length(); i++) {
            if (email.charAt(i) == '.') {
                if (i == labelStart || !isAsciiAlphanumeric(email.charAt(labelStart))
                        || !isAsciiAlphanumeric(email.charAt(i - 1))) {
                    return false;
                }
                labelStart = i + 1;
            } else if (!isAsciiAlphanumeric(email.charAt(i)) && email.charAt(i) != '-') {
                return false;
            }
        }
        return email.length() - labelStart >= 2 && isAsciiAlphanumeric(email.charAt(labelStart))
                && isAsciiAlphanumeric(email.charAt(email.length() - 1));
    }

    /** Matches the ASCII letters and digits accepted by the command syntax. */
    private static boolean isAsciiAlphanumeric(char character) {
        return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z'
                || character >= '0' && character <= '9';
    }

    @Override
    public String toString() {
        return value;
    }

    /** Compares contact identity while preserving local-part case and the original display spelling. */
    public boolean isSameAddress(Email other) {
        requireNonNull(other);
        int separator = value.indexOf('@');
        int otherSeparator = other.value.indexOf('@');
        return value.substring(0, separator).equals(other.value.substring(0, otherSeparator))
                && value.substring(separator + 1).equalsIgnoreCase(other.value.substring(otherSeparator + 1));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
