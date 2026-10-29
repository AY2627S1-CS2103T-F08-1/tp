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
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUpEvent;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
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
    private final List<JsonAdaptedInteractionNote> interactionNotes = new ArrayList<>();
    private final String followUpDate;
    private final List<JsonAdaptedFollowUpEvent> followUpHistory = new ArrayList<>();
    private final String promisedPaymentDate;
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
            @JsonProperty("followUpDate") String followUpDate,
            @JsonProperty("followUpHistory") List<JsonAdaptedFollowUpEvent> followUpHistory,
            @JsonProperty("promisedPaymentDate") String promisedPaymentDate,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.debtorId = debtorId;
        this.outstandingAmount = outstandingAmount;
        this.followUpDate = followUpDate;
        this.promisedPaymentDate = promisedPaymentDate;
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
     * Backwards-compatible constructor for data and tests that do not specify an amount.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, null, null, null, null, null, null, tags);
    }

    /** Backwards-compatible constructor for callers specifying debtor fields without follow-up data. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, Integer debtorId,
            String outstandingAmount, List<JsonAdaptedInteractionNote> interactionNotes,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes,
                null, null, null, tags);
    }

    /** Backwards-compatible constructor for callers specifying follow-up data without a promised payment date. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, Integer debtorId,
            String outstandingAmount, List<JsonAdaptedInteractionNote> interactionNotes,
            String followUpDate, List<JsonAdaptedFollowUpEvent> followUpHistory, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, debtorId, outstandingAmount, interactionNotes,
                followUpDate, followUpHistory, null, tags);
    }

    /**
     * Backwards-compatible constructor for callers specifying an amount but no remark.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            String outstandingAmount, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, null, outstandingAmount, null, null, null, null, tags);
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
        followUpDate = source.getFollowUpDate().map(Object::toString).orElse(null);
        promisedPaymentDate = source.getPromisedPaymentDate().map(Object::toString).orElse(null);
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
        final Optional<LocalDate> modelFollowUpDate;
        try {
            modelFollowUpDate = followUpDate == null ? Optional.empty() : Optional.of(LocalDate.parse(followUpDate));
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException("Follow-up date must be a valid ISO local date.");
        }
        final List<FollowUpEvent> modelFollowUpHistory = new ArrayList<>();
        for (JsonAdaptedFollowUpEvent event : followUpHistory) {
            if (event == null) {
                throw new IllegalValueException("Follow-up history entry cannot be null.");
            }
            modelFollowUpHistory.add(event.toModelType());
        }
        final Optional<LocalDate> modelPromisedPaymentDate;
        try {
            if (promisedPaymentDate != null && !promisedPaymentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new DateTimeParseException("Invalid promised payment date", promisedPaymentDate, 0);
            }
            modelPromisedPaymentDate = promisedPaymentDate == null
                    ? Optional.empty() : Optional.of(LocalDate.parse(promisedPaymentDate));
            if (modelPromisedPaymentDate.isPresent() && modelPromisedPaymentDate.get().getYear() < 1) {
                throw new DateTimeParseException("Invalid promised payment year", promisedPaymentDate, 0);
            }
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException("Promised payment date must be a valid ISO date from year 0001 to 9999.");
        }
        if (debtorId != null && debtorId < 0) {
            throw new IllegalValueException(Person.MESSAGE_INVALID_DEBTOR_ID);
        }
        if (debtorId != null && debtorId == Integer.MAX_VALUE) {
            throw new IllegalValueException(DEBTOR_ID_OUT_OF_RANGE_MESSAGE);
        }
        return new Person(modelName, modelPhone, modelEmail, modelAddress,
                debtorId == null ? 0 : debtorId, modelOutstandingAmount, modelInteractionNotes,
                modelFollowUpDate, modelFollowUpHistory, modelPromisedPaymentDate, modelTags);
    }

}
