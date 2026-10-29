package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameOrPhoneMatchesKeywords_returnsTrue() {
        // Beginning, middle, and end fragments
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Ali"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        predicate = new NameContainsKeywordsPredicate(List.of("ice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        predicate = new NameContainsKeywordsPredicate(List.of("Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Exact phone number
        predicate = new NameContainsKeywordsPredicate(List.of("91234567"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").withPhone("91234567").build()));

        // One keyword matches the name and another matches the phone
        predicate = new NameContainsKeywordsPredicate(List.of("Carol", "91234567"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").withPhone("91234567").build()));

        // Supported name punctuation is matched literally
        predicate = new NameContainsKeywordsPredicate(List.of("-Jane"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice-Jane O'Neil").build()));

        predicate = new NameContainsKeywordsPredicate(List.of("O'Ne"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice-Jane O'Neil").build()));
    }

    @Test
    public void test_numericKeywordMatchesNameButOnlyPartiallyMatchesPhone_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("1234"));
        assertTrue(predicate.test(new PersonBuilder().withName("Case 1234").withPhone("12345").build()));
    }

    @Test
    public void test_nameAndPhoneDoNotMatchKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Partial phone number does not match when the name does not contain the keyword
        predicate = new NameContainsKeywordsPredicate(List.of("1234"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345").build()));

        // Regular-expression syntax is treated literally, not executed
        predicate = new NameContainsKeywordsPredicate(List.of("Ali.*"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Keywords match email and address, but do not match name or phone
        predicate = new NameContainsKeywordsPredicate(List.of("alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void constructor_mutableKeywordList_makesDefensiveCopy() {
        List<String> mutableKeywords = new ArrayList<>(List.of("Alice"));
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(mutableKeywords);
        mutableKeywords.set(0, "Bob");

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Bob Tan").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
