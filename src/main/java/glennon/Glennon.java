package glennon;

import glennon.exception.GlennonException;
import glennon.task.Task;

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
                Parser.CommandType commandType = Parser.parseCommandType(command);

                if (commandType == Parser.CommandType.BYE) {
                    System.out.println("Signing off. Catch you on the next mission!");
                    System.out.println(divider);
                    break;
                }

                switch (commandType) {
                case LIST -> {
                    System.out.println("Mission log:");
                    for (int i = 0; i < missions.size(); i++) {
                        System.out.println((i + 1) + ". " + missions.get(i));
                    }
                }
                case DELETE -> {
                    int missionIndex = Parser.parseMissionIndex(
                            command, commandType, missions.size());
                    Task removedMission = missions.remove(missionIndex);
                    System.out.println("Mission removed:");
                    System.out.println("  " + removedMission);
                    printMissionCount(missions);
                }
                case MARK, UNMARK -> {
                    boolean shouldCompleteMission = commandType == Parser.CommandType.MARK;
                    int missionIndex = Parser.parseMissionIndex(
                            command, commandType, missions.size());
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
                }
                case TODO -> addMission(missions, Parser.parseTodo(command));
                case DEADLINE -> addMission(missions, Parser.parseDeadline(command));
                case EVENT -> addMission(missions, Parser.parseEvent(command));
                case UNKNOWN -> throw new GlennonException(
                        "Glennon doesn't recognize that command.\n"
                                + "Try: todo, deadline, event, list, mark, unmark, delete, or bye.");
                case BYE -> throw new IllegalStateException(
                        "Bye should be handled before dispatch.");
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

}
