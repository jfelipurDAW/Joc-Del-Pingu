package model.game;

import model.board.Board;
import model.board.SquareType;
import model.entity.Player;
import model.item.ObjectType;
import model.board.EventManager;

public class BoardManager {
    
    public ActionResult executeSquareAction(Player player, Board board) {
        if (board == null) {
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

    private ActionResult handleIceHole(Player player, Board board) {
        int destination = board.getDestination(player.getSquareIndex());
        player.setSquare(destination);
        ActionResult res = new ActionResult(ActionResult.ActionType.ICE_HOLE, player.getName());
        res.setValue(destination);
        return res;
    }

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

    private ActionResult handleBear(Player player, Board board) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            player.getInventory().useObject(ObjectType.FISH, 1);
            return new ActionResult(ActionResult.ActionType.BEAR_SAFE, player.getName());
        } else {
            player.setSquare(0);
            return new ActionResult(ActionResult.ActionType.BEAR_ATTACK, player.getName());
        }
    }

    private ActionResult handleEvent(Player player, Board board) {
        player.setLastEvent(EventManager.triggerEvent(player, board));
        if (player.getLastEvent().getNewPosition() >= 0) {
            player.setNumSquare(player.getLastEvent().getNewPosition());
        }
        ActionResult res = new ActionResult(ActionResult.ActionType.EVENT, player.getName());
        res.setEventMessage(player.getLastEvent().toString());
        return res;
    }

    private ActionResult handleBrokenFloor(Player player, Board board) {
        int totalItems = player.getInventory().getTotalItemCount();
        if (totalItems > 5) {
            int brokenSquare = player.getSquareIndex();
            player.setSquare(0);
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
            player.setSkipNextTurn(true);
            ActionResult res = new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_CRACK, player.getName());
            res.setValue(totalItems);
            return res;
        } else {
            return new ActionResult(ActionResult.ActionType.BROKEN_FLOOR_SAFE, player.getName());
        }
    }

    public boolean validateTurn(Player player) {
        if (player.shouldSkipNextTurn()) {
            player.setSkipNextTurn(false);
            return false;
        }
        return true;
    }
}
