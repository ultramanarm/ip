package glennon.gui;

import glennon.Glennon;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Presents Glennon's mission commands in a JavaFX chat window.
 */
public class Main extends Application {
    private final Glennon glennon = new Glennon();
    private final VBox dialogContainer = new VBox();
    private final TextField userInput = new TextField();
    private final Button sendButton = new Button("Send");

    @Override
    public void start(Stage stage) {
        ScrollPane scrollPane = new ScrollPane(dialogContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) ->
                scrollPane.setVvalue(1.0));
        userInput.setPromptText("What's the mission?");
        userInput.setOnAction(event -> handleUserInput());
        sendButton.setOnAction(event -> handleUserInput());
        userInput.setPrefHeight(42);
        sendButton.setPrefSize(76, 42);

        AnchorPane root = new AnchorPane(scrollPane, userInput, sendButton);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 56.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(userInput, 8.0);
        AnchorPane.setLeftAnchor(userInput, 8.0);
        AnchorPane.setRightAnchor(userInput, 92.0);
        AnchorPane.setBottomAnchor(sendButton, 8.0);
        AnchorPane.setRightAnchor(sendButton, 8.0);
        dialogContainer.getChildren().add(DialogBox.getGlennonDialog(glennon.getWelcome()));
        userInput.setDisable(glennon.hasStartupError());
        sendButton.setDisable(glennon.hasStartupError());
        stage.setTitle("Glennon | Mission Control");
        stage.setScene(new Scene(root, 480, 640));
        stage.setMinWidth(380);
        stage.setMinHeight(360);
        stage.show();
        userInput.requestFocus();
    }

    /** Appends a command and its response, allowing the goodbye to display before closing. */
    private void handleUserInput() {
        String input = userInput.getText();
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getGlennonDialog(glennon.getResponse(input)));
        userInput.clear();
        userInput.requestFocus();
        if (glennon.isExit()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}
