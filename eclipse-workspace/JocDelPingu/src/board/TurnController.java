package board;

import java.util.ArrayList;
import entity.Entity;

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
	
	public void removePlayer(Entity player) {
		players.remove(player);
	}
	
	
	public Entity getCurrentTurn() {
		return players.get(turn);
	}
	
	public void nextTurn() {
		turn = (turn + 1) % players.size();
	}
	
	public ArrayList<Entity> getAllPlayers() {
		return players;
	}
	
}
