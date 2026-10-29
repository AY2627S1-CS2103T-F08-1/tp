package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.DEBTOR_ID_OUT_OF_RANGE_MESSAGE;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.storage.JsonAdaptedPerson.NULL_TAG_MESSAGE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.RecoveryStage;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final int INVALID_DEBTOR_ID = -1;

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_OUTSTANDING_AMOUNT = "1,250.00";
    private static final String INVALID_OUTSTANDING_AMOUNT = "-1.00";
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_validOutstandingAmount_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .build();

        assertEquals(VALID_OUTSTANDING_AMOUNT, person.toModelType().getOutstandingAmount().toString());
    }

    @Test
    public void toModelType_nonzeroOutstandingAmountRoundTrip_returnsPerson() throws Exception {
        Person debtor = new PersonBuilder(BENSON).withOutstandingAmount(VALID_OUTSTANDING_AMOUNT).build();

        assertEquals(debtor, new JsonAdaptedPerson(debtor).toModelType());
    }

    @Test
    public void toModelType_defaultOutstandingAmountRoundTrip_returnsPerson() throws Exception {
        assertEquals(BENSON, new JsonAdaptedPerson(BENSON).toModelType());
    }

    @Test
    public void toModelType_interactionNotesRoundTrip_returnsPerson() throws Exception {
        InteractionNote note = InteractionNote.fromStorage("2026-10-09T14:30:00",
                "Called debtor, promised payment.");
        Person debtor = new PersonBuilder(BENSON).withInteractionNotes(List.of(note)).build();

        assertEquals(debtor, new JsonAdaptedPerson(debtor).toModelType());
    }

    @Test
    public void toModelType_nonDefaultRecoveryStageRoundTrip_returnsPerson() throws Exception {
        Person debtor = new Person(BENSON.getName(), BENSON.getPhone(), BENSON.getEmail(), BENSON.getAddress(),
                BENSON.getDebtorId(), BENSON.getOutstandingAmount(), BENSON.getInteractionNotes(),
                RecoveryStage.CONTACTED, BENSON.getTags());

        assertEquals(debtor, new JsonAdaptedPerson(debtor).toModelType());
    }

    @Test
    public void toModelType_missingOrNullRecoveryStage_defaultsToNew() throws Exception {
        JsonAdaptedPerson missingStage = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .build();

        assertEquals(RecoveryStage.NEW, missingStage.toModelType().getRecoveryStage());
    }

    @Test
    public void toModelType_invalidRecoveryStage_throwsIllegalValueException() {
        JsonAdaptedPerson invalidStage = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .withRecoveryStage("unknown")
                .build();

        assertThrows(IllegalValueException.class, RecoveryStage.MESSAGE_CONSTRAINTS, invalidStage::toModelType);
    }

    @Test
    public void toModelType_nullInteractionNote_throwsIllegalValueException() {
        List<JsonAdaptedInteractionNote> notes = new ArrayList<>();
        notes.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .withInteractionNotes(notes)
                .build();

        assertThrows(IllegalValueException.class, JsonAdaptedPerson.NULL_INTERACTION_NOTE_MESSAGE,
                person::toModelType);
    }

    @Test
    public void toModelType_nullTag_throwsIllegalValueException() {
        List<JsonAdaptedTag> tags = new ArrayList<>(VALID_TAGS);
        tags.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, tags)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .build();

        assertThrows(IllegalValueException.class, NULL_TAG_MESSAGE, person::toModelType);
    }

    @Test
    public void toModelType_invalidOutstandingAmount_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withOutstandingAmount(INVALID_OUTSTANDING_AMOUNT)
                .build();

        assertThrows(IllegalValueException.class, OutstandingAmount.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_negativeDebtorId_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withDebtorId(INVALID_DEBTOR_ID)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .build();

        assertThrows(IllegalValueException.class, Person.MESSAGE_INVALID_DEBTOR_ID, person::toModelType);
    }

    @Test
    public void toModelType_maximumDebtorId_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS)
                .withDebtorId(Integer.MAX_VALUE)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT)
                .build();

        assertThrows(IllegalValueException.class, DEBTOR_ID_OUT_OF_RANGE_MESSAGE, person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_TAGS).build();
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_TAGS).build();
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person = new JsonAdaptedPersonBuilder(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, invalidTags).build();
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
