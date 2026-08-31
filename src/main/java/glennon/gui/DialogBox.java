package glennon.gui;

import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Displays a speaker badge beside a wrapping chat message.
 */
public class DialogBox extends HBox {
    private DialogBox(String text, String speaker) {
        Label dialog = new Label(text);
        dialog.setWrapText(true);
        dialog.setMinHeight(USE_PREF_SIZE);
        Label badge = new Label(speaker);
        badge.setMinWidth(48);
        getChildren().addAll(dialog, badge);
        setAlignment(Pos.TOP_RIGHT);
        setSpacing(12);
        setPadding(new Insets(12));
    }

    /** Moves the speaker badge to the left for Glennon's responses. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Creates a right-aligned user message.
     *
     * @param text user input.
     * @return user dialog.
     */
    public static DialogBox getUserDialog(String text) {
        return new DialogBox(text, "You");
    }

    /**
     * Creates a left-aligned Glennon response.
     *
     * @param text response text.
     * @return Glennon dialog.
     */
    public static DialogBox getGlennonDialog(String text) {
        DialogBox dialogBox = new DialogBox(text, "G");
        dialogBox.flip();
        return dialogBox;
    }
}
