package model.item.objects;

import model.item.ObjectType;
import model.entity.Player;

public class SnowBall extends model.item.GameObject {
    
    private int backwardSteps;
    
    public SnowBall() {
    	super(ObjectType.SNOWBALL);
    	this.setName("Snowball");
    	this.backwardSteps = 1;
    }
    
    /**
     * Throws the snowball at a target player.
     * @param target The player who will be hit by the snowball.
     */
    public void throwSnowball(Player target) {
        int currentPosition = target.getPosition();
        int newPosition = java.lang.Math.max(0, currentPosition - backwardSteps);
        
        target.setSquare(newPosition);
        // Log handled by controller/manaager
    }
    
    public int getBackwardSteps() {
    	return backwardSteps;
    }
    
    public void setBackwardSteps(int steps) {
    	this.backwardSteps = steps;
    }
}