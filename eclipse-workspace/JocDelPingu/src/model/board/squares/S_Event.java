package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Event extends Square {

	public S_Event(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.EVENT;
	}

	/**
	 * No-op. The real event resolution happens in
	 * {@code BoardManager.handleEvent} → {@code EventManager.triggerEvent},
	 * which has the canonical weighted probabilities (30% slow dice, 20%
	 * snowballs, 15% fish, 12% lose turn, 10% lose item, 8% fast dice,
	 * 5% snowmobile). This override used to contain a duplicated 1-in-6
	 * randomizer that biased the outcomes; it is kept empty to satisfy
	 * the abstract contract without diverging from the central manager.
	 */
	@Override
	public String action(model.entity.Player player) {
		return null;
	}

}
