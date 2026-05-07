package model.item;

import java.util.ArrayList;
import java.util.Random;

public class Inventory {

	private int snowballQuantity;
	private int fishQuantity;
	private int diceQuantity;

	private int fastdiceQuantity;
	private int slowdiceQuantity;

	public static final int MAX_SNOWBALLS = 6;
	public static final int MAX_FISH = 2;
	public static final int MAX_DICE = 3;

	public Inventory(int entityId) {
		this.snowballQuantity = 0;
		this.fishQuantity = 0;
		this.diceQuantity = 0;
		this.fastdiceQuantity = 0;
		this.slowdiceQuantity = 0;
	}

	//////////////////////////////
	///   GETTERS I SETTERS    ///
	//////////////////////////////

	public int getSnowballQuantity() {
		return snowballQuantity;
	}

	public void setSnowballQuantity(int snowballQuantity) {
		this.snowballQuantity = snowballQuantity;
	}

	public int getFishQuantity() {
		return fishQuantity;
	}

	public void setFishQuantity(int fishQuantity) {
		this.fishQuantity = fishQuantity;
	}

	public int getDiceQuantity() {
		return diceQuantity;
	}

	public void setDiceQuantity(int diceQuantity) {
		this.diceQuantity = diceQuantity;
	}

	public int getFastdiceQuantity() {
		return fastdiceQuantity;
	}

	public void setFastdiceQuantity(int fastdiceQuantity) {
		this.fastdiceQuantity = fastdiceQuantity;
	}

	public int getSlowdiceQuantity() {
		return slowdiceQuantity;
	}

	public void setSlowdiceQuantity(int slowdiceQuantity) {
		this.slowdiceQuantity = slowdiceQuantity;
	}


	///////////////////
	///   METHODS   ///
	///////////////////

	/**
	 * Returns the total number of items in the inventory.
	 */
	public int getTotalItemCount() {
		return snowballQuantity + fishQuantity + fastdiceQuantity + slowdiceQuantity;
	}

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
				fastdiceQuantity -= quantity;
				diceQuantity -= quantity;
				return true;
			}
			break;

		case SLOWDICE:
			if (slowdiceQuantity >= quantity) {
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
	 * Convenience method: add snowballs (capped at MAX)
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
	 * Convenience method: add fish (capped at MAX)
	 */
	public boolean addFish() {
		if (fishQuantity < MAX_FISH) {
			fishQuantity++;
			return true;
		}
		return false;
	}

	/**
	 * Convenience method: add a dice of given type (capped at MAX total dice)
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
	 */
	public ObjectType removeRandomItem() {
		if (getTotalItemCount() == 0) return null;

		Random rand = new Random();
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
	 */
	public void removeHalf() {
		snowballQuantity = snowballQuantity / 2;
		fishQuantity = fishQuantity / 2;
		int fastRemove = fastdiceQuantity / 2;
		int slowRemove = slowdiceQuantity / 2;
		fastdiceQuantity -= fastRemove;
		slowdiceQuantity -= slowRemove;
		diceQuantity = fastdiceQuantity + slowdiceQuantity;
	}

	@Override
	public String toString() {
		return "Inventory[snowballs=" + snowballQuantity +
			   ", fish=" + fishQuantity +
			   ", fastDice=" + fastdiceQuantity +
			   ", slowDice=" + slowdiceQuantity + "]";
	}
}
