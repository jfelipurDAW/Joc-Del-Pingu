package entity;

import board.Board;
import board.EventManager;
import board.SquareType;
import ObjectManagers.ObjectType;

public abstract class Entity {
	
	protected int entityId;
	protected int numSquare;
	protected EntityType type;
	protected Board board;
	protected boolean skipNextTurn;
	
	// Last event result for UI display
	protected EventManager.EventResult lastEvent;

	public int getSquareIndex() {
		return this.numSquare;
	}
	
	/**
	 * Advance the entity by a number of squares, clamped to not exceed the board.
	 * Returns true if the entity reached or passed the END square.
	 */
	public boolean advance(int squares) {
		int newPos = this.numSquare + squares;
		if (newPos >= Board.MAX_SQUARES - 1) {
			this.numSquare = Board.MAX_SQUARES - 1;
			return true; // reached the end!
		} else {
			this.numSquare = newPos;
			return false;
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
		this.numSquare = Math.max(0, Math.min(newPosition, Board.MAX_SQUARES - 1));
	}
	
	/**
	 * Process the effect of landing on the current square.
	 * Returns a description of what happened for the game log.
	 */
	public String updatePosition(int newPosition) {
		if (board == null) {
			System.err.println("Entity's board is not set. Cannot update position effects.");
			return "Error: board not set";
		}

		SquareType currentSquare = board.getSquareType(newPosition);
		System.out.println("--- " + getName() + " landed on: " + currentSquare + " (square " + newPosition + ")");
		
		switch(currentSquare) {
        case ICE_HOLE:
            return handleIceHole();
        case SLED:
            return handleSled();
        case BEAR:
            return handleBear();
        case EVENT:
            return handleEvent();
        case BROKEN_FLOOR:
            return handleBrokenFloor();
        case START:
        	return getName() + " is at the start.";
        case END:
        	return getName() + " reached the END! 🎉";
        case NORMAL:
        default:
        	return getName() + " landed on a normal square.";
	    }
	}
	
	private String handleIceHole() {
		int destination = board.getDestination(this.numSquare);
		this.setSquare(destination);
		String msg = getName() + " fell into an ice hole! Sent back to square " + destination;
		System.out.println(msg);
		return "🕳️ " + msg;
	}
	
	private String handleSled() {
		int destination = board.getDestination(this.numSquare);
		if (destination != this.numSquare) {
			this.setSquare(destination);
			String msg = getName() + " found a sled! Zooming forward to square " + destination;
			System.out.println(msg);
			return "🛷 " + msg;
		} else {
			return "🛷 " + getName() + " found the last sled. Nothing happens.";
		}
	}
	
	private String handleBear() {
		if (this instanceof Player) {
			Player player = (Player) this;
			if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
				player.getInventory().useObject(ObjectType.FISH, 1);
				String msg = getName() + " bribed the bear with a fish! 🐟 Safe!";
				System.out.println(msg);
				return "🐻 " + msg;
			} else {
				this.setSquare(0);
				String msg = getName() + " was attacked by the bear! No fish to bribe! Back to START!";
				System.out.println(msg);
				return "🐻💥 " + msg;
			}
		}
		return getName() + " encountered a bear.";
	}
	
	private String handleEvent() {
		if (this instanceof Player) {
			Player player = (Player) this;
			lastEvent = EventManager.triggerEvent(player, board);
			String msg = lastEvent.toString();
			System.out.println(msg);
			
			// If event changed position (snowmobile), update display
			if (lastEvent.getNewPosition() >= 0) {
				this.numSquare = lastEvent.getNewPosition();
			}
			
			return "❓ " + msg;
		}
		return getName() + " triggered an event.";
	}
	
	private String handleBrokenFloor() {
		if (this instanceof Player) {
			Player player = (Player) this;
			int totalItems = player.getInventory().getTotalItemCount();
			
			if (totalItems > 5) {
				// Too heavy! Fall through, return to start
				this.setSquare(0);
				String msg = getName() + " was too heavy (" + totalItems + " items)! Fell through the broken floor! Back to START!";
				System.out.println(msg);
				return "💔 " + msg;
			} else if (totalItems > 0) {
				// Has some items, lose a turn
				player.setSkipNextTurn(true);
				String msg = getName() + " cracked the broken floor (" + totalItems + " items). Loses next turn!";
				System.out.println(msg);
				return "⚠️ " + msg;
			} else {
				// No items, pass safely!
				String msg = getName() + " crosses the broken floor safely (no items)!";
				System.out.println(msg);
				return "✅ " + msg;
			}
		}
		return getName() + " encountered broken floor.";
	}
	
	// ---------- Getters / Setters ----------
	
	public EventManager.EventResult getLastEvent() {
		return lastEvent;
	}
	
	public void clearLastEvent() {
		lastEvent = null;
	}
	
	public void setLastEvent(EventManager.EventResult event) {
		this.lastEvent = event;
	}
	
	public boolean shouldSkipNextTurn() {
		return skipNextTurn;
	}
	
	public void setSkipNextTurn(boolean skip) {
		this.skipNextTurn = skip;
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
