package model.board;

/**
 * Enumeration of every possible square category on the Joc del Pingu board.
 *
 * <p>Each value identifies the gameplay behavior of a tile. The {@link Board}
 * uses these values to decide which {@code S_*} subclass (e.g. {@code S_Bear},
 * {@code S_IceHole}) to instantiate when generating or loading a board, and
 * the board controller branches on the type to apply landing effects.</p>
 *
 * <ul>
 *   <li>{@link #ICE_HOLE}     - sends the player backwards AND freezes the sprite.</li>
 *   <li>{@link #SLED}         - launches the player forward to the next sled.</li>
 *   <li>{@link #BEAR}         - costs the player a turn (or a fish, if available).</li>
 *   <li>{@link #EVENT}        - triggers a random item reward / penalty.</li>
 *   <li>{@link #BROKEN_FLOOR} - cracks under heavy inventories, sending the player back.</li>
 *   <li>{@link #NORMAL}       - filler tile; no special effect.</li>
 *   <li>{@link #START}        - spawn point for every player (index 0).</li>
 *   <li>{@link #END}          - goal tile that ends the match (last index).</li>
 * </ul>
 */
public enum SquareType {


	/////////////////////////////
	///       VALUES         ///
	/////////////////////////////

	ICE_HOLE,
	SLED,
	BEAR,
	EVENT,
	BROKEN_FLOOR,
	NORMAL,
	START,
	END
}

