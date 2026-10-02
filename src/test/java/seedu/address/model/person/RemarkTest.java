package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void equals_sameValue_returnsTrue() {
        assertEquals(new Remark(""), new Remark(""));
        assertEquals(new Remark("  anything!  "), new Remark("  anything!  "));
    }

    @Test
    public void equals_differentValue_returnsFalse() {
        assertNotEquals(new Remark("Likes baseball"), new Remark("Likes football"));
    }

    @Test
    public void toStringMethod_returnsValue() {
        assertEquals("", new Remark("").toString());
        assertEquals("  anything!  ", new Remark("  anything!  ").toString());
    }
}
