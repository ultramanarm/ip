package glennon;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
    private Path temporaryDirectory;

    @Test
    void loadMissions_missingFile_returnsEmptyList() throws GlennonException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.loadMissions().isEmpty());
    }

    @Test
    void loadMissions_missingParent_returnsEmptyList() throws GlennonException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing").resolve("missions.txt"));

        assertTrue(storage.loadMissions().isEmpty());
    }

    @Test
    void loadMissions_directoryPath_reportsLoadFailure() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(temporaryDirectory).loadMissions());

        assertEquals("Glennon could not load the mission data.", exception.getMessage());
        assertTrue(Files.isDirectory(temporaryDirectory));
    }

    @Test
    void loadMissions_parentIsFile_reportsLoadFailure() throws IOException {
        Path parent = temporaryDirectory.resolve("parent.txt");
        Files.writeString(parent, "keep parent contents");

        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(parent.resolve("missions.txt")).loadMissions());

        assertEquals("Glennon could not load the mission data.", exception.getMessage());
        assertEquals("keep parent contents", Files.readString(parent));
    }

    @Test
    void loadMissions_readDenied_reportsPermissionsWithoutDiscardingData() throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Files.writeString(dataPath, "T\t0\tdGFzaw==\n");
        assumeTrue(Files.getFileStore(dataPath).supportsFileAttributeView("posix"));
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(dataPath);
        GlennonException exception;
        try {
            Files.setPosixFilePermissions(dataPath, Set.of());
            assumeFalse(Files.isReadable(dataPath), "This account can bypass read permissions.");
            exception = assertThrows(GlennonException.class, () -> new Storage(dataPath).loadMissions());
        } finally {
            Files.setPosixFilePermissions(dataPath, permissions);
        }

        assertEquals("Glennon cannot read the mission data. Check the file and folder permissions.",
                exception.getMessage());
        assertEquals("T\t0\tdGFzaw==\n", Files.readString(dataPath));
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
    void saveMissions_emptyList_replacesExistingData() throws IOException, GlennonException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Storage storage = new Storage(dataPath);
        storage.saveMissions(List.of(new Todo("temporary")));

        storage.saveMissions(List.of());

        assertTrue(storage.loadMissions().isEmpty());
        assertTrue(Files.exists(dataPath));
        assertOnlyDataFileRemains(dataPath);
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
    void loadMissions_invalidDateFields_reportsCorruptedLine() throws IOException {
        String validDate = encode("2026-09-17T09:00");
        String invalidDate = encode("2026-02-30T09:00");
        for (String line : List.of(
                "D\t0\tdGFzaw==\t" + invalidDate,
                "E\t0\tdGFzaw==\t" + invalidDate + "\t" + validDate,
                "E\t0\tdGFzaw==\t" + validDate + "\t" + invalidDate,
                "D\t0\tdGFzaw==\t" + encode("not a date"))) {
            assertCorruptedSecondLine(line);
        }
    }

    @Test
    void loadMissions_blankDescriptions_reportsCorruptedLine() throws IOException {
        for (String description : List.of("", " ", "\t\n", "\u2003")) {
            assertCorruptedSecondLine("T\t0\t" + encode(description));
        }
    }

    @Test
    void loadMissions_reversedEvent_reportsCorruptedLine() throws IOException {
        assertCorruptedSecondLine("E\t0\tdGFzaw==\t" + encode("2026-09-18T09:00")
                + "\t" + encode("2026-09-17T09:00"));
    }

    @Test
    void loadMissions_equalEventTimes_reportsCorruptedLine() throws IOException {
        String dateTime = encode("2026-09-17T09:00");
        assertCorruptedSecondLine("E\t0\tdGFzaw==\t" + dateTime + "\t" + dateTime);
    }

    @Test
    void loadMissions_controlCharactersInDescription_reportsCorruptedLine() throws IOException {
        for (String description : List.of("line\nbreak", "hidden\u0000control", "unicode\u2028break")) {
            assertCorruptedSecondLine("T\t0\t" + encode(description));
        }
    }

    @Test
    void loadMissions_duplicateTaskTypesAndStatuses_reportsLineAndPreservesFile() throws IOException {
        String dateTime = encode("2026-09-17T09:00");
        String endDateTime = encode("2026-09-17T10:00");
        List<String> records = List.of("T\t0\tdGFzaw==", "D\t0\tdGFzaw==\t" + dateTime,
                "E\t0\tdGFzaw==\t" + dateTime + "\t" + endDateTime);
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        for (String record : records) {
            String contents = record + "\n" + record.replace("\t0\t", "\t1\t") + "\n";
            Files.writeString(dataPath, contents);

            GlennonException exception = assertThrows(
                    GlennonException.class, () -> new Storage(dataPath).loadMissions());

            assertEquals("Mission data is corrupted at line 2: duplicate mission.", exception.getMessage());
            assertEquals(contents, Files.readString(dataPath));
        }
    }

    @Test
    void loadMissions_invalidBase64_reportsCorruptedLine() throws IOException {
        assertCorruptedSecondLine("T\t0\t%%%");
        assertCorruptedSecondLine("D\t0\tdGFzaw==\t%%%");
    }

    @Test
    void loadMissions_invalidDecodedUtf8_reportsCorruptedLine() throws IOException {
        assertCorruptedSecondLine("T\t0\t/w==");
        assertCorruptedSecondLine("T\t0\tww==");
        assertCorruptedSecondLine("D\t0\tdGFzaw==\t/w==");
    }

    @Test
    void loadMissions_invalidRawUtf8_reportsCorruptedLine() throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        byte[] corruptedData = { 'T', '\t', '0', '\t', (byte) 0xff, '\n' };
        Files.write(dataPath, corruptedData);

        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(dataPath).loadMissions());

        assertEquals("Mission data is corrupted at line 1.", exception.getMessage());
        assertArrayEquals(corruptedData, Files.readAllBytes(dataPath));
    }

    @Test
    void loadMissions_wrongFieldCount_reportsCorruptedLine() throws IOException {
        assertCorruptedSecondLine("T\t0");
        assertCorruptedSecondLine("D\t0\tdGFzaw==");
        assertCorruptedSecondLine("E\t0\tdGFzaw==\t" + encode("2026-09-17T09:00"));
        assertCorruptedSecondLine("T\t0\tdGFzaw==\textra");
    }

    @Test
    void saveMissions_unsupportedTaskType_preservesExistingFile() throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        String originalData = "T\t0\tdGFzaw==\n";
        Files.writeString(dataPath, originalData);
        Storage storage = new Storage(dataPath);
        Task unsupportedTask = new Task("unsupported") { };

        GlennonException exception = assertThrows(
                GlennonException.class, () -> storage.saveMissions(List.of(unsupportedTask)));

        assertEquals("Glennon cannot save an unsupported mission type.", exception.getMessage());
        assertEquals(originalData, Files.readString(dataPath));
        assertOnlyDataFileRemains(dataPath);
    }

    @Test
    void saveMissions_replacementFailure_preservesExistingFileAndCleansTemporaryFile()
            throws IOException, GlennonException {
        assertReplacementFailurePreservesData(new IOException("Replacement failed."),
                "Glennon could not save the mission data.");
    }

    @Test
    void saveMissions_interruptedReplacement_preservesExistingFileAndCleansTemporaryFile()
            throws IOException, GlennonException {
        assertReplacementFailurePreservesData(new InterruptedIOException("Replacement interrupted."),
                "Glennon could not save the mission data.");
    }

    @Test
    void saveMissions_atomicMoveUnavailable_preservesExistingFileWithoutFallback()
            throws IOException, GlennonException {
        assertReplacementFailurePreservesData(
                new AtomicMoveNotSupportedException("temporary", "missions", "Unsupported operation."),
                "Glennon could not save the mission data.");
    }

    @Test
    void saveMissions_replacementDenied_reportsPermissionsAndPreservesExistingFile()
            throws IOException, GlennonException {
        assertReplacementFailurePreservesData(new AccessDeniedException("missions.txt"),
                "Glennon cannot save the mission data. Check the file and folder permissions.");
    }

    @Test
    void saveMissions_directoryTargets_preservesDirectoriesAndContents() throws IOException {
        Path emptyDirectory = Files.createDirectory(temporaryDirectory.resolve("empty"));
        Path occupiedDirectory = Files.createDirectory(temporaryDirectory.resolve("occupied"));
        Path existingFile = occupiedDirectory.resolve("keep.txt");
        Files.writeString(existingFile, "keep contents");

        for (Path directory : List.of(emptyDirectory, occupiedDirectory)) {
            Storage storage = new Storage(directory);
            GlennonException exception = assertThrows(
                    GlennonException.class, () -> storage.saveMissions(List.of(new Todo("replacement"))));

            assertEquals("Glennon could not save the mission data.", exception.getMessage());
            assertTrue(Files.isDirectory(directory));
        }
        assertEquals("keep contents", Files.readString(existingFile));
    }

    @Test
    void saveMissions_symbolicLink_preservesLinkAndDestination() throws IOException {
        Path originalPath = temporaryDirectory.resolve("original.txt");
        Path linkedPath = temporaryDirectory.resolve("missions.txt");
        Files.writeString(originalPath, "T\t0\tdGFzaw==\n");
        Files.createSymbolicLink(linkedPath, originalPath.getFileName());

        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(linkedPath).saveMissions(List.of(new Todo("replacement"))));

        assertEquals("Glennon could not save the mission data.", exception.getMessage());
        assertTrue(Files.isSymbolicLink(linkedPath));
        assertEquals("T\t0\tdGFzaw==\n", Files.readString(originalPath));
    }

    @Test
    void saveMissions_danglingSymbolicLink_preservesLinkWithoutCreatingDestination() throws IOException {
        Path missingPath = temporaryDirectory.resolve("missing.txt");
        Path linkedPath = temporaryDirectory.resolve("missions.txt");
        Files.createSymbolicLink(linkedPath, missingPath.getFileName());

        assertThrows(
                GlennonException.class, () -> new Storage(linkedPath).saveMissions(List.of(new Todo("replacement"))));

        assertTrue(Files.isSymbolicLink(linkedPath));
        assertFalse(Files.exists(missingPath));
    }

    /**
     * Verifies that invalid stored values identify their line and leave the file unchanged.
     */
    private void assertCorruptedSecondLine(String line) throws IOException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        String corruptedData = "T\t0\tdGFzaw==\n" + line + "\n";
        Files.writeString(dataPath, corruptedData);

        GlennonException exception = assertThrows(
                GlennonException.class, () -> new Storage(dataPath).loadMissions());

        assertEquals("Mission data is corrupted at line 2.", exception.getMessage());
        assertEquals(corruptedData, Files.readString(dataPath));
    }

    /**
     * Verifies failures after a complete temporary write cannot damage the saved mission log.
     */
    private void assertReplacementFailurePreservesData(IOException failure, String expectedMessage)
            throws IOException, GlennonException {
        Path dataPath = temporaryDirectory.resolve("missions.txt");
        Storage storage = new Storage(dataPath);
        storage.saveMissions(List.of(new Todo("original")));
        byte[] originalData = Files.readAllBytes(dataPath);
        Storage failingStorage = new Storage(dataPath) {
            @Override
            void replaceDataFile(Path temporaryPath) throws IOException {
                assertEquals(List.of("T\t0\t" + StorageTest.encode("replacement")),
                        Files.readAllLines(temporaryPath));
                throw failure;
            }
        };

        GlennonException exception = assertThrows(
                GlennonException.class, () -> failingStorage.saveMissions(List.of(new Todo("replacement"))));

        assertEquals(expectedMessage, exception.getMessage());
        assertSame(failure, exception.getCause());
        assertArrayEquals(originalData, Files.readAllBytes(dataPath));
        assertEquals("original", storage.loadMissions().getFirst().getDescription());
        assertOnlyDataFileRemains(dataPath);
    }

    /**
     * Verifies that a completed or failed save leaves no temporary files behind.
     */
    private void assertOnlyDataFileRemains(Path dataPath) throws IOException {
        try (Stream<Path> entries = Files.list(temporaryDirectory)) {
            assertEquals(List.of(dataPath), entries.toList());
        }
    }

    /**
     * Encodes hand-crafted records independently of the production serializer.
     */
    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
