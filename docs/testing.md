# Automated testing

Use Zulu JDK `25.0.3.fx-zulu`. The complete suite needs a desktop session
because it includes nineteen JavaFX interaction tests. Tests use temporary
data files; the console regression runner also uses a separate working
directory for each case.

## Run the complete checks

From the project root on macOS:

```sh
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3.fx-zulu"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
./gradlew clean check jacocoTestReport
python3 test/run_ui_plan.py --plan test/ui-test-plan.md -- \
  "$JAVA_HOME/bin/java" -cp "$PWD/build/classes/java/main" glennon.Glennon
```

Both version commands must report `25.0.3`. The console runner requires the
local `test-ui` skill and `test/ui-test-plan.md`, which are not tracked in this
working copy. It checks complete responses, including whitespace, and rejects
duplicate command sequences. Review that plan before running it and preserve
its expected output unless the intended behavior changes.

Reports:

- JUnit: `build/reports/tests/test/index.html`
- Coverage: `build/reports/jacoco/test/html/index.html`
- Machine-readable coverage: `build/reports/jacoco/test/jacocoTestReport.xml`
- Checkstyle: `build/reports/checkstyle/main.html` and `test.html`

## Tested contracts

| Area | Automated checks |
| --- | --- |
| Parser | Every command family and bound argument; missing/extra arguments; separators and repeated flags; index overflow; strict dates, leap centuries, and midnight/year transitions; Unicode and forbidden control characters |
| Tasks | Description validation; exact type/detail comparisons; completion transitions and idempotence; date overlap; all-day versus timed display; nanosecond precision; English date rendering with a Chinese format locale |
| Task list | Duplicates across the entire list; every indexed operation on empty/populated lists; defensive copying and read-only views; filter snapshots; stable sorting, ties, event start times, and preserved status |
| Storage | Exact serialized records; all task types and completion states; Unicode and precise date-time roundtrips; Windows line endings and missing final newline; corrupt records and duplicates; independent loads; atomic replacement and cleanup failures |
| Commands | Read-only commands never save and preserve task identities/status; exact result numbering and empty results; mutating commands preserve memory and disk after a failed save and support safe retries |
| Sessions | GUI-facing responses and error status; restart persistence; console greeting/dividers; EOF with or without a final newline; rejection followed by recovery; queued input after exit; startup failures preserve data and close input |
| JavaFX | Nineteen existing FXML interaction, validation, scrolling, focus, and resizing scenarios; see [the GUI plan](gui-test-plan.md) |

The A-MoreTesting increment adds 58 JUnit tests, bringing the suite to 221.
Several tests check tables of related input boundaries within one scenario.
These assertions verify observable results and preserved state, rather than
only invoking methods to increase coverage.

## Coverage scope and remaining gaps

JaCoCo measures all production classes outside `glennon.gui`. GUI tests still
execute; excluding their presentation classes from this report keeps core
coverage separate from visual checks. The project pins
[JaCoCo 0.8.14](https://github.com/jacoco/jacoco/releases/tag/v0.8.14), which
supports Java 25.

The expanded suite covers **518/523 core lines (99.0%)**, **205/208 branches
(98.6%)**, and **123/125 methods (98.4%)**. All task and command classes have
100% line and branch coverage. Treat these numbers as a snapshot; regenerate
the report after further changes.

The remaining gaps are:

- The no-argument `Glennon` constructor and CLI `main` forwarding method are
  outside the JUnit coverage. The full console plan exercises `main` in fresh
  processes, while JUnit tests `run()` with isolated streams and storage.
- Two parser branches reject empty descriptions after earlier argument and
  separator checks have already rejected those inputs. They cannot be reached
  through valid public call paths.
- Storage's default task-type switch arm is unreachable after field-count
  validation has already rejected unknown type markers. Tests verify that
  rejection at the public storage boundary.

No production behavior changes are needed for these tests. The existing console
test plan and its expected responses remain unchanged.

## Manual portability checks

Automated checks on macOS, including Unicode data, Windows-style line endings,
and Chinese format-locale assertions, do not substitute for running on another
operating system. Windows/Linux and alternate OS language settings are not
claimed as manually verified by this increment.

Use a temporary folder for each manual run. Follow the
[GUI visual and packaged checks](gui-test-plan.md#visual-and-packaged-checks),
then record the OS, JDK, language, display scale, resolution, and result for:

1. Launching the packaged application, entering English and Chinese mission
   descriptions, and restoring them after restart.
2. Minimum and maximized window sizes, display scaling, and long text wrapping
   without clipped controls or unreadable characters.
3. Keyboard input, focus, scrolling, closing the window, and the `bye` command.
4. Deadline/event date rendering and parsing under English and Chinese OS
   language settings.

Keep manual results separate from the automated pass counts and note any
environment that has not been tested.
