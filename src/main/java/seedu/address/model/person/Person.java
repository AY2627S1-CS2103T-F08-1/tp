package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_INVALID_DEBTOR_ID = "Debtor ID cannot be negative.";

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final int debtorId;
    private final OutstandingAmount outstandingAmount;
    private final List<InteractionNote> interactionNotes;
    private final RecoveryStage recoveryStage;
    private final Optional<LocalDate> followUpDate;
    private final List<FollowUpEvent> followUpHistory;
    private final Optional<LocalDate> promisedPaymentDate;
    private final ContactPreferences contactPreferences;
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
        this(name, phone, email, address, debtorId, outstandingAmount, List.of(), RecoveryStage.NEW, tags);
    }

    /** Creates a person with all debtor fields, including interaction notes. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes, Set<Tag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, RecoveryStage.NEW, tags);
    }

    /** Creates a person with all debtor fields including a recovery stage. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes,
            RecoveryStage recoveryStage, Set<Tag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, recoveryStage,
                Optional.empty(), List.of(), tags, new ContactPreferences(null, null));
    }

    /** Creates a person with every debtor field, including follow-up state and history. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes,
            Optional<LocalDate> followUpDate, List<FollowUpEvent> followUpHistory, Set<Tag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, RecoveryStage.NEW,
                followUpDate, followUpHistory, tags, new ContactPreferences(null, null));
    }

    /** Creates a person with contact preferences. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes, Set<Tag> tags,
            ContactPreferences contactPreferences) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, RecoveryStage.NEW,
                Optional.empty(), List.of(), tags, contactPreferences);
    }

    /** Creates a person with every debtor field and no promised payment date. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes,
            RecoveryStage recoveryStage, Optional<LocalDate> followUpDate,
            List<FollowUpEvent> followUpHistory, Set<Tag> tags,
            ContactPreferences contactPreferences) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, recoveryStage,
                followUpDate, followUpHistory, Optional.empty(), tags, contactPreferences);
    }

    /** Creates a person with every debtor field, including independent follow-up and promised-payment dates. */
    public Person(Name name, Phone phone, Email email, Address address, int debtorId,
            OutstandingAmount outstandingAmount, List<InteractionNote> interactionNotes,
            RecoveryStage recoveryStage, Optional<LocalDate> followUpDate,
            List<FollowUpEvent> followUpHistory, Optional<LocalDate> promisedPaymentDate, Set<Tag> tags,
            ContactPreferences contactPreferences) {
        requireAllNonNull(name, phone, email, address, outstandingAmount, interactionNotes,
                recoveryStage, tags, contactPreferences);
        requireAllNonNull(followUpDate, followUpHistory, promisedPaymentDate);
        if (debtorId < 0) {
            throw new IllegalArgumentException(MESSAGE_INVALID_DEBTOR_ID);
        }
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.debtorId = debtorId;
        this.outstandingAmount = outstandingAmount;
        this.interactionNotes = List.copyOf(interactionNotes);
        this.recoveryStage = recoveryStage;
        this.followUpDate = followUpDate;
        this.followUpHistory = List.copyOf(followUpHistory);
        this.promisedPaymentDate = promisedPaymentDate;
        this.contactPreferences = contactPreferences;
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
        return new Person(name, phone, email, address, newDebtorId, outstandingAmount,
                interactionNotes, recoveryStage, followUpDate, followUpHistory,
                promisedPaymentDate, tags, contactPreferences);
    }

    public ContactPreferences getContactPreferences() {
        return contactPreferences;
    }

    /** Returns a copy with updated preferences. */
    public Person withContactPreferences(ContactPreferences preferences) {
        return new Person(name, phone, email, address, debtorId, outstandingAmount,
                interactionNotes, recoveryStage, followUpDate, followUpHistory, promisedPaymentDate, tags, preferences);
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

    /** Returns this debtor's manually selected recovery stage. */
    public RecoveryStage getRecoveryStage() {
        return recoveryStage;
    }

    /** Returns a copy with the given recovery stage. */
    public Person withRecoveryStage(RecoveryStage updatedStage) {
        requireNonNull(updatedStage);
        return new Person(name, phone, email, address, debtorId, outstandingAmount,
                interactionNotes, updatedStage, followUpDate, followUpHistory,
                promisedPaymentDate, tags, contactPreferences);
    }

    /** Returns this debtor's pending follow-up date, if one is scheduled. */
    public Optional<LocalDate> getFollowUpDate() {
        return followUpDate;
    }

    /** Returns follow-up changes in insertion order. */
    public List<FollowUpEvent> getFollowUpHistory() {
        return followUpHistory;
    }

    /** Returns the date on which the debtor promised to pay, if one is recorded. */
    public Optional<LocalDate> getPromisedPaymentDate() {
        return promisedPaymentDate;
    }

    /** Returns a copy with the promised-payment date set or cleared. */
    public Person withPromisedPaymentDate(Optional<LocalDate> date) {
        requireNonNull(date);
        return new Person(name, phone, email, address, debtorId, outstandingAmount, interactionNotes,
                recoveryStage, followUpDate, followUpHistory, date, tags, contactPreferences);
    }

    /** Returns a copy with the supplied pending date and one appended history event. */
    public Person withFollowUpChange(Optional<LocalDate> date, FollowUpEvent event) {
        requireNonNull(date);
        requireNonNull(event);
        List<FollowUpEvent> updatedHistory = new ArrayList<>(followUpHistory);
        updatedHistory.add(event);
        return new Person(name, phone, email, address, debtorId, outstandingAmount, interactionNotes,
                recoveryStage, date, updatedHistory, promisedPaymentDate, tags, contactPreferences);
    }

    /**
     * Returns a copy of this person with {@code note} appended to the interaction history.
     */
    public Person withAddedInteractionNote(InteractionNote note) {
        requireNonNull(note);
        List<InteractionNote> updatedNotes = new ArrayList<>(interactionNotes);
        updatedNotes.add(note);
        return new Person(name, phone, email, address, debtorId, outstandingAmount, updatedNotes,
                recoveryStage, followUpDate, followUpHistory, promisedPaymentDate, tags, contactPreferences);
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
                && recoveryStage.equals(otherPerson.recoveryStage)
                && followUpDate.equals(otherPerson.followUpDate)
                && followUpHistory.equals(otherPerson.followUpHistory)
                && promisedPaymentDate.equals(otherPerson.promisedPaymentDate)
                && contactPreferences.equals(otherPerson.contactPreferences)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, outstandingAmount, interactionNotes,
                recoveryStage, followUpDate, followUpHistory, promisedPaymentDate, tags, contactPreferences);
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
                .add("recoveryStage", recoveryStage)
                .add("tags", tags)
                .toString();
    }

}
