package model.game;

import java.util.ArrayList;
import java.util.List;

import model.entity.Entity;
import model.entity.Player;


/**
 * Owns the round-robin turn order for a match. The controller stores the
 * participating {@link Entity} instances (humans and, optionally, the seal)
 * in insertion order and exposes helpers to:
 *  - read / advance the current turn (respecting the skip-turn flag set by
 *    the bear-attack and broken-floor squares),
 *  - look up which other players share a board square (used to trigger
 *    snowball wars / throws in {@link PlayerManager}).
 *
 * Collaborates with {@link GameManager} (asks "whose turn is it?"),
 * {@link PlayerManager}, and {@link SaveLoadService} (which persists the
 * current turn index).
 */
public class TurnController {


	/////////////////////////////
	///       FIELDS          ///
	/////////////////////////////

	/** Participants in turn order. Insertion order is the turn order. */
	private ArrayList<Entity> players;

	/** Index of the entity whose turn it currently is. */
	private int turn;


	/////////////////////////////
	///    CONSTRUCTORS       ///
	/////////////////////////////

	/**
	 * Creates an empty controller; players are added via {@link #addPlayer(Entity)}
	 * during game setup.
	 */
	public TurnController() {
		players = new ArrayList<Entity>();
		turn = 0;
	}


	/////////////////////////////
	///       METHODS         ///
	/////////////////////////////

	/**
	 * Appends a player (or the seal) to the turn queue.
	 *
	 * @param player entity to add to the rotation
	 */
	public void addPlayer(Entity player) {
		players.add(player);
	}

	/**
	 * @return the entity whose turn it currently is
	 */
	public Entity getCurrentTurn() {
		return players.get(turn);
	}

	/**
	 * @return the numeric index of the current turn (used by SaveLoadService)
	 */
	public int getCurrentTurnIndex() {
		return turn;
	}

	/**
	 * Restores the turn pointer when loading a saved game. The modulo guards
	 * against an out-of-range index if the player list size has changed.
	 *
	 * @param turnIndex desired turn position
	 */
	public void setCurrentTurn(int turnIndex) {
		this.turn = turnIndex % players.size();
	}

	/**
	 * Advance to the next turn. Handles skip-turn logic.
	 *
	 * If the next player has a pending skip flag (set by bear/broken-floor
	 * penalties), the flag is consumed here and the rotation jumps one more
	 * step so they effectively lose this turn.
	 */
	public void nextTurn() {
		turn = (turn + 1) % players.size();

		// Check if next player should skip
		Entity next = players.get(turn);
		if (next.shouldSkipNextTurn()) {
			next.setSkipNextTurn(false);

			// Skip to the next player again
			turn = (turn + 1) % players.size();
		}
	}

	/**
	 * @return the full list of participants (humans + seal if enabled)
	 */
	public ArrayList<Entity> getAllPlayers() {
		return players;
	}

	/**
	 * Returns all human players (not seal).
	 *
	 * Filters out the seal entity so UI code that only cares about humans
	 * (stats, registered-player matching, etc.) gets a clean list.
	 *
	 * @return list of {@link Player} entities, in turn order
	 */
	public List<Player> getHumanPlayers() {
		List<Player> humans = new ArrayList<>();
		for (Entity e : players) {
			if (e instanceof Player) {
				humans.add((Player) e);
			}
		}
		return humans;
	}

	/**
	 * Check if any other players are on the same square as the given player.
	 * Returns a list of conflicting players (excluding the given one).
	 *
	 * Used to detect collisions that trigger snowball wars / throws when a
	 * player lands on an occupied square.
	 *
	 * @param squareIndex   board index to inspect
	 * @param excludePlayer entity to omit from the result (typically the one
	 *                      who just moved)
	 * @return list of other human players currently on that square
	 */
	public List<Player> getPlayersAtSquare(int squareIndex, Entity excludePlayer) {
		List<Player> atSquare = new ArrayList<>();
		for (Entity e : players) {
			if (e != excludePlayer && e instanceof Player && e.getSquareIndex() == squareIndex) {
				atSquare.add((Player) e);
			}
		}
		return atSquare;
	}
}
