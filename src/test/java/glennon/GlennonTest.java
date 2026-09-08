package glennon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests GUI command responses, session state, and persistence boundaries.
 */
class GlennonTest {
    @TempDir
    private Path directory;

    private Glennon createGlennon() {
        return new Glennon(directory.resolve("missions.txt").toString());
    }

    @Test
    void normalizeLineEndings_mixedLineEndings_returnsLineFeeds() {
        assertEquals("first\nsecond\nthird", Glennon.normalizeLineEndings("first\r\nsecond\rthird"));
    }

    @Test
    void getResponse_missionLifecycle_preservesStateAndFormatting() {
        Glennon glennon = createGlennon();
        assertEquals("Hey there! Glennon online.\nWhat's the mission?", glennon.getWelcome());
        assertEquals("Mission added: [T][ ] read book\nMission log now has 1 mission.",
                glennon.getResponse("todo read book"));
        assertEquals("Mission marked complete:\n  [T][X] read book", glennon.getResponse("mark 1"));
        assertEquals("Mission log:\n1. [T][X] read book", glennon.getResponse("list"));
        assertEquals("Mission marked incomplete:\n  [T][ ] read book", glennon.getResponse("unmark 1"));
        assertEquals("Mission removed:\n  [T][ ] read book\nMission log now has 0 missions.",
                glennon.getResponse("delete 1"));
        assertEquals("Mission log:", glennon.getResponse("list"));
    }

    @Test
    void getResponse_scheduledMissions_supportsFindAndDateFilters() {
        Glennon glennon = createGlennon();
        glennon.getResponse("deadline report /by 2/12/2019 1800");
        glennon.getResponse("event workshop /from 2/12/2019 /to 3/12/2019");
        assertEquals("Here are the matching tasks in your list:\n1.[D][ ] report (by: Dec 2 2019, 6:00 PM)",
                glennon.getResponse("find report"));
        assertTrue(glennon.getResponse("on 3/12/2019").contains("workshop"));
        assertFalse(glennon.getResponse("on 3/12/2019").contains("report"));
    }

    @Test
    void getResponse_invalidInputs_reportsErrorsAndPreservesMissions() {
        Glennon glennon = createGlennon();
        glennon.getResponse("todo keep me");
        String before = glennon.getResponse("list");
        for (String input : new String[] {"", "todo", "delete 8", "mark nope", "find", "on 31/2/2026"}) {
            assertTrue(glennon.getResponse(input).startsWith("Mission control alert!\n"));
            assertEquals(before, glennon.getResponse("list"));
        }
    }

    @Test
    void getResponse_newSession_restoresSavedMissions() {
        Glennon original = createGlennon();
        original.getResponse("todo saved mission");
        original.getResponse("mark 1");
        Glennon restored = createGlennon();
        assertEquals("Mission log:\n1. [T][X] saved mission", restored.getResponse("list"));
        restored.getWelcome();
        assertEquals("Mission log:\n1. [T][X] saved mission", restored.getResponse("list"));
    }

    @Test
    void getResponse_exit_preventsFurtherMutations() {
        Glennon glennon = createGlennon();
        assertFalse(glennon.hasExited());
        String goodbye = "Signing off. Catch you on the next mission!";
        assertEquals(goodbye, glennon.getResponse("bye"));
        assertTrue(glennon.hasExited());
        assertEquals(goodbye, glennon.getResponse("todo too late"));
        assertEquals("Mission log:", createGlennon().getResponse("list"));
    }

    @Test
    void getWelcome_corruptStorage_blocksCommandsWithoutOverwritingFile() throws IOException {
        Path data = directory.resolve("missions.txt");
        Files.writeString(data, "corrupt data");
        Glennon glennon = createGlennon();
        assertEquals("Mission control alert!\nMission data is corrupted at line 1.", glennon.getWelcome());
        assertTrue(glennon.hasStartupError());
        assertEquals(glennon.getWelcome(), glennon.getResponse("todo cannot overwrite"));
        assertEquals("corrupt data", Files.readString(data));
    }

    @Test
    void getResponse_saveFailure_returnsHelpfulError() throws IOException {
        Path parent = directory.resolve("not-a-directory");
        Files.writeString(parent, "occupied");
        Glennon glennon = new Glennon(parent.resolve("missions.txt").toString());
        assertEquals("Mission control alert!\nGlennon could not save the mission data.",
                glennon.getResponse("todo blocked"));
    }
}
