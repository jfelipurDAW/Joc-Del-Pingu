package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Sled extends Square {

	public S_Sled(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.SLED;
	};

	@Override
	public String action(model.entity.Player player) {
		return null; // Handled by BoardManager
	}
}
