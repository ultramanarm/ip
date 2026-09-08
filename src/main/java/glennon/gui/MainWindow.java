package glennon.gui;

import glennon.Glennon;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Handles chat interactions while leaving mission logic to Glennon.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Glennon glennon;

    /** Keeps the latest response visible when the dialog container grows. */
    @FXML
    private void initialize() {
        dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) ->
                scrollPane.setVvalue(1.0));
    }

    /**
     * Connects a session and displays its greeting or storage error.
     *
     * @param glennon application session used by this window.
     */
    public void setGlennon(Glennon glennon) {
        this.glennon = glennon;
        dialogContainer.getChildren().add(DialogBox.createGlennonDialog(glennon.getWelcome()));
        userInput.setDisable(glennon.hasStartupError());
        sendButton.setDisable(glennon.hasStartupError());
        Platform.runLater(userInput::requestFocus);
    }

    /** Appends the exchange and lets the goodbye remain visible before closing. */
    @FXML
    private void handleUserInput() {
        if (glennon.isExit() || glennon.hasStartupError()) {
            return;
        }
        String input = userInput.getText();
        dialogContainer.getChildren().addAll(
                DialogBox.createUserDialog(input),
                DialogBox.createGlennonDialog(glennon.getResponse(input)));
        userInput.clear();
        userInput.requestFocus();
        if (glennon.isExit()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> userInput.getScene().getWindow().hide());
            pause.play();
        }
    }
}
