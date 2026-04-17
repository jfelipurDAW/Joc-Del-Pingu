package model.game;
import java.util.Base64;
import java.sql.Connection;
import java.util.ArrayList;
import model.db.BBDD;

import model.board.Board;
import model.entity.Player;
import model.entity.Seal;
public class GameManager {
    
    private String gameId;
    private int numPlayers;
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
        this.numPlayers = numPlayers;
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

    public String playTurn(int diceResult) {
        Player current = getCurrentPlayer();
        String moveMsg = playerManager.movePlayer(current, diceResult, board);
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
        return SaveLoadService.saveGame(gameId, board, turnController, seal);
    }
    
    public boolean loadGame() {
        return SaveLoadService.loadGame(gameId);
    }

    public boolean isGameOver() {
        return game.isGameOver() || (turnController != null && turnController.isGameWon());
    }

    public Player getWinner() {
        if (game.getWinner() != null) return game.getWinner();
        return turnController != null ? turnController.getWinner() : null;
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
    
    /**
     * 1. MÉTODO PARA ENCRIPTAR (Requisito obligatorio del PDF)
     */
    public String encriptarTexto(String textoNormal) {
        return Base64.getEncoder().encodeToString(textoNormal.getBytes());
    }

    /**
     * 2. MÉTODO PARA GUARDAR UNA PARTIDA NUEVA CADA VEZ
     */
    public void guardarPartida(Connection con, ArrayList<Player> listaJugadores) {
        
        // A) Juntamos toda la info de los jugadores en un solo texto
        StringBuilder textoDatos = new StringBuilder();
        
        for (Player p : listaJugadores) {
            textoDatos.append("Jugador:").append(p.getName())
                      .append(",Casilla:").append(p.getSquare())
                      .append(",Bolas:").append(p.getInventory().getSnowballQuantity())
                      .append(",Peces:").append(p.getInventory().getFishQuantity())
                      .append("; ");
        }
        
        String datosFinales = textoDatos.toString();
        
        // B) Encriptamos el texto para que sea un CLOB de letras raras
        String datosEncriptados = encriptarTexto(datosFinales);
        
        // C) EL TRUCO: Creamos un ID ÚNICO con la hora exacta
        // Esto genera nombres como "PARTIDA_1700583921"
        // Como el número cambia cada milisegundo, SIEMPRE se guardará como una fila nueva
        String idUnico = "PARTIDA_" + System.currentTimeMillis();
        
        // D) Hacemos el INSERT en Oracle usando tu clase BBDD
        String sql = "INSERT INTO SAVED_GAMES (GAME_ID, GAME_DATA) " +
                     "VALUES ('" + idUnico + "', '" + datosEncriptados + "')";
        
        int filas = BBDD.insert(con, sql);
        
        if (filas > 0) {
            System.out.println("¡Partida guardada con éxito en Oracle! ID: " + idUnico);
        } else {
            System.out.println("Error al guardar la partida en Oracle.");
        }
    }
}