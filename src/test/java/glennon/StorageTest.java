package glennon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Task;
import glennon.task.Todo;

/**
 * Tests mission persistence without touching the application's real data file.
 */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadMissions_missingFile_returnsEmptyList() throws GlennonException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.loadMissions().isEmpty());
    }

    @Test
    void saveAndLoadMissions_allTaskTypes_preservesFieldsAndStatus()
            throws GlennonException {
        Path dataPath = temporaryDirectory.resolve("nested").resolve("missions.txt");
        Storage storage = new Storage(dataPath);
        Todo todo = new Todo("tabs\t and Unicode 新加坡");
        Deadline deadline = new Deadline(
                "submit report", LocalDateTime.of(2026, 8, 31, 23, 59));
        Event event = new Event(
                "overnight event",
                LocalDateTime.of(2026, 9, 1, 22, 0),
                LocalDateTime.of(2026, 9, 2, 1, 30));
        deadline.markAsDone();

        storage.saveMissions(List.of(todo, deadline, event));
        List<Task> loaded = storage.loadMissions();

        assertEquals(3, loaded.size());
        Todo loadedTodo = assertInstanceOf(Todo.class, loaded.get(0));
        Deadline loadedDeadline = assertInstanceOf(Deadline.class, loaded.get(1));
        Event loadedEvent = assertInstanceOf(Event.class, loaded.get(2));
        assertEquals(todo.getDescription(), loadedTodo.getDescription());
        assertFalse(loadedTodo.isDone());
        assertEquals(deadline.getDescription(), loadedDeadline.getDescription());
        assertEquals(deadline.getDueDateTime(), loadedDeadline.getDueDateTime());
        assertTrue(loadedDeadline.isDone());
        assertEquals(event.getDescription(), loadedEvent.getDescription());
        assertEquals(event.getStartDateTime(), loadedEvent.getStartDateTime());
        assertEquals(event.getEndDateTime(), loadedEvent.getEndDateTime());
        assertFalse(loadedEvent.isDone());
    }

    @Test
    void saveMissions_emptyList_replacesExistingData() throws GlennonException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Storage storage = new Storage(dataPath);
        storage.saveMissions(List.of(new Todo("temporary")));

        storage.saveMissions(List.of());

        assertTrue(storage.loadMissions().isEmpty());
        assertTrue(Files.exists(dataPath));
    }

    @Test
    void loadMissions_corruptedSecondLine_reportsLineNumber() throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Files.writeString(dataPath, "T\t0\tdmFsaWQ=\ninvalid\n", StandardCharsets.UTF_8);
        Storage storage = new Storage(dataPath);

        GlennonException exception = assertThrows(
                GlennonException.class, storage::loadMissions);

        assertEquals("Mission data is corrupted at line 2.", exception.getMessage());
    }

    @Test
    void loadMissions_invalidStatus_reportsCorruption() throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Files.writeString(dataPath, "T\t2\tdGFzaw==\n", StandardCharsets.UTF_8);

        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(dataPath).loadMissions());

        assertEquals("Mission data is corrupted at line 1.", exception.getMessage());
    }

    @Test
    void saveMissions_unsupportedTaskType_throwsHelpfulException() {
        Storage storage = new Storage(temporaryDirectory.resolve("missions.txt"));
        Task unsupportedTask = new Task("unsupported") { };

        GlennonException exception = assertThrows(
                GlennonException.class, () -> storage.saveMissions(List.of(unsupportedTask)));

        assertEquals("Glennon cannot save an unsupported mission type.", exception.getMessage());
    }
}
