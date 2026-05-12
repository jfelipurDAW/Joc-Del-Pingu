package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Cracked-ice tile whose effect scales with how loaded the player is.
 *
 * <p>Landing rule (based on the player's total inventory item count):</p>
 * <ul>
 *   <li><b>&gt; 5 items:</b> the floor breaks completely; the player
 *       falls and is sent back to the start. (Elsewhere in {@code Board}
 *       this tile is also upgraded into a permanent ice hole.)</li>
 *   <li><b>1-5 items:</b> 50% chance to drop a random item, otherwise
 *       the player loses their next turn while crossing carefully.</li>
 *   <li><b>0 items:</b> the player is light enough to walk across with
 *       no penalty at all.</li>
 * </ul>
 *
 * <p>This creates a meaningful trade-off: hoarding items helps with the
 * Bear and Event squares but makes Broken Floors dangerous.</p>
 */
public class S_BrokenFloor extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#BROKEN_FLOOR}).
	 */
	public S_BrokenFloor(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#BROKEN_FLOOR}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.BROKEN_FLOOR;
	};

	/**
	 * Resolves the cracked-floor effect based on the player's inventory weight.
	 *
	 * <p>Side effects: may move the player back to start, remove a random
	 * item from the inventory, or set the skip-next-turn flag.</p>
	 *
	 * @param player the player who stepped onto the broken floor.
	 * @return a log message describing the outcome.
	 */
	@Override
	public String action(model.entity.Player player) {

		int totalItems = player.getInventory().getTotalItemCount();

		// Heavy inventory: the ice gives way completely.
		if (totalItems > 5) {
			player.setSquare(0);
			return player.getName() + " fell through the broken floor! Too many items!";

		} else if (totalItems > 0) {

			// Medium load: coin flip between losing an item or a turn.
			if (Math.random() < 0.5) {
				model.item.ObjectType lost = player.getInventory().removeRandomItem();
				return player.getName() + " cracked the broken floor and dropped a "
						+ (lost != null ? lost.name().toLowerCase() : "item") + "!";
			}

			player.setSkipNextTurn(true);
			return player.getName() + " carefully crossed the broken floor but loses a turn!";

		} else {

			// Empty hands: no consequences.
			return player.getName() + " passed the broken floor without problems!";
		}
	}
}
