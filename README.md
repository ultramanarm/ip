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

Deadline and event dates use the `d/M/yyyy` format and may include a time in
`HHmm` format. The time uses a 24-hour clock, so `1800` means 6:00 PM. A
date-only deadline defaults to 11:59 PM, while date-only event boundaries make
an all-day event.

For example:

```text
deadline return book /by 2/12/2019 1800
event project meeting /from 2/12/2019 1400 /to 2/12/2019 1600
deadline submit report /by 3/12/2019
event conference /from 4/12/2019 /to 5/12/2019
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

## Checking coding style

Checkstyle automatically checks production and test Java sources for formatting,
import ordering, naming, and Javadoc rules. It complements the
`seedu-java-coding-standard` skill: automated checks are repeatable, while code
review is still needed for meaningful names, clear comments, and design.

The configuration in `config/checkstyle/` comes from
[AddressBook Level 3](https://github.com/se-edu/addressbook-level3/tree/master/config/checkstyle),
following the [SE-EDU tutorial](https://se-education.org/guides/tutorials/checkstyle.html).
Checkstyle is pinned to version 11.0.0. Both errors and warnings fail the build.

On macOS, select the required JDK and run the checks from the project root:

```shell
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3.fx-zulu"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew checkstyleMain checkstyleTest
```

Reports are generated at `build/reports/checkstyle/main.html` and
`build/reports/checkstyle/test.html`. `./gradlew check` runs both Checkstyle tasks
and the JUnit suite; the console UI test plan is run separately.

For editor feedback, install IntelliJ's Checkstyle-IDEA plugin, select version
11.0.0, add `config/checkstyle/checkstyle.xml` as an active local configuration,
and set its scan scope to include Java test sources.

## Creating an executable JAR

The Shadow plugin packages Glennon and all of its runtime dependencies into one
executable (or "fat") JAR. From the project root, run:

```shell
./gradlew clean shadowJar
```

On Windows, use `gradlew.bat clean shadowJar` instead. The generated file is:

```text
build/libs/Glennon.jar
```

To test the distributable in the same way as an end user, copy `Glennon.jar`
into an empty folder, open a terminal in that folder, and run:

```shell
java -jar "Glennon.jar"
```

Glennon reads and writes its `data/glennon.txt` save file relative to the folder
from which the JAR is run. The `build/` directory is ignored by Git, so the
generated JAR should be attached to a GitHub release instead of committed to
the repository.
