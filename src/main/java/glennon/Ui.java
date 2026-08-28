package glennon;

import glennon.task.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all interaction with the user, reading typed commands and printing
 * Glennon's responses.
 */
public class Ui {
    /** Horizontal rule printed around each response. */
    private static final String DIVIDER = "_".repeat(60);

    /** Format used in headings for date-filter results. */
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH);

    /** Glennon title art displayed when a session starts. */
    private static final String BANNER = """
            +==========================================================+
            |                                                          |
            |             ________                                     |
            |            / ____/ /__  ____  ____  ____  ____           |
            |           / / __/ / _ \\/ __ \\/ __ \\/ __ \\/ __ \\          |
            |          / /_/ / /  __/ / / / / / / /_/ / / / /          |
            |          \\____/_/\\___/_/ /_/_/ /_/\\____/_/ /_/           |
            |                                                          |
            +==========================================================+
            """;

    /** Reads commands from standard input. */
    private final Scanner scanner;

    /**
     * Creates a user interface that reads commands from standard input.
     */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the banner and greeting shown when Glennon starts.
     */
    public void showWelcome() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Hey there! Glennon online.");
        System.out.println("What's the mission?");
        System.out.println(DIVIDER);
    }

    /**
     * Checks whether the user has entered another command.
     *
     * @return true while input remains available
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command typed by the user.
     *
     * @return the raw input line, without interpretation
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays the divider that separates one response from the next.
     */
    public void showDivider() {
        System.out.println(DIVIDER);
    }

    /**
     * Displays the message shown before Glennon exits.
     */
    public void showGoodbye() {
        System.out.println("Signing off. Catch you on the next mission!");
    }

    /**
     * Displays every stored mission with its one-based position.
     *
     * @param missions missions to display.
     */
    public void showMissionList(List<Task> missions) {
        System.out.println("Mission log:");
        for (int i = 0; i < missions.size(); i++) {
            System.out.println((i + 1) + ". " + missions.get(i));
        }
    }

    /**
     * Displays the deadlines and events occurring on a particular date.
     *
     * @param date date selected by the user.
     * @param missions missions scheduled on that date.
     */
    public void showScheduledMissions(LocalDate date, List<Task> missions) {
        System.out.println("Missions on " + date.format(DATE_DISPLAY_FORMAT) + ":");
        for (int i = 0; i < missions.size(); i++) {
            System.out.println((i + 1) + ". " + missions.get(i));
        }
    }

    /**
     * Confirms that a mission was added.
     *
     * @param mission mission that was added.
     * @param missionCount number of missions now stored.
     */
    public void showMissionAdded(Task mission, int missionCount) {
        System.out.println("Mission added: " + mission);
        showMissionCount(missionCount);
    }

    /**
     * Confirms that a mission was removed.
     *
     * @param mission mission that was removed.
     * @param missionCount number of missions now stored.
     */
    public void showMissionRemoved(Task mission, int missionCount) {
        System.out.println("Mission removed:");
        System.out.println("  " + mission);
        showMissionCount(missionCount);
    }

    /**
     * Confirms that a mission's completion status changed.
     *
     * @param mission mission whose status changed.
     * @param isComplete true when the mission was marked complete.
     */
    public void showMissionStatusChanged(Task mission, boolean isComplete) {
        System.out.println(isComplete
                ? "Mission marked complete:"
                : "Mission marked incomplete:");
        System.out.println("  " + mission);
    }

    /**
     * Displays an error raised while carrying out a command.
     *
     * @param message explanation shown to the user.
     */
    public void showError(String message) {
        System.out.println("Mission control alert!");
        System.out.println(message);
    }

    /**
     * Releases the input source once Glennon has finished.
     */
    public void close() {
        scanner.close();
    }

    /**
     * Displays the mission total using the correct singular or plural noun.
     *
     * @param missionCount number of missions currently stored.
     */
    private void showMissionCount(int missionCount) {
        String missionLabel = missionCount == 1 ? "mission" : "missions";
        System.out.println("Mission log now has " + missionCount + " " + missionLabel + ".");
    }
}
