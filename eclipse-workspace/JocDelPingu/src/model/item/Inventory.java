package model.item;

import java.util.ArrayList;
import java.util.Random;


/**
 * Per-player inventory that tracks how many copies of each {@link ObjectType}
 * a {@code Player} currently owns.
 *
 * <p>The class deliberately stores raw counters (snowballs, fish, fast dice,
 * slow dice) instead of a list of {@link GameObject} instances - the game only
 * needs quantities, and integer fields make the save/load and the HUD logic
 * much simpler.</p>
 *
 * <p>Each item type has a per-player cap exposed as a {@code MAX_*} constant
 * so the rule "you cannot stockpile more than X of an item" is enforced in a
 * single place. The dice counters are special: every fast or slow dice also
 * counts toward the shared {@link #MAX_DICE} cap (see {@link #addDice}).</p>
 */
public class Inventory {


	/////////////////////////////
	///       FIELDS         ///
	/////////////////////////////

	// Per-item counters. Kept as plain ints so the save/load layer can
	// serialize them directly without iterating a list of objects.
	private int snowballQuantity;
	private int fishQuantity;
	private int diceQuantity;

	// Specific dice variants. diceQuantity == fastdiceQuantity + slowdiceQuantity.
	// The split is kept so the UI knows which die icons to show in the hotbar.
	private int fastdiceQuantity;
	private int slowdiceQuantity;


	/////////////////////////////
	///      CONSTANTS       ///
	/////////////////////////////

	// Per-player caps. Hitting these limits causes addX() methods to refuse
	// further insertions, which is how "you already have enough" rules work.
	public static final int MAX_SNOWBALLS = 6;
	public static final int MAX_FISH = 2;
	public static final int MAX_DICE = 3;


	/////////////////////////////
	///     CONSTRUCTOR      ///
	/////////////////////////////

	/**
	 * Builds an empty inventory for the given entity. The {@code entityId}
	 * is accepted only for compatibility with older code paths - the
	 * counters themselves do not depend on it.
	 *
	 * @param entityId the id of the entity owning this inventory
	 */
	public Inventory(int entityId) {
		this.snowballQuantity = 0;
		this.fishQuantity = 0;
		this.diceQuantity = 0;
		this.fastdiceQuantity = 0;
		this.slowdiceQuantity = 0;
	}


	//////////////////////////////
	///   GETTERS AND SETTERS  ///
	//////////////////////////////

	/** @return current number of snowballs */
	public int getSnowballQuantity() {
		return snowballQuantity;
	}

	/** @param snowballQuantity new snowball count (used by save/load) */
	public void setSnowballQuantity(int snowballQuantity) {
		this.snowballQuantity = snowballQuantity;
	}

	/** @return current number of fish */
	public int getFishQuantity() {
		return fishQuantity;
	}

	/** @param fishQuantity new fish count (used by save/load) */
	public void setFishQuantity(int fishQuantity) {
		this.fishQuantity = fishQuantity;
	}

	/** @return total dice count (fast + slow) */
	public int getDiceQuantity() {
		return diceQuantity;
	}

	/** @param diceQuantity new total dice count (used by save/load) */
	public void setDiceQuantity(int diceQuantity) {
		this.diceQuantity = diceQuantity;
	}

	/** @return current number of fast dice */
	public int getFastdiceQuantity() {
		return fastdiceQuantity;
	}

	/** @param fastdiceQuantity new fast-dice count (used by save/load) */
	public void setFastdiceQuantity(int fastdiceQuantity) {
		this.fastdiceQuantity = fastdiceQuantity;
	}

	/** @return current number of slow dice */
	public int getSlowdiceQuantity() {
		return slowdiceQuantity;
	}

	/** @param slowdiceQuantity new slow-dice count (used by save/load) */
	public void setSlowdiceQuantity(int slowdiceQuantity) {
		this.slowdiceQuantity = slowdiceQuantity;
	}



	///////////////////
	///   METHODS   ///
	///////////////////

	/**
	 * Returns the total number of items in the inventory.
	 *
	 * <p>Note: the generic {@code diceQuantity} field is intentionally
	 * excluded from the sum because every fast / slow die already contributes
	 * once via its specific counter. Including it would double-count dice.</p>
	 *
	 * @return the sum of snowballs, fish and dice variants
	 */
	public int getTotalItemCount() {
		return snowballQuantity + fishQuantity + fastdiceQuantity + slowdiceQuantity;
	}


	/**
	 * Reads the count for a single {@link ObjectType}. Used by the HUD and
	 * by gameplay code that needs to ask "do I have any of X?" before
	 * triggering an action.
	 *
	 * @param object the type to look up
	 * @return the number of items of that type, or -1 if the type is unknown
	 */
	public int getObjectQuantity(ObjectType object) {

		switch(object) {

		case SNOWBALL:
			return this.snowballQuantity;

		case FISH:
			return this.fishQuantity;

		case DICE:
			return this.diceQuantity;

		case FASTDICE:
			return this.fastdiceQuantity;

		case SLOWDICE:
			return this.slowdiceQuantity;

		default:
			return -1;
		}
	}


