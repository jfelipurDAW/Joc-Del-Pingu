package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Hazard tile guarded by a polar bear.
 *
 * <p>Landing rule:</p>
 * <ul>
 *   <li>If the player carries at least one fish, one fish is consumed
 *       to feed the bear and the player stays on the tile.</li>
 *   <li>Otherwise the bear attacks and the player is sent back to the
 *       start (index 0).</li>
 * </ul>
 *
 * <p>This is the main reason fish are valuable: they are the only way to
 * avoid the costly trip back to the spawn.</p>
 */
public class S_Bear extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#BEAR}).
	 */
	public S_Bear(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#BEAR}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.BEAR;
	};


	/**
	 * Resolves the bear encounter.
	 *
	 * <p>Side effects: may consume one fish from the player's inventory
	 * (safe path) or reset the player's square index to 0 (attacked).</p>
	 *
	 * @param player the player who stepped onto the bear tile.
	 * @return a log message describing whether the bear was bribed or
	 *         the player got attacked.
	 */
	@Override
	public String action(model.entity.Player player) {

		// Bribe path: a single fish satisfies the bear and lets the
		// player remain on the square with no further penalty.
		if (player.getInventory().getFishQuantity() > 0) {
			player.getInventory().useObject(model.item.ObjectType.FISH, 1);
			return player.getName() + " feed the Bear a fish and avoided returning to start!";

		} else {

			// Attack path: no fish means the player is mauled and
			// teleported back to the start tile.
			player.setSquare(0);
			return player.getName() + " attacked by Bear! Back to start!";
		}
	}

}
