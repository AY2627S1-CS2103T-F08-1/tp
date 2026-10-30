package seedu.address.model.person;

import java.util.Locale;

/** The manually assigned stage of a debtor's recovery case. */
public enum RecoveryStage {
    NEW("new"), CONTACTED("contacted"), NEGOTIATING("negotiating"),
    DISPUTED("disputed"), BROKEN_PROMISE("broken-promise"), SETTLED("settled");

    public static final String MESSAGE_CONSTRAINTS = "Stage must be one of: new, contacted, negotiating, "
            + "disputed, broken-promise, settled.";

    private final String value;

    RecoveryStage(String value) {
        this.value = value;
    }

    /** Parses a stage without depending on the computer's locale. */
    public static RecoveryStage parse(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        for (RecoveryStage stage : values()) {
            if (stage.value.equals(normalized)) {
                return stage;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return value;
    }
}
