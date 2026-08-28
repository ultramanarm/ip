package glennon.command;

import glennon.Storage;
import glennon.TaskList;
import glennon.Ui;
import glennon.exception.GlennonException;
import glennon.task.Task;

/**
 * Adds one parsed mission and persists the updated mission log.
 */
public final class AddCommand extends Command {
    /** Mission to add. */
    private final Task mission;

    /**
     * Creates a command that adds the specified mission.
     *
     * @param mission parsed mission to add
     */
    public AddCommand(Task mission) {
        this.mission = mission;
    }

    /**
     * Adds, saves, and displays the mission.
     *
     * @param missions missions in the current session
     * @param ui interface used to display the result
     * @param storage storage used to persist the change
     * @throws GlennonException if the updated mission list cannot be saved
     */
    @Override
    public void execute(TaskList missions, Ui ui, Storage storage)
            throws GlennonException {
        missions.add(mission);
        storage.saveMissions(missions.asList());
        ui.showMissionAdded(mission, missions.size());
    }
}
