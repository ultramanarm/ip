package glennon.command;

import glennon.Storage;
import glennon.Ui;
import glennon.exception.GlennonException;
import glennon.task.Task;
import glennon.task.TaskList;

/**
 * Deletes one mission by its zero-based position.
 */
public final class DeleteCommand extends Command {
    /** Zero-based position of the mission to delete. */
    private final int missionIndex;

    /**
     * Creates a mission-deletion command.
     *
     * @param missionIndex zero-based position of the mission to delete.
     */
    public DeleteCommand(int missionIndex) {
        this.missionIndex = missionIndex;
    }

    /**
     * Removes, saves, and displays the selected mission.
     *
     * @param missions missions in the current session.
     * @param ui interface used to display the result.
     * @param storage storage used to persist the change.
     * @throws GlennonException if the index or save operation fails
     */
    @Override
    public void execute(TaskList missions, Ui ui, Storage storage)
            throws GlennonException {
        Task removedMission = missions.remove(missionIndex);
        storage.saveMissions(missions.asList());
        ui.showMissionRemoved(removedMission, missions.size());
    }
}
