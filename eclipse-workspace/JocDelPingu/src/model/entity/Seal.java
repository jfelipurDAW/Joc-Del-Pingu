package model.entity;

import java.util.List;
import java.util.Random;

import model.board.Board;
import model.game.ActionResult;
import model.item.ObjectType;

/**
 * CPU-controlled Seal entity.
 * Its goal is to win the game and hinder players.
 * - If it passes through a player's square: player loses half inventory
 * - If it lands on same square as player: tail hit → player goes to previous ice hole
 * - Players can feed it a fish to block it for 2 turns
 */
public class Seal extends Entity {

    private boolean isBribed;
    private int blockedTurns;
    private String name;
    private Random random;
    
    public Seal() {
        this.name = "Seal";
        this.setType(EntityType.SEAL);
        this.setNumSquare(0);
        this.setSkipNextTurn(false);
        this.isBribed = false;
        this.blockedTurns = 0;
        this.random = new Random();
    }
    
    @Override
    public String getName() {
        return "🦭 Seal";
    }
    
    public boolean hasBeenBribed() {
        return isBribed;
    }
    
    public boolean isBlocked() {
    	return blockedTurns > 0;
    }
    
    public int getBlockedTurns() {
    	return blockedTurns;
    }
    
    public void setBlockedTurns(int blockedTurns) {
    	this.blockedTurns = blockedTurns;
    }
    
    /**
     * Feed the seal a fish. Blocks it for 2 turns.
     */
    public ActionResult bribeSeal(Player player) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            player.getInventory().useObject(ObjectType.FISH, 1);
            this.isBribed = true;
            this.blockedTurns = 2;
            return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_BRIBED, player.getName());
        } else {
            return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_NO_FISH, player.getName());
        }
    }
    
    /**
     * Seal hits a player and sends them to the previous ice hole.
     */
    public model.game.ActionResult hitPlayer(Player player) {
        if (this.getBoard() != null) {
            int previousHole = findPreviousIceHole(player.getSquareIndex());
            player.setSquare(previousHole);
            model.game.ActionResult res = new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_HIT_HOLE, player.getName());
            res.setValue(previousHole);
            return res;
        }
        player.setSquare(0);
        return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_HIT_START, player.getName());
    }
    
    /**
     * When seal passes through a player's square, they lose half inventory.
     */
    public model.game.ActionResult passThrough(Player player) {
    	player.loseHalfInventory();
    	return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_PASS, player.getName());
    }
    
    /**
     * Seal interaction when a player lands on the seal's square.
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
     */
    public model.game.ActionResult updateSealTurns() {
        if (this.blockedTurns > 0) {
            this.blockedTurns--;
            
            if (this.blockedTurns == 0) {
                this.isBribed = false;
                return new model.game.ActionResult(model.game.ActionResult.ActionType.SEAL_ACTIVE, this.getName());
            }
        }
        return null;
    }
    
    /**
     * CPU AI: Seal plays its turn.
     * Rolls a dice, moves, and affects players.
     * Returns a list of log messages describing what happened.
     */
    public List<model.game.ActionResult> playTurn(List<Player> allPlayers) {
    	List<model.game.ActionResult> log = new java.util.ArrayList<>();
    	
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
    	
    	// Check all squares between old and new position for players to affect
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
    
    /**
     * Find the previous ice hole before a given position.
     */
    private int findPreviousIceHole(int position) {
    	if (this.getBoard() != null) {
    		List<Integer> holes = this.getBoard().getIceHole_Array();
    		int previousHole = 0;
    		for (int hole : holes) {
    			if (hole < position) {
    				previousHole = hole;
    			} else {
    				break;
    			}
    		}
    		return previousHole;
    	}
    	return 0;
    }
}