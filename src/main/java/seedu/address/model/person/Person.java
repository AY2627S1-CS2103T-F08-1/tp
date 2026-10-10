package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final int debtorId;
    private final OutstandingAmount outstandingAmount;
    private final List<InteractionNote> interactionNotes;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, 0, new OutstandingAmount(), tags);
    }

    /**


     * Creates a person with the supplied outstanding amount.


     */
    public Person(Name name, Phone phone, Email email, Address address,
            OutstandingAmount outstandingAmount, Set<Tag> tags) {
        this(name, phone, email, address, 0, outstandingAmount, tags);
    }

    /**


     * Creates a person with a persistent debtor ID and outstanding amount.


     */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, Set<Tag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, List.of(), tags);
    }

    /**


     * Creates a person with all debtor fields, including interaction notes.


     */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, outstandingAmount, interactionNotes, tags);
        if (debtorId < 0) {
            throw new IllegalArgumentException("Debtor ID cannot be negative.");
        }
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.debtorId = debtorId;
        this.outstandingAmount = outstandingAmount;
        this.interactionNotes = List.copyOf(interactionNotes);
        this.tags.addAll(tags);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**


     * Returns this person's persistent debtor ID, or 0 before the person is added.


     */
    public int getDebtorId() {
        return debtorId;
    }

    /**


     * Returns a copy of this person with the supplied persistent debtor ID.


     */
    public Person withDebtorId(int newDebtorId) {
        return new Person(name, phone, email, address, newDebtorId, outstandingAmount, interactionNotes, tags);
    }

    public OutstandingAmount getOutstandingAmount() {
        return outstandingAmount;
    }

    /**


     * Returns the chronological interaction history of this debtor.


     */
    public List<InteractionNote> getInteractionNotes() {
        return interactionNotes;
    }

    /**


     * Returns a copy of this person with {@code note} appended to the interaction history.


     */
    public Person withAddedInteractionNote(InteractionNote note) {
        requireNonNull(note);
        List<InteractionNote> updatedNotes = new ArrayList<>(interactionNotes);
        updatedNotes.add(note);
        return new Person(name, phone, email, address, debtorId, outstandingAmount, updatedNotes, tags);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same phone number or email address.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && (otherPerson.getPhone().equals(getPhone()) || otherPerson.getEmail().equals(getEmail()));
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && outstandingAmount.equals(otherPerson.outstandingAmount)
                && interactionNotes.equals(otherPerson.interactionNotes)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, outstandingAmount, interactionNotes, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("debtorId", debtorId)
                .add("outstandingAmount", outstandingAmount)
                .add("tags", tags)
                .toString();
    }

}
