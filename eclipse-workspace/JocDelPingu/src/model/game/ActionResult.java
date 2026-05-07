package model.game;

public class ActionResult {
    public enum ActionType {
        ICE_HOLE, SLED_FOUND, SLED_LAST, BEAR_SAFE, BEAR_ATTACK, EVENT, 
        BROKEN_FLOOR_FALL, BROKEN_FLOOR_CRACK, BROKEN_FLOOR_SAFE, BROKEN_FLOOR_LOSE_ITEM,
        NORMAL_SQUARE, START_SQUARE, END_SQUARE, WIN,
        SEAL_BRIBED, SEAL_NO_FISH, SEAL_HIT_HOLE, SEAL_HIT_START, SEAL_PASS, SEAL_EATING, SEAL_ACTIVE, SEAL_ROLL, SEAL_MOVE,
        SNOWBALL_WAR_WIN, SNOWBALL_WAR_TIE, SNOWBALL_WAR_EMPTY, SNOWBALL_THROW
    }

    private ActionType actionType;
    private String playerName;
    private String targetName; // For interactions (e.g. snowball war loser)
    private int value;         // General number (square index, dice roll, turns, amount, etc.)
    private int value2;        // Secondary number (e.g. difference in snowballs)
    private String eventMessage; // Custom non-localized string if it comes from an event

    public ActionResult(ActionType type) {
        this.actionType = type;
    }

    public ActionResult(ActionType type, String playerName) {
        this.actionType = type;
        this.playerName = playerName;
    }

    public ActionType getType() { return actionType; }
    public void setType(ActionType type) { this.actionType = type; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public int getValue2() { return value2; }
    public void setValue2(int value2) { this.value2 = value2; }

    public String getEventMessage() { return eventMessage; }
    public void setEventMessage(String eventMessage) { this.eventMessage = eventMessage; }
}
