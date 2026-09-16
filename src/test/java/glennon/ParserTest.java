package glennon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import glennon.exception.GlennonException;
import glennon.task.Deadline;
import glennon.task.Event;
import glennon.task.Todo;

/**
 * Tests Glennon's parsing and input-validation rules.
 */
class ParserTest {
    @Test
    void parseCommandType_validCommands_returnsMatchingTypes() {
        assertEquals(Parser.CommandType.BYE, Parser.parseCommandType("bye"));
        assertEquals(Parser.CommandType.LIST, Parser.parseCommandType("list"));
        assertEquals(Parser.CommandType.SORT, Parser.parseCommandType("sort"));
        assertEquals(Parser.CommandType.FIND, Parser.parseCommandType("find book"));
        assertEquals(Parser.CommandType.TODO, Parser.parseCommandType("todo read book"));
        assertEquals(Parser.CommandType.DEADLINE,
                Parser.parseCommandType("deadline submit report /by 2/12/2019 1800"));
        assertEquals(Parser.CommandType.EVENT,
                Parser.parseCommandType("event meeting /from 2/12/2019 1400 /to 2/12/2019 1600"));
    }

    @Test
    void parseCommandType_keywordPrefixOrWrongCase_returnsUnknown() {
        assertEquals(Parser.CommandType.UNKNOWN, Parser.parseCommandType("listing"));
        assertEquals(Parser.CommandType.UNKNOWN, Parser.parseCommandType("Todo read book"));
        assertEquals(Parser.CommandType.UNKNOWN, Parser.parseCommandType(" bye"));
    }

    @Test
    void parseTodo_validDescription_returnsTodo() throws GlennonException {
        Todo todo = Parser.parseTodo("todo   read the JUnit guide   ");

        assertEquals("read the JUnit guide", todo.getDescription());
    }

