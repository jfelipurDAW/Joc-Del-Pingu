package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Start extends Square {
	
	public S_Start(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.START;
	}
	
	@Override
	public String action(model.entity.Player player) {
		return player.getName() + " is at the start.";
	}
}
