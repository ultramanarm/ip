package glennon.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that occurs between specified start and end date-times.
 */
public class Event extends Task {
    /** Format used when showing event boundaries to the user. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d uuuu, h:mm a", Locale.ENGLISH);

    /** Date and time when the event starts. */
    private final LocalDateTime from;

    /** Date and time when the event ends. */
    private final LocalDateTime to;

    /**
     * Creates a pending event task with the given description and time range.
     *
     * @param description description of the event
     * @param from date and time when the event starts
     * @param to date and time when the event ends
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event's start date and time.
     *
     * @return event start date and time
     */
    public LocalDateTime getFrom() {
        return from;
    }

    /**
     * Returns the event's end date and time.
     *
     * @return event end date and time
     */
    public LocalDateTime getTo() {
        return to;
    }

    /**
     * Returns the task with its type, completion status, and time range.
     *
     * @return display-ready text such as
     *         {@code [E][ ] meeting (from: Dec 2 2019, 2:00 PM
     *         to: Dec 2 2019, 4:00 PM)}
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + from.format(DISPLAY_FORMAT)
                + " to: " + to.format(DISPLAY_FORMAT) + ")";
    }
}
