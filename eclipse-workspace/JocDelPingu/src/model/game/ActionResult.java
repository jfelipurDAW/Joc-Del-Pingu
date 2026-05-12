package model.game;


/**
 * Value object that describes the outcome of a single game action so the UI
 * layer (controllers / animations / sound effects) can render the correct
 * visual and audio feedback without needing to inspect game state directly.
 *
 * Instances are produced by {@link BoardManager} (square-landing effects),
 * {@link PlayerManager} (movement, snowball fights) and the {@link model.entity.Seal}
 * (seal interactions). They flow upward to the GameBoardController.
 *
 * The class deliberately exposes a small set of generic slots (value, value2,
 * targetName, eventMessage) so a single DTO type can represent many different
 * outcomes; the {@link ActionType} enum is the discriminator the UI switches on.
 */
public class ActionResult {


    /////////////////////////////
    ///    ACTION TYPES       ///
    /////////////////////////////

    /**
     * Enumeration of every possible action result the UI knows how to render.
     * Grouped by source square / interaction so it is easy to extend with new
     * board events without breaking the existing switch statements in the UI.
     */
    public enum ActionType {
        // Movement-triggering squares
        ICE_HOLE, SLED_FOUND, SLED_LAST, BEAR_SAFE, BEAR_ATTACK, EVENT,

        // Broken-floor outcomes (fall back to start, crack-skip, safe, or item loss)
        BROKEN_FLOOR_FALL, BROKEN_FLOOR_CRACK, BROKEN_FLOOR_SAFE, BROKEN_FLOOR_LOSE_ITEM,

        // Neutral / boundary / win signalling
        NORMAL_SQUARE, START_SQUARE, END_SQUARE, WIN,

        // Seal-related outcomes (bribery, hits, movement, etc.)
        SEAL_BRIBED, SEAL_NO_FISH, SEAL_HIT_HOLE, SEAL_HIT_START, SEAL_PASS, SEAL_EATING, SEAL_ACTIVE, SEAL_ROLL, SEAL_MOVE,

        // Player-vs-player snowball interactions
        SNOWBALL_WAR_WIN, SNOWBALL_WAR_TIE, SNOWBALL_WAR_EMPTY, SNOWBALL_THROW
    }


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    /** Discriminator the UI uses to choose the rendering/animation path. */
    private ActionType actionType;

    /** Player primarily responsible for / affected by this action. */
    private String playerName;

    /** Secondary player for two-party interactions (e.g. snowball war loser). */
    private String targetName; // For interactions (e.g. snowball war loser)

    /** Generic numeric payload: destination index, dice roll, item count, etc. */
    private int value;         // General number (square index, dice roll, turns, amount, etc.)

    /** Secondary numeric payload for actions that need two numbers. */
    private int value2;        // Secondary number (e.g. difference in snowballs)

    /** Free-form non-localized message, mainly used by random EVENT squares. */
    private String eventMessage; // Custom non-localized string if it comes from an event


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Minimal constructor used when only the action type is known up-front
     * (UI code can later enrich the result via setters).
     *
     * @param type the kind of action that occurred
     */
    public ActionResult(ActionType type) {
        this.actionType = type;
    }

    /**
     * Convenience constructor for the common "X happened to player Y" case.
     *
     * @param type       the kind of action that occurred
     * @param playerName name of the player primarily affected
     */
    public ActionResult(ActionType type, String playerName) {
        this.actionType = type;
        this.playerName = playerName;
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    public ActionType getType() { return actionType; }
    public void setType(ActionType type) { this.actionType = type; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public int getValue2() { return value2; }
    public void setValue2(int value2) { this.value2 = value2; }

    public String getEventMessage() { return eventMessage; }
    public void setEventMessage(String eventMessage) { this.eventMessage = eventMessage; }
}
