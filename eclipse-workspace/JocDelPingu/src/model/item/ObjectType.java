package model.item;


/**
 * Enumeration of every item / consumable that can exist in a player's
 * {@link Inventory}.
 *
 * <p>The values cover both pick-ups dropped by event squares and the
 * special dice variants that players can roll on top of the default die:</p>
 * <ul>
 *   <li>{@link #SNOWBALL} - thrown at opponents to push them back.</li>
 *   <li>{@link #FISH}     - used to bribe the bear or to feed the seal.</li>
 *   <li>{@link #DICE}     - generic dice marker (total dice count).</li>
 *   <li>{@link #FASTDICE} - rolls high values (5-10) for big advances.</li>
 *   <li>{@link #SLOWDICE} - rolls low values (1-3) for short, safer moves.</li>
 * </ul>
 */
public enum ObjectType {

    SNOWBALL,
    FISH,
    DICE,
    FASTDICE,
    SLOWDICE
}
