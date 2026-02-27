package board;

public class Board {
	
	public static final int widthBoard = 10;
	public static final int heightBoard = 5;
	public static final int MAX_SQUARES = widthBoard*heightBoard;
	public static final int NORMAL_SQUARE_PERCENTAGE = 50;
	
//	private int widthBoard;
//	private int heightBoard;
	private Square[] board = new Square[MAX_SQUARES];
	

	public SquareType checkSquare(int square) {
		return board[square].getType();
	}
	
	public void createNewBoard() {
		for (int i = 0; i < board.length; i++) {
			if ((int) (Math.random()*100+1) <= NORMAL_SQUARE_PERCENTAGE) {
				board[i] = new Square(SquareType.NORMAL);
				System.out.println("NORMAL");
			} else {
				int randomType = (int) (Math.random()*4+1);
				switch(randomType) {
				case 1: 
					board[i] = new Square(SquareType.ICE_HOLE);
					System.out.println("ICE_HOLE");
					break;
				case 2: 
					board[i] = new Square(SquareType.SLED);
					System.out.println("SLED");
					break;
				case 3: 
					board[i] = new Square(SquareType.BEAR);
					System.out.println("BEAR");
					break;
				case 4: 
					board[i] = new Square(SquareType.EVENT);
					System.out.println("EVENT");
					break;
				}
			}
		}
	}
}
