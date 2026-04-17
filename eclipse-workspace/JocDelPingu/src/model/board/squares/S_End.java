package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_End extends Square {
	
	public S_End(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.END;
	}
	
	@Override
	public String action(model.entity.Player player) {
		return player.getName() + " reached the END! 🎉";
	}
}
