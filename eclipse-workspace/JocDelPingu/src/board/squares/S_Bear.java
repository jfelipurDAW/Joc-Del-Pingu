package board.squares;

import board.Square;
import board.SquareType;

public class S_Bear extends Square {

	public S_Bear(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.BEAR;
	};
	
	
	@Override
	public void action() {
	};

}
