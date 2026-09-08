package glennon.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import glennon.Glennon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Exercises the real FXML controls on the JavaFX application thread.
 */
class MainWindowTest {
    @TempDir
    private Path directory;

    private AnchorPane root;
    private TextField input;
    private Button sendButton;
    private VBox dialogContainer;
    private Stage stage;

    @BeforeAll
    static void startJavaFx() throws Exception {
        FutureTask<Void> startup = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        Platform.startup(startup);
        startup.get(10, TimeUnit.SECONDS);
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    /** Runs a complete scenario on the FX thread and propagates assertion failures. */
    private void runScenario(Runnable scenario) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
                root = loader.load();
                loader.<MainWindow>getController().setGlennon(
                        new Glennon(directory.resolve("missions.txt").toString()));
                stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                input = (TextField) root.lookup("#userInput");
                sendButton = (Button) root.lookup("#sendButton");
                dialogContainer = (VBox) root.lookup("#dialogContainer");
                scenario.run();
            } finally {
                if (stage != null) {
                    stage.close();
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private void submit(String command) {
        input.setText(command);
        sendButton.fire();
    }

    private String lastResponse() {
        return ((Label) dialogContainer.getChildren().getLast().lookup("#dialog")).getText();
    }

    @Test
    void initialize_freshSession_displaysGreeting() throws Exception {
        runScenario(() -> {
            assertEquals(1, dialogContainer.getChildren().size());
            assertEquals("Hey there! Glennon online.\nWhat's the mission?", lastResponse());
            assertFalse(input.isDisabled());
        });
    }

    @Test
    void handleUserInput_sendButton_addsExchangeAndClearsInput() throws Exception {
        runScenario(() -> {
            submit("todo send a mission");
            assertEquals(3, dialogContainer.getChildren().size());
            assertEquals("", input.getText());
            assertEquals("Mission added: [T][ ] send a mission\nMission log now has 1 mission.", lastResponse());
        });
    }

    @Test
    void handleUserInput_enterKey_usesSameCommandHandler() throws Exception {
        runScenario(() -> {
            input.setText("todo keyboard mission");
            input.fireEvent(new ActionEvent());
            submit("list");
            assertEquals("Mission log:\n1. [T][ ] keyboard mission", lastResponse());
        });
    }

    @Test
    void dialogBox_speakers_alignOnOppositeSides() throws Exception {
        runScenario(() -> {
            submit("list");
            DialogBox user = (DialogBox) dialogContainer.getChildren().get(1);
            DialogBox reply = (DialogBox) dialogContainer.getChildren().get(2);
            assertEquals(Pos.TOP_RIGHT, user.getAlignment());
            assertEquals(Pos.TOP_LEFT, reply.getAlignment());
            assertEquals("You", ((Label) user.getChildren().getLast()).getText());
            assertEquals("G", ((Label) reply.getChildren().getFirst()).getText());
        });
    }

    @Test
    void handleUserInput_longMission_wrapsWithoutLosingText() throws Exception {
        runScenario(() -> {
            String mission = "a long mission description ".repeat(20).stripTrailing();
            submit("todo " + mission);
            root.setPrefWidth(380);
            root.resize(380, 500);
            root.applyCss();
            root.layout();
            Label reply = (Label) dialogContainer.getChildren().getLast().lookup("#dialog");
            assertTrue(reply.isWrapText());
            assertTrue(reply.getHeight() > 40);
            assertTrue(reply.getText().contains(mission));
            assertTrue(reply.getWidth() < 380);
        });
    }

    @Test
    void handleUserInput_manyExchanges_scrollsToLatestResponse() throws Exception {
        runScenario(() -> {
            for (int i = 0; i < 20; i++) {
                submit("todo mission " + i);
            }
            root.applyCss();
            root.layout();
            ScrollPane scroll = (ScrollPane) root.lookup("#scrollPane");
            assertEquals(1.0, scroll.getVvalue());
            assertEquals(41, dialogContainer.getChildren().size());
        });
    }

    @Test
    void handleUserInput_bye_disablesFurtherSubmissions() throws Exception {
        runScenario(() -> {
            submit("bye");
            assertEquals("Signing off. Catch you on the next mission!", lastResponse());
            assertTrue(input.isDisabled());
            assertTrue(sendButton.isDisabled());
            submit("todo too late");
            assertEquals(3, dialogContainer.getChildren().size());
        });
    }

    @Test
    void handleUserInput_blankInput_reportsErrorAndPreservesMissions() throws Exception {
        runScenario(() -> {
            submit("todo keep blank case");
            submit("");
            assertTrue(lastResponse().startsWith("Mission control alert!"));
            submit("list");
            assertEquals("Mission log:\n1. [T][ ] keep blank case", lastResponse());
        });
    }

    @Test
    void handleUserInput_invalidIndex_reportsErrorAndPreservesMissions() throws Exception {
        runScenario(() -> {
            submit("todo keep index case");
            submit("delete 99");
            assertTrue(lastResponse().startsWith("Mission control alert!"));
            submit("list");
            assertEquals("Mission log:\n1. [T][ ] keep index case", lastResponse());
        });
    }

    @Test
    void initialize_corruptStorage_disablesInputAndPreservesData() throws Exception {
        Files.writeString(directory.resolve("missions.txt"), "corrupt");
        runScenario(() -> {
            assertTrue(lastResponse().contains("Mission data is corrupted at line 1."));
            assertTrue(input.isDisabled());
            assertTrue(sendButton.isDisabled());
            submit("todo cannot overwrite");
            assertEquals(1, dialogContainer.getChildren().size());
        });
        assertEquals("corrupt", Files.readString(directory.resolve("missions.txt")));
    }
}
