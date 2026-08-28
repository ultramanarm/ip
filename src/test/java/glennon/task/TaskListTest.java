package glennon.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import glennon.exception.GlennonException;

/**
 * Tests mission-list state changes, validation, and date filtering.
 */
class TaskListTest {
    @Test
    void addAndGet_multipleMissions_preservesInsertionOrder() throws GlennonException {
        TaskList missions = new TaskList();
        Todo first = new Todo("first");
        Todo second = new Todo("second");

        missions.add(first);
        missions.add(second);

        assertEquals(2, missions.size());
        assertSame(first, missions.get(0));
        assertSame(second, missions.get(1));
    }

    @Test
    void markAndUnmark_validIndex_updatesAndReturnsMission() throws GlennonException {
        Todo todo = new Todo("test state changes");
        TaskList missions = new TaskList(List.of(todo));

        assertSame(todo, missions.mark(0));
        assertTrue(todo.isDone());
        assertSame(todo, missions.unmark(0));
        assertFalse(todo.isDone());
    }

    @Test
    void remove_firstAndLastMissions_removesCorrectItems() throws GlennonException {
        Todo first = new Todo("first");
        Todo middle = new Todo("middle");
        Todo last = new Todo("last");
        TaskList missions = new TaskList(List.of(first, middle, last));

        assertSame(first, missions.remove(0));
        assertSame(last, missions.remove(1));
        assertEquals(List.of(middle), missions.asList());
    }

    @Test
    void indexedOperations_outOfRange_throwHelpfulExceptionAndPreserveState() {
        Todo todo = new Todo("only mission");
        TaskList missions = new TaskList(List.of(todo));

        GlennonException negative = assertThrows(
                GlennonException.class, () -> missions.get(-1));
        GlennonException upperBound = assertThrows(
                GlennonException.class, () -> missions.remove(1));

        assertEquals("Please enter a valid mission number.", negative.getMessage());
        assertEquals(negative.getMessage(), upperBound.getMessage());
        assertEquals(List.of(todo), missions.asList());
    }

    @Test
    void asList_attemptedModification_throwsAndPreservesState() {
        Todo todo = new Todo("protected mission");
        TaskList missions = new TaskList(List.of(todo));

        assertThrows(UnsupportedOperationException.class,
                () -> missions.asList().add(new Todo("intruder")));
        assertEquals(List.of(todo), missions.asList());
    }

    @Test
    void occurringOn_mixedTasks_returnsScheduledMatchesInOriginalOrder() {
        LocalDate target = LocalDate.of(2026, 8, 29);
        Todo todo = new Todo("unscheduled");
        Deadline deadline = new Deadline(
                "due today", LocalDateTime.of(2026, 8, 29, 23, 59));
        Event spanningEvent = new Event(
                "conference",
                LocalDateTime.of(2026, 8, 28, 9, 0),
                LocalDateTime.of(2026, 8, 30, 17, 0));
        Deadline otherDeadline = new Deadline(
                "due tomorrow", LocalDateTime.of(2026, 8, 30, 8, 0));
        TaskList missions = new TaskList(List.of(todo, deadline, spanningEvent, otherDeadline));

        assertEquals(List.of(deadline, spanningEvent), missions.occurringOn(target));
    }

    @Test
    void occurringOn_noMatches_returnsUnmodifiableEmptyList() {
        TaskList missions = new TaskList(List.of(new Todo("unscheduled")));
        List<Task> matches = missions.occurringOn(LocalDate.of(2026, 8, 29));

        assertTrue(matches.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> matches.add(new Todo("intruder")));
    }
}
