package glennon;

import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Task;
import glennon.task.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        String divider = "_".repeat(60);
        String banner = """
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

        System.out.println(divider);
        System.out.print(banner);
        System.out.println("Hey there! Glennon online.");
        System.out.println("What's the mission?");
        System.out.println(divider);

        Scanner scanner = new Scanner(System.in);
        List<Task> missions = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(divider);

            try {
                boolean isMarkCommand = command.equals("mark") || command.startsWith("mark ");
                boolean isUnmarkCommand = command.equals("unmark")
                        || command.startsWith("unmark ");
                boolean isTodoCommand = command.equals("todo") || command.startsWith("todo ");
                boolean isDeadlineCommand = command.equals("deadline")
                        || command.startsWith("deadline ");
                boolean isEventCommand = command.equals("event")
                        || command.startsWith("event ");

                if (command.equals("bye")) {
                    System.out.println("Signing off. Catch you on the next mission!");
                    System.out.println(divider);
                    break;
                }

                if (command.equals("list")) {
                    System.out.println("Mission log:");
                    for (int i = 0; i < missions.size(); i++) {
                        System.out.println((i + 1) + ". " + missions.get(i));
                    }
                } else if (isMarkCommand || isUnmarkCommand) {
                    boolean shouldCompleteMission = isMarkCommand;
                    String commandName = shouldCompleteMission ? "mark" : "unmark";
                    String missionNumber = command.substring(commandName.length()).trim();
                    int missionIndex = parseMissionIndex(missionNumber, missions.size());
                    Task mission = missions.get(missionIndex);
                    if (shouldCompleteMission) {
                        mission.markAsDone();
                    } else {
                        mission.markAsNotDone();
                    }
                    String message = shouldCompleteMission
                            ? "Mission marked complete:"
                            : "Mission marked incomplete:";
                    System.out.println(message);
                    System.out.println("  " + mission);
                } else if (isTodoCommand) {
                    String description = command.substring("todo".length()).trim();
                    if (description.isEmpty()) {
                        throw new GlennonException("Please enter a mission after todo.");
                    }
                    addMission(missions, new Todo(description));
                } else if (isDeadlineCommand) {
                    String details = command.substring("deadline".length()).trim();
                    int bySeparatorIndex = details.indexOf(" /by ");
                    if (bySeparatorIndex <= 0
                            || bySeparatorIndex + " /by ".length() >= details.length()) {
                        throw new GlennonException(
                                "Use: deadline <mission> /by <date or time>.");
                    }
                    String description = details.substring(0, bySeparatorIndex).trim();
                    String by = details.substring(bySeparatorIndex + " /by ".length()).trim();
                    addMission(missions, new Deadline(description, by));
                } else if (isEventCommand) {
                    String details = command.substring("event".length()).trim();
                    int fromSeparatorIndex = details.indexOf(" /from ");
                    int fromValueIndex = fromSeparatorIndex + " /from ".length();
                    int toSeparatorIndex = details.indexOf(" /to ", fromValueIndex);
                    if (fromSeparatorIndex <= 0
                            || toSeparatorIndex <= fromValueIndex
                            || toSeparatorIndex + " /to ".length() >= details.length()) {
                        throw new GlennonException(
                                "Use: event <mission> /from <start> /to <end>.");
                    }
                    String description = details.substring(0, fromSeparatorIndex).trim();
                    String from = details.substring(fromValueIndex, toSeparatorIndex).trim();
                    String to = details.substring(toSeparatorIndex + " /to ".length()).trim();
                    if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                        throw new GlennonException(
                                "Use: event <mission> /from <start> /to <end>.");
                    }
                    addMission(missions, new Event(description, from, to));
                } else {
                    throw new GlennonException(
                            "Glennon doesn't recognize that command.\n"
                                    + "Try: todo, deadline, event, list, mark, unmark, or bye.");
                }
            } catch (GlennonException e) {
                System.out.println("Mission control alert!");
                System.out.println(e.getMessage());
            }
            System.out.println(divider);
        }

        scanner.close();
    }

    /**
     * Adds a typed mission and displays its type-specific representation.
     *
     * @param missions mission list for the current session
     * @param mission mission to add
     */
    private static void addMission(List<Task> missions, Task mission) {
        missions.add(mission);
        System.out.println("Mission added: " + mission);
        printMissionCount(missions);
    }

    /**
     * Displays the current mission total using the correct singular or plural noun.
     *
     * @param missions mission list for the current session
     */
    private static void printMissionCount(List<Task> missions) {
        int missionCount = missions.size();
        String missionLabel = missionCount == 1 ? "mission" : "missions";
        System.out.println("Mission log now has " + missionCount + " " + missionLabel + ".");
    }

    /**
     * Converts a one-based mission number into a valid list index.
     *
     * @param missionNumber user-provided mission number
     * @param missionCount number of missions currently stored
     * @return zero-based mission index
     * @throws GlennonException if the number is missing, malformed, or out of range
     */
    private static int parseMissionIndex(String missionNumber, int missionCount)
            throws GlennonException {
        try {
            int missionIndex = Integer.parseInt(missionNumber) - 1;
            if (missionIndex < 0 || missionIndex >= missionCount) {
                throw new GlennonException("Please enter a valid mission number.");
            }
            return missionIndex;
        } catch (NumberFormatException e) {
            throw new GlennonException("Please enter a valid mission number.", e);
        }
    }
}
