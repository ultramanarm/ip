# Glennon

Glennon is a chatbot developed as a greenfield Java project. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/glennon/Glennon.java` file, right-click it, and choose `Run Glennon.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see Glennon's startup banner followed by `Hey there! Glennon online.`

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Dates and times

Deadline and event date-times use the `d/M/yyyy HHmm` format. The time uses a
24-hour clock, so `1800` means 6:00 PM.

For example:

```text
deadline return book /by 2/12/2019 1800
event project meeting /from 2/12/2019 1400 /to 2/12/2019 1600
```

Glennon displays those date-times in a more readable format:

```text
[D][ ] return book (by: Dec 2 2019, 6:00 PM)
[E][ ] project meeting (from: Dec 2 2019, 2:00 PM to: Dec 2 2019, 4:00 PM)
```

An event may start and end at the same date-time, but its end cannot be before
its start.

Use `on d/M/yyyy` to list deadlines due on a date and events that overlap that
date. To-dos are excluded because they have no date:

```text
on 2/12/2019
```
