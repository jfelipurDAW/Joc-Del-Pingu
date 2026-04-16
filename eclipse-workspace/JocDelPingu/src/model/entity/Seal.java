package model.entity;

import java.util.List;
import java.util.Random;

import model.board.Board;
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
        this.type = EntityType.SEAL;
        this.numSquare = 0;
        this.skipNextTurn = false;
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
    public String bribeSeal(Player player) {
        if (player.getInventory().getObjectQuantity(ObjectType.FISH) > 0) {
            player.getInventory().useObject(ObjectType.FISH, 1);
            this.isBribed = true;
            this.blockedTurns = 2;
            return "🐟 " + player.getName() + " fed the seal a fish! It's blocked for 2 turns!";
        } else {
            return "❌ " + player.getName() + " has no fish to feed the seal!";
        }
    }
    
    /**
     * Seal hits a player and sends them to the previous ice hole.
     */
    public String hitPlayer(Player player) {
        if (board != null) {
            int previousHole = findPreviousIceHole(player.getSquareIndex());
            player.setSquare(previousHole);
            return "🦭💥 The seal hits " + player.getName() + " with its tail! Sent to ice hole at square " + previousHole + "!";
        }
        player.setSquare(0);
        return "🦭💥 The seal hits " + player.getName() + "! Sent back to start!";
    }
    
    /**
     * When seal passes through a player's square, they lose half inventory.
     */
    public String passThrough(Player player) {
    	player.loseHalfInventory();
    	return "🦭 The seal passed through " + player.getName() + "'s square! Lost half inventory!";
    }
    
    /**
     * Seal interaction when a player lands on the seal's square.
     */
    public String interact(Player player) {
        // If seal is eating (blocked), player is safe
        if (this.blockedTurns > 0) {
            return "🦭😴 The seal is still eating the fish. " + player.getName() + " is safe!";
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
    public void updateSealTurns() {
        if (this.blockedTurns > 0) {
            this.blockedTurns--;
            
            if (this.blockedTurns == 0) {
                this.isBribed = false;
                System.out.println("🦭 The seal has finished eating and is dangerous again!");
            }
        }
    }
    
    /**
     * CPU AI: Seal plays its turn.
     * Rolls a dice, moves, and affects players.
     * Returns a list of log messages describing what happened.
     */
    public List<String> playTurn(List<Player> allPlayers) {
    	List<String> log = new java.util.ArrayList<>();
    	
    	if (isBlocked()) {
    		log.add("🦭😴 The seal is eating a fish and can't move. (" + blockedTurns + " turns left)");
    		updateSealTurns();
    		return log;
    	}
    	
    	// Roll dice (1-6)
    	int roll = random.nextInt(6) + 1;
    	log.add("🦭 The seal rolls: " + roll);
    	
    	int oldPos = this.numSquare;
    	int newPos = Math.min(oldPos + roll, Board.MAX_SQUARES - 1);
    	
    	// Check all squares between old and new position for players to affect
    	for (int sq = oldPos + 1; sq < newPos; sq++) {
    		for (Player p : allPlayers) {
    			if (p.getSquareIndex() == sq) {
    				log.add(passThrough(p));
    			}
    		}
    	}
    	
    	this.numSquare = newPos;
    	log.add("🦭 The seal moves to square " + newPos);
    	
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
    	if (board != null) {
    		List<Integer> holes = board.getIceHole_Array();
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