package model.item.objects;

import model.item.ObjectType;


/**
 * Fish item carried in a player's inventory.
 *
 * <p>Fish have two distinct uses in the game:</p>
 * <ul>
 *   <li>Bribing the bear when landing on a BEAR square - the player gives
 *       up one fish instead of being sent back to the start.</li>
 *   <li>Feeding the seal when the seal lands on the player's square -
 *       the seal stops moving for two turns instead of attacking.</li>
 * </ul>
 *
 * <p>This class itself stores no extra state; it simply tags an inventory
 * item as a fish via the {@link ObjectType#FISH} category. All counts are
 * tracked inside {@code Inventory}.</p>
 */
public class Fish extends model.item.GameObject {


    /**
     * Creates a fish item. The category is fixed to {@link ObjectType#FISH}
     * and the display name is set so dialogs / logs can show "Fish".
     */
    public Fish() {
		super(ObjectType.FISH);
		this.setName("Fish");
	}
}
