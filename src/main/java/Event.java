/**
 * Represents a task that occurs between user-provided start and end times.
 * The time values are stored as text without date or time parsing.
 */
public class Event extends Task {
    /** Event start text supplied by the user. */
    private final String from;

    /** Event end text supplied by the user. */
    private final String to;

    /**
     * Creates a pending event task with the given description and time range.
     *
     * @param description description of the event
     * @param from date or time when the event starts
     * @param to date or time when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the task with its type, completion status, and time range.
     *
     * @return display-ready text such as
     *         {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
