# GUI regression plan

Use Zulu JDK `25.0.3.fx-zulu`. Run `./gradlew clean test` in a desktop
session; `MainWindowTest` loads real FXML and opens temporary windows.
All test data lives in JUnit temporary directories, never the user's save file.
The separate console plan remains unchanged and must also pass.

## Automated interaction coverage

| Case | Kind | Distinct behavior | JUnit method |
| --- | --- | --- | --- |
| GUI-01 | Positive | Fresh startup and greeting | `initialize_freshSession_displaysGreeting` |
| GUI-02 | Positive | Send button, response pair, input clearing | `handleUserInput_sendButton_addsExchangeAndClearsInput` |
| GUI-03 | Positive | Enter uses the same command path | `handleUserInput_enterKey_usesSameCommandHandler` |
| GUI-04 | Positive | Opposing speaker alignment and badges | `dialogBox_speakers_alignOnOppositeSides` |
| GUI-05 | Positive | Long text wraps at a narrow width | `handleUserInput_longMission_wrapsWithoutLosingText` |
| GUI-06 | Positive | Overflow scrolls to the latest exchange | `handleUserInput_manyExchanges_scrollsToLatestResponse` |
| GUI-07 | Positive | Goodbye blocks subsequent submissions | `handleUserInput_bye_disablesFurtherSubmissions` |
| GUI-08 | Negative | Empty command preserves existing missions | `handleUserInput_blankInput_reportsErrorAndPreservesMissions` |
| GUI-09 | Negative | Invalid deletion preserves existing missions | `handleUserInput_invalidIndex_reportsErrorAndPreservesMissions` |
| GUI-10 | Negative | Corrupt storage blocks input and preserves file | `initialize_corruptStorage_disablesInputAndPreservesData` |

All seven positive and three negative scenarios exercise distinct paths.
Their complete command sequences are unique. `GlennonTest` additionally tests
all mission command families, restart persistence, and storage write failures.

## Visual and packaged checks

1. Build `./gradlew shadowJar` and launch the JAR from a temporary directory.
2. Check the greeting, readable speaker badges, input focus, and button states.
3. Submit a long mission, a deadline, an event, `list`, and invalid input.
4. Resize down to the minimum and up to a large window. Check wrapping, no
   horizontal scroll, anchored controls, and no overlapping or clipped text.
5. Fill the chat, verify automatic scrolling, then scroll back to older replies.
6. Enter `bye`: its response appears, inputs disable, and the window closes
   after one second. Closing the window directly must also exit the application.
7. Relaunch from the same temporary directory and verify `list` restores data.

Do not run mutation checks against the project's real `data/glennon.txt`.
