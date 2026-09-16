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
    void add_duplicateTodoWithDifferentStatus_rejectsAndPreservesOriginalMission() throws GlennonException {
        Todo existingMission = new Todo("read book");
        existingMission.markAsDone();
        TaskList missions = new TaskList(List.of(existingMission));

        GlennonException exception = assertThrows(
                GlennonException.class, () -> missions.add(new Todo("  read book\t")));

        assertEquals("That mission already exists in the log.", exception.getMessage());
        assertEquals(1, missions.size());
        assertSame(existingMission, missions.get(0));
        assertTrue(existingMission.isDone());
    }

    @Test
    void add_duplicateDeadlineWithDifferentStatus_rejectsAndPreservesOriginalMission() throws GlennonException {
        LocalDateTime dueDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Deadline existingMission = new Deadline("return book", dueDateTime);
        Deadline duplicateMission = new Deadline("return book", dueDateTime);
        duplicateMission.markAsDone();
        TaskList missions = new TaskList(List.of(existingMission));

        GlennonException exception = assertThrows(
                GlennonException.class, () -> missions.add(duplicateMission));

        assertEquals("That mission already exists in the log.", exception.getMessage());
        assertEquals(1, missions.size());
        assertSame(existingMission, missions.get(0));
        assertFalse(existingMission.isDone());
    }

    @Test
    void add_duplicateEventWithDifferentStatus_rejectsAndPreservesOriginalMission() throws GlennonException {
        LocalDateTime startDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(1);
        Event existingMission = new Event("meeting", startDateTime, endDateTime);
        existingMission.markAsDone();
        TaskList missions = new TaskList(List.of(existingMission));

        GlennonException exception = assertThrows(
                GlennonException.class, () -> missions.add(new Event(" meeting ", startDateTime, endDateTime)));

        assertEquals("That mission already exists in the log.", exception.getMessage());
        assertEquals(1, missions.size());
        assertSame(existingMission, missions.get(0));
        assertTrue(existingMission.isDone());
    }

    @Test
    void add_sameDescriptionWithDifferentTypes_preservesAllMissions() throws GlennonException {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Todo todo = new Todo("read book");
        Deadline deadline = new Deadline("read book", dateTime);
        Event event = new Event("read book", dateTime, dateTime.plusHours(1));
        TaskList missions = new TaskList();

        missions.add(todo);
        missions.add(deadline);
        missions.add(event);

        assertEquals(List.of(todo, deadline, event), missions.asList());
    }

    @Test
    void add_differentCaseOrInternalWhitespace_preservesDistinctDescriptions() throws GlennonException {
        Todo originalMission = new Todo("read book");
        Todo differentCase = new Todo("Read book");
        Todo extraSpace = new Todo("read  book");
        Todo internalTab = new Todo("read\tbook");
        TaskList missions = new TaskList(List.of(originalMission));

        missions.add(differentCase);
        missions.add(extraSpace);
        missions.add(internalTab);

        assertEquals(List.of(originalMission, differentCase, extraSpace, internalTab), missions.asList());
    }

    @Test
    void add_sameDeadlineDescriptionWithDifferentDatesOrTimes_preservesDistinctMissions()
            throws GlennonException {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        Deadline originalMission = new Deadline("read book", dateTime);
        Deadline differentDate = new Deadline("read book", dateTime.plusDays(1));
        Deadline differentTime = new Deadline("read book", dateTime.plusMinutes(1));
        TaskList missions = new TaskList(List.of(originalMission));

        missions.add(differentDate);
        missions.add(differentTime);

        assertEquals(List.of(originalMission, differentDate, differentTime), missions.asList());
    }

    @Test
    void add_sameEventDescriptionWithDifferentBoundaries_preservesDistinctMissions() throws GlennonException {
        LocalDateTime startDateTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(1);
        Event originalMission = new Event("meeting", startDateTime, endDateTime);
        Event differentStart = new Event("meeting", startDateTime.plusMinutes(1), endDateTime);
        Event differentEnd = new Event("meeting", startDateTime, endDateTime.plusMinutes(1));
        TaskList missions = new TaskList(List.of(originalMission));

        missions.add(differentStart);
        missions.add(differentEnd);

        assertEquals(List.of(originalMission, differentStart, differentEnd), missions.asList());
    }

    @Test
    void add_unicodeAndPunctuation_keepsTextAndRejectsExactDuplicate() throws GlennonException {
        String description = "Café / notes: \"図書館\" 😊\t$5";
        Todo existingMission = new Todo(description);
        TaskList missions = new TaskList();
        missions.add(existingMission);

        assertThrows(GlennonException.class, () -> missions.add(new Todo(description)));

        assertEquals(1, missions.size());
        assertEquals(description, missions.get(0).getDescription());
        assertSame(existingMission, missions.get(0));
    }

    @Test
    void add_previouslyDeletedMission_acceptsRecreatedMission() throws GlennonException {
        Todo firstMission = new Todo("first");
        Todo deletedMission = new Todo("read book");
        TaskList missions = new TaskList(List.of(firstMission, deletedMission));
        missions.remove(1);
        Todo recreatedMission = new Todo("read book");

        missions.add(recreatedMission);

        assertEquals(List.of(firstMission, recreatedMission), missions.asList());
        assertSame(recreatedMission, missions.get(1));
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

        assertThrows(
                UnsupportedOperationException.class, () -> missions.asList().add(new Todo("intruder")));
        assertEquals(List.of(todo), missions.asList());
    }

    @Test
    void findByDescription_mixedTasks_returnsSubstringMatchesInOriginalOrder() {
        Todo todo = new Todo("read book");
        Deadline deadline = new Deadline(
                "return book", LocalDateTime.of(2026, 9, 1, 18, 0));
        Event event = new Event(
                "book club",
                LocalDateTime.of(2026, 9, 2, 14, 0),
                LocalDateTime.of(2026, 9, 2, 16, 0));
        Todo unrelated = new Todo("buy groceries");
        TaskList missions = new TaskList(List.of(todo, deadline, event, unrelated));

        assertEquals(List.of(todo, deadline, event), missions.findByDescription("book"));
    }

    @Test
    void findByDescription_caseMismatch_returnsUnmodifiableEmptyList() {
        Todo todo = new Todo("read book");
        TaskList missions = new TaskList(List.of(todo));

        List<Task> matches = missions.findByDescription("Book");

        assertTrue(matches.isEmpty());
        assertThrows(
                UnsupportedOperationException.class, () -> matches.add(new Todo("intruder")));
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
        assertThrows(
                UnsupportedOperationException.class, () -> matches.add(new Todo("intruder")));
    }

    @Test
    void sortChronologically_mixedMissions_ordersDatesAndKeepsTiesStable() {
        Todo firstTodo = new Todo("first unscheduled");
        Deadline laterDeadline = new Deadline(
                "later deadline", LocalDateTime.of(2026, 9, 3, 9, 0));
        Event tiedEvent = new Event(
                "tied event",
                LocalDateTime.of(2026, 9, 2, 9, 0),
                LocalDateTime.of(2026, 9, 2, 10, 0));
        Deadline earliestDeadline = new Deadline(
                "earliest deadline", LocalDateTime.of(2026, 9, 1, 18, 0));
        Deadline tiedDeadline = new Deadline(
                "tied deadline", LocalDateTime.of(2026, 9, 2, 9, 0));
        Todo secondTodo = new Todo("second unscheduled");
        TaskList missions = new TaskList(List.of(
                firstTodo, laterDeadline, tiedEvent,
                earliestDeadline, tiedDeadline, secondTodo));
        tiedEvent.markAsDone();

        missions.sortChronologically();

        assertEquals(List.of(
                earliestDeadline, tiedEvent, tiedDeadline,
                laterDeadline, firstTodo, secondTodo), missions.asList());
        assertTrue(tiedEvent.isDone());
    }

    @Test
    void sortChronologically_emptyOrSingleMission_preservesContents() {
        TaskList emptyMissions = new TaskList();
        Todo onlyMission = new Todo("only mission");
        TaskList singleMission = new TaskList(List.of(onlyMission));

        emptyMissions.sortChronologically();
        singleMission.sortChronologically();

        assertTrue(emptyMissions.asList().isEmpty());
        assertEquals(List.of(onlyMission), singleMission.asList());
    }
}
