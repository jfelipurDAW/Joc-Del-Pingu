package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Plain tile that has no gameplay effect. The board is mostly filled with
 * these (roughly 70%, see {@code Board.NORMAL_SQUARE_PERCENTAGE}) so that
 * the rarer special tiles feel meaningful when the player lands on one.
 *
 * <p>Landing on a normal square simply produces a log message; nothing
 * about the player state changes.</p>
 */
public class S_Normal extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#NORMAL}).
	 */
	public S_Normal(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#NORMAL}; overrides the field-based
	 *         lookup so the type is fixed even if the constructor is
	 *         called with a mismatched tag.
	 */
	@Override
	public SquareType getType() {
		return SquareType.NORMAL;
	};

	/**
	 * No state change - just emits a neutral log line.
	 *
	 * @param player the player who landed on the tile.
	 * @return a human-readable message for the game log.
	 */
	@Override
	public String action(model.entity.Player player) {
		return player.getName() + " landed on a normal square.";
	}
}
