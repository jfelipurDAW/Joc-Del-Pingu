package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Normal extends Square {
	
	public S_Normal(SquareType type) {
		super(type);
	}

	@Override
	public SquareType getType() {
		return SquareType.NORMAL;
	};
	
	@Override
	public void action() {
	};
}
