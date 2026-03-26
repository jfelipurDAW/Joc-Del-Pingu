package gamePanel;

import entity.Player;
import java.util.List;

public class GameSetupConfig {
    private static List<Player> players;
    private static boolean sealEnabled = false;

    public static List<Player> getPlayers() {
        return players;
    }

    public static void setPlayers(List<Player> players) {
        GameSetupConfig.players = players;
    }

    public static boolean isSealEnabled() {
        return sealEnabled;
    }

    public static void setSealEnabled(boolean enabled) {
        GameSetupConfig.sealEnabled = enabled;
    }
    private static boolean isLoadedGame = false;
    private static java.util.List<String> loadedBoardState;
    private static int loadedTurnIndex;
    private static java.util.Map<String, Object> loadedSealState;

    public static boolean isLoadedGame() { return isLoadedGame; }
    public static void setLoadedGame(boolean loadedGame) { isLoadedGame = loadedGame; }

    public static java.util.List<String> getLoadedBoardState() { return loadedBoardState; }
    public static void setLoadedBoardState(java.util.List<String> loadedBoardState) { GameSetupConfig.loadedBoardState = loadedBoardState; }

    public static int getLoadedTurnIndex() { return loadedTurnIndex; }
    public static void setLoadedTurnIndex(int loadedTurnIndex) { GameSetupConfig.loadedTurnIndex = loadedTurnIndex; }

    public static java.util.Map<String, Object> getLoadedSealState() { return loadedSealState; }
    public static void setLoadedSealState(java.util.Map<String, Object> loadedSealState) { GameSetupConfig.loadedSealState = loadedSealState; }
}
