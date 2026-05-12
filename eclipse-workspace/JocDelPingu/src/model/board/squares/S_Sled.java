package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Bonus tile that launches the player forward to the next sled in the
 * chain. Conceptually the opposite of {@link S_IceHole}.
 *
 * <p>As with the ice hole, the actual jump requires global knowledge of
 * every sled position ({@code Sled_Array} in {@link model.board.Board}),
 * so {@link #action} returns {@code null} and the board controller
 * performs the lookup via {@code Board.getDestination()}.</p>
 */
public class S_Sled extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#SLED}).
	 */
	public S_Sled(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#SLED}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.SLED;
	};

	/**
	 * Resolution is delegated to {@code BoardManager}, which knows the
	 * full ordered list of sled tiles and can pick the next one.
	 *
	 * @param player ignored at this layer.
	 * @return always {@code null} to signal "deferred".
	 */
	@Override
	public String action(model.entity.Player player) {
		return null; // Handled by BoardManager
	}
}
