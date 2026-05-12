package model.game;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;


/**
 * High-level orchestrator for one match of "Joc del Pingu". It is the single
 * entry point the UI controller ({@code GameBoardController}) talks to —
 * everything else (board rules, movement, turn rotation, persistence) is
 * delegated to specialised collaborators kept as fields here:
 *  - {@link BoardManager}    resolves square-landing effects,
 *  - {@link PlayerManager}   moves players and handles snowball combat,
 *  - {@link TurnController}  rotates whose turn it is,
 *  - {@link Game}            is the underlying state aggregate (also used for save/load),
 *  - {@link SaveLoadService} is the static facade for YAML+AES persistence.
 *
 * The manager exposes a coarse API (startNewGame, playTurn, save/load, get
 * winner...) so controllers don't have to know about the internal moving parts.
 */
public class GameManager {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    /** Human-readable identifier used as the save-game name. */
    private String gameId;

    /** True between startNewGame and the end-of-match. */
    private boolean isActive;

    /** Board being played on. Shared with the underlying {@link Game}. */
    private Board board;

    /** Rotates whose turn it is and respects skip-turn penalties. */
    private TurnController turnController;

    /** Optional seal antagonist; null if the feature is disabled. */
    private Seal seal;

    /** Knows how to resolve every {@link model.board.SquareType}. */
    private BoardManager boardManager;

    /** Owns movement and snowball-combat logic. */
    private PlayerManager playerManager;

    /** Plain aggregate that carries the whole game state (used for save/load). */
    private Game game;


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Builds an inactive manager and wires up the helper subsystems. The
     * board / players / seal are injected separately so the manager can be
     * created early (e.g. before the user picks the seal option).
     *
     * @param gameId     save-game name to use when persisting
     * @param numPlayers number of players (kept for API parity; the actual
     *                   list is set later via the turn controller)
     */
    // Constructor
    public GameManager(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.isActive = false;
        this.boardManager = new BoardManager();
        this.playerManager = new PlayerManager();
        this.game = new Game();
    }


    /////////////////////////////
    /// SETUP & INJECTION     ///
    /////////////////////////////

    /**
     * Injects the board and mirrors it into the {@link Game} aggregate so
     * persistence sees the same reference.
     */
    public void setBoard(Board board) {
        this.board = board;
        this.game.setBoard(board);
    }

    public void setTurnController(TurnController turnController) {
        this.turnController = turnController;
    }

    /**
     * Injects the seal and mirrors it into the {@link Game} aggregate.
     */
    public void setSeal(Seal seal) {
        this.seal = seal;
        this.game.setSeal(seal);
    }

    /**
     * Marks the match as active so the UI can begin processing turns.
     */
    public void startNewGame() {
        this.isActive = true;
    }


    /////////////////////////////
    ///    TURN HANDLING      ///
    /////////////////////////////

    /**
     * Runs one full turn: moves the current player by the dice result and,
     * unless the move ended the game, asks the {@link BoardManager} to
     * resolve whatever square they landed on.
     *
     * @param diceResult number of squares to advance
     * @return an {@link ActionResult} describing what happened (WIN, square
     *         effect, etc.) so the UI can animate it
     */
    public ActionResult playTurn(int diceResult) {
        Player current = getCurrentPlayer();
        ActionResult moveMsg = playerManager.movePlayer(current, diceResult, board);

        // PlayerManager only returns non-null when the player just won;
        // in that case skip square-effect resolution and end the game.
        if (moveMsg != null) {
            this.game.setGameOver(true);
            this.game.setWinner(current);
            return moveMsg;
        }
        return boardManager.executeSquareAction(current, board);
    }

    public boolean isGameActive() {
        return this.isActive;
    }


    /////////////////////////////
    ///     PERSISTENCE       ///
    /////////////////////////////

    /**
     * Serializes the current match through {@link SaveLoadService}.
     *
     * @return true on successful save
     */
    public boolean saveGame() {
        return SaveLoadService.saveGame(gameId, board, turnController, seal, null);
    }

    /**
     * Loads a previously saved match by this manager's gameId.
     *
     * @return true on successful load
     */
    public boolean loadGame() {
        return SaveLoadService.loadGame(gameId);
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public Player getWinner() {
        return game.getWinner();
    }

    /**
     * @return the {@link Player} whose turn it currently is, or null if the
     *         current participant is the seal (not a Player) or unset
     */
    public Player getCurrentPlayer() {
        if (turnController != null && turnController.getCurrentTurn() instanceof Player) {
            return (Player) turnController.getCurrentTurn();
        }
        return null;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public Seal getSeal() {
        return seal;
    }
}
