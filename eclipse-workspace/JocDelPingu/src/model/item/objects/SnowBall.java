package model.item.objects;

import model.item.ObjectType;


/**
 * Snowball item carried in a player's inventory.
 *
 * <p>Snowballs are the offensive resource of the game:</p>
 * <ul>
 *   <li>A player may "throw" a snowball at another player at any time
 *       during their turn, pushing the target backwards a number of
 *       squares determined by the action logic.</li>
 *   <li>When two players share a square they enter a "snowball war":
 *       the player with the most snowballs wins and the loser retreats.</li>
 * </ul>
 *
 * <p>Like {@code Fish}, the class itself carries no behaviour - it simply
 * tags an inventory entry as {@link ObjectType#SNOWBALL}. The counters
 * are stored inside {@code Inventory}.</p>
 */
public class SnowBall extends model.item.GameObject {


    /**
     * Creates a snowball item with the correct {@link ObjectType} category
     * and a human-readable display name ("Snowball") for the UI.
     */
    public SnowBall() {
    	super(ObjectType.SNOWBALL);
    	this.setName("Snowball");
    }
}
