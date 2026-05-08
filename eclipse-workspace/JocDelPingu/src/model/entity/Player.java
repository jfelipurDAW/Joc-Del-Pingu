package model.entity;

import java.util.ArrayList;
import java.util.List;

import model.board.Board;
import model.item.Inventory;

public class Player extends Entity {

	private static final int MAX_EVENT_HISTORY = 50;

	private String name;
	private String colour;
	private String password;
    private String avatarPath;
	private Inventory inventory;
	private List<String> eventHistory = new ArrayList<>();


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
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

	public String getColour() {
		return this.colour;
	}

	public String getPassword() {
		return this.password;
	}

    public String getAvatarPath() {
        return this.avatarPath;
    }

	@Override
    public String toString() {
        return name + " (" + colour + ") - Square: " + this.getNumSquare() + " " + inventory.toString();
    }

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setSquare(int i) {
		int target = Math.max(0, Math.min(i, this.getBoard() != null ? Board.MAX_SQUARES - 1 : i));
		// setNumSquare updates facingRight based on direction
		this.setNumSquare(target);
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
	 * Append an event/action message to the player's history (capped at
	 * MAX_EVENT_HISTORY entries to keep saved games compact).
	 */
	public void recordEvent(String message) {
		if (message == null || message.isEmpty()) return;
		this.eventHistory.add(message);
		while (this.eventHistory.size() > MAX_EVENT_HISTORY) {
			this.eventHistory.remove(0);
		}
	}

	public List<String> getEventHistory() {
		return this.eventHistory;
	}

	public void setEventHistory(List<String> history) {
		this.eventHistory = (history != null) ? new ArrayList<>(history) : new ArrayList<>();
	}
}
