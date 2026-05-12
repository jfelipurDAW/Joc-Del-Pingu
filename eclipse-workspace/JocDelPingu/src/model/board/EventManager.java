package model.board;

import java.util.Random;
import model.item.Inventory;
import model.item.ObjectType;
import model.entity.Player;

/**
 * Central resolver for EVENT squares.
 *
 * <p>When a player lands on a {@link model.board.squares.S_Event} tile, the
 * board controller delegates to {@link #triggerEvent(Player, Board)}, which
 * rolls a single weighted random outcome and applies it to the player and
 * inventory. The exact probabilities (slow dice 30%, snowballs 20%, fish
 * 15%, lose turn 12%, lose item 10%, fast dice 8%, snowmobile 5%) are
 * documented on {@link #triggerEvent} and intentionally live in one place
 * so the distribution stays consistent across the game.</p>
 *
 * <p>The class also exposes:</p>
 * <ul>
 *   <li>{@link EventType} - the catalogue of possible events along with
 *       human-readable messages used by the UI overlay.</li>
 *   <li>{@link EventResult} - a value object describing the outcome that
 *       the UI consumes to update the log and player position.</li>
 * </ul>
 *
 * <p>Events:</p>
 * <ul>
 *   <li>Get a fish</li>
 *   <li>Get 1-3 snowballs</li>
 *   <li>Get a fast die (low probability ~8%)</li>
 *   <li>Get a slow die (high probability ~30%)</li>
 *   <li>Lose a turn</li>
 *   <li>Lose a random item</li>
 *   <li>Snowmobile: advance to next sled</li>
 * </ul>
 */
public class EventManager {


	/////////////////////////////
	///       FIELDS         ///
	/////////////////////////////

	// Single shared RNG so the random stream is consistent across calls
	// (and so we don't allocate a new Random per event).
	private static final Random random = new Random();


	/////////////////////////////
	///    INNER CLASSES     ///
	/////////////////////////////

	/**
	 * Catalogue of every event the {@link EventManager} can roll, together
	 * with a localized message string ready for the UI to display.
	 */
	public enum EventType {
		GET_FISH("🐟 You found a fish!"),
		GET_SNOWBALLS("⛄ You found snowballs!"),
		GET_FAST_DICE("🎲✨ You found a FAST die! (5-10 squares)"),
		GET_SLOW_DICE("🎲 You found a slow die (1-3 squares)"),
		LOSE_TURN("❄️ You slipped on ice! Lose your next turn."),
		LOSE_ITEM("💨 A gust of wind blew away one of your items!"),
		SNOWMOBILE("🏂 You found a snowmobile! Advancing to next sled!");

		private final String message;

		EventType(String message) {
			this.message = message;
		}

		/**
		 * @return the headline message associated with this event type.
		 */
		public String getMessage() {
			return message;
		}
	}

	/**
	 * Result object returned by triggerEvent for the UI to display.
	 *
	 * <p>Bundles the rolled {@link EventType}, the canned headline
	 * message, an optional detail line (e.g. "You now have 3 fish"),
	 * and an optional new board position used when the event teleports
	 * the player (snowmobile). A {@code newPosition} of -1 means the
	 * player's position did not change as a result of the event.</p>
	 */
	public static class EventResult {


		/////////////////////////////
		///       FIELDS         ///
		/////////////////////////////

		private final EventType type;
		private final String message;
		private final String detail;

		// -1 if the event did not move the player, otherwise the new
		// square index the UI should animate the pawn to.
		private final int newPosition;


		/////////////////////////////
		///    CONSTRUCTORS      ///
		/////////////////////////////

		/**
		 * @param type        the event that was rolled.
		 * @param detail      optional second line for the UI overlay
		 *                    (e.g. inventory counts), may be {@code null}.
		 * @param newPosition new player position, or -1 if unchanged.
		 */
		public EventResult(EventType type, String detail, int newPosition) {
			this.type = type;
			this.message = type.getMessage();
			this.detail = detail;
			this.newPosition = newPosition;
		}


		/////////////////////////////
		/// GETTERS AND SETTERS  ///
		/////////////////////////////

		public EventType getType() { return type; }
		public String getMessage() { return message; }
		public String getDetail() { return detail; }
		public int getNewPosition() { return newPosition; }


		/////////////////////////////
		///      OVERRIDES       ///
		/////////////////////////////

		/**
		 * @return the headline message concatenated with the detail line
		 *         when one is present - useful for plain-text logging.
		 */
		@Override
		public String toString() {
			return message + (detail != null ? " " + detail : "");
		}
	}


	/////////////////////////////
	///      METHODS         ///
	/////////////////////////////

