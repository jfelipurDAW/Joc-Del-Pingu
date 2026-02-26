package entity;

import ObjectManagers.Inventory;

public class Entity {
	
	protected int entityId;
	protected Inventory inventory;
	protected int numBox;
	
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	
}
