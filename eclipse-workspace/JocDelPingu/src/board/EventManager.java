package board;

import java.util.Random;
import ObjectManagers.Inventory;
import ObjectManagers.ObjectType;
import entity.Player;

/**
 * Manages random events when a player lands on an EVENT square.
 * Events:
 *   - Get a fish
 *   - Get 1-3 snowballs
 *   - Get a fast die (low probability ~15%)
 *   - Get a slow die (~25%)
 *   - Lose a turn
 *   - Lose a random item
 *   - Snowmobile: advance to next sled
 */
public class EventManager {
    
    private static final Random random = new Random();
    
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
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Result object returned by triggerEvent for the UI to display.
     */
    public static class EventResult {
        private final EventType type;
        private final String message;
        private final String detail;
        private final int newPosition; // -1 if position unchanged
        
        public EventResult(EventType type, String detail, int newPosition) {
            this.type = type;
            this.message = type.getMessage();
            this.detail = detail;
            this.newPosition = newPosition;
        }
        
        public EventType getType() { return type; }
        public String getMessage() { return message; }
        public String getDetail() { return detail; }
        public int getNewPosition() { return newPosition; }
        
        @Override
        public String toString() {
            return message + (detail != null ? " " + detail : "");
        }
    }
    
    /**
     * Triggers a random event for the given player.
     * @param player The player who landed on the event square
     * @param board The game board (needed for snowmobile/sled lookup)
     * @return EventResult describing what happened
     */
    public static EventResult triggerEvent(Player player, Board board) {
        // Weighted random selection:
        // Fish: 20%, Snowballs: 25%, FastDice: 10%, SlowDice: 20%, 
        // LoseTurn: 10%, LoseItem: 10%, Snowmobile: 5%
        int roll = random.nextInt(100);
        
        if (roll < 20) {
            return handleGetFish(player);
        } else if (roll < 45) {
            return handleGetSnowballs(player);
        } else if (roll < 55) {
            return handleGetFastDice(player);
        } else if (roll < 75) {
            return handleGetSlowDice(player);
        } else if (roll < 85) {
            return handleLoseTurn(player);
        } else if (roll < 95) {
            return handleLoseItem(player);
        } else {
            return handleSnowmobile(player, board);
        }
    }
    
    private static EventResult handleGetFish(Player player) {
        Inventory inv = player.getInventory();
        if (inv.addFish()) {
            return new EventResult(EventType.GET_FISH, 
                "You now have " + inv.getFishQuantity() + " fish.", -1);
        } else {
            return new EventResult(EventType.GET_FISH, 
                "But your fish pouch is full! (" + inv.getFishQuantity() + "/" + Inventory.MAX_FISH + ")", -1);
        }
    }
    
    private static EventResult handleGetSnowballs(Player player) {
        int count = random.nextInt(3) + 1; // 1-3 snowballs
        Inventory inv = player.getInventory();
        int added = inv.addSnowballs(count);
        return new EventResult(EventType.GET_SNOWBALLS, 
            "Found " + count + " snowballs! Added " + added + ". Total: " + inv.getSnowballQuantity(), -1);
    }
    
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
    
    private static EventResult handleLoseTurn(Player player) {
        player.setSkipNextTurn(true);
        return new EventResult(EventType.LOSE_TURN, null, -1);
    }
    
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
    
    private static EventResult handleSnowmobile(Player player, Board board) {
        int currentPos = player.getSquareIndex();
        // Find the next sled after current position
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
