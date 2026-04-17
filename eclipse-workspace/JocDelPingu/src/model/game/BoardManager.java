package model.game;

import model.board.Board;
import model.board.SquareType;
import model.entity.Player;
import model.item.ObjectType;
import model.board.EventManager;

public class BoardManager {
    
    public String executeSquareAction(Player player, Board board) {
        if (board == null) return "Error: board not set";
        
        int newPosition = player.getSquareIndex();
        SquareType currentSquare = board.getSquareType(newPosition);
        
        switch(currentSquare) {
            case ICE_HOLE: return handleIceHole(player, board);
            case SLED: return handleSled(player, board);
            case BEAR: return handleBear(player, board);
            case EVENT: return handleEvent(player, board);
            case BROKEN_FLOOR: return handleBrokenFloor(player, board);
            case START: return player.getName() + " is at the start.";
            case END: return player.getName() + " reached the END! 🎉";
            case NORMAL:
            default: return player.getName() + " landed on a normal square.";
        }
    }

    private String handleIceHole(Player player, Board board) {
        int destination = board.getDestination(player.getSquareIndex());
        player.setSquare(destination);
        return "🕳️ " + player.getName() + " fell into an ice hole! Sent back to square " + destination;
    }

    private String handleSled(Player player, Board board) {
        int destination = board.getDestination(player.getSquareIndex());
        if (destination != player.getSquareIndex()) {
            player.setSquare(destination);
            return "🛷 " + player.getName() + " found a sled! Zooming forward to square " + destination;
        } else {
            return "🛷 " + player.getName() + " found the last sled. Nothing happens.";
        }
    }

    private String handleBear(Player player, Board board) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            player.getInventory().useObject(ObjectType.FISH, 1);
            return "🐻 " + player.getName() + " bribed the bear with a fish! 🐟 Safe!";
        } else {
            player.setSquare(0);
            return "🐻💥 " + player.getName() + " was attacked by the bear! No fish to bribe! Back to START!";
        }
    }

    private String handleEvent(Player player, Board board) {
        player.setLastEvent(EventManager.triggerEvent(player, board));
        if (player.getLastEvent().getNewPosition() >= 0) {
            player.setNumSquare(player.getLastEvent().getNewPosition());
        }
        return "❓ " + player.getLastEvent().toString();
    }

    private String handleBrokenFloor(Player player, Board board) {
        int totalItems = player.getInventory().getTotalItemCount();
        if (totalItems > 5) {
            player.setSquare(0);
            return "💔 " + player.getName() + " was too heavy (" + totalItems + " items)! Fell through the broken floor! Back to START!";
        } else if (totalItems > 0) {
            player.setSkipNextTurn(true);
            return "⚠️ " + player.getName() + " cracked the broken floor (" + totalItems + " items). Loses next turn!";
        } else {
            return "✅ " + player.getName() + " crosses the broken floor safely (no items)!";
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
