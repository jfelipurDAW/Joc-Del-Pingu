package ObjectManagers;

import java.util.ArrayList;

public class Inventory {
	
	
	private ArrayList<Object> inventory;
	
	private int entityId;
	
	private int snowballQuantity;
	private int fishQuantity;
	private int diceQuantity;
	
	private int fastdiceQuantity;
	private int slowdiceQuantity;
	
	////////////////////////
	///   CONSTRUCTORS   ///
	////////////////////////
	public Inventory (int entityId) {
		this.inventory = new ArrayList<Object>();
		this.entityId = entityId;
		
		this.snowballQuantity = 0;
		this.fishQuantity = 0;
		this.diceQuantity = 0;
		this.fastdiceQuantity = 0;
		this.slowdiceQuantity = 0;
	}
	
	public Inventory (int entityId, ArrayList<Object> inventory, int snowballQuantity, int fishQuantity, int diceQuantity, int fastdiceQuantity, int slowdiceQuantity) {
		this.inventory = inventory;
		this.entityId = entityId;
		
		this.snowballQuantity = snowballQuantity;
		this.fishQuantity = fishQuantity;
		this.diceQuantity = diceQuantity;
		this.fastdiceQuantity = fastdiceQuantity;
		this.slowdiceQuantity = slowdiceQuantity;
	}
	
	
	///////////////////
	///   METHODS   ///
	///////////////////
	public void addObject(Object object) {
		
		switch(object.getType()) {
		
		case SNOWBALL:
			if (snowballQuantity < 6) {
				this.inventory.add(object);
				snowballQuantity++;
			}
			
			
			break;
		case FISH:
			if (fishQuantity < 2) {
				this.inventory.add(object);
				fishQuantity++;
			}
			
			
			break;
		case DICE:
			if (diceQuantity < 3) {
				this.inventory.add(object);
				diceQuantity++;
			}
			
			
			break;
		}
		
	}
}
