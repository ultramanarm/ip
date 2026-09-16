package glennon.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests shared description validation and task detail comparisons through
 * concrete task types.
 */
class TaskTest {
    @Test
    void constructor_nullOrBlankDescriptions_throwsHelpfulException() {
        String[] invalidDescriptions = {null, "", " ", "\t", " \t\n ", "\u2003"};

        for (String description : invalidDescriptions) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new Todo(description));
            assertEquals("Mission descriptions must not be blank.", exception.getMessage());
        }
    }

    @Test
    void constructor_surroundingWhitespace_stripsEdgesAndPreservesInternalText() {
        Todo todo = new Todo(" \t\u2003Café  reading\t@図書館 / notes: \"hi\" 😊\u2003\t ");

        assertEquals("Café  reading\t@図書館 / notes: \"hi\" 😊", todo.getDescription());
        assertEquals("[T][ ] Café  reading\t@図書館 / notes: \"hi\" 😊", todo.toString());
    }

    @Test
    void constructor_lineBreaksOrControlCharacters_rejectsUnsafeDescriptions() {
        List<Integer> invalidCharacters = List.of(0, 7, 8, 10, 11, 12, 13, 27, 127, 133, 0x2028, 0x2029);

        for (int character : invalidCharacters) {
            String description = "before" + new String(Character.toChars(character)) + "after";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new Todo(description));
            assertEquals("Mission descriptions must be on one line without control characters.",
                    exception.getMessage());
        }
    }

    @Test
    void constructor_surroundingLineBreaks_rejectsInsteadOfSilentlyTrimming() {
        for (String description : List.of("\nmission", "mission\n", "\rmission", "mission\u2028")) {
            assertThrows(IllegalArgumentException.class, () -> new Todo(description));
        }
    }

    @Test
    void hasSameDetails_matchingDescriptions_ignoresCompletionButPreservesObjectEquality() {
        Todo first = new Todo("read book");
        Todo second = new Todo("  read book\t");
        second.markAsDone();

        assertTrue(first.hasSameDetails(second));
        assertTrue(second.hasSameDetails(first));
        assertTrue(first.hasSameDetails(first));
        assertNotEquals(first, second);
    }

    @Test
    void hasSameDetails_differentDescriptions_preservesCaseAndInternalWhitespace() {
        Todo todo = new Todo("read book");

        assertFalse(todo.hasSameDetails(new Todo("Read book")));
        assertFalse(todo.hasSameDetails(new Todo("read  book")));
        assertFalse(todo.hasSameDetails(new Todo("read\tbook")));
        assertFalse(todo.hasSameDetails(new Todo("read books")));
    }

    @Test
    void hasSameDetails_nullOrDifferentConcreteType_returnsFalse() {
        Todo todo = new Todo("read book");
        Deadline deadline = new Deadline("read book", LocalDateTime.of(2026, 9, 1, 9, 0));
        SpecializedTodo specializedTodo = new SpecializedTodo("read book");

        assertFalse(todo.hasSameDetails(null));
        assertFalse(todo.hasSameDetails(deadline));
        assertFalse(deadline.hasSameDetails(todo));
        assertFalse(todo.hasSameDetails(specializedTodo));
        assertFalse(specializedTodo.hasSameDetails(todo));
    }

    /**
     * Supplies a distinct concrete task type for exact-type comparisons.
     */
    private static final class SpecializedTodo extends Todo {
        private SpecializedTodo(String description) {
            super(description);
        }
    }
}
