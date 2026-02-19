package entity;

public class Player extends Entity{
	
	private int id;
	private String colour;
	private String name;
	
	
	public Player(String name, String colour) {
		
		this.setID();
		this.setName(name);
		this.setColour(colour);
		
		this.setNumCasella(0);		
		
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public void setID() {
		
	}
}
