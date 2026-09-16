# Glennon User Guide

Glennon is a personal mission tracker for to-dos, deadlines, and events. It
accepts one command per line and saves changes in `data/glennon.txt`.

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

Mission numbers are the one-based numbers shown by `list`, `find`, `on`, or
`sort`.

## Exiting

Use `bye` to close Glennon.
