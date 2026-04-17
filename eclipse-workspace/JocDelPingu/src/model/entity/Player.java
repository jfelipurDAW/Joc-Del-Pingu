package model.entity;

import java.util.ArrayList;
import java.util.Random;

import model.board.Board;
import model.item.Inventory;
import model.item.ObjectType;

public class Player extends Entity {
	
	private String name;
	private String colour;
	private String password;
	private Inventory inventory;

	
	public Player(String name, String colour) {
		this.setType(EntityType.PLAYER);
		this.setEntityId((int) (Math.random() * 100000));
		this.name = name;
		this.colour = colour;
		this.setNumSquare(0);
		this.setSkipNextTurn(false);
		this.inventory = new Inventory(this.getEntityId());
	}
	
    /**
     * Constructor with password (for database)
     */
    public Player(String name, String colour, String password) {
        this.setType(EntityType.PLAYER);
        this.setEntityId((int) (Math.random() * 100000));
        this.name = name;
        this.colour = colour;
        this.password = password;
        this.setNumSquare(0);
        this.setSkipNextTurn(false);
        this.inventory = new Inventory(this.getEntityId());
    }
	
	public void setName(String name) {
		this.name = name;
	}	
	public void setColour(String colour) {
		this.colour = colour;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	@Override
    public String toString() {
        return name + " (" + colour + ") - Square: " + this.getNumSquare() + " " + inventory.toString();
    }
	
	/**
	 * Snowball war between two players.
	 * Returns the winner (or null if tie).
	 * The loser retreats by the difference in snowball count.
	 */
	public static SnowballWarResult snowballWar(Player player1, Player player2) {
		int balls1 = player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
		int balls2 = player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
		
		// Both spend ALL their snowballs
		player1.getInventory().useObject(ObjectType.SNOWBALL, balls1);
		player2.getInventory().useObject(ObjectType.SNOWBALL, balls2);
		
		int difference = Math.abs(balls1 - balls2);
		
		if (balls1 > balls2) {
			// Player1 wins, Player2 goes back
			int newPos = Math.max(0, player2.getSquareIndex() - difference);
			player2.setSquare(newPos);
			return new SnowballWarResult(player1, player2, balls1, balls2, difference);
		} else if (balls2 > balls1) {
			// Player2 wins, Player1 goes back
			int newPos = Math.max(0, player1.getSquareIndex() - difference);
			player1.setSquare(newPos);
			return new SnowballWarResult(player2, player1, balls2, balls1, difference);
		} else {
			// Tie: both spend all, no retreat
			return new SnowballWarResult(null, null, balls1, balls2, 0);
		}
	}
	
	/**
	 * Result of a snowball war.
	 */
	public static class SnowballWarResult {
		private final Player winner;
		private final Player loser;
		private final int winnerBalls;
		private final int loserBalls;
		private final int difference;
		
		public SnowballWarResult(Player winner, Player loser, int winnerBalls, int loserBalls, int difference) {
			this.winner = winner;
			this.loser = loser;
			this.winnerBalls = winnerBalls;
			this.loserBalls = loserBalls;
			this.difference = difference;
		}
		
		public Player getWinner() { return winner; }
		public Player getLoser() { return loser; }
		public int getWinnerBalls() { return winnerBalls; }
		public int getLoserBalls() { return loserBalls; }
		public int getDifference() { return difference; }
		public boolean isTie() { return winner == null; }
		
		@Override
		public String toString() {
			if (isTie()) {
				return "It's a tie! (" + winnerBalls + " vs " + loserBalls + ") Both spend all snowballs. No one retreats.";
			} else {
				return winner.getName() + " wins! (" + winnerBalls + " vs " + loserBalls + ") " + 
				       loser.getName() + " retreats " + difference + " squares!";
			}
		}
	}

	public int getPosition() {
		return this.getNumSquare();
	}

	/**
	 * Manage player's needs (required by spec).
	 */
	public void manageNeeds() {
		// Manage needs implementation
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Move forward by a number and process the square effect.
	 * Returns a log message describing what happened.
	 */
	public String moveForward(int number) {
		boolean reachedEnd = this.advance(number);
		if (reachedEnd) {
			return this.getName() + " reached the END! 🎉 WINNER!";
		}
        return this.updatePosition(this.getSquareIndex());
    }

	@Override
	public void setSquare(int i) {
		this.setNumSquare(Math.max(0, Math.min(i, this.getBoard() != null ? Board.MAX_SQUARES - 1 : i)));
	}
	
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	/**
	 * Lose half of all inventory items. Used when seal passes through.
	 */
	public void loseHalfInventory() {
		this.inventory.removeHalf();
	}
	
	/**
	 * Lose a random item from inventory.
	 */
	public ObjectType loseRandomItem() {
		return this.inventory.removeRandomItem();
	}
}
