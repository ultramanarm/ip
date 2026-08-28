package glennon;

import glennon.exception.GlennonException;
import glennon.task.Task;

/**
 * Starts the Glennon chatbot.
 */
public class Glennon {
    /**
     * Prevents instantiation of this application entry-point class.
     */
    private Glennon() {
    }

    /**
     * Greets the user, stores missions, lists or updates their completion status
     * on request, and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        Storage storage = new Storage();
        TaskList missions;
        try {
            missions = new TaskList(storage.loadMissions());
        } catch (GlennonException e) {
            ui.showError(e.getMessage());
            ui.close();
            return;
        }

        boolean isSigningOff = false;
        while (!isSigningOff && ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showDivider();

            try {
                Parser.CommandType commandType = Parser.parseCommandType(command);

                switch (commandType) {
                case BYE -> {
                    ui.showGoodbye();
                    isSigningOff = true;
                }
                case LIST -> ui.showMissionList(missions.asList());
                case ON -> {
                    var date = Parser.parseDate(command);
                    ui.showScheduledMissions(date, missions.occurringOn(date));
                }
                case DELETE -> {
                    int missionIndex = Parser.parseMissionIndex(command, commandType);
                    Task removedMission = missions.remove(missionIndex);
                    storage.saveMissions(missions.asList());
                    ui.showMissionRemoved(removedMission, missions.size());
                }
                case MARK, UNMARK -> {
                    boolean shouldCompleteMission = commandType == Parser.CommandType.MARK;
                    int missionIndex = Parser.parseMissionIndex(command, commandType);
                    Task mission = missions.get(missionIndex);
                    if (shouldCompleteMission) {
                        mission.markAsDone();
                    } else {
                        mission.markAsNotDone();
                    }
                    storage.saveMissions(missions.asList());
                    ui.showMissionStatusChanged(mission, shouldCompleteMission);
                }
                case TODO -> addMission(
                        missions, Parser.parseTodo(command), storage, ui);
                case DEADLINE -> addMission(
                        missions, Parser.parseDeadline(command), storage, ui);
                case EVENT -> addMission(
                        missions, Parser.parseEvent(command), storage, ui);
                case UNKNOWN -> throw new GlennonException(
                        "Glennon doesn't recognize that command.\n"
                                + "Try: todo, deadline, event, list, on, mark, unmark, "
                                + "delete, or bye.");
                }
            } catch (GlennonException e) {
                ui.showError(e.getMessage());
            }
            ui.showDivider();
        }

        ui.close();
    }

    /**
     * Adds a typed mission and displays its type-specific representation.
     *
     * @param missions mission list for the current session
     * @param mission mission to add
     * @param storage storage used to persist the updated mission list
     * @param ui interface used to confirm the addition
     * @throws GlennonException if the updated mission list cannot be saved
     */
    private static void addMission(
            TaskList missions, Task mission, Storage storage, Ui ui)
            throws GlennonException {
        missions.add(mission);
        storage.saveMissions(missions.asList());
        ui.showMissionAdded(mission, missions.size());
    }

}
