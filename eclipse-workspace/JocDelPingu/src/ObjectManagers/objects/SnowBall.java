package ObjectManagers.objects;

import ObjectManagers.ObjectType;
import entity.Player;

public class SnowBall extends ObjectManagers.Object {
    
    private int backwardSteps;
    
    public SnowBall() {
    	super(ObjectType.SNOWBALL);
    	this.name = "Snowball";
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
        
        java.lang.System.out.println(target.getName() + " moves back " + backwardSteps + 
                          " spaces (position: " + newPosition + ")");
    }
    
    public int getBackwardSteps() {
    	return backwardSteps;
    }
    
    public void setBackwardSteps(int steps) {
    	this.backwardSteps = steps;
    }
}