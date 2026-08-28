package glennon.command;

import glennon.Storage;
import glennon.TaskList;
import glennon.Ui;

/**
 * Ends the current Glennon session.
 */
public final class ExitCommand extends Command {
    /**
     * Displays Glennon's goodbye message.
     *
     * @param missions missions in the current session
     * @param ui interface used to display the result
     * @param storage storage used by the application
     */
    @Override
    public void execute(TaskList missions, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    /**
     * Reports that this command ends the application.
     *
     * @return true
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
