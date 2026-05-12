package model.board;

/**
 * Abstract base class for every tile on the board.
 *
 * <p>A {@code Square} stores its {@link SquareType} (which determines its
 * gameplay role) and its index inside the {@link Board} array. Concrete
 * subclasses live in {@code model.board.squares} (e.g. {@code S_Normal},
 * {@code S_Bear}, {@code S_IceHole}) and implement {@link #action(model.entity.Player)}
 * to describe what happens when a player lands on the tile.</p>
 *
 * <p>The visual layer (background sprite, foreground sprite, entities) is
 * rendered by the UI controllers; this class only carries the model data.</p>
 */
public abstract class Square {


	/////////////////////////////
	///       FIELDS         ///
	/////////////////////////////

	// Gameplay category of this tile - used by the board logic to branch
	// on landing effects without having to do instanceof checks.
	private SquareType type;

	// Position of this tile inside the linear Board array. The Board lays
	// out tiles in a snake pattern visually, but the underlying storage
	// is a single 1D array indexed 0..MAX_SQUARES-1.
	private int SquareID;


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * Builds a square of the given type.
	 *
	 * @param type gameplay category of this tile.
	 */
	public Square(SquareType type) {
		this.type = type;
	}


	/////////////////////////////
	/// GETTERS AND SETTERS  ///
	/////////////////////////////

	/**
	 * @return the gameplay category of this tile.
	 */
	public SquareType getType() {
		return this.type;
	};

	/**
	 * @return the linear board index assigned to this tile.
	 */
	public int getSquareID() {
		return this.SquareID;
	}

	/**
	 * Assigns this tile its position inside the board array. Called by
	 * {@link Board} during generation or loading.
	 *
	 * @param SquareID linear index of this tile (0-based).
	 */
	public void setSquareID(int SquareID) {
		this.SquareID = SquareID;
	}


	/////////////////////////////
	///      METHODS         ///
	/////////////////////////////

	/**
	 * Resolves the gameplay effect of a player landing on this tile.
	 *
	 * <p>Implementations may mutate the player (move, skip turn, modify
	 * inventory) and return a human-readable description for the UI log.
	 * Some subclasses (e.g. ice hole, sled, event) return {@code null}
	 * because their resolution is delegated to {@code BoardManager} which
	 * needs access to the global board state.</p>
	 *
	 * @param player the player who just landed on this tile.
	 * @return a log message describing the outcome, or {@code null} if
	 *         resolution is deferred to the central controller.
	 */
	public abstract String action(model.entity.Player player);
}
