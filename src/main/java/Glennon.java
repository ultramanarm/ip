/**
 * Starts the Glennon chatbot.
 */
public class Glennon {
    /**
     * Greets the user, displays a farewell message, and then exits.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        String divider = "_".repeat(60);
        String banner = """
                +==========================================================+
                |                                                          |
                |             ________                                     |
                |            / ____/ /__  ____  ____  ____  ____           |
                |           / / __/ / _ \\/ __ \\/ __ \\/ __ \\/ __ \\          |
                |          / /_/ / /  __/ / / / / / / /_/ / / / /          |
                |          \\____/_/\\___/_/ /_/_/ /_/\\____/_/ /_/           |
                |                                                          |
                +==========================================================+
                """;

        System.out.println(divider);
        System.out.print(banner);
        System.out.println("Hey there! Glennon online.");
        System.out.println("What's the mission?");
        System.out.println(divider);
        System.out.println("Signing off. Catch you on the next mission!");
        System.out.println(divider);
    }
}
