package seedu.address.storage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.ContactPreferences;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.RecoveryStage;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String NULL_INTERACTION_NOTE_MESSAGE = "Interaction note entry cannot be null.";
    public static final String DEBTOR_ID_OUT_OF_RANGE_MESSAGE = "Debtor ID exceeds the supported range.";
    public static final String NULL_TAG_MESSAGE = "Tag entry cannot be null.";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final Integer debtorId;
    private final String outstandingAmount;
    private final String recoveryStage;
    private final JsonContactPreferences contactPreferences;
    private final List<JsonAdaptedInteractionNote> interactionNotes = new ArrayList<>();
    private final String followUpDate;
    private final List<JsonAdaptedFollowUpEvent> followUpHistory = new ArrayList<>();
    private final String promisedPaymentDate;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /** Backwards-compatible constructor for callers specifying every field except a promised payment date. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, Integer debtorId,
            String outstandingAmount, List<JsonAdaptedInteractionNote> interactionNotes, String recoveryStage,
            String followUpDate, List<JsonAdaptedFollowUpEvent> followUpHistory, List<JsonAdaptedTag> tags,
            JsonContactPreferences contactPreferences) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes, recoveryStage,
                followUpDate, followUpHistory, tags, contactPreferences, null);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("debtorId") Integer debtorId,
            @JsonProperty("outstandingAmount") String outstandingAmount,
            @JsonProperty("interactionNotes") List<JsonAdaptedInteractionNote> interactionNotes,
            @JsonProperty("recoveryStage") String recoveryStage,
            @JsonProperty("followUpDate") String followUpDate,
            @JsonProperty("followUpHistory") List<JsonAdaptedFollowUpEvent> followUpHistory,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("contactPreferences") JsonContactPreferences contactPreferences,
            @JsonProperty("promisedPaymentDate") String promisedPaymentDate) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.debtorId = debtorId;
        this.outstandingAmount = outstandingAmount;
        this.recoveryStage = recoveryStage;
        this.followUpDate = followUpDate;
        this.promisedPaymentDate = promisedPaymentDate;
        this.contactPreferences = contactPreferences;
        if (interactionNotes != null) {
            this.interactionNotes.addAll(interactionNotes);
        }
        if (followUpHistory != null) {
            this.followUpHistory.addAll(followUpHistory);
        }
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        debtorId = source.getDebtorId() == 0 ? null : source.getDebtorId();
        outstandingAmount = source.getOutstandingAmount().toString();
        recoveryStage = source.getRecoveryStage().toString();
        followUpDate = source.getFollowUpDate().map(Object::toString).orElse(null);
        promisedPaymentDate = source.getPromisedPaymentDate().map(Object::toString).orElse(null);
        contactPreferences = new JsonContactPreferences(source.getContactPreferences());
        interactionNotes.addAll(source.getInteractionNotes().stream()
                .map(JsonAdaptedInteractionNote::new)
                .collect(Collectors.toList()));
        followUpHistory.addAll(source.getFollowUpHistory().stream()
                .map(JsonAdaptedFollowUpEvent::new).collect(Collectors.toList()));
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        Set<Tag> modelTags = convertTags();
        Name modelName = convertName();
        Phone modelPhone = convertPhone();
        Email modelEmail = convertEmail();
        Address modelAddress = convertAddress();
        List<InteractionNote> modelInteractionNotes = convertInteractionNotes();
        OutstandingAmount modelOutstandingAmount = convertOutstandingAmount();
        RecoveryStage modelRecoveryStage = convertRecoveryStage();
        Optional<LocalDate> modelFollowUpDate = parseOptionalDate(followUpDate, FollowUpEvent.MESSAGE_INVALID_DATE);
        List<FollowUpEvent> modelFollowUpHistory = convertFollowUpHistory(modelFollowUpDate);
        Optional<LocalDate> modelPromisedPaymentDate = parseOptionalDate(promisedPaymentDate,
                "Promised payment date must be a valid ISO date from year 0001 to 9999.");
        int modelDebtorId = convertDebtorId();
        ContactPreferences modelContactPreferences = convertContactPreferences();
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelDebtorId, modelOutstandingAmount,
                modelInteractionNotes, modelRecoveryStage, modelFollowUpDate, modelFollowUpHistory,
                modelPromisedPaymentDate, modelTags, modelContactPreferences);
    }

    private Set<Tag> convertTags() throws IllegalValueException {
        Set<Tag> personTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(NULL_TAG_MESSAGE);
            }
            personTags.add(tag.toModelType());
        }
        return personTags;
    }

    private Name convertName() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(name);
    }

    private Phone convertPhone() throws IllegalValueException {
        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(phone);
    }

    private Email convertEmail() throws IllegalValueException {
        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(email);
    }

    private Address convertAddress() throws IllegalValueException {
        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(address);
    }

    private List<InteractionNote> convertInteractionNotes() throws IllegalValueException {
        List<InteractionNote> modelInteractionNotes = new ArrayList<>();
        for (JsonAdaptedInteractionNote note : interactionNotes) {
            if (note == null) {
                throw new IllegalValueException(NULL_INTERACTION_NOTE_MESSAGE);
            }
            modelInteractionNotes.add(note.toModelType());
        }
        return modelInteractionNotes;
    }

    private OutstandingAmount convertOutstandingAmount() throws IllegalValueException {
        try {
            return outstandingAmount == null || outstandingAmount.equals("0.00")
                    ? new OutstandingAmount() : new OutstandingAmount(outstandingAmount);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(OutstandingAmount.MESSAGE_CONSTRAINTS);
        }
    }

    private RecoveryStage convertRecoveryStage() throws IllegalValueException {
        try {
            return recoveryStage == null ? RecoveryStage.NEW : RecoveryStage.parse(recoveryStage);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(RecoveryStage.MESSAGE_CONSTRAINTS);
        }
    }

    private static Optional<LocalDate> parseOptionalDate(String storedDate, String errorMessage)
            throws IllegalValueException {
        if (storedDate == null) {
            return Optional.empty();
        }
        Optional<LocalDate> parsedDate = FollowUpEvent.tryParseDate(storedDate);
        if (parsedDate.isEmpty()) {
            throw new IllegalValueException(errorMessage);
        }
        return parsedDate;
    }

    private List<FollowUpEvent> convertFollowUpHistory(Optional<LocalDate> modelFollowUpDate)
            throws IllegalValueException {
        List<FollowUpEvent> modelFollowUpHistory = new ArrayList<>();
        Optional<LocalDate> eventCurrentDate = Optional.empty();
        for (JsonAdaptedFollowUpEvent event : followUpHistory) {
            if (event == null) {
                throw new IllegalValueException("Follow-up history entry cannot be null.");
            }
            FollowUpEvent modelEvent = event.toModelType(eventCurrentDate);
            // A record may carry a pending date from before history was kept, so the first event
            // starts from whichever date it says it replaced.
            boolean isFirstEvent = modelFollowUpHistory.isEmpty();
            if (!isFirstEvent && !modelEvent.getPreviousDate().equals(eventCurrentDate)) {
                throw new IllegalValueException("Follow-up history contains a discontinuous date transition.");
            }
            modelFollowUpHistory.add(modelEvent);
            eventCurrentDate = modelEvent.getNewDate();
        }
        if (!modelFollowUpHistory.isEmpty() && !eventCurrentDate.equals(modelFollowUpDate)) {
            throw new IllegalValueException("Follow-up history does not match the current follow-up date.");
        }
        return modelFollowUpHistory;
    }

    private int convertDebtorId() throws IllegalValueException {
        if (debtorId != null && debtorId < 0) {
            throw new IllegalValueException(Person.MESSAGE_INVALID_DEBTOR_ID);
        }
        if (debtorId != null && debtorId == Integer.MAX_VALUE) {
            throw new IllegalValueException(DEBTOR_ID_OUT_OF_RANGE_MESSAGE);
        }
        return debtorId == null ? 0 : debtorId;
    }

    private ContactPreferences convertContactPreferences() throws IllegalValueException {
        return contactPreferences == null ? new ContactPreferences(null, null) : contactPreferences.toModelType();
    }
}
