package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Hazard tile that sends the player backwards along a chain of holes.
 *
 * <p>Behavior is intentionally not implemented here: resolving an ice
 * hole requires knowledge of the full board (specifically the
 * {@code IceHole_Array} maintained by {@link model.board.Board}) so that
 * the player can be sent to the previous hole in the chain (or to the
 * start if this is the first hole). The board controller calls
 * {@code Board.getDestination()} for that lookup, and additionally
 * freezes the player sprite to reflect the splash.</p>
 *
 * <p>This subclass therefore returns {@code null} from {@link #action}
 * to indicate "handled elsewhere".</p>
 */
public class S_IceHole extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#ICE_HOLE}).
	 */
	public S_IceHole(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#ICE_HOLE}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.ICE_HOLE;
	};

	/**
	 * Resolution is delegated to {@code BoardManager}, which has access
	 * to the ice-hole index array needed to compute the destination.
	 *
	 * @param player ignored at this layer.
	 * @return always {@code null} to signal "deferred".
	 */
	@Override
	public String action(model.entity.Player player) {
		return null; // Handled by BoardManager
	}
}
