package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Creates deterministic comparators for the displayed debtor worklist.
 *
 * <p>Sorting is a view concern: these comparators must be applied to the model's sorted view instead of the
 * underlying address-book list. Name and amount sorts always use the persistent debtor ID as their final tie-breaker,
 * so the same records appear in a predictable order after every refresh.
 */
public final class PersonSort {

    /** Fields by which the displayed debtor worklist can be sorted. */
    public enum Field {
        ID("id"),
        NAME("name"),
        AMOUNT("amount");

        private final String token;

        Field(String token) {
            this.token = token;
        }

        /** Returns the command-line token for this field. */
        public String getToken() {
            return token;
        }

        /**
         * Parses a command-line token without depending on the computer's default locale.
         */
        public static Optional<Field> fromToken(String token) {
            requireNonNull(token);
            String normalizedToken = token.toLowerCase(Locale.ROOT);
            for (Field field : values()) {
                if (field.token.equals(normalizedToken)) {
                    return Optional.of(field);
                }
            }
            return Optional.empty();
        }
    }

    /** Directions supported by the displayed debtor worklist. */
    public enum Direction {
        ASCENDING("asc", "ascending"),
        DESCENDING("desc", "descending");

        private final String token;
        private final String description;

        Direction(String token, String description) {
            this.token = token;
            this.description = description;
        }

        /** Returns the command-line token for this direction. */
        public String getToken() {
            return token;
        }

        /** Returns a user-facing description of this direction. */
        public String getDescription() {
            return description;
        }

        /**
         * Parses a command-line token without depending on the computer's default locale.
         */
        public static Optional<Direction> fromToken(String token) {
            requireNonNull(token);
            String normalizedToken = token.toLowerCase(Locale.ROOT);
            for (Direction direction : values()) {
                if (direction.token.equals(normalizedToken)) {
                    return Optional.of(direction);
                }
            }
            return Optional.empty();
        }
    }

    /** Default order used when a command resets the worklist view. */
    public static final Comparator<Person> DEFAULT_COMPARATOR = createComparator(Field.ID, Direction.ASCENDING);

    private PersonSort() {
    }

    /**
     * Returns a comparator for {@code field} and {@code direction}.
     *
     * <p>For name and amount, only the primary comparison is reversed. Persistent debtor ID remains an ascending
     * final tie-breaker even when the primary field is descending. Debtor IDs are unique, so an ID sort needs no
     * additional tie-breaker.
     */
    public static Comparator<Person> createComparator(Field field, Direction direction) {
        requireNonNull(field);
        requireNonNull(direction);

        Comparator<Person> primaryComparator = getAscendingPrimaryComparator(field);
        if (direction == Direction.DESCENDING) {
            primaryComparator = primaryComparator.reversed();
        }

        return field == Field.ID
                ? primaryComparator
                : primaryComparator.thenComparingInt(Person::getDebtorId);
    }

    private static Comparator<Person> getAscendingPrimaryComparator(Field field) {
        return switch (field) {
            case ID -> Comparator.comparingInt(Person::getDebtorId);
            case NAME -> Comparator.comparing(person -> normalizeName(person.getName()));
            case AMOUNT -> Comparator.comparing(person -> person.getOutstandingAmount().getValue());
        };
    }

    private static String normalizeName(Name name) {
        return name.fullName.toLowerCase(Locale.ROOT);
    }
}
