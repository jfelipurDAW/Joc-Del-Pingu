package gamePanel;

import board.Board;
import board.SquareType;
import board.TurnController;
import entity.Entity;
import entity.Player;
import entity.Seal;
import ObjectManagers.Inventory;
import ObjectManagers.ObjectType;
import ObjectManagers.objects.Dice;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameBoardController {

    // --- FXML Bindings ---
    @FXML private BorderPane rootPane;
    @FXML private GridPane grid;
    @FXML private Button rollDiceButton;
    @FXML private Button rollFastDiceButton;
    @FXML private Button rollSlowDiceButton;
    @FXML private Button throwSnowballButton;
    @FXML private Button saveGameButton;
    
    @FXML private Text gameTitleText;
    @FXML private Label turnIndicatorLabel;
    @FXML private Label diceResultLabel;
    
    @FXML private Label currentPlayerName;
    @FXML private Label currentPlayerSquare;
    
    @FXML private Label snowballCount;
    @FXML private Label fishCount;
    @FXML private Label fastDiceCount;
    @FXML private Label slowDiceCount;
    @FXML private Label totalItemCount;
    
    @FXML private VBox allPlayersBox;
    @FXML private VBox sealStatusBox;
    @FXML private Label sealPositionLabel;
    @FXML private Label sealBlockedLabel;
    @FXML private VBox rightPanel;
    
    @FXML private TextArea eventLog;

    // --- Game State ---
    private Board gameBoard;
    private TurnController turnController;
    private Dice defaultDice;
    private Dice fastDice;
    private Dice slowDice;
    private Seal seal;
    private boolean sealEnabled;
    private boolean gameOver;

    // --- Sprites ---
    private final Image baseImage;
    private final Image colorImage;

    public GameBoardController() {
        baseImage  = loadImage("/assets/sprites/entities/player/player_idle.png");
        colorImage = loadImage("/assets/sprites/entities/player/player_idle_colour.png");
        gameOver = false;
    }

    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            Image img = new Image(is, 0, 0, true, false);
            is.close();
            return img;
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            return null;
        }
    }

    @FXML
    public void initialize() {
        gameBoard = new Board();
        gameBoard.createNewBoard();

        turnController = new TurnController();
        initializePlayers();

        defaultDice = new Dice(ObjectType.SLOWDICE); // Default: 1-3
        fastDice = new Dice(ObjectType.FASTDICE);     // 5-10
        slowDice = new Dice(ObjectType.SLOWDICE);     // 1-3

        sealEnabled = GameSetupConfig.isSealEnabled();
        if (sealEnabled) {
            seal = new Seal();
            seal.setBoard(gameBoard);
            sealStatusBox.setVisible(true);
            sealStatusBox.setManaged(true);
        }

        drawBoard();
        applyCss();
        updateHUD();
        logEvent("🐧 Game started! " + turnController.getPlayerCount() + " players.");
        logEvent("🎯 " + getCurrentPlayer().getName() + "'s turn!");
    }

    private void initializePlayers() {
        List<Player> players = GameSetupConfig.getPlayers();
        if (players != null && !players.isEmpty()) {
            for (Player player : players) {
                player.setBoard(gameBoard);
                player.setSquare(0);
                turnController.addPlayer(player);
            }
        } else {
            Player player1 = new Player("Pingu", "FF0000");
            player1.setBoard(gameBoard);
            Player player2 = new Player("Robby", "0040FF");
            player2.setBoard(gameBoard);
            turnController.addPlayer(player1);
            turnController.addPlayer(player2);
        }
    }

    private void applyCss() {
        try {
            rootPane.getStylesheets().clear();
            rootPane.getStylesheets().add(getClass().getResource("/assets/styles/penguin_theme.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
    }

    // ============================================
    //  ACTION HANDLERS
    // ============================================

    @FXML
    private void rollDice() {
        if (gameOver) return;
        int result = defaultDice.roll();
        processDiceRoll(result, "Normal");
    }

    @FXML
    private void rollFastDice() {
        if (gameOver) return;
        Player current = getCurrentPlayer();
        if (current.getInventory().getObjectQuantity(ObjectType.FASTDICE) > 0) {
            current.getInventory().useObject(ObjectType.FASTDICE, 1);
            int result = fastDice.roll();
            processDiceRoll(result, "Fast");
        } else {
            showAlert("No Fast Dice", "You don't have any fast dice! Land on event squares to find some.");
        }
    }

    @FXML
    private void rollSlowDice() {
        if (gameOver) return;
        Player current = getCurrentPlayer();
        if (current.getInventory().getObjectQuantity(ObjectType.SLOWDICE) > 0) {
            current.getInventory().useObject(ObjectType.SLOWDICE, 1);
            int result = slowDice.roll();
            processDiceRoll(result, "Slow");
        } else {
            showAlert("No Slow Dice", "You don't have any slow dice in your inventory!");
        }
    }

    @FXML
    private void throwSnowball() {
        if (gameOver) return;
        Player current = getCurrentPlayer();
        
        if (current.getInventory().getObjectQuantity(ObjectType.SNOWBALL) <= 0) {
            showAlert("No Snowballs", "You don't have any snowballs! Land on event squares to find some.");
            return;
        }

        // Get list of other players to target
        List<Player> targets = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && e != current) {
                targets.add((Player) e);
            }
        }

        if (targets.isEmpty()) {
            showAlert("No Targets", "There are no other players to throw snowballs at!");
            return;
        }

        // Show target selection dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Throw Snowball ⛄");
        dialog.setHeaderText("Choose a target to hit with a snowball!");
        dialog.setContentText("Target:");
        
        List<String> targetNames = new ArrayList<>();
        for (Player p : targets) {
            targetNames.add(p.getName() + " (Square " + p.getSquareIndex() + ")");
        }
        dialog.getItems().addAll(targetNames);
        dialog.setSelectedItem(targetNames.get(0));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            int selectedIndex = targetNames.indexOf(result.get());
            Player target = targets.get(selectedIndex);
            
            // Use 1 snowball, move target back 1-3 squares
            current.getInventory().useObject(ObjectType.SNOWBALL, 1);
            int backSteps = (int)(Math.random() * 3) + 1;
            int oldPos = target.getSquareIndex();
            int newPos = Math.max(0, oldPos - backSteps);
            target.setSquare(newPos);
            
            String msg = "⛄ " + current.getName() + " threw a snowball at " + target.getName() + 
                        "! " + target.getName() + " goes back " + backSteps + " squares (" + oldPos + " → " + newPos + ")";
            logEvent(msg);

            // Animate snowball effect
            animateSnowballThrow();
            
            drawBoard();
            updateHUD();
            
            // End turn after throwing
            endTurn();
        }
    }

    @FXML
    private void saveGame() {
        logEvent("💾 Game save requested...");
        showAlert("Save Game", "Game save feature requires Oracle DB connection.\nThis will be available when connected to the school network.");
    }

    // ============================================
    //  GAME LOGIC
    // ============================================

    private void processDiceRoll(int diceResult, String diceType) {
        disableActions();
        Player current = getCurrentPlayer();
        
        // Animate dice result display
        diceResultLabel.setText("🎲 " + diceResult);
        animateDiceResult();
        
        logEvent("🎲 " + current.getName() + " rolled " + diceResult + " (" + diceType + " die)");

        // Move the player
        String moveResult = current.moveForward(diceResult);
        logEvent(moveResult);

        drawBoard();

        // Check win
        if (current.getSquareIndex() >= Board.MAX_SQUARES - 1) {
            handleWin(current);
            return;
        }

        // Check for player collisions (snowball war)
        List<Player> collisions = turnController.getPlayersAtSquare(current.getSquareIndex(), current);
        if (!collisions.isEmpty()) {
            for (Player other : collisions) {
                handlePlayerWar(current, other);
            }
            drawBoard();
        }

        // Check seal collision
        if (sealEnabled && seal != null && seal.getSquareIndex() == current.getSquareIndex()) {
            String sealResult = seal.interact(current);
            logEvent(sealResult);
            drawBoard();
        }

        updateHUD();
        
        // End turn
        endTurn();
    }

    private void endTurn() {
        if (gameOver) return;

        // Advance turn
        turnController.nextTurn();
        
        // If seal is enabled, play seal turn between player turns
        if (sealEnabled && seal != null) {
            playSealTurn();
        }

        // Update seal turns
        if (seal != null) {
            seal.updateSealTurns();
        }

        // Update HUD for next player
        updateHUD();
        enableActions();
        
        Player next = getCurrentPlayer();
        logEvent("──────────────────");
        logEvent("🎯 " + next.getName() + "'s turn!");
    }

    private void playSealTurn() {
        List<Player> humanPlayers = turnController.getHumanPlayers();
        List<String> sealLog = seal.playTurn(humanPlayers);
        for (String msg : sealLog) {
            logEvent(msg);
        }
        
        // Check if seal reached end (seal wins = all players lose)
        if (seal.getSquareIndex() >= Board.MAX_SQUARES - 1) {
            logEvent("🦭 THE SEAL REACHED THE END! ALL PLAYERS LOSE!");
            gameOver = true;
            disableActions();
            showAlert("Game Over!", "🦭 The Seal won the game! Better luck next time!");
        }
        
        drawBoard();
        updateSealStatus();
    }

    private void handlePlayerWar(Player attacker, Player defender) {
        int atkBalls = attacker.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int defBalls = defender.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        
        if (atkBalls == 0 && defBalls == 0) {
            logEvent("⚔️ " + attacker.getName() + " and " + defender.getName() + " meet on the same square, but neither has snowballs!");
            return;
        }

        logEvent("⚔️ SNOWBALL WAR! " + attacker.getName() + " vs " + defender.getName() + "!");
        
        Player.SnowballWarResult warResult = Player.snowballWar(attacker, defender);
        logEvent("⚔️ " + warResult.toString());
        
        // Flash animation for war
        animateWarFlash();
    }

    private void handleWin(Player winner) {
        gameOver = true;
        disableActions();
        logEvent("🎉🎉🎉 " + winner.getName() + " WINS THE GAME! 🎉🎉🎉");
        
        // Win animation
        animateWin();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Winner!");
        alert.setHeaderText(winner.getName() + " wins!");
        alert.setContentText("Congratulations! " + winner.getName() + " reached the end first!\n\n" +
                           "Final position: Square " + winner.getSquareIndex());
        alert.showAndWait();
    }

    // ============================================
    //  HUD UPDATES
    // ============================================

    private void updateHUD() {
        Player current = getCurrentPlayer();
        
        // Hide old HUD panels if they exist in the scene (assuming rightPanel holds them)
        if (rightPanel != null) {
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
        }

        // Create Top-Left Hotbar HUD
        HBox hotbar = createHotbar(current);
        rootPane.setTop(hotbar);
        
        // Inventory
        Inventory inv = current.getInventory();
        
        // Dice button states
        rollFastDiceButton.setDisable(inv.getFastdiceQuantity() <= 0);
        rollSlowDiceButton.setDisable(inv.getSlowdiceQuantity() <= 0);
        throwSnowballButton.setDisable(inv.getSnowballQuantity() <= 0);
        
        // Seal status
        if (sealEnabled && seal != null) {
            updateSealStatus();
        }
    }
    
    private HBox createHotbar(Player player) {
        HBox hotbar = new HBox(15);
        hotbar.setAlignment(Pos.CENTER_LEFT);
        hotbar.setPadding(new javafx.geometry.Insets(10));
        // Use pixel-art style background for the bar
        hotbar.setStyle("-fx-background-color: -fx-water-dark; -fx-border-color: -fx-horizon-purple; -fx-border-width: 0 0 4px 0;");

        // --- Player Info (Portrait + Name) ---
        VBox playerInfo = new VBox(5);
        playerInfo.setAlignment(Pos.CENTER);
        
        StackPane portrait = new StackPane();
        // Crop to middle-top part (Face) for both layers
        if (baseImage != null && colorImage != null) {
            double w = baseImage.getWidth();
            double h = baseImage.getHeight();
            double sx = w * 0.25;
            double sy = 0;
            double sw = w * 0.5;
            double sh = h * 0.5;
            double targetSize = 48;
            
            // Render Base Layer using Canvas for crisp scaling
            Canvas baseCanvas = new Canvas(targetSize, targetSize);
            GraphicsContext gcBase = baseCanvas.getGraphicsContext2D();
            gcBase.setImageSmoothing(false);
            gcBase.drawImage(baseImage, sx, sy, sw, sh, 0, 0, targetSize, targetSize);

            // Render Color Layer
            Canvas colorCanvas = new Canvas(targetSize, targetSize);
            GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
            gcColor.setImageSmoothing(false);
            gcColor.drawImage(colorImage, sx, sy, sw, sh, 0, 0, targetSize, targetSize);

            Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
            lighting.setSurfaceScale(0.0);
            colorCanvas.setEffect(lighting);
            
            portrait.getChildren().addAll(baseCanvas, colorCanvas);
        }
        
        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("hotbar-player-name");
        
        playerInfo.getChildren().addAll(portrait, nameLabel);
        
        // --- Inventory Slots (Minecraft Style) ---
        HBox slots = new HBox(8);
        slots.setAlignment(Pos.CENTER_LEFT);
        
        Inventory inv = player.getInventory();
        
        slots.getChildren().add(createInventorySlot(loadImage("/assets/sprites/objects/snowball.png"), inv.getSnowballQuantity()));
        slots.getChildren().add(createInventorySlot(loadImage("/assets/sprites/objects/fish.png"), inv.getFishQuantity()));
        slots.getChildren().add(createInventorySlot(loadImage("/assets/sprites/objects/fastdice.png"), inv.getFastdiceQuantity()));
        slots.getChildren().add(createInventorySlot(loadImage("/assets/sprites/objects/slowdice.png"), inv.getSlowdiceQuantity()));
        
        // Turn Indicator Text (replacing old label)
        Label turnLabel = new Label("IT'S YOUR TURN!");
        turnLabel.getStyleClass().add("hotbar-turn-label");
        
        hotbar.getChildren().add(playerInfo);
        hotbar.getChildren().add(slots);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hotbar.getChildren().addAll(spacer, turnLabel);
        
        return hotbar;
    }
    
    private StackPane createInventorySlot(Image icon, int quantity) {
        StackPane slot = new StackPane();
        slot.getStyleClass().add("button"); // Use ice block style
        slot.setPrefSize(64, 64);
        slot.setMaxSize(64, 64);
        
        if (quantity > 0 && icon != null) {
            double targetSize = 40;
            Canvas iconCanvas = new Canvas(targetSize, targetSize);
            GraphicsContext gc = iconCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);

            // Calculate aspect ratio safe dimensions
            double aspect = icon.getWidth() / icon.getHeight();
            double dw = targetSize;
            double dh = targetSize;
            if (aspect > 1) dh = dw / aspect;
            else dw = dh * aspect;
            double dx = (targetSize - dw) / 2;
            double dy = (targetSize - dh) / 2;

            gc.drawImage(icon, dx, dy, dw, dh);
            slot.getChildren().add(iconCanvas);
            
            if (quantity > 1) {
                Label qtyLabel = new Label(String.valueOf(quantity));
                qtyLabel.getStyleClass().add("inventory-quantity");
                StackPane.setAlignment(qtyLabel, Pos.BOTTOM_RIGHT);
                slot.getChildren().add(qtyLabel);
            }
        } else {
            // Empty slot visual
            slot.setOpacity(0.5);
        }
        
        return slot;
    }

    private void updateAllPlayersSummary() {
        // Keep first child (the title label), remove the rest
        while (allPlayersBox.getChildren().size() > 1) {
            allPlayersBox.getChildren().remove(allPlayersBox.getChildren().size() - 1);
        }
        
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                boolean isCurrent = (p == getCurrentPlayer());
                
                Label label = new Label(
                    (isCurrent ? "▶ " : "  ") + 
                    p.getName() + " - Sq:" + p.getSquareIndex() + 
                    " (❄" + p.getInventory().getSnowballQuantity() + 
                    " 🐟" + p.getInventory().getFishQuantity() + ")"
                );
                label.setStyle(
                    "-fx-text-fill: #" + padColor(p.getColour()) + ";" +
                    "-fx-font-size: 11;" +
                    (isCurrent ? "-fx-font-weight: bold;" : "")
                );
                allPlayersBox.getChildren().add(label);
            }
        }
    }

    private void updateSealStatus() {
        if (seal != null) {
            sealPositionLabel.setText("📍 Position: Square " + seal.getSquareIndex());
            sealBlockedLabel.setText(seal.isBlocked() ? 
                "😴 Eating fish (" + seal.getBlockedTurns() + " turns left)" : 
                "⚡ Active & Dangerous!");
        }
    }

    // ============================================
    //  BOARD DRAWING  
    // ============================================

    private void drawBoard() {
        int cols = Board.widthBoard;
        int rows = Board.heightBoard;

        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        grid.getChildren().clear();
        
        grid.setHgap(5);
        grid.setVgap(5);
        grid.getStyleClass().add("game-grid");

        DoubleBinding cellSize = Bindings.createDoubleBinding(
            () -> Math.min(grid.getWidth() / cols, grid.getHeight() / rows),
            grid.widthProperty(),
            grid.heightProperty()
        );

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.prefWidthProperty().bind(cellSize);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.prefHeightProperty().bind(cellSize);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }

        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < Board.MAX_SQUARES; i++) {
            int row = i / cols;
            int col = (row % 2 == 0)
                ? (i % cols)
                : (cols - 1 - (i % cols));

            StackPane cell = createCell(i);
            grid.add(cell, col, row);
        }
    }

    private StackPane createCell(int squareIndex) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("square");

        SquareType type = gameBoard.getSquareType(squareIndex);
        if (type != null) {
            switch (type) {
                case NORMAL       -> cell.getStyleClass().add("square-normal");
                case ICE_HOLE     -> cell.getStyleClass().add("square-ice-hole");
                case SLED         -> cell.getStyleClass().add("square-sled");
                case BEAR         -> cell.getStyleClass().add("square-bear");
                case EVENT        -> cell.getStyleClass().add("square-event");
                case BROKEN_FLOOR -> cell.getStyleClass().add("square-broken-floor");
                case START        -> cell.getStyleClass().add("square-start");
                case END          -> cell.getStyleClass().add("square-end");
            }
        }

        // Square number + type emoji
        String emoji = getSquareEmoji(type);
        Label numberLabel = new Label(squareIndex + " " + emoji);
        numberLabel.getStyleClass().add("square-number");
        StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
        cell.getChildren().add(numberLabel);

        // Add player sprites
        addPlayerSpritesToCell(cell, squareIndex);

        // Add seal sprite if enabled
        if (sealEnabled && seal != null && seal.getSquareIndex() == squareIndex) {
            Label sealLabel = new Label("🦭");
            sealLabel.setStyle("-fx-font-size: 18;");
            StackPane.setAlignment(sealLabel, Pos.BOTTOM_RIGHT);
            cell.getChildren().add(sealLabel);
        }

        return cell;
    }

    private String getSquareEmoji(SquareType type) {
        if (type == null) return "";
        return switch (type) {
            case NORMAL       -> "";
            case ICE_HOLE     -> "🕳️";
            case SLED         -> "🛷";
            case BEAR         -> "🐻";
            case EVENT        -> "❓";
            case BROKEN_FLOOR -> "💔";
            case START        -> "🏁";
            case END          -> "🏆";
        };
    }

    private void addPlayerSpritesToCell(StackPane cell, int squareIndex) {
        List<Player> playersHere = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && e.getSquareIndex() == squareIndex) {
                playersHere.add((Player) e);
            }
        }
        
        if (playersHere.isEmpty()) return;

        double spriteSize = 50; // Bigger players
        int count = 0;
        double spacing = 20; // Adjusted spacing for bigger sprites

        for (Player player : playersHere) {
            StackPane playerToken = new StackPane();

            if (baseImage != null && colorImage != null) {
                // Base layer (bottom, untinted) via Canvas
                Canvas baseCanvas = new Canvas(spriteSize, spriteSize);
                GraphicsContext gcBase = baseCanvas.getGraphicsContext2D();
                gcBase.setImageSmoothing(false);
                gcBase.drawImage(baseImage, 0, 0, spriteSize, spriteSize);

                // Color layer (top, tinted) via Canvas
                Canvas colorCanvas = new Canvas(spriteSize, spriteSize);
                GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
                gcColor.setImageSmoothing(false);
                gcColor.drawImage(colorImage, 0, 0, spriteSize, spriteSize);

                // Tint effect
                Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
                lighting.setSurfaceScale(0.0); // Flat lighting for tint effect
                colorCanvas.setEffect(lighting);

                playerToken.getChildren().addAll(baseCanvas, colorCanvas);
            } else {
                Circle fallback = new Circle(spriteSize / 2);
                fallback.setFill(getColorFromHex(player.getColour()));
                fallback.setStroke(Color.WHITE);
                fallback.setStrokeWidth(2);
                playerToken.getChildren().add(fallback);
            }

            // Centered Offset for multiple players
            double offsetX = (count - (playersHere.size() - 1) / 2.0) * spacing;
            playerToken.setTranslateX(offsetX);

            cell.getChildren().add(playerToken);
            count++;
        }
    }

    // ============================================
    //  ANIMATIONS
    // ============================================

    private void animateDiceResult() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), diceResultLabel);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    private void animateSnowballThrow() {
        // Flash the grid briefly
        FadeTransition flash = new FadeTransition(Duration.millis(100), grid);
        flash.setFromValue(1.0);
        flash.setToValue(0.7);
        flash.setCycleCount(4);
        flash.setAutoReverse(true);
        flash.play();
    }

    private void animateWarFlash() {
        // Red flash effect for snowball war
        FadeTransition flash = new FadeTransition(Duration.millis(150), grid);
        flash.setFromValue(1.0);
        flash.setToValue(0.5);
        flash.setCycleCount(6);
        flash.setAutoReverse(true);
        flash.play();
    }

    private void animateWin() {
        // Celebration scale bounce on the title
        ScaleTransition bounce = new ScaleTransition(Duration.millis(500), gameTitleText);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.3);
        bounce.setToY(1.3);
        bounce.setCycleCount(6);
        bounce.setAutoReverse(true);
        bounce.play();
    }

    // ============================================
    //  UTILITIES
    // ============================================

    private Player getCurrentPlayer() {
        return (Player) turnController.getCurrentTurn();
    }

    private void logEvent(String message) {
        eventLog.appendText(message + "\n");
        // Auto-scroll to bottom
        eventLog.setScrollTop(Double.MAX_VALUE);
    }

    private void disableActions() {
        rollDiceButton.setDisable(true);
        rollFastDiceButton.setDisable(true);
        rollSlowDiceButton.setDisable(true);
        throwSnowballButton.setDisable(true);
    }

    private void enableActions() {
        rollDiceButton.setDisable(false);
        // Fast/slow dice buttons depend on inventory (handled in updateHUD)
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String padColor(String hex) {
        if (hex == null) return "FFFFFF";
        hex = hex.trim();
        if (hex.length() < 6) {
            hex = "0".repeat(6 - hex.length()) + hex;
        }
        return hex;
    }

    private Color getColorFromHex(String hex) {
        try {
            return Color.web("#" + padColor(hex));
        } catch (Exception e) {
            return Color.GRAY;
        }
    }

    public Board getCurrentGameBoard() {
        return this.gameBoard;
    }
}