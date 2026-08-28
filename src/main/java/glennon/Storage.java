package glennon;

import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Task;
import glennon.task.Todo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Reads and writes Glennon's mission data using a relative, OS-independent
 * file path.
 */
public class Storage {
    /** Default location of Glennon's saved mission data. */
    private static final Path DEFAULT_DATA_PATH = Path.of("data", "glennon.txt");

    /** Separates fields without conflicting with Base64-encoded task text. */
    private static final String FIELD_SEPARATOR = "\t";

    private final Path dataPath;

    /**
     * Creates storage that uses Glennon's default data file.
     */
    public Storage() {
        this(DEFAULT_DATA_PATH);
    }

    /**
     * Creates storage that uses the specified data file.
     *
     * @param dataPath path of the data file
     */
    public Storage(Path dataPath) {
        this.dataPath = dataPath;
    }

    /**
     * Returns all saved missions, or an empty list when the data file does not
     * exist.
     *
     * @return saved missions
     * @throws GlennonException if the data file cannot be read or parsed
     */
    public List<Task> loadMissions() throws GlennonException {
        if (!Files.exists(dataPath)) {
            return new ArrayList<>();
        }

        try {
            List<Task> missions = new ArrayList<>();
            List<String> lines = Files.readAllLines(dataPath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                missions.add(parseMission(lines.get(i), i + 1));
            }
            return missions;
        } catch (IOException e) {
            throw new GlennonException("Glennon could not load the mission data.", e);
        }
    }

    /**
     * Replaces the data file with the specified missions, creating its parent
     * folder when necessary.
     *
     * @param missions missions to save
     * @throws GlennonException if the folder or data file cannot be written
     */
    public void saveMissions(List<Task> missions) throws GlennonException {
        try {
            Path parentPath = dataPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            List<String> lines = new ArrayList<>();
            for (Task mission : missions) {
                lines.add(formatMission(mission));
            }
            Files.write(dataPath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GlennonException("Glennon could not save the mission data.", e);
        }
    }

    /**
     * Converts one task into Glennon's storage format.
     *
     * @param mission mission to convert
     * @return storage-ready line
     * @throws GlennonException if the task type is unsupported
     */
    private String formatMission(Task mission) throws GlennonException {
        String status = mission.isDone() ? "1" : "0";
        String description = encode(mission.getDescription());
        if (mission instanceof Todo) {
            return String.join(FIELD_SEPARATOR, "T", status, description);
        }
        if (mission instanceof Deadline deadline) {
            return String.join(
                    FIELD_SEPARATOR,
                    "D",
                    status,
                    description,
                    encode(deadline.getBy().toString()));
        }
        if (mission instanceof Event event) {
            return String.join(
                    FIELD_SEPARATOR,
                    "E",
                    status,
                    description,
                    encode(event.getFrom().toString()),
                    encode(event.getTo().toString()));
        }
        throw new GlennonException("Glennon cannot save an unsupported mission type.");
    }

    /**
     * Converts one stored line into its task subtype and completion state.
     *
     * @param line stored mission data
     * @param lineNumber one-based line number used in error messages
     * @return restored mission
     * @throws GlennonException if the line is not valid mission data
     */
    private Task parseMission(String line, int lineNumber) throws GlennonException {
        try {
            String[] fields = line.split(FIELD_SEPARATOR, -1);
            requireFieldCount(fields);
            Task mission = switch (fields[0]) {
            case "T" -> new Todo(decode(fields[2]));
            case "D" -> new Deadline(
                    decode(fields[2]), LocalDateTime.parse(decode(fields[3])));
            case "E" -> new Event(
                    decode(fields[2]),
                    LocalDateTime.parse(decode(fields[3])),
                    LocalDateTime.parse(decode(fields[4])));
            default -> throw new IllegalArgumentException();
            };

            if (fields[1].equals("1")) {
                mission.markAsDone();
            } else if (!fields[1].equals("0")) {
                throw new IllegalArgumentException();
            }
            return mission;
        } catch (IllegalArgumentException e) {
            throw new GlennonException(
                    "Mission data is corrupted at line " + lineNumber + ".", e);
        }
    }

    /**
     * Checks that a stored mission has the number of fields required by its
     * type marker.
     *
     * @param fields stored mission fields
     */
    private void requireFieldCount(String[] fields) {
        if (fields.length < 1
                || fields[0].equals("T") && fields.length != 3
                || fields[0].equals("D") && fields.length != 4
                || fields[0].equals("E") && fields.length != 5) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Encodes user-provided text so field separators remain unambiguous.
     *
     * @param value text to encode
     * @return Base64-encoded text
     */
    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes text stored in one mission field.
     *
     * @param value Base64-encoded text
     * @return decoded user-provided text
     */
    private String decode(String value) {
        byte[] bytes = Base64.getDecoder().decode(value);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
