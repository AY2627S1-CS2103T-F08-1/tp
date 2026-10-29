package seedu.address.storage;

import java.util.List;

/** Test builder that keeps optional stored-person fields explicit and avoids null-padded constructor calls. */
class JsonAdaptedPersonBuilder {
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags;
    private Integer debtorId;
    private String outstandingAmount;
    private List<JsonAdaptedInteractionNote> interactionNotes;
    private String recoveryStage;
    private String followUpDate;
    private List<JsonAdaptedFollowUpEvent> followUpHistory;
    private JsonContactPreferences contactPreferences;

    JsonAdaptedPersonBuilder(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags = tags;
    }

    JsonAdaptedPersonBuilder withDebtorId(Integer debtorId) {
        this.debtorId = debtorId;
        return this;
    }

    JsonAdaptedPersonBuilder withOutstandingAmount(String outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
        return this;
    }

    JsonAdaptedPersonBuilder withInteractionNotes(List<JsonAdaptedInteractionNote> interactionNotes) {
        this.interactionNotes = interactionNotes;
        return this;
    }

    JsonAdaptedPersonBuilder withRecoveryStage(String recoveryStage) {
        this.recoveryStage = recoveryStage;
        return this;
    }

    JsonAdaptedPersonBuilder withFollowUp(String followUpDate, List<JsonAdaptedFollowUpEvent> followUpHistory) {
        this.followUpDate = followUpDate;
        this.followUpHistory = followUpHistory;
        return this;
    }

    JsonAdaptedPersonBuilder withContactPreferences(JsonContactPreferences contactPreferences) {
        this.contactPreferences = contactPreferences;
        return this;
    }

    JsonAdaptedPerson build() {
        return new JsonAdaptedPerson(name, phone, email, address, debtorId, outstandingAmount,
                interactionNotes, recoveryStage, followUpDate, followUpHistory, tags, contactPreferences);
    }
}
