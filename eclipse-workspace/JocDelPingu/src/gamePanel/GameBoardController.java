package gamePanel;

import board.Board;
import board.SquareType;
import board.TurnController;
import entity.EntityType;
import entity.Player;
import ObjectManagers.objects.Dice;
import ObjectManagers.ObjectType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class GameBoardController {

    @FXML
    private GridPane grid;

    @FXML
    private Button rollDiceButton;

    private Board gameBoard;
    private TurnController turnController;
    private Dice slowDice;

    @FXML
    public void initialize() {
    	gameBoard = new Board();
    	gameBoard.createNewBoard();
    	
    	// Initialize turn controller and players
    	turnController = new TurnController();
    	initializePlayers();
    	
    	// Initialize dice
    	slowDice = new Dice(ObjectType.SLOWDICE);
    	
        drawBoard();
    }

    /**
     * Initialize the players for the game
     */
    private void initializePlayers() {
    	// Create players with different colors
    	Player player1 = new Player("Player 1", "ffffff");
    	Player player2 = new Player("Player 2", "ff0000");
    	Player player3 = new Player("Player 3", "ff77aa");
    	Player player4 = new Player("Player 4", "444444");
    	
    	// Add players to turn controller
    	turnController.addPlayer(player1);
    	turnController.addPlayer(player2);
    	turnController.addPlayer(player3);
    	turnController.addPlayer(player4);
    }

    /**
     * Handle the roll dice button click
     * Rolls the slow dice and moves the current player
     */
    @FXML
    private void rollDice() {
    	rollDiceButton.setDisable(true);
    	// Get the current player
    	Player currentPlayer = (Player) turnController.getCurrentTurn();
    	
    	// Roll the slow dice
    	int diceResult = slowDice.roll();
    	System.out.println(currentPlayer.getName() + " rolled: " + diceResult);
    	
    	// Move the player
    	currentPlayer.advance(diceResult);
    	System.out.println(currentPlayer.getName() + " moved to square: " + currentPlayer.getSquareIndex());
    	
    	// Redraw the board to show new positions
    	drawBoard();
    	
    	// Move to next turn
    	turnController.nextTurn();
    	rollDiceButton.setDisable(false);
    }

	public Board getCurrentGameBoard() {
    	return this.gameBoard;
    }

    /**
     * Get all players in the game
     */
    private java.util.List<Player> getAllPlayers() {
    	java.util.List<Player> players = new java.util.ArrayList<>();
    	for (entity.Entity e : turnController.getAllPlayers()) {
    		if (e instanceof Player) {
    			players.add((Player) e);
    		}
    	}
    	return players;
    }

    private void drawBoard() {
        int cols = Board.widthBoard;
        int rows = Board.heightBoard;

        // ==========================================
        // PART 1: RESPONSIVE AND SQUARED BOARD (1:1)
        // ==========================================
        
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        DoubleBinding midaCella = Bindings.createDoubleBinding(
            () -> Math.min(grid.getWidth() / cols, grid.getHeight() / rows),
            grid.widthProperty(), 
            grid.heightProperty()
        );

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.prefWidthProperty().bind(midaCella);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.prefHeightProperty().bind(midaCella);
            grid.getRowConstraints().add(row);
        }

        grid.setAlignment(javafx.geometry.Pos.CENTER);


        // ==========================================
        // PART 2: FOLLOW THE "S" PATTERN ON DRAW
        // ==========================================
        
        for (int i = 0; i < Board.MAX_SQUARES; i++) {
            
            int row = i / cols; 
            int col;
            
            if (row % 2 == 0) {
                col = i % cols; // Even Row: left to right
            } else {
                col = (cols - 1) - (i % cols); // Odd Row: right to left
            }

            StackPane cell = new StackPane();
            cell.getStyleClass().add("square");

            switch (gameBoard.getSquareType(i)) {
                case NORMAL: cell.getStyleClass().add("square-normal"); break;
                case ICE_HOLE: cell.getStyleClass().add("square-ice-hole"); break;
                case SLED: cell.getStyleClass().add("square-sled"); break;
                case BEAR: cell.getStyleClass().add("square-bear"); break;
                case EVENT: cell.getStyleClass().add("square-event"); break;
                case BROKEN_FLOOR: cell.getStyleClass().add("square-broken-floor"); break;
                case START: cell.getStyleClass().add("square-start"); break;
                case END: cell.getStyleClass().add("square-end");
            }

            boolean isLeft  = (col == 0);
            boolean isRight = (col == cols - 1);
            boolean isEven  = (row % 2 == 0);
            boolean isOdd   = (row % 2 != 0);
            
            if (gameBoard.getSquareType(i) != SquareType.START && gameBoard.getSquareType(i) != SquareType.END) {
            	if (isLeft) {
            		if (isEven) {
            			cell.getStyleClass().add("square-bottom-left");
            		}   
            		else if (isOdd) {
            			cell.getStyleClass().add("square-top-left");
            		} 
            	}
            	else if (isRight) {
            		if (isOdd) {
            			cell.getStyleClass().add("square-bottom-right");
            		}   
            		else if (isEven) {
            			cell.getStyleClass().add("square-top-right");
            		} 
            	}
            }

            // Add player circles to the cell
            addPlayerCirclesToCell(cell, i);

            grid.add(cell, col, row);
        }
    }

    /**
     * Add player circles to a specific cell if players are on that square
     */
    private void addPlayerCirclesToCell(StackPane cell, int squareIndex) {
    	java.util.List<Player> players = getAllPlayers();
    	int playerCount = 0;
    	
    	for (Player player : players) {
    		if (player.getSquareIndex() == squareIndex) {
    			// Create a circle for the player
    			Circle playerCircle = new Circle(8); // Radius of 8 pixels
    			
    			// Set color based on player
    			playerCircle.setFill(javafx.scene.paint.Color.web(player.getColour()));
    			
    			// Position the circle within the cell
    			playerCircle.setTranslateX(playerCount * 15 - 15); // Offset circles horizontally
    			playerCircle.setTranslateY(0);
    			
    			cell.getChildren().add(playerCircle);
    			playerCount++;
    		}
    	}
    }
    
}