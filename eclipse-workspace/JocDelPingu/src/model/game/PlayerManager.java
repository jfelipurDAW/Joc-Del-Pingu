package model.game;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;
import model.item.ObjectType;

public class PlayerManager {

    public ActionResult movePlayer(Player player, int steps, Board board) {
        int newPos = player.getSquareIndex() + steps;
        if (newPos >= Board.MAX_SQUARES - 1) {
            player.setNumSquare(Board.MAX_SQUARES - 1);
            return new ActionResult(ActionResult.ActionType.WIN, player.getName());
        } else {
            player.setNumSquare(newPos);
            return null; // Square logic logic is delegated to the BoardManager
        }
    }

    public ActionResult snowballWar(Player player1, Player player2) {
        int balls1 = player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int balls2 = player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        
        player1.getInventory().useObject(ObjectType.SNOWBALL, balls1);
        player2.getInventory().useObject(ObjectType.SNOWBALL, balls2);
        
        int difference = Math.abs(balls1 - balls2);
        
        if (balls1 == 0 && balls2 == 0) {
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_EMPTY, player1.getName());
            res.setTargetName(player2.getName());
            return res;
        }

        if (balls1 > balls2) {
            int newPos = Math.max(0, player2.getSquareIndex() - difference);
            player2.setSquare(newPos);
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_WIN, player1.getName());
            res.setTargetName(player2.getName());
            res.setValue(balls1);  // winner balls
            res.setValue2(difference); // squares lost
            return res;
        } else if (balls2 > balls1) {
            int newPos = Math.max(0, player1.getSquareIndex() - difference);
            player1.setSquare(newPos);
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_WIN, player2.getName());
            res.setTargetName(player1.getName());
            res.setValue(balls2);  // winner balls
            res.setValue2(difference); // squares lost
            return res;
        } else {
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_TIE, player1.getName());
            res.setTargetName(player2.getName());
            res.setValue(balls1);
            return res;
        }
    }

    public ActionResult throwSnowball(Player attacker, Player target) {
        attacker.getInventory().useObject(ObjectType.SNOWBALL, 1);
        int backSteps = (int)(Math.random() * 3) + 1;
        int oldPos = target.getSquareIndex();
        int newPos = Math.max(0, oldPos - backSteps);
        target.setSquare(newPos);
        
        ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_THROW, attacker.getName());
        res.setTargetName(target.getName());
        res.setValue(backSteps);
        res.setValue2(newPos);
        return res;
    }

    public ActionResult handleSealInteraction(Seal seal, Player player) {
        return seal.interact(player);
    }
}
