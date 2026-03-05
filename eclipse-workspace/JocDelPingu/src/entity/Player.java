package entity;
import ObjectManagers.Inventory;
public class Player extends Entity{
	
	    
	    private String colour;
	    private String name;
	    private String password;
	    
	    public Player(String name, String colour) {
	        this.setID();
	        this.setName(name);
	        this.setColour(colour);
	        this.numSquare = 0;
	        this.inventory = new Inventory(); 
	    }
	    
	    public void setName(String name) {
	        this.name = name;
	    }
	    
	    public String getName() {
	        return name;
	    }
	    
	    public void setColour(String colour) {
	        this.colour = colour;
	    }
	    
	    public String getColour() {
	        return colour;
	    }
	    
	    public void setID() {
	        this.entityId = (int) (Math.random() * 100000);
	    }
	    
	    @Override
	    public String toString() {
	        return name + " (" + colour + ") - Casella: " + numSquare;
	    }
	}
