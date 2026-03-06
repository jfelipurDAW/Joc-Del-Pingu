package entity;

import ObjectManagers.Inventory;
import ObjectManagers.ObjectType;
import board.Board;

public class Entity {
	
	protected int entityId;
	protected Inventory inventory;
	protected int numSquare;
	protected EntityType type;
	protected String name;
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public int getSquareIndex() {
		return this.numSquare;
	}
	public void advance(int squares) {
		if ((this.numSquare += squares) < Board.MAX_SQUARES) {
			this.numSquare += squares;			
		}
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	public EntityType getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
}
