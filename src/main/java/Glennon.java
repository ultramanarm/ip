/**
 * Starts the Glennon chatbot.
 */
public class Glennon {
    /**
     * Displays the Glennon chatbot banner when the application starts.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        String banner = """
                +------------------------------------------------+
                |                                                |
                |        ________                                |
                |       / ____/ /__  ____  ____  ____  ____      |
                |      / / __/ / _ \\/ __ \\/ __ \\/ __ \\/ __ \\     |
                |     / /_/ / /  __/ / / / / / / /_/ / / / /     |
                |     \\____/_/\\___/_/ /_/_/ /_/\\____/_/ /_/      |
                |                                                |
                +------------------------------------------------+
                """;
        System.out.print(banner);
    }
}
