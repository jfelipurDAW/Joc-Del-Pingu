package gamePanel;

import board.Board;
import board.SquareType;
import board.TurnController;
import entity.Entity;
import entity.Player;
import ObjectManagers.ObjectType;
import ObjectManagers.objects.Dice;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameBoardController {

    @FXML private GridPane grid;
    @FXML private Button rollDiceButton;

    private Board gameBoard;
    private TurnController turnController;
    private Dice slowDice;

    // Imágenes cargadas una sola vez (classpath)
    private final Image baseImage;
    private final Image colorImage;

    public GameBoardController() {
        baseImage  = loadImage("/assets/sprites/entities/player/player_idle.png");
        colorImage = loadImage("/assets/sprites/entities/player/player_idle_colour.png");

        if (baseImage == null) {
            System.err.println("No se pudo cargar: player_idle.png");
        }
        if (colorImage == null) {
            System.err.println("No se pudo cargar: player_idle_colour.png");
        }
    }

    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Recurso no encontrado en classpath: " + path);
                return null;
            }
            // smooth = false → evita borrosidad en pixel art
            Image img = new Image(is, 0, 0, true, false);
            is.close();
            return img;
        } catch (Exception e) {
            System.err.println("Error al cargar imagen " + path + ": " + e.getMessage());
            return null;
        }
    }

    @FXML
    public void initialize() {
        gameBoard = new Board();
        gameBoard.createNewBoard();

        turnController = new TurnController();
        initializePlayers();

        slowDice = new Dice(ObjectType.SLOWDICE);

        drawBoard();
    }

    /*
     * Initialize the players for the game
     */
    private void initializePlayers() {
    	// Create players with different colors
    	Player player1 = new Player("Player 1", "222222");
    	player1.setBoard(gameBoard);
    	Player player2 = new Player("Player 2", "ff0000");
    	player2.setBoard(gameBoard);
    	Player player3 = new Player("Player 3", "ff77aa");
    	player3.setBoard(gameBoard);
    	Player player4 = new Player("Player 4", "444444");
    	player4.setBoard(gameBoard);
    	
    	// Add players to turn controller
    	turnController.addPlayer(player1);
    	turnController.addPlayer(player2);
    	turnController.addPlayer(player3);
    	turnController.addPlayer(player4);
    }

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

    private List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player) {
                players.add((Player) e);
            }
        }
        return players;
    }

    private void drawBoard() {
        int cols = Board.widthBoard;
        int rows = Board.heightBoard;

        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        grid.getChildren().clear();  // ← muy importante

        DoubleBinding cellSize = Bindings.createDoubleBinding(
            () -> Math.min(grid.getWidth() / cols, grid.getHeight() / rows),
            grid.widthProperty(),
            grid.heightProperty()
        );

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.prefWidthProperty().bind(cellSize);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.prefHeightProperty().bind(cellSize);
            grid.getRowConstraints().add(row);
        }

        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < Board.MAX_SQUARES; i++) {
            int row = i / cols;
            int col = (row % 2 == 0)
                ? (i % cols)
                : (cols - 1 - (i % cols));

            StackPane cell = new StackPane();
            cell.getStyleClass().add("square");

            SquareType type = gameBoard.getSquareType(i);
            if (type != null) {
                switch (type) {
                    case NORMAL         -> cell.getStyleClass().add("square-normal");
                    case ICE_HOLE       -> cell.getStyleClass().add("square-ice-hole");
                    case SLED           -> cell.getStyleClass().add("square-sled");
                    case BEAR           -> cell.getStyleClass().add("square-bear");
                    case EVENT          -> cell.getStyleClass().add("square-event");
                    case BROKEN_FLOOR   -> cell.getStyleClass().add("square-broken-floor");
                    case START          -> cell.getStyleClass().add("square-start");
                    case END            -> cell.getStyleClass().add("square-end");
                }
            }

            boolean isLeft  = (col == 0);
            boolean isRight = (col == cols - 1);
            boolean isEven  = (row % 2 == 0);

            if (type != SquareType.START && type != SquareType.END) {
                if (isLeft) {
                    cell.getStyleClass().add(isEven ? "square-bottom-left" : "square-top-left");
                } else if (isRight) {
                    cell.getStyleClass().add(isEven ? "square-top-right" : "square-bottom-right");
                }
            }

            addPlayerSpritesToCell(cell, i);

            grid.add(cell, col, row);
        }
    }

    private void addPlayerSpritesToCell(StackPane cell, int squareIndex) {
        List<Player> players = getAllPlayers();
        int playerCount = 0;

        double spriteSize = 40;  // ajusta este valor según el tamaño deseado / resolución de tus sprites

        for (Player player : players) {
            if (player.getSquareIndex() == squareIndex) {
                StackPane playerToken = new StackPane();

                if (baseImage != null && colorImage != null) {
                    ImageView baseView = new ImageView(baseImage);
                    baseView.setFitWidth(spriteSize);
                    baseView.setFitHeight(spriteSize);
                    baseView.setPreserveRatio(true);
                    baseView.setSmooth(false);  // nitidez importante para pixel art

                    ImageView colorView = new ImageView(colorImage);
                    colorView.setFitWidth(spriteSize);
                    colorView.setFitHeight(spriteSize);
                    colorView.setPreserveRatio(true);
                    colorView.setSmooth(false);

                    ColorAdjust tint = new ColorAdjust();
                    tint.setHue(getHueForColor(player.getColour()));
                    // Puedes ajustar estos valores si el tinte no queda natural
                    // tint.setSaturation(0.7);
                    // tint.setBrightness(-0.05);

                    colorView.setEffect(tint);

                    playerToken.getChildren().addAll(baseView, colorView);
                } else {
                    // Fallback si falló la carga de imágenes
                    Circle fallback = new Circle(spriteSize / 2);
                    fallback.setFill(getColorFromHex(player.getColour()));
                    fallback.setStroke(Color.BLACK);
                    fallback.setStrokeWidth(1.2);
                    playerToken.getChildren().add(fallback);
                }

                // Distribución centrada cuando hay varios jugadores
                double offsetX = (playerCount - (players.size() - 1) / 2.0) * 22;
                playerToken.setTranslateX(offsetX);

                cell.getChildren().add(playerToken);
                playerCount++;
            }
        }
    }

    private double getHueForColor(String hexColor) {
        if (hexColor == null) return 0.0;

        String hex = hexColor.toUpperCase().trim();

        return switch (hex) {
            case "FF0000" -> 0.0;      // rojo
            case "F6FF00" -> 0.1667;   // amarillo
            case "00AB00" -> 0.3333;   // verde
            case "0040FF" -> 0.6667;   // azul
            default       -> 0.0;      // fallback rojo
        };
    }

    // Ayuda para el fallback (convierte hex a Color)
    private Color getColorFromHex(String hex) {
        try {
            return Color.web("#" + hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }
}