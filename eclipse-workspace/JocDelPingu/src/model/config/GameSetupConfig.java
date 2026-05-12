package model.config;

import model.entity.Player;
import java.util.List;


/**
 * Static, process-wide holder for the parameters chosen during the
 * "new game" setup screens. Acts as a tiny in-memory hand-off between the
 * setup controllers (PlayerSetupController, MainMenuController) and the
 * controller that actually builds the board (GameBoardController).
 *
 * <p>The class is intentionally a bag of static fields rather than an injected
 * service — JavaFX controllers in this project are constructed by the FXML
 * loader, so passing state directly between them is awkward and this holder
 * keeps the wiring simple.</p>
 *
 * <p>Two distinct flows are supported:</p>
 * <ul>
 *   <li><b>New game</b>: {@link #setPlayers(List)} and {@link #setSealEnabled(boolean)}
 *       are populated and {@link #isLoadedGame()} stays {@code false}.</li>
 *   <li><b>Load game</b>: {@link #setLoadedGame(boolean)} is flipped to {@code true}
 *       and the {@code loadedBoardState}/{@code loadedTurnIndex}/{@code loadedSealState}
 *       fields carry the deserialized snapshot that {@code GameBoardController}
 *       will rehydrate instead of building a fresh board.</li>
 * </ul>
 */
public class GameSetupConfig {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // Roster of human players configured on the setup screen.
    private static List<Player> players;

    // Whether the CPU-controlled seal antagonist should be spawned on the board.
    private static boolean sealEnabled = false;

    // Flag that tells GameBoardController to rehydrate a saved game instead of
    // creating a fresh board. When true, the loaded* fields below carry the snapshot.
    private static boolean isLoadedGame = false;

    // Serialized board state captured from a previous save (list of square encodings).
    private static java.util.List<String> loadedBoardState;

    // Index of the player whose turn it was when the game was saved.
    private static int loadedTurnIndex;

    // Map carrying the seal's persisted attributes (position, blocked turns, ...).
    private static java.util.Map<String, Object> loadedSealState;


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    /**
     * @return the player roster configured for the upcoming game
     */
    public static List<Player> getPlayers() {
        return players;
    }

    /**
     * Stores the player roster collected on the setup screen so the game
     * controller can read it back when the board is built.
     *
     * @param players the players that will take part in the next game
     */
    public static void setPlayers(List<Player> players) {
        GameSetupConfig.players = players;
    }


    /**
     * @return {@code true} when the seal antagonist must be spawned
     */
    public static boolean isSealEnabled() {
        return sealEnabled;
    }

    /**
     * Toggles whether the CPU-controlled seal participates in the game.
     *
     * @param enabled whether the seal should be enabled
     */
    public static void setSealEnabled(boolean enabled) {
        GameSetupConfig.sealEnabled = enabled;
    }


    /**
     * @return {@code true} when GameBoardController should load a saved game
     *         instead of starting a new one
     */
    public static boolean isLoadedGame() { return isLoadedGame; }

    /**
     * Marks the next game start as "load from save" so the board controller
     * reads {@link #getLoadedBoardState()} / {@link #getLoadedTurnIndex()} /
     * {@link #getLoadedSealState()} instead of generating fresh content.
     *
     * @param loadedGame {@code true} to trigger the load path
     */
    public static void setLoadedGame(boolean loadedGame) { isLoadedGame = loadedGame; }


    /**
     * @return the serialized squares of the board as captured at save time
     */
    public static java.util.List<String> getLoadedBoardState() { return loadedBoardState; }

    /**
     * @param loadedBoardState the deserialized board layout from a save file
     */
    public static void setLoadedBoardState(java.util.List<String> loadedBoardState) { GameSetupConfig.loadedBoardState = loadedBoardState; }


    /**
     * @return the index of the player whose turn it was when the game was saved
     */
    public static int getLoadedTurnIndex() { return loadedTurnIndex; }

    /**
     * @param loadedTurnIndex the turn pointer recovered from a save file
     */
    public static void setLoadedTurnIndex(int loadedTurnIndex) { GameSetupConfig.loadedTurnIndex = loadedTurnIndex; }


    /**
     * @return the persisted attributes of the seal entity (position, blocked turns, ...)
     */
    public static java.util.Map<String, Object> getLoadedSealState() { return loadedSealState; }

    /**
     * @param loadedSealState the seal state map recovered from a save file
     */
    public static void setLoadedSealState(java.util.Map<String, Object> loadedSealState) { GameSetupConfig.loadedSealState = loadedSealState; }
}
