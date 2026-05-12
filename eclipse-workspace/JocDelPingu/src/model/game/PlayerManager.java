package model.game;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;
import model.item.ObjectType;


/**
 * Encapsulates the rules that govern player movement and player-vs-player
 * combat (snowball wars and one-off snowball throws). It does not own any
 * state of its own — the {@link Player} instances passed in carry their
 * position and inventory.
 *
 * Collaborators:
 *  - {@link GameManager} calls {@link #movePlayer} every turn after the dice roll,
 *  - {@link BoardManager} then resolves what the destination square does,
 *  - the snowball methods are invoked when {@link TurnController#getPlayersAtSquare}
 *    detects a collision between two players.
 */
public class PlayerManager {


    /////////////////////////////
    ///       METHODS         ///
    /////////////////////////////

    /**
     * Advances a player by {@code steps} squares. If the move reaches or
     * exceeds the last square the player is clamped to the final position
     * and a WIN result is returned; the caller (GameManager) then ends the
     * game. Otherwise null is returned to signal "no win yet — let the
     * BoardManager resolve the square effect".
     *
     * @param player the player being moved
     * @param steps  the dice roll
     * @param board  the board (used for its size constant)
     * @return WIN {@link ActionResult} if this move wins the game, otherwise null
     */
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

    /**
     * Resolves a "snowball war" between two players who landed on the same
     * square. Both players spend every snowball in their inventory; whoever
     * threw more wins, and the loser is pushed back by the difference (clamped
     * to square 0). Ties just consume the ammo, and a war with no ammo at all
     * is reported as an empty stand-off.
     *
     * @param player1 first combatant (treated as the "attacker" in the result)
     * @param player2 second combatant
     * @return an {@link ActionResult} describing the outcome for the UI
     */
    public ActionResult snowballWar(Player player1, Player player2) {
        int balls1 = player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int balls2 = player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL);

        // Both players spend ALL of their snowballs regardless of who wins
        player1.getInventory().useObject(ObjectType.SNOWBALL, balls1);
        player2.getInventory().useObject(ObjectType.SNOWBALL, balls2);

        int difference = Math.abs(balls1 - balls2);

        if (balls1 == 0 && balls2 == 0) {
            // Neither side has ammo — purely a narrative event, no movement
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_EMPTY, player1.getName());
            res.setTargetName(player2.getName());
            return res;
        }

        if (balls1 > balls2) {
            // Push the loser back by the snowball difference, clamped at 0
            int newPos = Math.max(0, player2.getSquareIndex() - difference);
            player2.setSquare(newPos);
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_WIN, player1.getName());
            res.setTargetName(player2.getName());
            res.setValue(balls1);  // winner balls
            res.setValue2(difference); // squares lost
            return res;
        } else if (balls2 > balls1) {
            // Symmetric case — player2 wins
            int newPos = Math.max(0, player1.getSquareIndex() - difference);
            player1.setSquare(newPos);
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_WIN, player2.getName());
            res.setTargetName(player1.getName());
            res.setValue(balls2);  // winner balls
            res.setValue2(difference); // squares lost
            return res;
        } else {
            // Equal counts — tie, no one moves
            ActionResult res = new ActionResult(ActionResult.ActionType.SNOWBALL_WAR_TIE, player1.getName());
            res.setTargetName(player2.getName());
            res.setValue(balls1);
            return res;
        }
    }

    /**
     * Resolves a single thrown snowball: the attacker spends one snowball
     * and the target is pushed back a random 1-3 squares (clamped at 0).
     *
     * @param attacker player throwing the snowball
     * @param target   player being hit
     * @return an {@link ActionResult} describing how far the target was pushed
     */
    public ActionResult throwSnowball(Player attacker, Player target) {
        attacker.getInventory().useObject(ObjectType.SNOWBALL, 1);

        // Random penalty: 1, 2 or 3 squares backwards
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

    /**
     * Thin delegate so callers don't need to touch the {@link Seal} type
     * directly. Forwards the encounter to the seal's own interaction logic.
     *
     * @param seal   the seal entity
     * @param player the player encountering the seal
     * @return whatever {@link Seal#interact(Player)} returns
     */
    public ActionResult handleSealInteraction(Seal seal, Player player) {
        return seal.interact(player);
    }
}
