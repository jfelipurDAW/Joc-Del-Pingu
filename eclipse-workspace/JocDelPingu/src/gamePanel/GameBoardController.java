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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
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
    	Player player1 = new Player("Player 1", "00FF00");
    	Player player2 = new Player("Player 2", "0040FF");
    	Player player3 = new Player("Player 3", "00AB00");
    	Player player4 = new Player("Player 4", "F6FF00");
    	
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
        grid.getChildren().clear();
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
            addPlayerSpritesToCell(cell, i);

            grid.add(cell, col, row);
        }
    }

    /**
     * Add player sprites to a specific cell if players are on that square
     */
    private void addPlayerSpritesToCell(StackPane cell, int squareIndex) {
        java.util.List<Player> players = getAllPlayers();
        int playerCount = 0;

        // Load images once 
        Image baseImage = new Image("file:///D:/Usuarios/martavoytk/Joc-Del-Pingu/eclipse-workspace/JocDelPingu/src/assets/sprites/entities/player/player_idle.png");
        Image colorImage = new Image("file:///D:/Usuarios/martavoytk/Joc-Del-Pingu/eclipse-workspace/JocDelPingu/src/assets/sprites/entities/player/player_idle_colour.png");

        // Desired display size — adjust according to your cell size / sprite resolution
        double spriteSize = 40;  

        for (Player player : players) {
            if (player.getSquareIndex() == squareIndex) {

                // Container for this player's sprite (allows stacking + translation)
                StackPane playerToken = new StackPane();

                // 1. Base layer (outline / shadow / details)
                ImageView baseView = new ImageView(baseImage);
                baseView.setFitWidth(spriteSize);
                baseView.setFitHeight(spriteSize);
                baseView.setPreserveRatio(true);
                baseView.setSmooth(false);           

                // 2. Color overlay layer
                ImageView colorView = new ImageView(colorImage);
                colorView.setFitWidth(spriteSize);
                colorView.setFitHeight(spriteSize);
                colorView.setPreserveRatio(true);
                colorView.setSmooth(false);

             // tint
                ColorAdjust tint = new ColorAdjust();
                double hue = getHueForColor(player.getColour());
                tint.setHue(hue);

                
                tint.setSaturation(0.8);    
                tint.setBrightness(-0.05);  
                tint.setContrast(0.2);    
                colorView.setEffect(tint);

                playerToken.getChildren().addAll(baseView, colorView);


                double offsetX = (playerCount - (players.size() - 1) / 2.0) * 22;
                playerToken.setTranslateX(offsetX);

                cell.getChildren().add(playerToken);
                playerCount++;
            }
        }
    }
    private static final java.util.Map<String, Double> COLOR_HUES = 
    	    java.util.Map.of(
    	        "FF0000", 0.0,      
    	        "F6FF00", 0.1667,   
    	        "00AB00", 0.3333,   
    	        "0040FF", 0.6667    
    	    );

    	private double getHueForColor(String colour) {
    	    if (colour == null) return 0.0;
    	    String hex = colour.toUpperCase().trim();
    	    return COLOR_HUES.getOrDefault(hex, 0.0);
    	}
    
    
}