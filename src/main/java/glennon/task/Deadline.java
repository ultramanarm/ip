package glennon.task;

/**
 * Represents a task that must be completed by a specified date or time.
 * The deadline is stored as user-provided text without date parsing.
 */
public class Deadline extends Task {
    /** Deadline text supplied by the user. */
    private final String by;

    /**
     * Creates a pending deadline task with the given description and deadline.
     *
     * @param description description of the task
     * @param by date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline text supplied for this task.
     *
     * @return deadline text
     */
    public String getBy() {
        return by;
    }

    /**
     * Returns the task with its type, completion status, and deadline.
     *
     * @return display-ready text such as {@code [D][ ] return book (by: Sunday)}
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
