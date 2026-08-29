package glennon.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that occurs between specified start and end date-times.
 */
public class Event extends Task {
    /** Format used when showing event boundaries to the user. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d uuuu, h:mm a", Locale.ENGLISH);

    /** Format used when showing all-day event boundaries. */
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH);

    /** Date and time when the event starts. */
    private final LocalDateTime from;

    /** Date and time when the event ends. */
    private final LocalDateTime to;

    /**
     * Creates a pending event task with the given description and time range.
     *
     * @param description description of the event.
     * @param from date and time when the event starts.
     * @param to date and time when the event ends.
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
     * Checks whether any part of this event occurs on the given date. Both the
     * start and end dates are included for events spanning multiple days.
     *
     * @param date date to check.
     * @return true when the event overlaps that date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        LocalDate startDate = from.toLocalDate();
        LocalDate endDate = to.toLocalDate();
        return !date.isBefore(startDate) && !date.isAfter(endDate);
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
        if (from.toLocalTime().equals(LocalTime.MIN)
                && to.toLocalTime().equals(LocalTime.MAX)) {
            return "[E]" + super.toString()
                    + " (all day: " + from.toLocalDate().format(DATE_DISPLAY_FORMAT)
                    + " to: " + to.toLocalDate().format(DATE_DISPLAY_FORMAT) + ")";
        }
        return "[E]" + super.toString()
                + " (from: " + from.format(DISPLAY_FORMAT)
                + " to: " + to.format(DISPLAY_FORMAT) + ")";
    }
}
