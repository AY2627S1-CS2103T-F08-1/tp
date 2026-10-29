package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

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
            @JsonProperty("contactPreferences") JsonContactPreferences contactPreferences) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.debtorId = debtorId;
        this.outstandingAmount = outstandingAmount;
        this.recoveryStage = recoveryStage;
        this.followUpDate = followUpDate;
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
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(NULL_TAG_MESSAGE);
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        final List<InteractionNote> modelInteractionNotes = new ArrayList<>();
        for (JsonAdaptedInteractionNote note : interactionNotes) {
            if (note == null) {
                throw new IllegalValueException(NULL_INTERACTION_NOTE_MESSAGE);
            }
            modelInteractionNotes.add(note.toModelType());
        }
        final OutstandingAmount modelOutstandingAmount;
        try {
            modelOutstandingAmount = outstandingAmount == null || outstandingAmount.equals("0.00")
                    ? new OutstandingAmount() : new OutstandingAmount(outstandingAmount);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(OutstandingAmount.MESSAGE_CONSTRAINTS);
        }
        final RecoveryStage modelRecoveryStage;
        try {
            modelRecoveryStage = recoveryStage == null ? RecoveryStage.NEW : RecoveryStage.parse(recoveryStage);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(RecoveryStage.MESSAGE_CONSTRAINTS);
        }
        final Optional<LocalDate> modelFollowUpDate = followUpDate == null
                ? Optional.empty() : tryParseFollowUpDate(followUpDate);
        if (followUpDate != null && modelFollowUpDate.isEmpty()) {
            throw new IllegalValueException(FollowUpEvent.MESSAGE_INVALID_DATE);
        }
        final List<FollowUpEvent> modelFollowUpHistory = new ArrayList<>();
        Optional<LocalDate> eventCurrentDate = Optional.empty();
        for (JsonAdaptedFollowUpEvent event : followUpHistory) {
            if (event == null) {
                throw new IllegalValueException("Follow-up history entry cannot be null.");
            }
            FollowUpEvent modelEvent = event.toModelType(eventCurrentDate);
            if (!modelEvent.getPreviousDate().equals(eventCurrentDate)) {
                throw new IllegalValueException("Follow-up history contains a discontinuous date transition.");
            }
            modelFollowUpHistory.add(modelEvent);
            eventCurrentDate = modelEvent.getNewDate();
        }
        if (!modelFollowUpHistory.isEmpty() && !eventCurrentDate.equals(modelFollowUpDate)) {
            throw new IllegalValueException("Follow-up history does not match the current follow-up date.");
        }
        if (debtorId != null && debtorId < 0) {
            throw new IllegalValueException(Person.MESSAGE_INVALID_DEBTOR_ID);
        }
        if (debtorId != null && debtorId == Integer.MAX_VALUE) {
            throw new IllegalValueException(DEBTOR_ID_OUT_OF_RANGE_MESSAGE);
        }
        return new Person(modelName, modelPhone, modelEmail, modelAddress,
                debtorId == null ? 0 : debtorId, modelOutstandingAmount, modelInteractionNotes,
                modelRecoveryStage, modelFollowUpDate, modelFollowUpHistory, modelTags,
                contactPreferences == null ? new ContactPreferences(null, null) : contactPreferences.toModelType());
    }

    private Optional<LocalDate> tryParseFollowUpDate(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return Optional.empty();
        }
        try {
            LocalDate parsed = LocalDate.parse(value);
            return parsed.getYear() < 1 ? Optional.empty() : Optional.of(parsed);
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }
}
