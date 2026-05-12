package model.entity;

import java.util.List;
import java.util.Random;

import model.board.Board;
import model.game.ActionResult;
import model.item.ObjectType;


/**
 * CPU-controlled Seal entity.
 * Its goal is to win the game and hinder players.
 * <ul>
 *   <li>If it passes through a player's square: player loses half inventory</li>
 *   <li>If it lands on same square as player: tail hit, player goes to previous ice hole</li>
 *   <li>Players can feed it a fish to block it for 2 turns</li>
 * </ul>
 *
 * <p>The seal is rolled and moved by {@link #playTurn(List)} at the end of
 * each round; the method returns a list of {@link ActionResult} entries so
 * the controller can render every consequence (roll, move, pass-through,
 * landed-on-player) in order.</p>
 */
public class Seal extends Entity {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // Remaining turns during which the seal is "eating" and cannot move.
    // Set to 2 when a player successfully bribes it with a fish.
    private int blockedTurns;

    // Reusable RNG instance for the seal's dice rolls.
    private Random random;


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Builds a seal that starts at square 0, unblocked and ready to move.
     */
    public Seal() {
        this.setType(EntityType.SEAL);
        this.setNumSquare(0);
        this.setSkipNextTurn(false);
        this.blockedTurns = 0;
        this.random = new Random();
    }


    /////////////////////////////
    ///       METHODS         ///
    /////////////////////////////

    /**
     * @return the seal's display label (a seal emoji prefixes the word so it
     *         stands out in the in-game log)
     */
    @Override
    public String getName() {
        return "🦭 Seal";
    }


    /**
     * @return {@code true} when the seal is currently busy eating and skipping turns
     */
    public boolean isBlocked() {
    	return blockedTurns > 0;
    }


    /////////////////////////////
    /// GETTERS AND SETTERS   ///
    /////////////////////////////

    /**
     * @return how many turns the seal will remain blocked
     */
    public int getBlockedTurns() {
    	return blockedTurns;
    }


    /**
     * @param blockedTurns explicit override for the blocked-turns counter
     *                     (used when restoring a saved game)
     */
    public void setBlockedTurns(int blockedTurns) {
    	this.blockedTurns = blockedTurns;
    }


    /////////////////////////////
    ///   GAMEPLAY METHODS    ///
    /////////////////////////////

    /**
     * Feed the seal a fish. Blocks it for 2 turns.
     *
     * @param player the player paying the bribe
     * @return a {@link ActionResult} describing whether the bribe succeeded
     *         or the player had no fish to offer
     */
    public ActionResult bribeSeal(Player player) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            player.getInventory().useObject(ObjectType.FISH, 1);

