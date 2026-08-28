package glennon.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that must be completed by a specified date and time.
 */
public class Deadline extends Task {
    /** Format used when showing deadlines to the user. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d uuuu, h:mm a", Locale.ENGLISH);

    /** Date and time by which this task should be completed. */
    private final LocalDateTime by;

    /**
     * Creates a pending deadline task with the given description and deadline.
     *
     * @param description description of the task
     * @param by date and time by which the task should be completed
     */
    public Deadline(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline date and time.
     *
     * @return deadline date and time
     */
    public LocalDateTime getBy() {
        return by;
    }

    /**
     * Returns the task with its type, completion status, and deadline.
     *
     * @return display-ready text such as
     *         {@code [D][ ] return book (by: Dec 2 2019, 6:00 PM)}
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }
}
