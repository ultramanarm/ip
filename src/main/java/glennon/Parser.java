package glennon;

import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Todo;

/**
 * Recognizes Glennon commands and converts their arguments into values used by
 * the application.
 */
public final class Parser {
    /**
     * Identifies the command represented by a line of user input.
     */
    public enum CommandType {
        BYE("bye", false),
        LIST("list", false),
        MARK("mark", true),
        UNMARK("unmark", true),
        DELETE("delete", true),
        TODO("todo", true),
        DEADLINE("deadline", true),
        EVENT("event", true),
        UNKNOWN("", false);

        /** Keyword entered by the user to invoke this command. */
        private final String keyword;

        /** Whether the keyword may be followed by command arguments. */
        private final boolean acceptsArguments;

        /**
         * Creates a command type with its keyword and argument policy.
         *
         * @param keyword command keyword
         * @param acceptsArguments whether text may follow the keyword
         */
        CommandType(String keyword, boolean acceptsArguments) {
            this.keyword = keyword;
            this.acceptsArguments = acceptsArguments;
        }

        /**
         * Checks whether the input represents this command type.
         *
         * @param input complete user input
         * @return true when the input starts with this command correctly
         */
        private boolean matches(String input) {
            return input.equals(keyword)
                    || acceptsArguments && input.startsWith(keyword + " ");
        }
    }

    /** Separates a deadline's description from its date text. */
    private static final String BY_SEPARATOR = " /by ";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = " /from ";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = " /to ";

    /** Guidance shown when a deadline command cannot be parsed. */
    private static final String DEADLINE_USAGE =
            "Use: deadline <mission> /by <date or time>.";

    /** Guidance shown when an event command cannot be parsed. */
    private static final String EVENT_USAGE =
            "Use: event <mission> /from <start> /to <end>.";

    /** Prevents creation of this stateless utility class. */
    private Parser() {
    }

    /**
     * Identifies the command type while preserving Glennon's case-sensitive
     * command syntax.
     *
     * @param input complete user input
     * @return matching command type, or {@link CommandType#UNKNOWN}
     */
    public static CommandType parseCommandType(String input) {
        for (CommandType commandType : CommandType.values()) {
            if (commandType != CommandType.UNKNOWN && commandType.matches(input)) {
                return commandType;
            }
        }
        return CommandType.UNKNOWN;
    }

    /**
     * Parses a to-do command into a pending to-do mission.
     *
     * @param input complete to-do command
     * @return parsed to-do mission
     * @throws GlennonException if the description is missing
     */
    public static Todo parseTodo(String input) throws GlennonException {
        String description = parseArguments(input, CommandType.TODO);
        if (description.isEmpty()) {
            throw new GlennonException("Please enter a mission after todo.");
        }
        return new Todo(description);
    }

    /**
     * Parses a deadline command into a pending deadline mission.
     *
     * @param input complete deadline command
     * @return parsed deadline mission
     * @throws GlennonException if the description or deadline is missing
     */
    public static Deadline parseDeadline(String input) throws GlennonException {
        String details = parseArguments(input, CommandType.DEADLINE);
        int bySeparatorIndex = details.indexOf(BY_SEPARATOR);
        if (bySeparatorIndex <= 0
                || bySeparatorIndex + BY_SEPARATOR.length() >= details.length()) {
            throw new GlennonException(DEADLINE_USAGE);
        }
        String description = details.substring(0, bySeparatorIndex).trim();
        String by = details.substring(bySeparatorIndex + BY_SEPARATOR.length()).trim();
        return new Deadline(description, by);
    }

    /**
     * Parses an event command into a pending event mission.
     *
     * @param input complete event command
     * @return parsed event mission
     * @throws GlennonException if its description, start, or end is missing
     */
    public static Event parseEvent(String input) throws GlennonException {
        String details = parseArguments(input, CommandType.EVENT);
        int fromSeparatorIndex = details.indexOf(FROM_SEPARATOR);
        int fromValueIndex = fromSeparatorIndex + FROM_SEPARATOR.length();
        int toSeparatorIndex = details.indexOf(TO_SEPARATOR, fromValueIndex);
        if (fromSeparatorIndex <= 0
                || toSeparatorIndex <= fromValueIndex
                || toSeparatorIndex + TO_SEPARATOR.length() >= details.length()) {
            throw new GlennonException(EVENT_USAGE);
        }
        String description = details.substring(0, fromSeparatorIndex).trim();
        String from = details.substring(fromValueIndex, toSeparatorIndex).trim();
        String to = details.substring(toSeparatorIndex + TO_SEPARATOR.length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new GlennonException(EVENT_USAGE);
        }
        return new Event(description, from, to);
    }

    /**
     * Converts a command's one-based mission number into a zero-based index.
     * The caller checks the index against the stored missions.
     *
     * @param input complete command containing the mission number
     * @param commandType command whose arguments contain the number
     * @return zero-based mission index
     * @throws GlennonException if the number is missing or not an integer
     */
    public static int parseMissionIndex(
            String input, CommandType commandType) throws GlennonException {
        String missionNumber = parseArguments(input, commandType);
        try {
            return Integer.parseInt(missionNumber) - 1;
        } catch (NumberFormatException e) {
            throw new GlennonException("Please enter a valid mission number.", e);
        }
    }

    /**
     * Removes a recognized command's keyword and surrounding argument spaces.
     *
     * @param input complete user input
     * @param commandType recognized command type
     * @return trimmed command arguments
     */
    private static String parseArguments(String input, CommandType commandType) {
        return input.substring(commandType.keyword.length()).trim();
    }
}
