package entity;

import ObjectManagers.Inventory;

public class Entity {
	
	private int entityId;
	private int life;
	private Inventory inventory;
	private int numBox;
	
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
}
