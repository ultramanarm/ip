package glennon;

import glennon.exception.GlennonException;
import glennon.task.Task;

import java.nio.file.Path;

/**
 * Starts the Glennon chatbot.
 */
public class Glennon {
    /** Interface used for all console input and output. */
    private final Ui ui;

    /** Storage used to load and save missions. */
    private final Storage storage;

    /** Missions available during the current application session. */
    private TaskList missions;

    /**
     * Creates a Glennon application using the specified data file.
     *
     * @param dataPath path of the file used to persist missions
     */
    public Glennon(String dataPath) {
        this.ui = new Ui();
        this.storage = new Storage(Path.of(dataPath));
        this.missions = new TaskList();
    }

    /**
     * Greets the user, loads saved missions, processes commands, and closes the
     * user interface when input ends or the user enters {@code bye}.
     */
    public void run() {
        ui.showWelcome();

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
                case TODO -> addMission(Parser.parseTodo(command));
                case DEADLINE -> addMission(Parser.parseDeadline(command));
                case EVENT -> addMission(Parser.parseEvent(command));
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
     * Starts Glennon using its standard mission data file.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        new Glennon("data/glennon.txt").run();
    }

    /**
     * Adds a typed mission and displays its type-specific representation.
     *
     * @param mission mission to add
     * @throws GlennonException if the updated mission list cannot be saved
     */
    private void addMission(Task mission) throws GlennonException {
        missions.add(mission);
        storage.saveMissions(missions.asList());
        ui.showMissionAdded(mission, missions.size());
    }

}
