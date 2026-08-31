package glennon;

import java.nio.file.Path;

import glennon.command.Command;
import glennon.exception.GlennonException;
import glennon.task.TaskList;

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
     * @param dataPath path of the file used to persist missions.
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
                Command parsedCommand = Parser.parse(command);
                parsedCommand.execute(missions, ui, storage);
                isSigningOff = parsedCommand.isExit();
            } catch (GlennonException e) {
                ui.showError(e.getMessage());
            } finally {
                ui.showDivider();
            }
        }

        ui.close();
    }

    /**
     * Starts Glennon using its standard mission data file.
     *
     * @param args command-line arguments; currently unused.
     */
    public static void main(String[] args) {
        new Glennon("data/glennon.txt").run();
    }

}
