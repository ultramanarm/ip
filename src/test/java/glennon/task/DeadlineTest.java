package glennon.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Tests deadline validation, schedule matching, and defining detail comparisons.
 */
class DeadlineTest {
    @Test
    void constructor_nullDateTime_throwsHelpfulException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new Deadline("read book", null));

        assertEquals("Deadline date and time must not be null.", exception.getMessage());
    }

    @Test
    void constructor_invalidDescription_rejectsInheritedValidationFailures() {
        LocalDateTime dueDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);

        assertThrows(IllegalArgumentException.class, () -> new Deadline(null, dueDateTime));
        assertThrows(IllegalArgumentException.class, () -> new Deadline(" \t ", dueDateTime));
        assertThrows(IllegalArgumentException.class, () -> new Deadline("two\nlines", dueDateTime));
    }

    @Test
    void hasSameDetails_matchingDescriptionAndDateTime_ignoresCompletion() {
        LocalDateTime dueDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Deadline first = new Deadline("read book", dueDateTime);
        Deadline second = new Deadline(" read book ", dueDateTime);
        first.markAsDone();

        assertTrue(first.hasSameDetails(second));
        assertTrue(second.hasSameDetails(first));
    }

    @Test
    void hasSameDetails_differentDescriptionOrDateTime_returnsFalse() {
        LocalDateTime dueDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Deadline deadline = new Deadline("read book", dueDateTime);

        assertFalse(deadline.hasSameDetails(new Deadline("Read book", dueDateTime)));
        assertFalse(deadline.hasSameDetails(new Deadline("read book", dueDateTime.plusDays(1))));
        assertFalse(deadline.hasSameDetails(new Deadline("read book", dueDateTime.plusMinutes(1))));
        assertFalse(deadline.hasSameDetails(new Deadline("read  book", dueDateTime)));
    }

    @Test
    void hasSameDetails_nullOrOtherTaskType_returnsFalse() {
        LocalDateTime dueDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Deadline deadline = new Deadline("read book", dueDateTime);
        Event event = new Event("read book", dueDateTime, dueDateTime.plusHours(1));

        assertFalse(deadline.hasSameDetails(null));
        assertFalse(deadline.hasSameDetails(new Todo("read book")));
        assertFalse(deadline.hasSameDetails(event));
        assertFalse(event.hasSameDetails(deadline));
    }

    @Test
    void occursOn_dueDate_matchesOnlyThatDate() {
        Deadline deadline = new Deadline("read book", LocalDateTime.of(2026, 9, 1, 23, 59));

        assertTrue(deadline.occursOn(LocalDate.of(2026, 9, 1)));
        assertFalse(deadline.occursOn(LocalDate.of(2026, 8, 31)));
        assertFalse(deadline.occursOn(LocalDate.of(2026, 9, 2)));
    }
}
