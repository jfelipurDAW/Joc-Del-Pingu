package entity;

import ObjectManagers.ObjectType;

public class Player extends Entity{
	
	private String colour;
	private String name;
	private String password;
	
	
	public Player(String name, String colour) {
		
		this.setID();
		this.setName(name);
		this.setColour(colour);
		//this.setNumCasella(0);		
		
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public void setID() {
	
	}
	@Override
    public String toString() {
        return name + " (" + colour + ") - Casella: " + numSquare;
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

}
