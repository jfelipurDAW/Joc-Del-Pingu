package ObjectManagers.objects;

import ObjectManagers.ObjectType;
import java.util.Random;
import entity.Player;

public class SnowBall extends Object {
    
    private ObjectType objectType = ObjectType.SNOWBALL;
    private int backwardSteps;
    private Random random;
    
    
    public SnowBall() {
    	
    }
    
    /**
     * Throws the snowball at a target player.
     * @param target The player who will be hit by the snowball.
     */
    public void throwSnowball(Player target) {
        int currentPosition = target.getPosition();
        int newPosition = Math.max(0, currentPosition - backwardSteps);
        
        target.updatePosition(newPosition);
        
        System.out.println(target.getName() + " moves back " + backwardSteps + 
                          " spaces (position: " + newPosition + ")");
    }
    
    public ObjectType getObjectType() {
        return objectType;
    }
}