package board.squares;

import board.Square;
import board.SquareType;

public class S_BrokenFloor extends Square {
	
	public S_BrokenFloor(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.BROKEN_FLOOR;
	};
	
	@Override
	public void action() {
	};
}
