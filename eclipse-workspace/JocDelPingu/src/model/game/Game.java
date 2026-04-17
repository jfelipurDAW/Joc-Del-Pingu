package model.game;

import java.util.List;
import model.board.Board;
import model.entity.Player;
import model.entity.Seal;

public class Game {
    private List<Player> players;
    private Board board;
    private int currentTurn;
    private Player winner;
    private boolean gameOver;
    private Seal seal;
    private boolean sealEnabled;

    public Game() {
        this.gameOver = false;
    }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public Seal getSeal() { return seal; }
    public void setSeal(Seal seal) { this.seal = seal; }

    public boolean isSealEnabled() { return sealEnabled; }
    public void setSealEnabled(boolean sealEnabled) { this.sealEnabled = sealEnabled; }
}
