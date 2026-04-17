package model.board.squares;

import model.board.Square;
import model.board.SquareType;
import model.entity.Entity;

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
	}

	@Override
	public String action(model.entity.Player player) {
		return null; // Handled by BoardManager
	}
}