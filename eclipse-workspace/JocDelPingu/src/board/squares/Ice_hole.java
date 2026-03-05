package board.squares;

import board.Square;
import board.SquareType;
import entity.Entity;

public class Ice_hole extends Square {
	
	private int destination;

	public Ice_hole(SquareType type) {
		super(type);
	}
	
	public void activate(Entity entity) {
		this.getSquareID();
	}

}
