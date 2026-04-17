package model.game;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;
import model.item.ObjectType;

public class PlayerManager {
    
    public static class SnowballWarResult {
        private final Player winner;
        private final Player loser;
        private final int winnerBalls;
        private final int loserBalls;
        private final int difference;
        
        public SnowballWarResult(Player winner, Player loser, int winnerBalls, int loserBalls, int difference) {
            this.winner = winner;
            this.loser = loser;
            this.winnerBalls = winnerBalls;
            this.loserBalls = loserBalls;
            this.difference = difference;
        }
        
        public Player getWinner() { return winner; }
        public Player getLoser() { return loser; }
        public int getWinnerBalls() { return winnerBalls; }
        public int getLoserBalls() { return loserBalls; }
        public int getDifference() { return difference; }
        public boolean isTie() { return winner == null; }
        
        @Override
        public String toString() {
            if (isTie()) {
                return "It's a tie! (" + winnerBalls + " vs " + loserBalls + ") Both spend all snowballs. No one retreats.";
            } else {
                return winner.getName() + " wins! (" + winnerBalls + " vs " + loserBalls + ") " + 
                       loser.getName() + " retreats " + difference + " squares!";
            }
        }
    }

    public String movePlayer(Player player, int steps, Board board) {
        int newPos = player.getSquareIndex() + steps;
        if (newPos >= Board.MAX_SQUARES - 1) {
            player.setNumSquare(Board.MAX_SQUARES - 1);
            return player.getName() + " reached the END! 🎉 WINNER!";
        } else {
            player.setNumSquare(newPos);
            return null; // Square logic logic is delegated to the BoardManager
        }
    }

    public SnowballWarResult snowballWar(Player player1, Player player2) {
        int balls1 = player1.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int balls2 = player2.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        
        player1.getInventory().useObject(ObjectType.SNOWBALL, balls1);
        player2.getInventory().useObject(ObjectType.SNOWBALL, balls2);
        
        int difference = Math.abs(balls1 - balls2);
        
        if (balls1 > balls2) {
            int newPos = Math.max(0, player2.getSquareIndex() - difference);
            player2.setSquare(newPos);
            return new SnowballWarResult(player1, player2, balls1, balls2, difference);
        } else if (balls2 > balls1) {
            int newPos = Math.max(0, player1.getSquareIndex() - difference);
            player1.setSquare(newPos);
            return new SnowballWarResult(player2, player1, balls2, balls1, difference);
        } else {
            return new SnowballWarResult(null, null, balls1, balls2, 0);
        }
    }

    public String throwSnowball(Player attacker, Player target) {
        attacker.getInventory().useObject(ObjectType.SNOWBALL, 1);
        int backSteps = (int)(Math.random() * 3) + 1;
        int oldPos = target.getSquareIndex();
        int newPos = Math.max(0, oldPos - backSteps);
        target.setSquare(newPos);
        return "⛄ " + attacker.getName() + " threw a snowball at " + target.getName() + 
               "! " + target.getName() + " goes back " + backSteps + " squares (" + oldPos + " → " + newPos + ")";
    }

    public String handleSealInteraction(Seal seal, Player player) {
        return seal.interact(player);
    }
}
