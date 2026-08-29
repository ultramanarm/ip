package glennon;

import glennon.command.AddCommand;
import glennon.command.Command;
import glennon.command.DeleteCommand;
import glennon.command.ExitCommand;
import glennon.command.FindCommand;
import glennon.command.ListCommand;
import glennon.command.MarkCommand;
import glennon.command.OnCommand;
import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Recognizes Glennon commands and converts their arguments into values used by
 * the application.
 */
public final class Parser {
    /**
     * Identifies the command represented by a line of user input.
     */
    public enum CommandType {
        /** Ends the current application session. */
        BYE("bye", false),

        /** Displays every mission in the log. */
        LIST("list", false),

        /** Finds missions whose descriptions contain a keyword. */
        FIND("find", true),

        /** Displays scheduled missions occurring on a date. */
        ON("on", true),

        /** Marks a mission as complete. */
        MARK("mark", true),

        /** Marks a mission as incomplete. */
        UNMARK("unmark", true),

        /** Removes a mission from the log. */
        DELETE("delete", true),

        /** Adds a mission without a date or time. */
        TODO("todo", true),

        /** Adds a mission with a completion deadline. */
        DEADLINE("deadline", true),

        /** Adds a mission with a start and end time. */
        EVENT("event", true),

        /** Represents input that does not match a supported command. */
        UNKNOWN("", false);

        /** Keyword entered by the user to invoke this command. */
        private final String keyword;

        /** Whether the keyword may be followed by command arguments. */
        private final boolean acceptsArguments;

        /**
         * Creates a command type with its keyword and argument policy.
         *
         * @param keyword command keyword.
         * @param acceptsArguments whether text may follow the keyword.
         */
        CommandType(String keyword, boolean acceptsArguments) {
            this.keyword = keyword;
            this.acceptsArguments = acceptsArguments;
        }

        /**
         * Checks whether the input represents this command type.
         *
         * @param input complete user input.
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

    /** Format accepted for deadline and event date-times. */
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("d/M/uuuu HHmm")
                    .withResolverStyle(ResolverStyle.STRICT);

    /** Format accepted by the date-filter command. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /** Guidance shown when a scheduled date or date-time value is invalid. */
    private static final String DATE_TIME_USAGE =
            "Please enter dates as d/M/yyyy with an optional HHmm time, "
                    + "for example 2/12/2019 or 2/12/2019 1800.";

    /** Guidance shown when a date-filter value is invalid. */
    private static final String DATE_USAGE =
            "Please enter a date as d/M/yyyy, for example 2/12/2019.";

    /** Error shown when an event ends before it starts. */
    private static final String EVENT_ORDER_ERROR =
            "The event end must not be before its start.";

    /** Guidance shown when a deadline command cannot be parsed. */
    private static final String DEADLINE_USAGE =
            "Use: deadline <mission> /by <d/M/yyyy [HHmm]>.";

    /** Guidance shown when an event command cannot be parsed. */
    private static final String EVENT_USAGE =
            "Use: event <mission> /from <d/M/yyyy [HHmm]> /to <d/M/yyyy [HHmm]>.";

    /** Prevents creation of this stateless utility class. */
    private Parser() {
    }

    /**
     * Converts one complete line of user input into an executable command.
     *
     * @param input complete user input.
     * @return command containing all parsed arguments
     * @throws GlennonException if the command or any argument is invalid
     */
    public static Command parse(String input) throws GlennonException {
        CommandType commandType = parseCommandType(input);
        return switch (commandType) {
            case BYE -> new ExitCommand();
            case LIST -> new ListCommand();
            case FIND -> new FindCommand(parseKeyword(input));
            case ON -> new OnCommand(parseDate(input));
            case MARK -> new MarkCommand(parseMissionIndex(input, commandType), true);
            case UNMARK -> new MarkCommand(parseMissionIndex(input, commandType), false);
            case DELETE -> new DeleteCommand(parseMissionIndex(input, commandType));
            case TODO -> new AddCommand(parseTodo(input));
            case DEADLINE -> new AddCommand(parseDeadline(input));
            case EVENT -> new AddCommand(parseEvent(input));
            case UNKNOWN -> throw new GlennonException(
                    "Glennon doesn't recognize that command.\n"
                            + "Try: todo, deadline, event, list, find, on, mark, unmark, "
                            + "delete, or bye.");
        };
    }

    /**
     * Identifies the command type while preserving Glennon's case-sensitive
     * command syntax.
     *
     * @param input complete user input.
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
     * Parses the keyword supplied to a {@code find} command.
     *
     * @param input complete find command.
     * @return trimmed search keyword.
     * @throws GlennonException if the keyword is missing.
     */
    public static String parseKeyword(String input) throws GlennonException {
        String keyword = parseArguments(input, CommandType.FIND);
        if (keyword.isEmpty()) {
            throw new GlennonException("Please enter a keyword after find.");
        }
        return keyword;
    }

    /**
     * Parses a to-do command into a pending to-do mission.
     *
     * @param input complete to-do command.
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
     * @param input complete deadline command.
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
        return new Deadline(description, parseScheduledDateTime(by, LocalTime.of(23, 59)));
    }

    /**
     * Parses an event command into a pending event mission.
     *
     * @param input complete event command.
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
        LocalDateTime start = parseScheduledDateTime(from, LocalTime.MIN);
        LocalDateTime end = parseScheduledDateTime(to, LocalTime.MAX);
        if (end.isBefore(start)) {
            throw new GlennonException(EVENT_ORDER_ERROR);
        }
        return new Event(description, start, end);
    }

    /**
     * Converts a command's one-based mission number into a zero-based index.
     * The caller checks the index against the stored missions.
     *
     * @param input complete command containing the mission number.
     * @param commandType command whose arguments contain the number.
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
     * Parses the date supplied to an {@code on} command.
     *
     * @param input complete date-filter command.
     * @return parsed calendar date
     * @throws GlennonException if the date is missing, malformed, or impossible
     */
    public static LocalDate parseDate(String input) throws GlennonException {
        String value = parseArguments(input, CommandType.ON);
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new GlennonException(DATE_USAGE, e);
        }
    }

    /**
     * Removes a recognized command's keyword and surrounding argument spaces.
     *
     * @param input complete user input.
     * @param commandType recognized command type.
     * @return trimmed command arguments
     */
    private static String parseArguments(String input, CommandType commandType) {
        return input.substring(commandType.keyword.length()).trim();
    }

    /**
     * Converts a user-entered date or date-time into a strongly typed value.
     *
     * @param value date text with an optional time in {@code d/M/yyyy [HHmm]} format.
     * @param defaultTime time used when the input contains only a date.
     * @return parsed date and time
     * @throws GlennonException if the value is malformed or is not a real date
     */
    private static LocalDateTime parseScheduledDateTime(
            String value, LocalTime defaultTime) throws GlennonException {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (DateTimeParseException dateTimeException) {
            try {
                return LocalDate.parse(value, DATE_FORMAT).atTime(defaultTime);
            } catch (DateTimeParseException dateException) {
                throw new GlennonException(DATE_TIME_USAGE, dateException);
            }
        }
    }
}
