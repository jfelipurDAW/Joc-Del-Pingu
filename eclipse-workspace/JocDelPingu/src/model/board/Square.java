package model.board;

public abstract class Square {
	
	private SquareType type;
	private int SquareID;
		
	public Square(SquareType type) {
		this.type = type;
	}

	public SquareType getType() {
		return this.type;
	};
	
	public int getSquareID() {
		return this.SquareID;
	}
	
	public void setSquareID(int SquareID) {
		this.SquareID = SquareID;
	}
	
	public abstract String action(model.entity.Player player);
}