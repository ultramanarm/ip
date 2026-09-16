# Glennon User Guide

Glennon is a personal mission tracker for to-dos, deadlines, and events. It
accepts one command per line and saves changes in `data/glennon.txt`.

Command words are case-sensitive. Leading and trailing spaces or tabs are
accepted, as are repeated spaces between command arguments. Internal spacing,
punctuation, and Unicode in mission descriptions are preserved.

## Using the window

Type a command and press Enter or click Send. Your commands appear on the
right, while Glennon's replies use the available width for longer mission
lists. Glennon's robot avatar appears in the header, and a subtle orbital
background sits behind the conversation. Errors have an **ATTENTION NEEDED**
heading and a contrasting color.
A rejected command stays selected in the input field so you can correct it
immediately; successful commands clear the field.

Resize the window to suit your screen. Messages and command hints wrap, and
the input stays below the conversation. Scroll up to read older replies;
resizing keeps your relative position in the history. Sending a new command
brings its reply into view. You can also Tab to the conversation and use
Page Up, Page Down, or the arrow keys to scroll with the keyboard.

## Adding missions

- `todo <mission>` adds an unscheduled mission.
- `deadline <mission> /by <d/M/yyyy [HHmm]>` adds a deadline. When the time is
  omitted, Glennon uses 11:59 PM.
- `event <mission> /from <d/M/yyyy [HHmm]> /to <d/M/yyyy [HHmm]>` adds an event.
  A date without a time represents the corresponding all-day boundary.
  The end must be after the start. A same-day all-day event is valid, but an
  event with identical start and end timestamps is rejected.

Use `/by` once for a deadline, and `/from` followed by `/to` once each for an
event. These are reserved parameter words when surrounded by whitespace in
scheduled commands. Ordinary slashes in descriptions, such as `C++/Java`, are
allowed. Missing values, repeated parameters, and impossible dates produce an
error without changing the mission log.

Examples:

```text
todo read the project brief
deadline submit report /by 18/9/2026 2359
event consultation /from 17/9/2026 1400 /to 17/9/2026 1500
```

## Sorting missions chronologically

Use `sort` to reorder the mission log chronologically. Deadlines are ordered by
their due date and time, while events are ordered by their start date and time.
Unscheduled to-dos appear after all scheduled missions. Missions with the same
date and time keep their previous relative order.

The sorted order is saved and is therefore retained the next time Glennon
starts. For example:

```text
sort
```

```text
Mission log sorted chronologically:
1. [E][ ] consultation (from: Sep 17 2026, 2:00 PM to: Sep 17 2026, 3:00 PM)
2. [D][ ] submit report (by: Sep 18 2026, 11:59 PM)
3. [T][ ] read the project brief
```

The command takes no arguments. Inputs such as `sort date` are rejected.

## Viewing and finding missions

- `list` displays every mission in its saved order.
- `find <keyword>` displays missions whose descriptions contain the exact,
  case-sensitive keyword.
- `on <d/M/yyyy>` displays deadlines and events occurring on the date.

## Updating missions

- `mark <number>` marks a mission complete.
- `unmark <number>` marks a mission incomplete.
- `delete <number>` removes a mission.

Numbers must contain digits `0` through `9` and identify an existing mission,
starting at `1`. Signs, decimals, and extra arguments are rejected.

Mission numbers are the one-based numbers shown by `list`, `find`, `on`, or
`sort`.

## Exiting

Use `bye` to close Glennon.

## Recovering from storage errors

Glennon starts with an empty log when `data/glennon.txt` does not exist. If an
existing file cannot be read or contains invalid mission data, Glennon reports
the problem and blocks changes to protect the file. Corruption messages identify
the affected line. Back up the file, correct the reported problem, and restart
Glennon. For permission errors, check access to both the file and its folder.

If saving fails, the mission log keeps its previous contents, order, and
completion states. Fix the file or folder problem, then retry the command.
Glennon writes a temporary file and replaces the saved file only after the write
succeeds. The storage location must support atomic file replacement; otherwise,
the save fails safely. Use an ordinary file path for the mission data, rather
than a directory or symbolic link.
