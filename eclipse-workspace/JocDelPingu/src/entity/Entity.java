package entity;

import board.Board;
import board.SquareType;

public abstract class Entity {
	
	protected int entityId;
	protected int numSquare;
	protected EntityType type;
	protected Board board;

	public int getSquareIndex() {
		return this.numSquare;
	}
	public void advance(int squares) {
		if ((squares + this.numSquare) <= Board.MAX_SQUARES) {
			this.numSquare += squares;		
			this.updatePosition(this.numSquare);
		} else {
			System.out.println("Can not go forward");			
		}
	}
	
	public EntityType getType() {
		return this.type;
	}
	
	public String getName() {
		return null;
	}
	
	public void setBoard(Board board) {
		this.board = board;
	}
	
	public void setSquare(int newPosition) {
		this.numSquare = newPosition;
	}
	
	public void updatePosition(int newPosition) {
			if (board == null) {
				System.err.println("Entity's board is not set. Cannot update position effects.");
				return;
			}

			SquareType currentSquare = board.getSquareType(newPosition);
			System.out.println("--------------- " + board.getSquareType(newPosition));
			switch(currentSquare) {
	        case ICE_HOLE:
	            System.out.println("Player " + this.getName() + " fell into an ice hole and goes back to the previous hole");
	            this.fallIntoIceHole();
	            break;
	        case SLED:
	            break;
	        case BEAR:
	            // Handle bear encounter
	            break;
	        case EVENT:
	            // Trigger random event
	            break;
	        case BROKEN_FLOOR:
	            // Handle broken floor logic
	            break;
	        // etc.
			default:
				break;
	    }
	}
	private void fallIntoIceHole() {
		int destination = board.getDestination(this.numSquare);
		System.out.println("...moving back to square " + destination);
		this.setSquare(destination);
	}
	public int getEntityId() {
		return entityId;
	}
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}
	public int getNumSquare() {
		return numSquare;
	}
	public void setNumSquare(int numSquare) {
		this.numSquare = numSquare;
	}
	public Board getBoard() {
		return board;
	}
	public void setType(EntityType type) {
		this.type = type;
	}
}