    @Test
    void parseTodo_missingDescription_throwsHelpfulException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseTodo("todo   "));

        assertEquals("Please enter a mission after todo.", exception.getMessage());
    }

    @Test
    void parseKeyword_validKeyword_returnsTrimmedText() throws GlennonException {
        assertEquals("project book", Parser.parseKeyword("find   project book   "));
    }

    @Test
    void parseKeyword_missingKeyword_throwsHelpfulException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseKeyword("find   "));

        assertEquals("Please enter a keyword after find.", exception.getMessage());
    }

    @Test
    void parseDeadline_validLeapDay_returnsDeadline() throws GlennonException {
        Deadline deadline = Parser.parseDeadline("deadline celebrate /by 29/2/2024 0905");

        assertEquals("celebrate", deadline.getDescription());
        assertEquals(LocalDateTime.of(2024, 2, 29, 9, 5), deadline.getDueDateTime());
    }

    @Test
    void parseDeadline_dateWithoutTime_defaultsToEndOfDay() throws GlennonException {
        Deadline deadline = Parser.parseDeadline("deadline submit report /by 2/12/2019");

        assertEquals(LocalDateTime.of(2019, 12, 2, 23, 59), deadline.getDueDateTime());
    }

    @Test
    void parseDeadline_missingSeparator_throwsUsageException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseDeadline("deadline submit report 2/12/2019 1800"));

        assertEquals("Use: deadline <mission> /by <d/M/yyyy [HHmm]>.", exception.getMessage());
    }

    @Test
    void parseDeadline_impossibleDate_throwsDateTimeException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseDeadline("deadline submit report /by 31/2/2025 1800"));

        assertEquals("Please enter dates as d/M/yyyy with an optional HHmm time, "
                        + "for example 2/12/2019 or 2/12/2019 1800.",
                exception.getMessage());
    }

    @Test
    void parseEvent_multiDayRange_returnsEvent() throws GlennonException {
        Event event = Parser.parseEvent(
                "event hackathon /from 31/12/2025 2300 /to 1/1/2026 0100");

        assertEquals("hackathon", event.getDescription());
        assertEquals(LocalDateTime.of(2025, 12, 31, 23, 0), event.getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 1, 0), event.getEndDateTime());
    }

    @Test
    void parseEvent_datesWithoutTimes_defaultsToAllDayBoundaries() throws GlennonException {
        Event event = Parser.parseEvent("event conference /from 2/12/2019 /to 3/12/2019");

        assertEquals(LocalDate.of(2019, 12, 2).atStartOfDay(), event.getStartDateTime());
        assertEquals(LocalDate.of(2019, 12, 3).atTime(LocalTime.MAX), event.getEndDateTime());
        assertEquals("[E][ ] conference (all day: Dec 2 2019 to: Dec 3 2019)", event.toString());
    }

    @Test
    void parseEvent_dateOnlyEnd_defaultsToEndOfDay() throws GlennonException {
        Event event = Parser.parseEvent(
                "event workshop /from 2/12/2019 1400 /to 2/12/2019");

        assertEquals(LocalDateTime.of(2019, 12, 2, 14, 0), event.getStartDateTime());
        assertEquals(LocalDate.of(2019, 12, 2).atTime(LocalTime.MAX), event.getEndDateTime());
    }

    @Test
    void parseEvent_sameStartAndEnd_returnsEvent() throws GlennonException {
        Event event = Parser.parseEvent(
                "event checkpoint /from 2/12/2019 1800 /to 2/12/2019 1800");

        assertEquals(event.getStartDateTime(), event.getEndDateTime());
    }

    @Test
    void parseEvent_endBeforeStart_throwsOrderingException() {
        GlennonException exception = assertThrows(GlennonException.class, () -> Parser.parseEvent(
                "event meeting /from 2/12/2019 1801 /to 2/12/2019 1800"));

        assertEquals("The event end must not be before its start.", exception.getMessage());
    }

    @Test
    void parseEvent_missingEnd_throwsUsageException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseEvent("event meeting /from 2/12/2019 1800 /to "));

        assertEquals("Use: event <mission> /from <d/M/yyyy [HHmm]> /to <d/M/yyyy [HHmm]>.",
                exception.getMessage());
    }

    @Test
    void parseMissionIndex_oneBasedNumber_returnsZeroBasedIndex() throws GlennonException {
        assertEquals(0, Parser.parseMissionIndex("mark 1", Parser.CommandType.MARK));
        assertEquals(41, Parser.parseMissionIndex("delete 42", Parser.CommandType.DELETE));
    }

    @Test
    void parseMissionIndex_nonInteger_throwsHelpfulException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parseMissionIndex("mark first", Parser.CommandType.MARK));

        assertEquals("Please enter a valid mission number.", exception.getMessage());
    }

    @Test
    void parseTodo_mismatchedCommand_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> Parser.parseTodo("find book"));
    }

    @Test
    void parseMissionIndex_commandWithoutArguments_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> Parser.parseMissionIndex(
                "list", Parser.CommandType.LIST));
    }

    @Test
    void parseDate_validLeapDay_returnsDate() throws GlennonException {
        assertEquals(LocalDate.of(2024, 2, 29), Parser.parseDate("on 29/2/2024"));
    }

    @Test
    void parseDate_invalidOrMalformedDate_throwsHelpfulException() {
        GlennonException impossibleDate = assertThrows(
                GlennonException.class, () -> Parser.parseDate("on 29/2/2023"));
        GlennonException wrongFormat = assertThrows(
                GlennonException.class, () -> Parser.parseDate("on 2024-02-29"));

        assertEquals("Please enter a date as d/M/yyyy, for example 2/12/2019.",
                impossibleDate.getMessage());
        assertEquals(impossibleDate.getMessage(), wrongFormat.getMessage());
    }

    @Test
    void parse_taskCommands_returnsExecutableCommand() throws GlennonException {
        assertInstanceOf(glennon.command.AddCommand.class, Parser.parse("todo read book"));
        assertInstanceOf(glennon.command.FindCommand.class, Parser.parse("find book"));
        assertInstanceOf(glennon.command.MarkCommand.class, Parser.parse("mark 1"));
        assertInstanceOf(glennon.command.OnCommand.class, Parser.parse("on 2/12/2019"));
        assertInstanceOf(glennon.command.SortCommand.class, Parser.parse("sort"));
    }

    @Test
    void parse_unknownCommand_throwsGuidanceException() {
        GlennonException exception = assertThrows(
                GlennonException.class, () -> Parser.parse("launch rocket"));

        assertEquals("Glennon doesn't recognize that command.\n"
                        + "Try: todo, deadline, event, list, sort, find, on, mark, "
                        + "unmark, delete, or bye.",
                exception.getMessage());
    }
}
