package glennon.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Tests the inclusive date range used when filtering events.
 */
class EventTest {
    @Test
    void occursOn_multiDayEvent_includesStartMiddleAndEndDates() {
        Event event = new Event(
                "conference",
                LocalDateTime.of(2026, 8, 28, 23, 30),
                LocalDateTime.of(2026, 8, 30, 0, 15));

        assertTrue(event.occursOn(LocalDate.of(2026, 8, 28)));
        assertTrue(event.occursOn(LocalDate.of(2026, 8, 29)));
        assertTrue(event.occursOn(LocalDate.of(2026, 8, 30)));
    }

    @Test
    void occursOn_datesOutsideEvent_returnsFalse() {
        Event event = new Event(
                "conference",
                LocalDateTime.of(2026, 8, 28, 23, 30),
                LocalDateTime.of(2026, 8, 30, 0, 15));

        assertFalse(event.occursOn(LocalDate.of(2026, 8, 27)));
        assertFalse(event.occursOn(LocalDate.of(2026, 8, 31)));
    }

    @Test
    void occursOn_singleDayEvent_matchesOnlyThatDate() {
        Event event = new Event(
                "lecture",
                LocalDateTime.of(2026, 8, 29, 9, 0),
                LocalDateTime.of(2026, 8, 29, 10, 0));

        assertTrue(event.occursOn(LocalDate.of(2026, 8, 29)));
        assertFalse(event.occursOn(LocalDate.of(2026, 8, 28)));
        assertFalse(event.occursOn(LocalDate.of(2026, 8, 30)));
    }
}
