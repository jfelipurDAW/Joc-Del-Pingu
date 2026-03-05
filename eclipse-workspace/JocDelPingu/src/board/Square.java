package board;

public class Square {
	
	protected SquareType type;
	protected int SquareID;
	
	
	public Square(SquareType type) {
		this.type = type;
	}



	public SquareType getType() {
		return this.type;
	}
	
	public int getSquareID() {
		return this.SquareID;
	}
}