            // 2 turns is enough for the player to outrun the seal in most cases
            // while still keeping the bribe a finite/strategic resource.
            this.blockedTurns = 2;
            return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_BRIBED, player.getName());
        } else {
            return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_NO_FISH, player.getName());
        }
    }


    /**
     * Seal hits a player and sends them to the previous ice hole.
     *
     * @param player the unlucky player
     * @return a {@link model.game.ActionResult} describing the resulting teleport
     */
    public model.game.ActionResult hitPlayer(Player player) {
        if (this.getBoard() != null) {
            int previousHole = findPreviousIceHole(player.getSquareIndex());
            player.setSquare(previousHole);
            model.game.ActionResult res = new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_HIT_HOLE, player.getName());
            res.setValue(previousHole);
            return res;
        }

        // Fallback when the board reference is missing (defensive — shouldn't
        // happen in normal play): send the player back to square 0.
        player.setSquare(0);
        return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_HIT_START, player.getName());
    }


    /**
     * When seal passes through a player's square, they lose half inventory.
     *
     * @param player the player whose inventory is being raided
     * @return a {@link model.game.ActionResult} of type {@code SEAL_PASS}
     */
    public model.game.ActionResult passThrough(Player player) {
    	player.loseHalfInventory();
    	return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_PASS, player.getName());
    }


    /**
     * Seal interaction when a player lands on the seal's square.
     *
     * <p>Branches in priority order: a blocked seal does nothing, a player
     * holding a fish can bribe it, otherwise the seal hits the player.</p>
     *
     * @param player the player landing on the seal
     * @return the resulting {@link model.game.ActionResult}
     */
    public model.game.ActionResult interact(Player player) {
        // If seal is eating (blocked), player is safe
        if (this.blockedTurns > 0) {
            return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_EATING, player.getName());
        }

        // Check if player has a fish to bribe
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            return bribeSeal(player);
        } else {
            return hitPlayer(player);
        }
    }


    /**
     * Called at the end of each game turn to update seal's blocked state.
     *
     * @return a {@link model.game.ActionResult} when the seal just woke up,
     *         or {@code null} when there is nothing to report this turn
     */
    public model.game.ActionResult updateSealTurns() {
        if (this.blockedTurns > 0) {
            this.blockedTurns--;

            // Emit a SEAL_ACTIVE event the moment the counter hits zero so the
            // UI can warn players that the predator is hunting again.
            if (this.blockedTurns == 0) {
                return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_ACTIVE, this.getName());
            }
        }
        return null;
    }


    /**
     * CPU AI: Seal plays its turn.
     * Rolls a dice, moves, and affects players.
     *
     * @param allPlayers every player currently on the board, used to detect
     *                   pass-through and landed-on collisions
     * @return an ordered list of log messages describing what happened during
     *         the seal's turn
     */
    public List<model.game.ActionResult> playTurn(List<Player> allPlayers) {
    	List<model.game.ActionResult> log = new java.util.ArrayList<>();

    	// While blocked, the seal just decrements its counter and reports it.
    	if (isBlocked()) {
    		model.game.ActionResult res = new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_EATING, this.getName());
    		res.setValue(blockedTurns);
    		log.add(res);
    		model.game.ActionResult updateRes = updateSealTurns();
    		if (updateRes != null) log.add(updateRes);
    		return log;
    	}

    	// Roll dice (1-6)
    	int roll = random.nextInt(6) + 1;
    	model.game.ActionResult rollRes = new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_ROLL, this.getName());
    	rollRes.setValue(roll);
    	log.add(rollRes);

    	int oldPos = this.getNumSquare();
    	int newPos = Math.min(oldPos + roll, Board.MAX_SQUARES - 1);

    	// Check all squares between old and new position for players to affect.
    	// We deliberately exclude the destination square here — that case is
    	// handled below as a "landed on player" interaction.
    	for (int sq = oldPos + 1; sq < newPos; sq++) {
    		for (Player p : allPlayers) {
    			if (p.getSquareIndex() == sq) {
    				log.add(passThrough(p));
    			}
    		}
    	}

    	this.setNumSquare(newPos);
    	model.game.ActionResult moveRes = new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_MOVE, this.getName());
    	moveRes.setValue(newPos);
    	log.add(moveRes);

    	// Check if seal landed on same square as any player
    	for (Player p : allPlayers) {
    		if (p.getSquareIndex() == newPos) {
    			log.add(hitPlayer(p));
    		}
    	}

    	return log;
    }


    /////////////////////////////
    ///   HELPER METHODS      ///
    /////////////////////////////

    /**
     * Find the previous ice hole before a given position. Used to teleport
     * a player backwards after a tail hit.
     *
     * @param position the player's current square
     * @return the index of the closest ice hole strictly before {@code position},
     *         or {@code 0} if there is no earlier hole / no board attached
     */
    private int findPreviousIceHole(int position) {
    	if (this.getBoard() != null) {
    		List<Integer> holes = this.getBoard().getIceHole_Array();
    		int previousHole = 0;
    		// IceHole_Array is kept sorted, so iterating without an early
    		// break is equivalent — later holes do not update previousHole.
    		for (int hole : holes) {
    			if (hole < position) {
    				previousHole = hole;
    			}
    		}
    		return previousHole;
    	}
    	return 0;
    }
}
