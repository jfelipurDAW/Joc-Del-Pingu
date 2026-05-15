package model.entity;

import model.board.Board;
import model.board.EventManager;
import model.board.SquareType;
import model.item.ObjectType;


/**
 * Base class for every movable piece that can occupy a square on the board:
 * concretely the human {@link Player} and the CPU-controlled {@link Seal}.
 *
 * <p>An entity tracks its identifier, the index of the square it currently
 * stands on, the {@link Board} it belongs to and a handful of transient
 * visual / behavioural flags ({@code facingRight}, {@code damaged},
 * {@code frozen}). The flags marked "transient" here are deliberately not
 * persisted in save files — they describe in-memory render state that should
 * be reset on load rather than being part of the saved game.</p>
 *
 * <p>When an entity is {@code frozen}, the rendering layer swaps the regular
 * coloured sprite for the {@code ice_player_*} sprite variant — so freezing
 * is purely a sprite-path concern, not a logic concern in this class.</p>
 *
 * <p>Subclasses customise {@link #getName()} and add their own behaviour
 * (inventory, AI, etc.) on top of the movement primitives provided here.</p>
 */
public abstract class Entity {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // Unique identifier (random integer). Used to link an entity with its
    // inventory and to look it up in saved-game maps.
    private int entityId;

    // Current square index on the linear board path (0 = start).
    private int numSquare;

    // PLAYER or SEAL — lets generic code branch on entity kind without
    // resorting to instanceof checks.
    private EntityType type;

    // Back-reference to the board the entity currently lives on.
    private Board board;

    // True when the entity must skip its upcoming turn (e.g. a fter being
    // hit by a snowball event). Consumed by the turn manager.
    private boolean skipNextTurn;

    // Transient visual state — not persisted in saves. These describe how
    // the sprite is currently being drawn and are recomputed at runtime.
    private boolean facingRight = true;
    private boolean damaged = false;
    private boolean frozen = false;

    // Last event result for UI display. The UI reads this after each move
    // to render the appropriate effect/animation and then clears it.
    private EventManager.EventResult lastEvent;


    /////////////////////////////
    ///       METHODS         ///
    /////////////////////////////

    /**
     * @return the index of the square the entity currently stands on
     */
    public int getSquareIndex() {
        return this.numSquare;
    }


    /**
     * Advance the entity by a number of squares, clamped to not exceed the board.
     * Returns true if the entity reached or passed the END square.
     * Updates facingRight based on direction of movement.
     *
     * @param squares positive for forward movement, negative to move backwards
     * @return {@code true} if the entity reached (or would have overshot) the
     *         final square, {@code false} otherwise
     */
    public boolean advance(int squares) {
        // Update facing so the sprite mirrors the direction of motion.
        if (squares > 0)      this.facingRight = true;
        else if (squares < 0) this.facingRight = false;

        int newPos = this.numSquare + squares;
        if (newPos >= Board.MAX_SQUARES - 1) {
            // Clamp at the final square — the caller decides what to do
            // with the "reached end" signal (typically: declare a winner).
            this.numSquare = Board.MAX_SQUARES - 1;
            return true; // reached the end!
        } else {
            this.numSquare = newPos;
            return false;
        }
    }


    /**
     * @return the {@link EntityType} discriminator (PLAYER or SEAL)
     */
    public EntityType getType() {
        return this.type;
    }


    /**
     * Default display name. Subclasses override this to return their own
     * label (player nickname, seal emoji, ...).
     *
     * @return the display name, or {@code null} for the base entity
     */
    public String getName() {
        return null;
    }


    /**
     * Sets the back-reference to the board the entity belongs to.
     *
     * @param board the board hosting this entity
     */
    public void setBoard(Board board) {
        this.board = board;
    }


    /**
     * Teleports the entity to an absolute square, clamped to the legal range.
     * Also updates {@link #facingRight} so the sprite faces in the direction
     * of travel.
     *
     * @param newPosition the desired target square (will be clamped)
     */
    public void setSquare(int newPosition) {
        int clamped = Math.max(0, Math.min(newPosition, Board.MAX_SQUARES - 1));
        if (clamped > this.numSquare)      this.facingRight = true;
        else if (clamped < this.numSquare) this.facingRight = false;
        this.numSquare = clamped;
    }


    /*
     * Process the effect of landing on the current square.
     * Returns a description of what happened for the game log.
     */
    // Handled by BoardManager


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    /**
     * @return the most recent event the entity triggered, or {@code null}
     *         when no event is pending UI consumption
     */
    public EventManager.EventResult getLastEvent() {
        return lastEvent;
    }


    /**
     * Clears the pending event after the UI has rendered it.
     */
    public void clearLastEvent() {
        lastEvent = null;
    }


    /**
     * Stores the latest event so the UI can pick it up on the next refresh.
     *
     * @param event the event to expose to the UI layer
     */
    public void setLastEvent(EventManager.EventResult event) {
        this.lastEvent = event;
    }


    /**
     * @return whether this entity must skip its next turn
     */
    public boolean shouldSkipNextTurn() {
        return skipNextTurn;
    }


    /**
     * Marks (or clears) the "skip next turn" flag — typically set by event
     * effects such as a snowball hit.
     *
     * @param skip {@code true} to skip the next turn, {@code false} to play normally
     */
    public void setSkipNextTurn(boolean skip) {
        this.skipNextTurn = skip;
    }


    /**
     * @return the entity's unique numeric identifier
     */
    public int getEntityId() {
        return entityId;
    }


    /**
     * Assigns the entity's unique identifier. Typically set once at
     * construction with a random integer.
     *
     * @param entityId the identifier to assign
     */
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }


    /**
     * @return the square index the entity occupies (alias of {@link #getSquareIndex()})
     */
    public int getNumSquare() {
        return numSquare;
    }


    /**
     * Sets the absolute square index and updates facing direction accordingly.
     *
     * @param numSquare the new square index
     */
    public void setNumSquare(int numSquare) {
        if (numSquare > this.numSquare)      this.facingRight = true;
        else if (numSquare < this.numSquare) this.facingRight = false;
        this.numSquare = numSquare;
    }


    /**
     * @return {@code true} when the sprite currently faces right
     */
    public boolean isFacingRight() {
        return facingRight;
    }


    /**
     * @param facingRight whether the sprite should face right
     */
    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }


    /**
     * @return {@code true} when a "damaged" visual overlay should be drawn
     */
    public boolean isDamaged() {
        return damaged;
    }


    /**
     * Toggles the transient "damaged" visual flag. The UI uses this to
     * briefly render a hit/flash effect on the sprite.
     *
     * @param damaged whether to show the damaged overlay
     */
    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }


    /**
     * @return {@code true} when the entity must be rendered with the
     *         {@code ice_player_*} sprite instead of the coloured one
     */
    public boolean isFrozen() {
        return frozen;
    }


    /**
     * Toggles the transient "frozen" visual flag. When set, the renderer
     * swaps the entity's coloured sprite for the {@code ice_player_*}
     * variant from the assets folder.
     *
     * @param frozen whether the entity should be drawn frozen
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }


    /**
     * @return the board this entity currently belongs to
     */
    public Board getBoard() {
        return board;
    }


    /**
     * Sets the discriminator for the entity. Each subclass invokes this in
     * its constructor so generic code can branch by {@link EntityType}.
     *
     * @param type the kind of entity
     */
    public void setType(EntityType type) {
        this.type = type;
    }
}
