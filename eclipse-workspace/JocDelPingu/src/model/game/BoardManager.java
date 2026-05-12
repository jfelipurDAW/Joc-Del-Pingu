package model.game;

import model.board.Board;
import model.board.SquareType;
import model.entity.Player;
import model.item.ObjectType;
import model.board.EventManager;


/**
 * Resolves what happens after a player lands on a square. Acts as the bridge
 * between the {@link Board} (which only knows the type of each square) and
 * the rest of the game: it inspects the square type and runs the matching
 * behavior — sliding into an ice hole, finding a sled, fighting/feeding the
 * bear, triggering a random event or risking a broken-floor accident.
 *
 * Every branch produces an {@link ActionResult} so the UI layer
 * ({@code GameBoardController}) can animate the consequence consistently.
 * Holds no state — a single instance per match is fine.
 */
public class BoardManager {


    /////////////////////////////
    ///       METHODS         ///
    /////////////////////////////

    /**
     * Entry point invoked by {@link GameManager} after a player moves.
     * Dispatches on the square type at the player's current position and
     * delegates to the appropriate private handler.
     *
     * @param player the player who just landed
     * @param board  the board the player is on (may be null during error
     *               recovery — handled defensively)
     * @return a populated {@link ActionResult} describing the outcome
     */
    public ActionResult executeSquareAction(Player player, Board board) {
        if (board == null) {
            // Defensive: should never happen at runtime, but a stale UI
            // reference could otherwise NPE here. Return a neutral result.
            ActionResult err = new ActionResult(ActionResult.ActionType.NORMAL_SQUARE, player.getName());
            err.setEventMessage("Error: board not set");
            return err;
        }

        int newPosition = player.getSquareIndex();
        SquareType currentSquare = board.getSquareType(newPosition);

        switch(currentSquare) {
            case ICE_HOLE: return handleIceHole(player, board);
            case SLED: return handleSled(player, board);
            case BEAR: return handleBear(player, board);
            case EVENT: return handleEvent(player, board);
            case BROKEN_FLOOR: return handleBrokenFloor(player, board);
            case START: return new ActionResult(ActionResult.ActionType.START_SQUARE, player.getName());
            case END: return new ActionResult(ActionResult.ActionType.END_SQUARE, player.getName());
            case NORMAL:
            default: return new ActionResult(ActionResult.ActionType.NORMAL_SQUARE, player.getName());
        }
    }


    /////////////////////////////
    ///   SQUARE HANDLERS     ///
    /////////////////////////////

    /**
     * Ice hole: the player slides to the destination linked to this hole
     * (defined on the Board). Always sends the player away from their
     * current square.
     */
    private ActionResult handleIceHole(Player player, Board board) {
        int destination = board.getDestination(player.getSquareIndex());
        player.setSquare(destination);

        ActionResult res = new ActionResult(ActionResult.ActionType.ICE_HOLE, player.getName());
        res.setValue(destination);
        return res;
    }

    /**
     * Sled: usually moves the player forward to a faraway destination. If
     * the board reports the same square as the destination it means this is
     * the last available sled — the player stays put and the UI shows a
     * "no sleds left" message.
     */
    private ActionResult handleSled(Player player, Board board) {
        int destination = board.getDestination(player.getSquareIndex());
        if (destination != player.getSquareIndex()) {
            player.setSquare(destination);
            ActionResult res = new ActionResult(ActionResult.ActionType.SLED_FOUND, player.getName());
            res.setValue(destination);
            return res;
        } else {
            return new ActionResult(ActionResult.ActionType.SLED_LAST, player.getName());
        }
    }

    /**
     * Bear: if the player carries fish they bribe the bear (consume 1 fish
     * and continue safely). Otherwise the bear sends them back to start.
     */
    private ActionResult handleBear(Player player, Board board) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            // Bribe the bear with one fish and stay put
            player.getInventory().useObject(ObjectType.FISH, 1);
            return new ActionResult(ActionResult.ActionType.BEAR_SAFE, player.getName());
        } else {
            // No fish — the bear chases the player back to square 0
            player.setSquare(0);
            return new ActionResult(ActionResult.ActionType.BEAR_ATTACK, player.getName());
        }
    }

    /**
     * Event square: delegates to {@link EventManager} for a random scripted
     * event, stores it on the player (for history / replay) and applies any
     * position change requested by the event.
     */
    private ActionResult handleEvent(Player player, Board board) {
        player.setLastEvent(EventManager.triggerEvent(player, board));

        // Event may push the player to a new square; -1 means "no move"
        if (player.getLastEvent().getNewPosition() >= 0) {
            player.setNumSquare(player.getLastEvent().getNewPosition());
        }

        ActionResult res = new ActionResult(ActionResult.ActionType.EVENT, player.getName());
        res.setEventMessage(player.getLastEvent().toString());
        return res;
    }

    /**
     * Broken floor: severity depends on how much the player is carrying.
     *  - >5 items: too heavy → fall through, sent to start, and the square
     *    is permanently converted into an ice hole.
     *  - 1..5 items: 50/50 between "lose a turn" or "lose a random item".
     *  - 0 items: light enough to cross safely.
     */
    private ActionResult handleBrokenFloor(Player player, Board board) {
        int totalItems = player.getInventory().getTotalItemCount();
        if (totalItems > 5) {
            int brokenSquare = player.getSquareIndex();
            player.setSquare(0);

            // Permanently mutate the board: a broken floor that collapsed
            // becomes a regular ice hole for the rest of the match.
            board.convertBrokenFloorToIceHole(brokenSquare);

            ActionResult res = new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_FALL, player.getName());
            res.setValue(totalItems);
            return res;
        } else if (totalItems > 0) {
            // Spec: additional events on broken floor → lose a turn OR lose a random object
            // 50/50 split keeps the original behaviour viable while covering both outcomes.
            if (Math.random() < 0.5) {
                model.item.ObjectType lost = player.getInventory().removeRandomItem();
                ActionResult res = new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_LOSE_ITEM, player.getName());
                res.setValue(totalItems);
                res.setEventMessage(lost != null ? lost.name() : null);
                return res;
            }

            // Otherwise the crack just makes them lose their next turn
            player.setSkipNextTurn(true);
            ActionResult res = new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_CRACK, player.getName());
            res.setValue(totalItems);
            return res;
        } else {
            // Empty-handed: light enough to cross safely
            return new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_SAFE, player.getName());
        }
    }


    /////////////////////////////
    ///   TURN VALIDATION     ///
    /////////////////////////////

    /**
     * Checks (and consumes) the skip-turn flag on a player. Returns false
     * when the player must lose this turn — the flag is cleared so they
     * play normally on the next rotation.
     *
     * @param player player about to take a turn
     * @return true if the player can act this turn, false if they skip
     */
    public boolean validateTurn(Player player) {
        if (player.shouldSkipNextTurn()) {
            player.setSkipNextTurn(false);
            return false;
        }
        return true;
    }
}
