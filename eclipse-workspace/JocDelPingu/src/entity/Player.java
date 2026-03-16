package entity;

import ObjectManagers.Inventory;
import ObjectManagers.ObjectType;

public class Player extends Entity{
	
	private String name;
	private String colour;
	private String password;
	private Inventory inventory;

	
	public Player(String name, String colour) {
		
		this.type = EntityType.PLAYER;
		this.setID();
		this.setName(name);
		this.setColour(colour);	
		this.inventory = new Inventory(entityId);
		
	}
	 /**
     * Constructor with password (for database)
     */
    public Player(String name, String colour, String password) {
        super();
        this.type = EntityType.PLAYER;
        this.name = name;
        this.colour = colour;
        this.password = password;
        this.inventory = new Inventory(entityId);
       
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
	
	public void setID() {
	
	}
	@Override
    public String toString() {
        return name + " (" + colour + ") - Square: " + numSquare;
    }
	
	public Player SnowballWar(Player player1, Player player2) {
		
		if (player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL) > player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL)) {
			player1.getInventory().useObject(ObjectType.SNOWBALL, player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			player2.getInventory().useObject(ObjectType.SNOWBALL, player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			
			
			return player1;
			
		} else if (player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL) > player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL)) {
			player2.getInventory().useObject(ObjectType.SNOWBALL, player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			player1.getInventory().useObject(ObjectType.SNOWBALL, player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			
			
			return player2;
			
		} else {
			player1.getInventory().useObject(ObjectType.SNOWBALL, player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			player2.getInventory().useObject(ObjectType.SNOWBALL, player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL));
			
			
			return null;
			
		}
		
	}

	public int getPosition() {
		return this.numSquare;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void moveForward(int number) {
        this.setSquare(this.getSquareIndex()+number);
        //Play animation walking through squares
        
        this.updatePosition(this.getSquareIndex());
    }

	public void setSquare(int i) {
		this.numSquare = i;
	}
	
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	
	public Inventory getInventory() {
		return this.inventory;
	}
}
