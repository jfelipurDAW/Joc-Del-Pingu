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

/**
 * Model representation of the Joc del Pingu board.
 *
 * <p>The board is a fixed-size {@link Square} array indexed 0..MAX_SQUARES-1.
 * Visually the UI lays the tiles out in a snake pattern (alternating row
 * direction), but at the model level it is a single linear sequence. Index
 * 0 is always the START tile and the last index is always the END tile;
 * everything in between is filled by either {@link #createNewBoard()}
 * (random generation) or {@link #loadBoard(java.util.List)} (restoring a
 * saved game).</p>
 *
 * <p>Two auxiliary index lists are tracked so the gameplay rules for
 * chained tiles can be resolved cheaply:</p>
 * <ul>
 *   <li>{@code IceHole_Array} - indices of every ICE_HOLE square. When a
 *       player lands on a hole, they are sent to the PREVIOUS hole in
 *       this list (or to the start if they are on the first hole).</li>
 *   <li>{@code Sled_Array} - indices of every SLED square. Landing on a
 *       sled launches the player to the NEXT sled (or nothing if it is
 *       already the last one).</li>
 * </ul>
 *
 * <p>This class is plain model state; UI rendering and turn handling
 * live in the controller layer (e.g. {@code GameBoardController}).</p>
 */
public class Board {


	/////////////////////////////
	///      CONSTANTS       ///
	/////////////////////////////

	public static final int widthBoard = 10;
	public static final int heightBoard = 5;
	public static final int MAX_SQUARES = widthBoard*heightBoard;

	// Probability (out of 100) that any given non-edge tile is generated
	// as a NORMAL square. The remaining 30% rolls one of five hazard or
	// bonus types with equal weight.
	public static final int NORMAL_SQUARE_PERCENTAGE = 70;


	/////////////////////////////
	///       FIELDS         ///
	/////////////////////////////

	// Linear storage of every tile on the board. Index 0 = START, last
	// index = END, everything else is generated or loaded.
	private Square[] board = new Square[MAX_SQUARES];

	// Sorted indices of every ICE_HOLE square. Order matters: a player
	// landing on hole N gets sent to hole N-1 (or start) - see
	// getDestination().
	private ArrayList<Integer> IceHole_Array = new ArrayList<Integer>();

	// Sorted indices of every SLED square. A player landing on sled N
	// gets launched forward to sled N+1 (or stays put if last) - see
	// getDestination().
	private ArrayList<Integer> Sled_Array = new ArrayList<Integer>();


	/////////////////////////////
	///    CONSTRUCTORS      ///
	/////////////////////////////

	/**
	 * Creates an empty board. The caller is expected to immediately
	 * follow up with either {@link #createNewBoard()} or
	 * {@link #loadBoard(java.util.List)} before the board is used.
	 */
	public Board() {
	}


	/////////////////////////////
	///      METHODS         ///
	/////////////////////////////

