package model.game;

import java.util.List;
import model.board.Board;
import model.entity.Player;
import model.entity.Seal;


/**
 * Central aggregate that holds every piece of state describing one match of
 * "Joc del Pingu":
 *  - the {@link Board} (the snake-shaped ice path),
 *  - the list of {@link Player}s in turn order,
 *  - the current turn index,
 *  - the (optional) {@link Seal} antagonist and whether it is enabled,
 *  - the winner / game-over flags once the match ends.
 *
 * It is intentionally a plain POJO with public getters/setters: this same
 * object is what {@link SaveLoadService} traverses to serialize a save file
 * (YAML + AES) and what {@link GameManager} keeps as in-memory state at
 * runtime, so SnakeYAML and the controllers can read/write it directly.
 */
public class Game {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    /** Ordered list of players participating in this match. */
    private List<Player> players;

    /** The board on which the match is being played. */
    private Board board;

    /** Index into {@link #players} of the player whose turn it currently is. */
    private int currentTurn;

    /** Player who has reached the END square; null while the match is in progress. */
    private Player winner;

    /** Flag flipped to true once an end-condition is met (win or loss). */
    private boolean gameOver;

    /** Optional seal entity that roams the board as an antagonist. */
    private Seal seal;

    /** Whether the seal feature is enabled for this match (configurable at setup). */
    private boolean sealEnabled;


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Builds an empty game aggregate. Fields are populated externally by the
     * setup code (controllers / SaveLoadService) before the match starts.
     */
    public Game() {
        this.gameOver = false;
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public Seal getSeal() { return seal; }
    public void setSeal(Seal seal) { this.seal = seal; }

    public boolean isSealEnabled() { return sealEnabled; }
    public void setSealEnabled(boolean sealEnabled) { this.sealEnabled = sealEnabled; }
}
