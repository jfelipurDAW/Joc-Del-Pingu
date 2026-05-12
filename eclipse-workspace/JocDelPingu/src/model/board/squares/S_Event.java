package model.board.squares;

import model.board.Square;
import model.board.SquareType;

/**
 * Tile that triggers a random event when landed on.
 *
 * <p>The set of possible events (gain fish / snowballs / dice, lose a
 * turn, lose an item, snowmobile boost) and their canonical weighted
 * probabilities live in {@link model.board.EventManager}. The actual
 * resolution is invoked by {@code BoardManager.handleEvent} which has
 * the player and board context required.</p>
 *
 * <p>An earlier implementation had a duplicated 1-in-6 randomizer here,
 * which skewed the global distribution. The action is now intentionally
 * a no-op so that {@link model.board.EventManager#triggerEvent} remains
 * the single source of truth.</p>
 */
public class S_Event extends Square {


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * @param type the square type tag (always {@link SquareType#EVENT}).
	 */
	public S_Event(SquareType type) {
		super(type);
	}


	/////////////////////////////
	///      OVERRIDES       ///
	/////////////////////////////

	/**
	 * @return always {@link SquareType#EVENT}.
	 */
	@Override
	public SquareType getType() {
		return SquareType.EVENT;
	}

	/**
	 * No-op. The real event resolution happens in
	 * {@code BoardManager.handleEvent} -> {@code EventManager.triggerEvent},
	 * which has the canonical weighted probabilities (30% slow dice, 20%
	 * snowballs, 15% fish, 12% lose turn, 10% lose item, 8% fast dice,
	 * 5% snowmobile). This override used to contain a duplicated 1-in-6
	 * randomizer that biased the outcomes; it is kept empty to satisfy
	 * the abstract contract without diverging from the central manager.
	 *
	 * @param player ignored at this layer.
	 * @return always {@code null} to signal "deferred".
	 */
	@Override
	public String action(model.entity.Player player) {
		return null;
	}

}
