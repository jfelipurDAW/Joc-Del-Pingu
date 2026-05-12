package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * The goal tile (last board index). The first player to land on this
 * square wins the match. Board generation always overwrites the last
 * index with an {@code S_End} so the finish line cannot be replaced by
 * a randomly-rolled hazard.
 */
public class S_End extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#END}).
	 */
	public S_End(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#END}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.END;
	}

	/**
	 * Emits the victory log message. The actual win detection (stopping
	 * the game loop, showing the victory screen) is performed by the
	 * board controller after observing this square type.
	 *
	 * @param player the player who reached the goal.
	 * @return a celebratory message for the game log.
	 */
	@Override
	public String action(model.entity.Player player) {
		return player.getName() + " reached the END! 🎉";
	}
}
