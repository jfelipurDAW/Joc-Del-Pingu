package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_BrokenFloor extends Square {
	
	public S_BrokenFloor(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.BROKEN_FLOOR;
	};
	
	@Override
	public String action(model.entity.Player player) {
		int totalItems = player.getInventory().getTotalItemCount();
		if (totalItems > 5) {
			player.setSquare(0);
			return player.getName() + " fell through the broken floor! Too many items!";
		} else if (totalItems > 0) {
			if (Math.random() < 0.5) {
				model.item.ObjectType lost = player.getInventory().removeRandomItem();
				return player.getName() + " cracked the broken floor and dropped a "
						+ (lost != null ? lost.name().toLowerCase() : "item") + "!";
			}
			player.setSkipNextTurn(true);
			return player.getName() + " carefully crossed the broken floor but loses a turn!";
		} else {
			return player.getName() + " passed the broken floor without problems!";
		}
	}
}