	/**
	 * Consumes {@code quantity} items of the given type if the inventory
	 * has enough of them. Fast / slow dice usage also decrements the shared
	 * dice total because both counters must stay consistent.
	 *
	 * @param object   the item type to consume
	 * @param quantity how many to remove
	 * @return {@code true} if the items were consumed, {@code false} if there were not enough
	 */
	public boolean useObject(ObjectType object, int quantity) {

		switch(object) {

		case FISH:
			if (fishQuantity >= quantity) {
				fishQuantity -= quantity;
				return true;
			}
			break;

		case SNOWBALL:
			if (snowballQuantity >= quantity) {
				snowballQuantity -= quantity;
				return true;
			}
			break;

		case DICE:
			if (diceQuantity >= quantity) {
				diceQuantity -= quantity;
				return true;
			}
			break;

		case FASTDICE:
			if (fastdiceQuantity >= quantity) {
				// Keep the shared dice total in sync with the specific counter
				fastdiceQuantity -= quantity;
				diceQuantity -= quantity;
				return true;
			}
			break;

		case SLOWDICE:
			if (slowdiceQuantity >= quantity) {
				// Keep the shared dice total in sync with the specific counter
				slowdiceQuantity -= quantity;
				diceQuantity -= quantity;
				return true;
			}
			break;

		default:
			break;
		}

		return false;
	}


	/**
	 * Convenience method: add snowballs (capped at MAX).
	 *
	 * <p>The loop adds one snowball at a time so we can report exactly how
	 * many of the requested {@code count} actually fit before hitting the
	 * cap - useful when an event square tries to grant several at once.</p>
	 *
	 * @param count desired number of snowballs to add
	 * @return the number that were actually added (between 0 and {@code count})
	 */
	public int addSnowballs(int count) {
		int added = 0;
		for (int i = 0; i < count; i++) {
			if (snowballQuantity < MAX_SNOWBALLS) {
				snowballQuantity++;
				added++;
			}
		}
		return added;
	}


	/**
	 * Convenience method: add fish (capped at MAX).
	 *
	 * @return {@code true} if a fish was added, {@code false} if the cap was already reached
	 */
	public boolean addFish() {
		if (fishQuantity < MAX_FISH) {
			fishQuantity++;
			return true;
		}
		return false;
	}


	/**
	 * Convenience method: add a dice of given type (capped at MAX total dice).
	 *
	 * <p>The total dice count {@link #MAX_DICE} is shared between fast and
	 * slow dice. The specific counter for the chosen variant is also bumped
	 * so the UI can keep showing the correct icon.</p>
	 *
	 * @param diceType either {@link ObjectType#FASTDICE} or {@link ObjectType#SLOWDICE}
	 * @return {@code true} if the dice was added, {@code false} if the cap was already reached
	 */
	public boolean addDice(ObjectType diceType) {
		if (diceQuantity < MAX_DICE) {
			diceQuantity++;
			if (diceType == ObjectType.FASTDICE) {
				fastdiceQuantity++;
			} else {
				slowdiceQuantity++;
			}
			return true;
		}
		return false;
	}


	/**
	 * Remove a random item from inventory. Returns the type removed, or null if empty.
	 *
	 * <p>Used by the "broken floor" square, which makes the player drop one
	 * random item on a non-fatal stumble. Only types that currently have at
	 * least one instance are eligible, so the random choice never fails.</p>
	 *
	 * @return the type of item removed, or {@code null} if the inventory was empty
	 */
	public ObjectType removeRandomItem() {
		if (getTotalItemCount() == 0) return null;

		Random rand = new Random();

		// Build the eligible-types list only with categories that have at
		// least one item, so the random pick is never wasted on empty slots.
		ArrayList<ObjectType> available = new ArrayList<>();
		if (snowballQuantity > 0) available.add(ObjectType.SNOWBALL);
		if (fishQuantity > 0) available.add(ObjectType.FISH);
		if (fastdiceQuantity > 0) available.add(ObjectType.FASTDICE);
		if (slowdiceQuantity > 0) available.add(ObjectType.SLOWDICE);

		if (available.isEmpty()) return null;

		ObjectType chosen = available.get(rand.nextInt(available.size()));
		useObject(chosen, 1);
		return chosen;
	}


	/**
	 * Remove half of all items (rounded down). Used when seal passes through player.
	 *
	 * <p>Integer division does the rounding for us: a player with 3 snowballs
	 * keeps 1, with 5 they keep 2, etc. The fast/slow dice halves are computed
	 * separately and then summed back into the shared dice total so the
	 * counters stay consistent.</p>
	 */
	public void removeHalf() {
		snowballQuantity = snowballQuantity / 2;
		fishQuantity = fishQuantity / 2;

		// Halve each dice variant separately so each one keeps at least the
		// floored half rather than losing extra dice to combined rounding.
		int fastRemove = fastdiceQuantity / 2;
		int slowRemove = slowdiceQuantity / 2;
		fastdiceQuantity -= fastRemove;
		slowdiceQuantity -= slowRemove;
		diceQuantity = fastdiceQuantity + slowdiceQuantity;
	}


	/**
	 * Debug-friendly snapshot of the inventory state, mainly used by
	 * {@code System.out} traces and by IDE inspectors.
	 *
	 * @return a single-line description of all per-type counters
	 */
	@Override
	public String toString() {
		return "Inventory[snowballs=" + snowballQuantity +
			   ", fish=" + fishQuantity +
			   ", fastDice=" + fastdiceQuantity +
			   ", slowDice=" + slowdiceQuantity + "]";
	}
}
