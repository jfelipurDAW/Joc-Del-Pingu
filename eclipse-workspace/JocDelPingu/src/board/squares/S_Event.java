package board.squares;

import board.Square;
import board.SquareType;

public class S_Event extends Square {

	public S_Event(SquareType type) {
		super(type);
		
	}
	
	@Override
	public SquareType getType() {
		return SquareType.EVENT;
	};
	
	@Override
	public void action() {
	};

}
