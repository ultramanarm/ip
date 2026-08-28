package glennon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Reads and writes Glennon's mission data using a relative, OS-independent
 * file path.
 */
public class Storage {
    /** Default location of Glennon's saved mission data. */
    private static final Path DEFAULT_DATA_PATH = Path.of("data", "glennon.txt");

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
     * Returns all saved data lines, or an empty list when the file does not
     * exist.
     *
     * @return saved data lines
     * @throws IOException if the data file cannot be read
     */
    public List<String> loadLines() throws IOException {
        if (!Files.exists(dataPath)) {
            return Collections.emptyList();
        }
        return Files.readAllLines(dataPath, StandardCharsets.UTF_8);
    }

    /**
     * Replaces the data file with the specified lines, creating its parent
     * folder when necessary.
     *
     * @param lines data lines to save
     * @throws IOException if the folder or data file cannot be written
     */
    public void saveLines(List<String> lines) throws IOException {
        Path parentPath = dataPath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        Files.write(dataPath, lines, StandardCharsets.UTF_8);
    }
}
