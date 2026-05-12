package model.item.objects;

import model.item.ObjectType;


/**
 * Dice item used by players to advance on the board.
 *
 * <p>The class supports three flavours, selected through the
 * {@link ObjectType} passed to the constructor:</p>
 * <ul>
 *   <li>{@code FASTDICE} - rolls high values (5-10). Players use it to leap
 *       forward when they are close to the finish or want to risk it.</li>
 *   <li>{@code SLOWDICE} - rolls low values (1-3). Useful to avoid
 *       overshooting traps or to crawl over the broken-floor square.</li>
 *   <li>Anything else falls back to the regular 1-6 die.</li>
 * </ul>
 */
public class Dice extends model.item.GameObject {


    /////////////////////////////
    ///      CONSTANTS       ///
    /////////////////////////////

    // Slow dice range: small steps, safer for traps.
    private static final int SLOWDICE_MIN_VALUE = 1;
    private static final int SLOWDICE_MAX_VALUE = 3;

    // Fast dice range: big leaps to rush toward the end.
    private static final int FASTDICE_MIN_VALUE = 5;
    private static final int FASTDICE_MAX_VALUE = 10;


    /////////////////////////////
    ///       FIELDS         ///
    /////////////////////////////

    // Effective min/max set by the constructor based on the dice variant.
    // roll() uses these to produce a pseudo-random value in the inclusive range.
    private int minValue;
    private int maxValue;


    /////////////////////////////
    ///     CONSTRUCTOR      ///
    /////////////////////////////

    /**
     * Builds a dice of the requested variant and configures its value range.
     * Unknown / non-dice {@link ObjectType} values fall back to a standard
     * 1-6 die so callers always get a usable instance.
     *
     * @param diceType the dice flavour to create
     */
    public Dice(ObjectType diceType) {
    	super(diceType);

        switch (diceType) {

        case FASTDICE:
        	this.setName("Fast Dice");
        	this.minValue = FASTDICE_MIN_VALUE;
        	this.maxValue = FASTDICE_MAX_VALUE;
        	break;

        case SLOWDICE:
        	this.setName("Slow Dice");
        	this.minValue = SLOWDICE_MIN_VALUE;
        	this.maxValue = SLOWDICE_MAX_VALUE;
        	break;

        default:
        	// Default 1-6 cubic die used for the base "Roll" action.
        	this.setName("Dice");
        	this.minValue = 1;
        	this.maxValue = 6;
        	break;
        }
    }


    /////////////////////////////
    ///       METHODS        ///
    /////////////////////////////

    /**
     * Returns a fresh pseudo-random roll within this dice's configured range
     * (both bounds inclusive). This is the only source of randomness for the
     * gameplay-side dice; the debug "force next roll" feature lives in the
     * controller and bypasses this method when active.
     *
     * @return a value in {@code [minValue, maxValue]}
     */
    public int roll() {
        return (int) ((java.lang.Math.random() * (maxValue - minValue + 1)) + minValue);
    }
}
