package model.board.squares;

import model.board.Square;
import model.board.SquareType;

public class S_Sled extends Square{
	
	private int destination;

	public S_Sled(SquareType type) {
		super(type);
		
	}
	
	@Override
	public SquareType getType() {
		return SquareType.SLED;
	};
	
	@Override
	public void action() {
	};
	
}
