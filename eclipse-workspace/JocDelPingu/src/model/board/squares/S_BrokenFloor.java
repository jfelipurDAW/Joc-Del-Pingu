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
	public void action(model.entity.Player player) {
		int totalItems = player.getInventory().getTotalItemCount();
		if (totalItems > 5) {
			player.setSquare(0);
			System.out.println(player.getName() + " fell through the broken floor! Too many items!");
		} else if (totalItems > 0) {
			player.setSkipNextTurn(true);
			System.out.println(player.getName() + " carefully crossed the broken floor but loses a turn!");
		} else {
			System.out.println(player.getName() + " passed the broken floor without problems!");
		}
	}
}
