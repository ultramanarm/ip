package glennon.task;

import java.time.LocalDate;

/**
 * Represents a mission tracked by Glennon and its completion status.
 * Subclasses supply the type marker shown to the user, so this class is
 * never instantiated on its own.
 */
public abstract class Task {
    /** User-provided description of the mission. */
    private final String description;

    /** Whether the mission has been completed. */
    private boolean isDone;

    /**
     * Creates a pending task with the given description.
     *
     * @param description description of the mission.
     */
    protected Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return {@code X} when complete, or a space when pending.
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the description supplied for this task.
     *
     * @return task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks whether this task has been completed.
     *
     * @return true when the task is complete.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Marks this task as complete.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as pending.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Checks whether this task is scheduled on a particular date. Tasks with
     * no date are not scheduled on any date by default.
     *
     * @param date date to check.
     * @return true when this task occurs on the given date.
     */
    public boolean occursOn(LocalDate date) {
        return false;
    }

    /**
     * Returns the task description prefixed by its status icon.
     *
     * @return display-ready task text.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
