package model.board;

import java.util.ArrayList;
import model.board.squares.S_Bear;
import model.board.squares.S_BrokenFloor;
import model.board.squares.S_Event;
import model.board.squares.S_IceHole;
import model.board.squares.S_Normal;
import model.board.squares.S_Sled;
import model.board.squares.S_Start;
import model.board.squares.S_End;

public class Board {
	
	public static final int widthBoard = 10;
	public static final int heightBoard = 5;
	public static final int MAX_SQUARES = widthBoard*heightBoard;
	public static final int NORMAL_SQUARE_PERCENTAGE = 70;

	private Square[] board = new Square[MAX_SQUARES];
	private ArrayList<Integer> IceHole_Array = new ArrayList<Integer>();
	private ArrayList<Integer> Sled_Array = new ArrayList<Integer>();
	private Square[] squares; 
	public Board() {
	        this.squares = new Square[MAX_SQUARES];
	    }
	
	public void createNewBoard() {
		for (int i = 0; i < board.length; i++) {
			if ((int) (Math.random()*100+1) <= NORMAL_SQUARE_PERCENTAGE) {
				board[i] = new S_Normal(SquareType.NORMAL);
			} else {
				int randomType = (int) (Math.random()*5+1);
				switch(randomType) {
				case 1: 
					board[i] = new S_IceHole(SquareType.ICE_HOLE);
					IceHole_Array.add(i);
					break;
				case 2: 
					board[i] = new S_Sled(SquareType.SLED);
					Sled_Array.add(i);
					break;
				case 3: 
					board[i] = new S_Bear(SquareType.BEAR);
					break;
				case 4: 
					board[i] = new S_Event(SquareType.EVENT);
					break;
				case 5:
					board[i] = new S_BrokenFloor(SquareType.BROKEN_FLOOR);
					break;
				}
			}
		}
		
		board[0] = new S_Start(SquareType.START);
		board[board.length-1] = new S_End(SquareType.END);
	}

    public void loadBoard(java.util.List<String> boardTypes) {
        IceHole_Array.clear();
        Sled_Array.clear();
        for (int i = 0; i < board.length; i++) {
            if (i >= boardTypes.size()) break;
            String typeStr = boardTypes.get(i);
            SquareType type = SquareType.valueOf(typeStr);
            switch(type) {
                case NORMAL: board[i] = new S_Normal(type); break;
                case ICE_HOLE: 
                    board[i] = new S_IceHole(type); 
                    IceHole_Array.add(i);
                    break;
                case SLED: 
                    board[i] = new S_Sled(type); 
                    Sled_Array.add(i);
                    break;
                case BEAR: board[i] = new S_Bear(type); break;
                case EVENT: board[i] = new S_Event(type); break;
                case BROKEN_FLOOR: board[i] = new S_BrokenFloor(type); break;
                case START: board[i] = new S_Start(type); break;
                case END: board[i] = new S_End(type); break;
                default: break;
            }
        }
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
	 public Square[] getSquares() {
	        return squares;
	    }

	 public Square[] getBoard() {
		 return board;
	 }

	 public void setBoard(Square[] board) {
		 this.board = board;
	 }

	 public ArrayList<Integer> getIceHole_Array() {
		 return IceHole_Array;
	 }

	 public void setIceHole_Array(ArrayList<Integer> iceHole_Array) {
		 IceHole_Array = iceHole_Array;
	 }

	 public ArrayList<Integer> getSled_Array() {
		 return Sled_Array;
	 }

	 public void setSled_Array(ArrayList<Integer> sled_Array) {
		 Sled_Array = sled_Array;
	 }

	 public static int getWidthboard() {
		 return widthBoard;
	 }

	 public static int getHeightboard() {
		 return heightBoard;
	 }

	 public static int getMaxSquares() {
		 return MAX_SQUARES;
	 }

	 public static int getNormalSquarePercentage() {
		 return NORMAL_SQUARE_PERCENTAGE;
	 }

	 public void setSquares(Square[] squares) {
		 this.squares = squares;
	 }
}
