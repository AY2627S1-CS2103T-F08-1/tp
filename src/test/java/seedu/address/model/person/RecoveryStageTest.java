package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RecoveryStageTest {
    @Test
    void parse_supportedValuesAndCase() {
        assertEquals(RecoveryStage.NEW, RecoveryStage.parse("new"));
        assertEquals(RecoveryStage.CONTACTED, RecoveryStage.parse("CONTACTED"));
        assertEquals(RecoveryStage.NEGOTIATING, RecoveryStage.parse("negotiating"));
        assertEquals(RecoveryStage.DISPUTED, RecoveryStage.parse("disputed"));
        assertEquals(RecoveryStage.BROKEN_PROMISE, RecoveryStage.parse("broken-promise"));
        assertEquals(RecoveryStage.SETTLED, RecoveryStage.parse("settled"));
    }

    @Test
    void parse_unknownValueRejected() {
        assertThrows(IllegalArgumentException.class, () -> RecoveryStage.parse("broken promise"));
        assertThrows(IllegalArgumentException.class, () -> RecoveryStage.parse(""));
    }
}