	/**
	 * Triggers a random event for the given player.
	 *
	 * @param player The player who landed on the event square
	 * @param board The game board (needed for snowmobile/sled lookup)
	 * @return EventResult describing what happened
	 *
	 * Spec compliance:
	 *   - SLOW_DICE  -> highest probability  (30%)
	 *   - FAST_DICE  -> low probability      (8%)
	 *   - SNOWMOBILE -> rare extra event     (5%)
	 * Total = 100%.
	 */
	public static EventResult triggerEvent(Player player, Board board) {

		// One roll in [0, 100) bucketed by cumulative probability.
		// Order matters: the comparisons below assume ascending buckets.
		int roll = random.nextInt(100);

		if (roll < 30) {                // 30% - most likely outcome
			return handleGetSlowDice(player);
		} else if (roll < 50) {         // 20%
			return handleGetSnowballs(player);
		} else if (roll < 65) {         // 15%
			return handleGetFish(player);
		} else if (roll < 77) {         // 12%
			return handleLoseTurn(player);
		} else if (roll < 87) {         // 10%
			return handleLoseItem(player);
		} else if (roll < 95) {         // 8%  - rare outcome
			return handleGetFastDice(player);
		} else {                        // 5%  - rarest outcome
			return handleSnowmobile(player, board);
		}
	}

	/**
	 * Awards one fish to the player, respecting the inventory cap.
	 */
	private static EventResult handleGetFish(Player player) {

		Inventory inv = player.getInventory();

		if (inv.addFish()) {
			return new EventResult(EventType.GET_FISH,
				"You now have " + inv.getFishQuantity() + " fish.", -1);
		} else {
			// Inventory was already full; event message reflects the miss.
			return new EventResult(EventType.GET_FISH,
				"But your fish pouch is full! (" + inv.getFishQuantity() + "/" + Inventory.MAX_FISH + ")", -1);
		}
	}

	/**
	 * Awards 1-3 snowballs (uniform). The {@code Inventory.addSnowballs}
	 * call clamps to the cap, and we report the actual {@code added}
	 * amount so the player knows if part of the pile was wasted.
	 */
	private static EventResult handleGetSnowballs(Player player) {

		int count = random.nextInt(3) + 1; // 1-3 snowballs

		Inventory inv = player.getInventory();
		int added = inv.addSnowballs(count);

		return new EventResult(EventType.GET_SNOWBALLS,
			"Found " + count + " snowballs! Added " + added + ". Total: " + inv.getSnowballQuantity(), -1);
	}

	/**
	 * Awards a fast die (5-10 movement) subject to the shared dice cap.
	 */
	private static EventResult handleGetFastDice(Player player) {

		Inventory inv = player.getInventory();

		if (inv.addDice(ObjectType.FASTDICE)) {
			return new EventResult(EventType.GET_FAST_DICE,
				"You now have " + inv.getFastdiceQuantity() + " fast dice.", -1);
		} else {
			return new EventResult(EventType.GET_FAST_DICE,
				"But your dice bag is full! (" + inv.getDiceQuantity() + "/" + Inventory.MAX_DICE + ")", -1);
		}
	}

	/**
	 * Awards a slow die (1-3 movement) subject to the shared dice cap.
	 */
	private static EventResult handleGetSlowDice(Player player) {

		Inventory inv = player.getInventory();

		if (inv.addDice(ObjectType.SLOWDICE)) {
			return new EventResult(EventType.GET_SLOW_DICE,
				"You now have " + inv.getSlowdiceQuantity() + " slow dice.", -1);
		} else {
			return new EventResult(EventType.GET_SLOW_DICE,
				"But your dice bag is full! (" + inv.getDiceQuantity() + "/" + Inventory.MAX_DICE + ")", -1);
		}
	}

	/**
	 * Flags the player to skip their next turn (slipped on ice flavor).
	 */
	private static EventResult handleLoseTurn(Player player) {
		player.setSkipNextTurn(true);
		return new EventResult(EventType.LOSE_TURN, null, -1);
	}

	/**
	 * Removes one random item from the player's inventory. If the
	 * inventory was empty the removal is a harmless no-op and the
	 * message reflects that.
	 */
	private static EventResult handleLoseItem(Player player) {

		ObjectType lost = player.getInventory().removeRandomItem();

		if (lost != null) {
			return new EventResult(EventType.LOSE_ITEM,
				"You lost a " + lost.name().toLowerCase() + "!", -1);
		} else {
			return new EventResult(EventType.LOSE_ITEM,
				"But you had nothing to lose!", -1);
		}
	}

	/**
	 * Rare boost: scans the sled chain for the first sled strictly ahead
	 * of the player and teleports them onto it. If no sled is ahead the
	 * event degrades into a flavor message with no movement.
	 */
	private static EventResult handleSnowmobile(Player player, Board board) {

		int currentPos = player.getSquareIndex();

		// Find the next sled after current position. The sled array is
		// kept sorted by Board, so the first match is also the closest.
		for (int nextSled : board.getSled_Array()) {
			if (nextSled > currentPos) {
				player.setSquare(nextSled);
				return new EventResult(EventType.SNOWMOBILE,
					"Zooming to sled at square " + nextSled + "!", nextSled);
			}
		}

		// No sled ahead, just a message
		return new EventResult(EventType.SNOWMOBILE,
			"But there are no more sleds ahead! Nothing happens.", -1);
	}
}
