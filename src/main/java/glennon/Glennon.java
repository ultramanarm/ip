package glennon;

import glennon.exception.GlennonException;
import glennon.task.Task;

/**
 * Starts the Glennon chatbot.
 */
public class Glennon {
    /**
     * Greets the user, stores missions, lists or updates their completion status
     * on request, and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList missions = new TaskList();
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showDivider();

            try {
                Parser.CommandType commandType = Parser.parseCommandType(command);

                if (commandType == Parser.CommandType.BYE) {
                    ui.showGoodbye();
                    ui.showDivider();
                    break;
                }

                switch (commandType) {
                case LIST -> ui.showMissionList(missions.asList());
                case DELETE -> {
                    int missionIndex = Parser.parseMissionIndex(command, commandType);
                    Task removedMission = missions.remove(missionIndex);
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
                    ui.showMissionStatusChanged(mission, shouldCompleteMission);
                }
                case TODO -> addMission(missions, Parser.parseTodo(command), ui);
                case DEADLINE -> addMission(missions, Parser.parseDeadline(command), ui);
                case EVENT -> addMission(missions, Parser.parseEvent(command), ui);
                case UNKNOWN -> throw new GlennonException(
                        "Glennon doesn't recognize that command.\n"
                                + "Try: todo, deadline, event, list, mark, unmark, delete, or bye.");
                case BYE -> throw new IllegalStateException(
                        "Bye should be handled before dispatch.");
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
     * @param ui interface used to confirm the addition
     */
    private static void addMission(TaskList missions, Task mission, Ui ui) {
        missions.add(mission);
        ui.showMissionAdded(mission, missions.size());
    }

}
