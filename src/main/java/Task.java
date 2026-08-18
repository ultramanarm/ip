/**
 * Represents a mission tracked by Glennon and its completion status.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a pending task with the given description.
     *
     * @param description description of the mission
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return {@code X} when complete, or a space when pending
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
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
     * Returns the task description prefixed by its status icon.
     *
     * @return display-ready task text
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
