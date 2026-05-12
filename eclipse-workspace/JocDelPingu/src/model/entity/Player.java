package model.entity;

import java.util.ArrayList;
import java.util.List;

import model.board.Board;
import model.item.Inventory;


/**
 * Concrete {@link Entity} representing a human player.
 *
 * <p>A player carries the metadata configured on the setup screen
 * (display name, colour, optional password, avatar path) plus the gameplay
 * state that the rules need: an {@link Inventory} of collected objects and
 * a rolling history of recent events used by the in-game log panel.</p>
 *
 * <p>Passwords are stored already encrypted by {@code CryptoUtil}; this class
 * never sees nor stores plaintext credentials.</p>
 *
 * <p>The {@code avatarPath} is a classpath or filesystem reference to the
 * portrait chosen during setup; the {@code colour} string drives sprite
 * selection (e.g. {@code red_player_*}, {@code blue_player_*}).</p>
 */
public class Player extends Entity {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // Cap on the event history kept per player. Keeping this bounded prevents
    // long games from bloating save files.
    private static final int MAX_EVENT_HISTORY = 50;

    // Player nickname displayed in the UI and used to identify saves.
    private String name;

    // Sprite/theme colour (drives which sprite folder is used at render time).
    private String colour;

    // Encrypted password (produced by CryptoUtil); never stored in plaintext.
    private String password;

    // Path to the avatar image picked on the setup screen.
    private String avatarPath;

    // Player-owned objects (snowballs, fish, dice variants, ...).
    private Inventory inventory;

    // Rolling log of recent in-game events for this player. Capped at
    // MAX_EVENT_HISTORY to keep save files small.
    private List<String> eventHistory = new ArrayList<>();


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Builds a player without a password (used when password-protection is off).
     *
     * @param name   the player's display name
     * @param colour the colour theme that drives sprite selection
     */
    public Player(String name, String colour) {
        this.setType(EntityType.PLAYER);

        // Random id keeps the link between player and inventory unique even
        // when several saves are merged or compared.
        this.setEntityId((int) (Math.random() * 100000));
        this.name = name;
        this.colour = colour;
        this.setNumSquare(0);
        this.setSkipNextTurn(false);
        this.inventory = new Inventory(this.getEntityId());
    }


    /**
     * Constructor with password (for database).
     *
     * @param name     the player's display name
     * @param colour   the colour theme that drives sprite selection
     * @param password the already-encrypted password to store
     */
    public Player(String name, String colour, String password) {
        this.setType(EntityType.PLAYER);
        this.setEntityId((int) (Math.random() * 100000));
        this.name = name;
        this.colour = colour;
        this.password = password;
        this.setNumSquare(0);
        this.setSkipNextTurn(false);
        this.inventory = new Inventory(this.getEntityId());
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    /**
     * @param name the new display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param colour the new colour theme
     */
    public void setColour(String colour) {
        this.colour = colour;
    }

    /**
     * @param password the already-encrypted password to store
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param avatarPath path to the avatar image selected on the setup screen
     */
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }


    /**
     * @return the colour theme used to pick the player's sprites
     */
    public String getColour() {
        return this.colour;
    }


    /**
     * @return the encrypted password, or {@code null} if none was set
     */
    public String getPassword() {
        return this.password;
    }


    /**
     * @return the configured avatar image path
     */
    public String getAvatarPath() {
        return this.avatarPath;
    }


    /////////////////////////////
    ///       METHODS         ///
    /////////////////////////////

    /**
     * Compact text representation used for debugging and log lines.
     */
    @Override
    public String toString() {
        return name + " (" + colour + ") - Square: " + this.getNumSquare() + " " + inventory.toString();
    }


    /**
     * @return the player's display name (overrides the {@code null} default in {@link Entity})
     */
    @Override
    public String getName() {
        return this.name;
    }


    /**
     * Teleports the player to a specific square, clamped to the board bounds.
     * Delegates to {@link #setNumSquare(int)} so facing direction is updated
     * consistently with all other movement code.
     *
     * @param i the desired target square
     */
    @Override
    public void setSquare(int i) {
        int target = Math.max(0, Math.min(i, this.getBoard() != null ? Board.MAX_SQUARES - 1 : i));
        // setNumSquare updates facingRight based on direction
        this.setNumSquare(target);
    }


    /**
     * @param inventory the inventory to attach to this player (used when
     *                  rehydrating a saved game)
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }


    /**
     * @return the player's inventory
     */
    public Inventory getInventory() {
        return this.inventory;
    }


    /**
     * Lose half of all inventory items. Used when seal passes through.
     */
    public void loseHalfInventory() {
        this.inventory.removeHalf();
    }


    /**
     * Append an event/action message to the player's history (capped at
     * MAX_EVENT_HISTORY entries to keep saved games compact).
     *
     * @param message the event description to record; ignored if {@code null} or empty
     */
    public void recordEvent(String message) {
        if (message != null && !message.isEmpty()) {
            this.eventHistory.add(message);

            // Trim oldest entries so we never exceed the cap — drops from the
            // head of the list since that is where the oldest events live.
            while (this.eventHistory.size() > MAX_EVENT_HISTORY) {
                this.eventHistory.remove(0);
            }
        }
    }


    /**
     * @return the recorded event history (most recent at the end)
     */
    public List<String> getEventHistory() {
        return this.eventHistory;
    }


    /**
     * Replaces the event history, typically when restoring from a saved game.
     * A defensive copy is taken so the caller cannot mutate the internal list.
     *
     * @param history the history to restore, or {@code null} to clear it
     */
    public void setEventHistory(List<String> history) {
        this.eventHistory = (history != null) ? new ArrayList<>(history) : new ArrayList<>();
    }
}
