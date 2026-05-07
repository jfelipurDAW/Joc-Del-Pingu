package model.game;

import java.util.ArrayList;
import java.util.List;

import model.entity.Entity;
import model.entity.Player;

public class TurnController {

	private ArrayList<Entity> players;
	private int turn;

	public TurnController() {
		players = new ArrayList<Entity>();
		turn = 0;
	}

	public void addPlayer(Entity player) {
		players.add(player);
	}

	public Entity getCurrentTurn() {
		return players.get(turn);
	}

	public int getCurrentTurnIndex() {
		return turn;
	}

	public void setCurrentTurn(int turnIndex) {
		this.turn = turnIndex % players.size();
	}

	/**
	 * Advance to the next turn. Handles skip-turn logic.
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

	public ArrayList<Entity> getAllPlayers() {
		return players;
	}

	/**
	 * Returns all human players (not seal).
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
