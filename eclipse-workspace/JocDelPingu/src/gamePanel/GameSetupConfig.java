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
}
