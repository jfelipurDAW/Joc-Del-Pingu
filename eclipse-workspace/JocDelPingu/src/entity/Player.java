package entity;

import ObjectManagers.ObjectType;
import board.Board;
import board.Square;
import board.SquareType;
import gamePanel.GameBoardController;

public class Player extends Entity{
	
	private String colour;
	private String password;
	
	
	public Player(String name, String colour) {
		
		this.type = EntityType.PLAYER;
		this.setID();
		this.setName(name);
		this.setColour(colour);	
		
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public void setColour(String colour) {
		this.colour = colour;
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

	public void updatePosition(int newPosition) {
		
		Board board = new Board();
		SquareType currentSquare = board.getSquareType(newPosition);
		
	}

	public int getPosition() {
		return this.numSquare;
	}

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
}
