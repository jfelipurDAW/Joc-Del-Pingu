package board.squares;

import board.Square;
import board.SquareType;
import entity.Entity;

public class S_IceHole extends Square {

	public S_IceHole(SquareType type) {
		super(type);
	
	}
	
	public void activate(Entity entity) {
		this.getSquareID();
	}
	
	@Override
	public SquareType getType() {
		return SquareType.ICE_HOLE;
	};
	
	@Override
	public void action() {
	};

}