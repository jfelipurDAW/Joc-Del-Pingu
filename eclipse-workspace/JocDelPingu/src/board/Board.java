package board;

import java.util.ArrayList;

public class Board {
	
	public static final int widthBoard = 10;
	public static final int heightBoard = 5;
	public static final int MAX_SQUARES = widthBoard*heightBoard;
	public static final int NORMAL_SQUARE_PERCENTAGE = 50;

	private Square[] board = new Square[MAX_SQUARES];
	private ArrayList<Integer> IceHole_Array = new ArrayList<Integer>();
	private ArrayList<Integer> Sled_Array = new ArrayList<Integer>();
	
	public void createNewBoard() {
		for (int i = 0; i < board.length; i++) {
			if ((int) (Math.random()*100+1) <= NORMAL_SQUARE_PERCENTAGE) {
				board[i] = new Square(SquareType.NORMAL);
				System.out.println("NORMAL");
			} else {
				int randomType = (int) (Math.random()*5+1);
				switch(randomType) {
				case 1: 
					board[i] = new Square(SquareType.ICE_HOLE);
					System.out.println("ICE_HOLE");
					IceHole_Array.add(i);
					break;
				case 2: 
					board[i] = new Square(SquareType.SLED);
					System.out.println("SLED");
					Sled_Array.add(i);
					break;
				case 3: 
					board[i] = new Square(SquareType.BEAR);
					System.out.println("BEAR");
					break;
				case 4: 
					board[i] = new Square(SquareType.EVENT);
					System.out.println("EVENT");
					break;
				case 5:
					board[i] = new Square(SquareType.BROKEN_FLOOR);
					System.out.println("BROKEN_FLOOR");
					break;
				}
			}
		}
		
		board[0] = new Square(SquareType.START);
		board[board.length-1] = new Square(SquareType.END);
		
	}
	
	public SquareType getSquareType(int square) {
		return board[square].getType();
	}
	
	public int getDestination(Square square) {
		
		switch(square.getType()) {
		case ICE_HOLE:
			
			return IceHole_Array.get(IceHole_Array.indexOf(square.getSquareID())-1);
			
		case SLED:
			
			return Sled_Array.get(Sled_Array.indexOf(square.getSquareID())-1);
			
		}
		
		return square.getSquareID();
	}
}
