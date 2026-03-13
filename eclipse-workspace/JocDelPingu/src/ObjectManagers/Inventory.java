package ObjectManagers;

import java.util.ArrayList;

public class Inventory {
	
	
	private ArrayList<Object> inventory;
	private ArrayList<ObjectManagers.Object> lista;
	
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
		this.lista = new ArrayList<ObjectManagers.Object>();
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
	
		//////////////////////////////
		///   GETTERS I SETTERS    /// 
		//////////////////////////////
		
	public ArrayList<Object> getLista() {
		return lista;
		}
		
	public void setLista(ArrayList<Object> lista) {
		this.lista = lista;
		}
		
	public int getEntityId() {
		return entityId;
		}
		
	public void setEntityId(int entityId) {
		this.entityId = entityId;
		}
	
	public int getSnowballQuantity() {
		return snowballQuantity;
		}
		
	public void setSnowballQuantity(int snowballQuantity) {
		this.snowballQuantity = snowballQuantity;
		}
		
	public int getFishQuantity() {
		return fishQuantity;
		}
		
	public void setFishQuantity(int fishQuantity) {
		this.fishQuantity = fishQuantity;
		}
		
	public int getDiceQuantity() {
		return diceQuantity;
		}
		
	public void setDiceQuantity(int diceQuantity) {
		this.diceQuantity = diceQuantity;
		}
		
	public int getFastdiceQuantity() {
		return fastdiceQuantity;
		}
		
	public void setFastdiceQuantity(int fastdiceQuantity) {
		this.fastdiceQuantity = fastdiceQuantity;
		}
		
	public int getSlowdiceQuantity() {
		return slowdiceQuantity;
		}
		
	public void setSlowdiceQuantity(int slowdiceQuantity) {
		this.slowdiceQuantity = slowdiceQuantity;
		}
	
	
	///////////////////
	///   METHODS   ///
	///////////////////
	
	
	public int getObjectQuantity(ObjectType object) {
		
		switch(object) {
		
		case SNOWBALL:
			return this.snowballQuantity;
			
		case FISH:
			return this.fishQuantity;
			
		case DICE:
			return this.diceQuantity;
			
		case FASTDICE:
			return this.fastdiceQuantity;
			
		case SLOWDICE:
			return this.slowdiceQuantity;
		}
		
		return -1;
	}
	
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
	
	
	
	public boolean useObject(ObjectType object, int quantity) {
		
		switch(object) {

		case FISH:
			if (fishQuantity >= quantity) {
				fishQuantity -= quantity;
				return true;
			}
			
			
			break;
			
		case SNOWBALL:
			if (snowballQuantity >= quantity) {
				snowballQuantity -= quantity;
				return true;
			}
			
			
			break;

		case DICE:
			if (diceQuantity >= quantity) {
				diceQuantity -= quantity;
				return true;
			}
			
			
			break;
		}
		
		return false;
	}
}
