package glennon.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Displays a speaker badge beside a wrapping chat message loaded from FXML.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private Label displayBadge;

    private DialogBox(String text, String speaker) {
        FXMLLoader loader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Glennon could not load the dialog layout.", e);
        }
        dialog.setText(text);
        displayBadge.setText(speaker);
    }

    /** Moves the speaker badge to the left for Glennon's responses. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
        displayBadge.getStyleClass().add("glennon-badge");
    }

    /**
     * Creates a right-aligned user message.
     *
     * @param text user input.
     * @return user dialog.
     */
    public static DialogBox createUserDialog(String text) {
        return new DialogBox(text, "You");
    }

    /**
     * Creates a left-aligned Glennon response.
     *
     * @param text response text.
     * @return Glennon dialog.
     */
    public static DialogBox createGlennonDialog(String text) {
        DialogBox dialogBox = new DialogBox(text, "G");
        dialogBox.flip();
        return dialogBox;
    }
}
