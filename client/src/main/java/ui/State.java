package ui;

public class State {

    public enum UserState {
        OUT,
        IN,
        PLAY
    }

    public static String printPrompt(UserState state) {
        return switch (state) {
            case OUT -> " [LOGGED OUT]>>> ";
            case IN -> " [SIGNED IN]>>> ";
            case PLAY -> " [PLAYING GAME]>>> ";
        };
    }
}
