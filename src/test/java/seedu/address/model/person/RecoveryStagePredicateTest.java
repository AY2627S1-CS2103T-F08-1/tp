package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RecoveryStagePredicateTest {
    @Test
    public void test_stageMatches_independentOfOutstandingAmount() {
        for (RecoveryStage requestedStage : RecoveryStage.values()) {
            RecoveryStagePredicate predicate = new RecoveryStagePredicate(requestedStage);
            for (RecoveryStage actualStage : RecoveryStage.values()) {
                Person debtor = new PersonBuilder().withOutstandingAmount("0").build()
                        .withRecoveryStage(actualStage);
                assertEquals(requestedStage == actualStage, predicate.test(debtor));
                Person debtorWithBalance = new PersonBuilder().withOutstandingAmount("123.45").build()
                        .withRecoveryStage(actualStage);
                assertEquals(requestedStage == actualStage, predicate.test(debtorWithBalance));
            }
        }
    }

    @Test
    public void equals_sameStage_hasSameValueAndHash() {
        RecoveryStagePredicate predicate = new RecoveryStagePredicate(RecoveryStage.NEW);
        RecoveryStagePredicate sameStage = new RecoveryStagePredicate(RecoveryStage.NEW);
        assertEquals(predicate, predicate);
        assertEquals(predicate, sameStage);
        assertEquals(predicate.hashCode(), sameStage.hashCode());
        assertNotEquals(predicate, new RecoveryStagePredicate(RecoveryStage.SETTLED));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "new");
        assertEquals(RecoveryStage.NEW, predicate.getStage());
        assertEquals(RecoveryStagePredicate.class.getCanonicalName() + "{stage=new}", predicate.toString());
    }

    @Test
    public void nullInputs_rejectProgrammingErrors() {
        assertThrows(NullPointerException.class, () -> new RecoveryStagePredicate(null));
        assertThrows(NullPointerException.class, () -> new RecoveryStagePredicate(RecoveryStage.NEW).test(null));
    }
}
