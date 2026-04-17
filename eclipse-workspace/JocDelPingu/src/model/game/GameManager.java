package model.game;
import java.util.Base64;
import java.sql.Connection;
import java.util.ArrayList;
import model.db.BBDD;

public class GameManager {
    
    private String gameId;
    private int numPlayers;
    private boolean isActive;

    // Constructor
    public GameManager(String gameId, int numPlayers) {
        this.gameId = gameId;
        this.numPlayers = numPlayers;
        this.isActive = false; // La partida empieza inactiva hasta que la arrancamos
    }

    // Método para empezar la partida
    public void startNewGame() {
        System.out.println("Starting game: " + gameId + " with " + numPlayers + " players.");
        this.isActive = true;
    }

    // Método para jugar un turno
    public void playTurn() {
        System.out.println("Playing a turn...");
        // Aquí irá más adelante la lógica de tirar los dados y mover al jugador
    }

    // Método para comprobar si la partida sigue activa
    public boolean isGameActive() {
        return this.isActive;
    }

    // Método para guardar la partida (Base de datos)
    public void saveGame() {
        System.out.println("Saving game state to the database...");
        // Aquí irá más adelante el código SQL (INSERT/UPDATE) para guardar
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