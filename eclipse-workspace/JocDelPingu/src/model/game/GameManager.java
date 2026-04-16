package model.game; 

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
}