	/**
	 * Procedurally fills the board with a fresh random layout.
	 *
	 * <p>For each cell, rolls a percentage: with {@link #NORMAL_SQUARE_PERCENTAGE}
	 * probability the tile becomes NORMAL; otherwise one of five special
	 * tile types is picked uniformly. After the random pass the START
	 * and END tiles are forcibly written at the first and last indices,
	 * and any stale entries those overwrites left in the auxiliary
	 * arrays are dropped.</p>
	 */
	public void createNewBoard() {

		for (int i = 0; i < board.length; i++) {

			// Bias most tiles toward NORMAL to keep hazard density sane.
			if ((int) (Math.random()*100+1) <= NORMAL_SQUARE_PERCENTAGE) {
				board[i] = new S_Normal(SquareType.NORMAL);

			} else {

				// Uniform pick among the five special tile types.
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

			board[i].setSquareID(i);
		}

		// Force-place START and END at the fixed edge positions, even
		// if the random pass already wrote something else there.
		board[0] = new S_Start(SquareType.START);
		board[0].setSquareID(0);
		board[board.length-1] = new S_End(SquareType.END);
		board[board.length-1].setSquareID(board.length-1);

		// The START/END overwrite above may invalidate auxiliary indices that
		// the random pass added for those positions. Drop any stale entries so
		// later sled/hole lookups never resolve to START or END.
		dropEdgeIndices(IceHole_Array);
		dropEdgeIndices(Sled_Array);
	}

	/**
	 * Removes the start (0) and end (last) indices from an auxiliary
	 * list. Used after START/END are force-placed to keep
	 * {@code IceHole_Array} and {@code Sled_Array} clean.
	 *
	 * @param indices the auxiliary list to clean in-place.
	 */
	private void dropEdgeIndices(ArrayList<Integer> indices) {
		final int last = board.length - 1;
		indices.removeIf(idx -> idx == 0 || idx == last);
	}

	/**
	 * Rebuilds the board from a serialized list of {@link SquareType}
	 * names (as produced by the save system).
	 *
	 * <p>Iterates in lock-step with the provided list, instantiating the
	 * matching {@code S_*} subclass for each entry and rebuilding the
	 * ICE_HOLE / SLED auxiliary index arrays in the same pass.</p>
	 *
	 * @param boardTypes ordered list of SquareType names, one per cell.
	 */
	public void loadBoard(java.util.List<String> boardTypes) {

		// Wipe the chains before refilling so a reload does not
		// accumulate indices on top of the previous game.
		IceHole_Array.clear();
		Sled_Array.clear();

		for (int i = 0; i < board.length && i < boardTypes.size(); i++) {

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

			if (board[i] != null) {
				board[i].setSquareID(i);
			}
		}

		// Same defensive cleanup as createNewBoard: a corrupted save file
		// could list SLED or ICE_HOLE at the START/END indices. Drop those
		// stale entries so they never resolve to the START or END square.
		dropEdgeIndices(IceHole_Array);
		dropEdgeIndices(Sled_Array);
	}

	/**
	 * @param square linear board index.
	 * @return the {@link SquareType} of the tile at that index.
	 */
	public SquareType getSquareType(int square) {
		return board[square].getType();
	}

	/**
	 * Computes the destination index for a chained tile (ICE_HOLE or
	 * SLED). For non-chained tiles the index is returned unchanged.
	 *
	 * <p>Rules:</p>
	 * <ul>
	 *   <li>ICE_HOLE: send the player to the previous hole in the chain;
	 *       if this is the first hole, send them to start (index 0).</li>
	 *   <li>SLED: send the player to the next sled in the chain;
	 *       if this is the last sled, leave them where they are.</li>
	 * </ul>
	 *
	 * @param squareIndex index of the tile the player just landed on.
	 * @return the index the player should be moved to.
	 */
	public int getDestination(int squareIndex) {

		SquareType type = getSquareType(squareIndex);

		switch(type) {
		case ICE_HOLE:

			// Position of this hole inside the chain.
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


	/////////////////////////////
	/// GETTERS AND SETTERS  ///
	/////////////////////////////

	/**
	 * @return the underlying tile array. Exposed so the UI layer can
	 *         render each square directly.
	 */
	public Square[] getBoard() {
		return board;
	}

	/**
	 * @return the ordered list of ICE_HOLE tile indices. Mutating the
	 *         returned list will affect chained-tile resolution.
	 */
	public ArrayList<Integer> getIceHole_Array() {
		return IceHole_Array;
	}

	/**
	 * @return the ordered list of SLED tile indices.
	 */
	public ArrayList<Integer> getSled_Array() {
		return Sled_Array;
	}

	/**
	 * Converts a BROKEN_FLOOR square to an ICE_HOLE once a player falls through it.
	 * The new hole is inserted into IceHole_Array in sorted order so getDestination()
	 * continues to work correctly (holes link in ascending-index order).
	 *
	 * @param squareIndex index of the broken-floor tile that just collapsed.
	 */
	public void convertBrokenFloorToIceHole(int squareIndex) {

		board[squareIndex] = new S_IceHole(SquareType.ICE_HOLE);
		board[squareIndex].setSquareID(squareIndex);

		// Avoid duplicate entries if the same tile is somehow converted
		// twice; re-sort to preserve the ascending-order invariant the
		// chain lookup in getDestination() relies on.
		if (!IceHole_Array.contains(squareIndex)) {
			IceHole_Array.add(squareIndex);
			java.util.Collections.sort(IceHole_Array);
		}
	}
}
