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
                +-----------------------------------------+
                |   ____ _                                |
                |  / ___| | ___ _ __  _ __   ___  _ __    |
                | | |  _| |/ _ \\ '_ \\| '_ \\ / _ \\| '_ \\   |
                | | |_| | |  __/ | | | | | | | (_) | | | ||
                |  \\____|_|\\___|_| |_|_| |_|\\___/|_| |_|  |
                +-----------------------------------------+
                """;
        System.out.print(banner);
    }
}
