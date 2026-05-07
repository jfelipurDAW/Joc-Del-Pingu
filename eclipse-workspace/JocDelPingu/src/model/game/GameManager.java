package model.game;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;

public class GameManager {

    private String gameId;
    private boolean isActive;

    private Board board;
    private TurnController turnController;
    private Seal seal;
    private BoardManager boardManager;
    private PlayerManager playerManager;
    private Game game;

    // Constructor
    public GameManager(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.isActive = false;
        this.boardManager = new BoardManager();
        this.playerManager = new PlayerManager();
        this.game = new Game();
    }

    public void setBoard(Board board) {
        this.board = board;
        this.game.setBoard(board);
    }

    public void setTurnController(TurnController turnController) {
        this.turnController = turnController;
    }

    public void setSeal(Seal seal) {
        this.seal = seal;
        this.game.setSeal(seal);
    }

    public void startNewGame() {
        this.isActive = true;
    }

    public ActionResult playTurn(int diceResult) {
        Player current = getCurrentPlayer();
        ActionResult moveMsg = playerManager.movePlayer(current, diceResult, board);
        if (moveMsg != null) {
            this.game.setGameOver(true);
            this.game.setWinner(current);
            return moveMsg;
        }
        return boardManager.executeSquareAction(current, board);
    }

    public boolean isGameActive() {
        return this.isActive;
    }

    public boolean saveGame() {
        return SaveLoadService.saveGame(gameId, board, turnController, seal, null);
    }

    public boolean loadGame() {
        return SaveLoadService.loadGame(gameId);
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public Player getWinner() {
        return game.getWinner();
    }

    public Player getCurrentPlayer() {
        if (turnController != null && turnController.getCurrentTurn() instanceof Player) {
            return (Player) turnController.getCurrentTurn();
        }
        return null;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public Seal getSeal() {
        return seal;
    }
}
