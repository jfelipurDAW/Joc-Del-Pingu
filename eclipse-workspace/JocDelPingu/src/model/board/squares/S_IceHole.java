package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_IceHole extends Square {

	public S_IceHole(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.ICE_HOLE;
	};

	@Override
	public String action(model.entity.Player player) {
		return null; // Handled by BoardManager
	}
}
