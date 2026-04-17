package model.game;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.util.List;

import model.board.Board;
import model.entity.Entity;
import model.entity.Player;

public class TurnController {

	private ArrayList<Entity> players;
	private int turn;
	private boolean gameWon;
	private Player winner;
	
	public TurnController() {
		players = new ArrayList<Entity>();
		turn = 0;
		gameWon = false;
		winner = null;
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
			System.out.println(next.getName() + " skips this turn!");
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
	
	/**
	 * Check if a player has reached the end.
	 */
	public boolean checkWinCondition() {
		for (Entity e : players) {
			if (e instanceof Player && e.getSquareIndex() >= Board.MAX_SQUARES - 1) {
				gameWon = true;
				winner = (Player) e;
				return true;
			}
		}
		return false;
	}
	
	public boolean isGameWon() {
		return gameWon;
	}
	
	public Player getWinner() {
		return winner;
	}
	
	public int getPlayerCount() {
		return players.size();
	}
	@FXML
	private ListView<String> listaPartidas; // El componente de tu FXML

	// Este método se ejecuta al abrir la pantalla
	public void initialize() {
	    // 1. Buscamos todas las partidas que hay en Oracle
	    List<String> partidasEnBD = SaveLoadService.getAllSavedGameIds();
	    
	    // 2. Las metemos en la lista visual
	    listaPartidas.getItems().addAll(partidasEnBD);
	}

	@FXML
	private void onBotonCargarClick() {
	    // 3. Miramos qué nombre ha seleccionado el usuario con el ratón
	    String partidaSeleccionada = listaPartidas.getSelectionModel().getSelectedItem();
	    
	    if (partidaSeleccionada != null) {
	        // 4. Cargamos ESA partida exacta
	        boolean ok = SaveLoadService.loadGame(partidaSeleccionada);
	        if (ok) {
	            System.out.println("Cargando: " + partidaSeleccionada);
	            // Aquí cambias a la pantalla del juego...
	        }
	    } else {
	        System.out.println("Por favor, selecciona una partida de la lista.");
	    }
	}
}
