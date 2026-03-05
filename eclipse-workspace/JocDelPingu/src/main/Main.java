package main;

import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔══════════════════════════╗");
        System.out.println("║    PINGU GAME - TEST     ║");
        System.out.println("╚══════════════════════════╝\n");
        
        // Create a new game
        GameManager game = new GameManager("GAME_001", 2);
        game.startNewGame();
        
        // Simulate 10 turns
        for (int i = 0; i < 10 && game.isGameActive(); i++) {
            game.playTurn();
            
            // Pause to read console output
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
        
        // Save the game
        if (game.isGameActive()) {
            game.saveGame();
        }
        
        System.out.println("\n=== END OF TEST ===");
        scanner.close();
    }
}
