package board;

import java.util.ArrayList;

import board.squares.S_Bear;
import board.squares.S_BrokenFloor;
import board.squares.S_Event;
import board.squares.S_IceHole;
import board.squares.S_Normal;
import board.squares.S_Sled;

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
				board[i] = new S_Normal(SquareType.NORMAL);
				System.out.println("NORMAL");
			} else {
				int randomType = (int) (Math.random()*5+1);
				switch(randomType) {
				case 1: 
					board[i] = new S_IceHole(SquareType.ICE_HOLE);
					System.out.println("ICE_HOLE");
					IceHole_Array.add(i);
					break;
				case 2: 
					board[i] = new S_Sled(SquareType.SLED);
					System.out.println("SLED");
					Sled_Array.add(i);
					break;
				case 3: 
					board[i] = new S_Bear(SquareType.BEAR);
					System.out.println("BEAR");
					break;
				case 4: 
					board[i] = new S_Event(SquareType.EVENT);
					System.out.println("EVENT");
					break;
				case 5:
					board[i] = new S_BrokenFloor(SquareType.BROKEN_FLOOR);
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
	
	public int getDestination(int squareIndex) {
		SquareType type = getSquareType(squareIndex);
		
		switch(type) {
		case ICE_HOLE:
			int listIndex = IceHole_Array.indexOf(squareIndex);
			if (listIndex > 0) {
				return IceHole_Array.get(listIndex - 1);
			}
			return 0; // First ice hole, go to start
		case SLED:
			listIndex = Sled_Array.indexOf(squareIndex);
			if (listIndex > -1 && listIndex < Sled_Array.size() - 1) {
				return Sled_Array.get(listIndex + 1);
			}
			return squareIndex; // Last sled, do nothing
		default:
			return squareIndex;
		}
	}
}
