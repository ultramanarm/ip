package glennon;

import glennon.exception.GlennonException;
import glennon.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the missions of one Glennon session and guards every access by
 * mission number, so callers cannot reach a position outside the log.
 */
public class TaskList {
    private final List<Task> missions = new ArrayList<>();

    /**
     * Adds a mission to the end of the log.
     *
     * @param mission mission to store
     */
    public void add(Task mission) {
        missions.add(mission);
    }

    /**
     * Returns the mission at the given position.
     *
     * @param missionIndex zero-based position of the mission
     * @return the stored mission
     * @throws GlennonException if the position is outside the log
     */
    public Task get(int missionIndex) throws GlennonException {
        requireInRange(missionIndex);
        return missions.get(missionIndex);
    }

    /**
     * Removes the mission at the given position and returns it.
     *
     * @param missionIndex zero-based position of the mission
     * @return the mission that was removed
     * @throws GlennonException if the position is outside the log
     */
    public Task remove(int missionIndex) throws GlennonException {
        requireInRange(missionIndex);
        return missions.remove(missionIndex);
    }

    /**
     * Returns how many missions are currently stored.
     *
     * @return the mission count
     */
    public int size() {
        return missions.size();
    }

    /**
     * Returns a read-only view of the missions for display.
     *
     * @return the stored missions in insertion order
     */
    public List<Task> asList() {
        return Collections.unmodifiableList(missions);
    }

    /**
     * Rejects a position that no mission occupies.
     *
     * @param missionIndex zero-based position to check
     * @throws GlennonException if the position is outside the log
     */
    private void requireInRange(int missionIndex) throws GlennonException {
        if (missionIndex < 0 || missionIndex >= missions.size()) {
            throw new GlennonException("Please enter a valid mission number.");
        }
    }
}
