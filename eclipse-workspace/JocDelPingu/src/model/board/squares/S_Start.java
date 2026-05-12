package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * The spawn tile (board index 0). Every player begins the match here, and
 * effects that "send the player back to start" (Bear attack, falling
 * through a broken floor with a heavy inventory, ice hole chain) move the
 * player back onto this square.
 *
 * <p>It overrides any randomly-rolled tile at index 0 during board
 * generation, so the start position is deterministic.</p>
 */
public class S_Start extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#START}).
	 */
	public S_Start(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#START}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.START;
	}

	/**
	 * Landing on the start tile has no gameplay effect; it just produces
	 * a log message confirming the position.
	 *
	 * @param player the player who is at (or returned to) the start.
	 * @return a human-readable message for the game log.
	 */
	@Override
	public String action(model.entity.Player player) {
		return player.getName() + " is at the start.";
	}
}
