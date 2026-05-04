package controller.ui;

import model.board.Board;
import model.board.SquareType;
import model.config.GameSetupConfig;
import model.game.SaveLoadService;
import model.game.TurnController;
import model.entity.Entity;
import model.entity.Player;
import model.entity.Seal;
import model.item.Inventory;
import model.item.ObjectType;
import model.item.objects.Dice;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameBoardController {

    // --- FXML Bindings ---
    @FXML private StackPane mainStack;
    @FXML private BorderPane rootPane;
    @FXML private StackPane boardContainer;
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
    private model.game.GameManager gameManager;
    private Dice defaultDice;
    private Dice fastDice;
    private Dice slowDice;
    private Seal seal;
    private boolean sealEnabled;
    private boolean gameOver;
    private StackPane animationOverlay;

    // --- Sprites ---
    private final Image baseImage;
    private final Image colorImage;
    private final Map<String, Image> resourceCache = new HashMap<>();

    public GameBoardController() {
        baseImage  = loadImage("/assets/sprites/entities/player/player_idle.png");
        colorImage = loadImage("/assets/sprites/entities/player/player_idle_colour.png");
        gameOver = false;
    }

    private Image loadImage(String path) {
        if (resourceCache.containsKey(path)) {
            return resourceCache.get(path);
        }

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            Image img = new Image(is, 0, 0, true, false);
            resourceCache.put(path, img);
            return img;
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            return null;
        }
    }

    @FXML
    public void initialize() {
        gameManager = new model.game.GameManager("LOCAL_MATCH", 0);
        gameBoard = new Board();
        gameManager.setBoard(gameBoard);
        
        if (GameSetupConfig.isLoadedGame()) {
            gameBoard.loadBoard(GameSetupConfig.getLoadedBoardState());
            turnController = new TurnController();
            gameManager.setTurnController(turnController);
            initializePlayers();
            turnController.setCurrentTurn(GameSetupConfig.getLoadedTurnIndex());

            sealEnabled = GameSetupConfig.isSealEnabled();
            if (sealEnabled) {
                seal = new Seal();
                seal.setBoard(gameBoard);
                java.util.Map<String, Object> sealState = GameSetupConfig.getLoadedSealState();
                if (sealState != null) {
                    seal.setSquare(((Number) sealState.get("square")).intValue());
                    seal.setBlockedTurns(((Number) sealState.get("blockedTurns")).intValue());
                }
                gameManager.setSeal(seal);
                sealStatusBox.setVisible(true);
                sealStatusBox.setManaged(true);
            }
        } else {
            gameBoard.createNewBoard();
            turnController = new TurnController();
            gameManager.setTurnController(turnController);
            initializePlayers();

            sealEnabled = GameSetupConfig.isSealEnabled();
            if (sealEnabled) {
                seal = new Seal();
                seal.setBoard(gameBoard);
                gameManager.setSeal(seal);
                sealStatusBox.setVisible(true);
                sealStatusBox.setManaged(true);
            }
        }

        defaultDice = new Dice(ObjectType.DICE); // Default: 1-6
        fastDice = new Dice(ObjectType.FASTDICE);     // 5-10
        slowDice = new Dice(ObjectType.SLOWDICE);     // 1-3

        drawBoard();
        applyCss();
        updateHUD();
        
        if (GameSetupConfig.isLoadedGame()) {
            logEvent("📂 Game loaded successfully! " + turnController.getAllPlayers().size() + " players.");
            GameSetupConfig.setLoadedGame(false); 
        } else {
            logEvent("🐧 Game started! " + turnController.getAllPlayers().size() + " players.");
        }
        logEvent("🎯 " + getCurrentPlayer().getName() + "'s turn!");
        
        // Start background music
        model.game.SoundManager.getInstance().startBackgroundMusic();
        
        // Setup Animation Overlay
        animationOverlay = new StackPane();
        animationOverlay.setMouseTransparent(true);
        animationOverlay.setStyle("-fx-background-color: transparent;");
        mainStack.getChildren().add(animationOverlay);
    }

    private void initializePlayers() {
        List<Player> players = GameSetupConfig.getPlayers();
        if (players != null && !players.isEmpty()) {
            for (Player player : players) {
                player.setBoard(gameBoard);
                if (!GameSetupConfig.isLoadedGame()) {
                    player.setSquare(0);
                }
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
            mainStack.getStylesheets().clear();
            java.net.URL cssUrl = getClass().getResource("/assets/css/gameBoardStyle.css");
            if (cssUrl != null) {
                mainStack.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Could not load CSS: /assets/css/gameBoardStyle.css not found.");
            }
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
            
            model.game.ActionResult msg = gameManager.getPlayerManager().throwSnowball(current, target);
            logEvent(formatActionMessage(msg));

            // Animate snowball effect
            model.game.SoundManager.getInstance().playSnowballSound();
            animateSnowballThrow();
            
            drawBoard();
            updateHUD();
            
            // End turn after throwing
            endTurn();
        }
    }

    @FXML
    private void saveGame() {
        // 1. Crear el diálogo de entrada de texto
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("MiPartida");
        dialog.setTitle("Guardar Partida");
        dialog.setHeaderText("Introduce un nombre para identificar tu partida:");
        dialog.setContentText("Nombre:");

        // 2. Mostrar y esperar respuesta
        java.util.Optional<String> result = dialog.showAndWait();

        // 3. Si el usuario pulsó "Aceptar" y escribió algo
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // Llamamos al servicio con el nombre elegido
                boolean ok = SaveLoadService.saveGame(name, this.gameBoard, this.turnController, this.seal);
                
                if (ok) {
                    mostrarAlerta("Éxito", "Partida '" + name + "' guardada correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la partida. Quizás el nombre ya existe.");
                }
            }
        });
    }

    private void mostrarAlerta(String titulo, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ============================================
    //  GAME LOGIC
    // ============================================

    private void processDiceRoll(int diceResult, String diceType) {
        disableActions();
        model.game.SoundManager.getInstance().playDiceSound();
        Player current = getCurrentPlayer();
        int startSquare = current.getSquareIndex();
        
        // Animate dice result display
        diceResultLabel.setText("🎲 " + diceResult);
        animateDiceResult();
        
        logEvent("🎲 " + current.getName() + " rolled " + diceResult + " (" + diceType + " die)");

        // Animate player movement step-by-step
        animatePlayerMovement(current, diceResult, () -> {
            // After animation finishes, execute the actual game logic
            
            // Reset player to start position so moveForward calculates correctly from origin
            current.setSquare(startSquare);
            
            // Logical move
            model.game.ActionResult moveResult = gameManager.playTurn(diceResult);
            logEvent(formatActionMessage(moveResult));
            
            drawBoard();

            // Check win
            if (gameManager.isGameOver()) {
                handleWin(gameManager.getWinner());
                return;
            }

            // Cinematic check based on result
            if (moveResult != null) {
                switch(moveResult.getType()) {
                    case BEAR_ATTACK:
                        model.game.SoundManager.getInstance().playBearSound();
                        showBearAnimation();
                        break;
                    case EVENT:
                        model.game.SoundManager.getInstance().playEventSound();
                        break;
                    default:
                        break;
                }
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
                model.game.SoundManager.getInstance().playSealSound();
                showSealAnimation();
                model.game.ActionResult sealResult = gameManager.getPlayerManager().handleSealInteraction(seal, current);
                logEvent(formatActionMessage(sealResult));
                drawBoard();
            }

            updateHUD();
            
            // End turn
            endTurn();
        });
    }
    

    private void animatePlayerMovement(Player player, int steps, Runnable onFinished) {
        int startPos = player.getSquareIndex();
        Timeline timeline = new Timeline();
        double stepDuration = 250; // milliseconds per step

        for (int i = 1; i <= steps; i++) {
            int nextStep = i;
            KeyFrame kf = new KeyFrame(Duration.millis(i * stepDuration), e -> {
                // Visually update player position (clamped to board end)
                int visualPos = Math.min(startPos + nextStep, Board.MAX_SQUARES - 1);
                player.setSquare(visualPos);
                drawBoard();
            });
            timeline.getKeyFrames().add(kf);
        }
        
        timeline.setOnFinished(e -> onFinished.run());
        timeline.play();
    }

    private void endTurn() {
        if (gameOver) return;

        // Advance turn
        turnController.nextTurn();
        
        // If seal is enabled, play seal turn between player turns
        if (sealEnabled && seal != null) {
            playSealTurn();
        }

        // If the seal won during its turn, stop here
        if (gameOver) return;

        // Update HUD for next player
        updateHUD();
        enableActions();
        
        Player next = getCurrentPlayer();
        logEvent("──────────────────");
        logEvent("🎯 " + next.getName() + "'s turn!");
    }

    private void playSealTurn() {
        List<Player> humanPlayers = turnController.getHumanPlayers();
        List<model.game.ActionResult> sealLog = seal.playTurn(humanPlayers);
        for (model.game.ActionResult msg : sealLog) {
            logEvent(formatActionMessage(msg));
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
        
        model.game.ActionResult warResult = gameManager.getPlayerManager().snowballWar(attacker, defender);
        logEvent(formatActionMessage(warResult));
        
        model.game.SoundManager.getInstance().playSnowballSound();
        // Flash animation for war
        animateWarFlash();
    }

    private String formatActionMessage(model.game.ActionResult res) {
        if (res == null) return "";
        switch(res.getType()) {
            case ICE_HOLE: return "🕳️ " + res.getPlayerName() + " fell into an ice hole! Sent back to square " + res.getValue();
            case SLED_FOUND: return "🛷 " + res.getPlayerName() + " found a sled! Zooming forward to square " + res.getValue();
            case SLED_LAST: return "🛷 " + res.getPlayerName() + " found the last sled. Nothing happens.";
            case BEAR_SAFE: return "🐻 " + res.getPlayerName() + " bribed the bear with a fish! 🐟 Safe!";
            case BEAR_ATTACK: return "🐻💥 " + res.getPlayerName() + " was attacked by the bear! No fish to bribe! Back to START!";
            case EVENT: return "❓ " + res.getEventMessage();
            case BROKEN_FLOOR_FALL: return "💔 " + res.getPlayerName() + " was too heavy (" + res.getValue() + " items)! Fell through the broken floor! Back to START!";
            case BROKEN_FLOOR_CRACK: return "⚠️ " + res.getPlayerName() + " cracked the broken floor (" + res.getValue() + " items). Loses next turn!";
            case BROKEN_FLOOR_SAFE: return "✅ " + res.getPlayerName() + " crosses the broken floor safely (no items)!";
            case WIN: return "🎉 " + res.getPlayerName() + " reached the END! WINNER!";
            case START_SQUARE: return res.getPlayerName() + " is at the start.";
            case END_SQUARE: return res.getPlayerName() + " reached the END! 🎉";
            case NORMAL_SQUARE: 
                if (res.getEventMessage() != null) return res.getEventMessage();
                return res.getPlayerName() + " landed on a normal square.";
            case SEAL_BRIBED: return "🐟 " + res.getPlayerName() + " fed the seal a fish! It's blocked for 2 turns!";
            case SEAL_NO_FISH: return "❌ " + res.getPlayerName() + " has no fish to feed the seal!";
            case SEAL_HIT_HOLE: return "🦭💥 The seal hits " + res.getPlayerName() + " with its tail! Sent to ice hole at square " + res.getValue() + "!";
            case SEAL_HIT_START: return "🦭💥 The seal hits " + res.getPlayerName() + "! Sent back to start!";
            case SEAL_PASS: return "🦭 The seal passed through " + res.getPlayerName() + "'s square! Lost half inventory!";
            case SEAL_EATING: return "🦭😴 The seal is eating a fish and can't move. (" + res.getValue() + " turns left)";
            case SEAL_ACTIVE: return "🦭 The seal has finished eating and is dangerous again!";
            case SEAL_ROLL: return "🦭 The seal rolls: " + res.getValue();
            case SEAL_MOVE: return "🦭 The seal moves to square " + res.getValue();
            case SNOWBALL_WAR_EMPTY: return "⚔️ It's a tie! Both " + res.getPlayerName() + " and " + res.getTargetName() + " have no snowballs!";
            case SNOWBALL_WAR_WIN: return "⚔️ " + res.getPlayerName() + " wins! (" + res.getValue() + " balls) " + res.getTargetName() + " retreats " + res.getValue2() + " squares!";
            case SNOWBALL_WAR_TIE: return "⚔️ It's a tie! Both spend all snowballs. No one retreats.";
            case SNOWBALL_THROW: return "⛄ " + res.getPlayerName() + " threw a snowball at " + res.getTargetName() + "! " + res.getTargetName() + " goes back " + res.getValue() + " squares to " + res.getValue2() + "!";
            case DICE_ROLL: return "🎲 " + res.getPlayerName() + " rolled " + res.getValue();
            case MOVE: return "🚶 " + res.getPlayerName() + " moved to " + res.getValue();
            default: return "Activity from " + res.getPlayerName();
        }
    }

    // Old handleWin removed to fix duplicate method compilation error

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
        hotbar.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 0 0 4px 0;");

        // --- Player Info (Portrait + Name) ---
        VBox playerInfo = new VBox(5);
        playerInfo.setAlignment(Pos.CENTER);
        
        StackPane portrait = new StackPane();
        double targetSize = 48;
        
        if (player.getAvatarPath() != null) {
            try {
                Image customAvatar = new Image(player.getAvatarPath(), targetSize, targetSize, false, true);
                ImageView avatarView = new ImageView(customAvatar);
                
                // Add a nice border
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(targetSize, targetSize);
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                avatarView.setClip(clip);
                
                portrait.getChildren().add(avatarView);
            } catch (Exception e) {
                System.out.println("Could not load custom avatar, falling back to default.");
            }
        }
        
        // Fallback to default tinted sprite if no custom avatar or loading failed
        if (portrait.getChildren().isEmpty() && baseImage != null && colorImage != null) {
            double w = baseImage.getWidth();
            double h = baseImage.getHeight();
            double sx = w * 0.25;
            double sy = 0;
            double sw = w * 0.5;
            double sh = h * 0.5;
            
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
        
        // Priority: Snowball -> Fish -> FastDice -> SlowDice
        addSlotIfPresent(slots, inv, ObjectType.SNOWBALL, "/assets/sprites/objects/snowball.png");
        addSlotIfPresent(slots, inv, ObjectType.FISH, "/assets/sprites/objects/fish.png");
        addSlotIfPresent(slots, inv, ObjectType.FASTDICE, "/assets/sprites/objects/fastdice.png");
        addSlotIfPresent(slots, inv, ObjectType.SLOWDICE, "/assets/sprites/objects/slowdice.png");
        
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
    
    private void addSlotIfPresent(HBox container, Inventory inv, ObjectType type, String path) {
        int qty = inv.getObjectQuantity(type);
        if (qty > 0) {
            container.getChildren().add(createInventorySlot(loadImage(path), qty));
        }
    }
    
    private StackPane createInventorySlot(Image icon, int quantity) {
        StackPane slot = new StackPane();
        slot.getStyleClass().add("button"); // Use ice block style
        slot.setPrefSize(80, 80); // Bigger slot
        slot.setMaxSize(80, 80);
        
        if (quantity > 0 && icon != null) {
            double targetSize = 55; // Bigger icon
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

        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new javafx.geometry.Insets(0));
        if (!grid.getStyleClass().contains("game-grid")) {
            grid.getStyleClass().add("game-grid");
        }

        // Dynamic cell size that maintains square proportions
        DoubleBinding cellSize = Bindings.createDoubleBinding(
            () -> {
                double availableWidth = boardContainer.getWidth();
                double availableHeight = boardContainer.getHeight();
                if (availableWidth <= 0 || availableHeight <= 0) {
                    return 60.0; // Default fallback
                }
                return Math.min(availableWidth / cols, availableHeight / rows);
            },
            boardContainer.widthProperty(),
            boardContainer.heightProperty()
        );

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.prefWidthProperty().bind(cellSize);
            col.minWidthProperty().bind(cellSize);
            col.maxWidthProperty().bind(cellSize);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.prefHeightProperty().bind(cellSize);
            row.minHeightProperty().bind(cellSize);
            row.maxHeightProperty().bind(cellSize);
            grid.getRowConstraints().add(row);
        }

        // Bind grid size to maintain proportions
        grid.prefWidthProperty().bind(cellSize.multiply(cols));
        grid.prefHeightProperty().bind(cellSize.multiply(rows));
        grid.maxWidthProperty().bind(cellSize.multiply(cols));
        grid.maxHeightProperty().bind(cellSize.multiply(rows));

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
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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

        Image cellBackground = loadImage(getBackgroundImagePath(squareIndex));
        if (cellBackground != null) {
            ImageView bgView = new ImageView(cellBackground);
            bgView.setPreserveRatio(true);
            bgView.setSmooth(false);
            bgView.setCache(true);
            bgView.fitWidthProperty().bind(cell.widthProperty());
            bgView.fitHeightProperty().bind(cell.heightProperty());
            cell.getChildren().add(bgView);
        }

        Image overlayImage = getForegroundImageForType(type);
        if (overlayImage != null) {
            ImageView overlayView = new ImageView(overlayImage);
            overlayView.setPreserveRatio(true);
            overlayView.setSmooth(false);
            overlayView.setCache(true);
            overlayView.fitWidthProperty().bind(cell.widthProperty());
            overlayView.fitHeightProperty().bind(cell.heightProperty());
            cell.getChildren().add(overlayView);
        }

        addPlayerSpritesToCell(cell, squareIndex);

        if (sealEnabled && seal != null && seal.getSquareIndex() == squareIndex) {
            Label sealLabel = new Label("🦭");
            sealLabel.setStyle("-fx-font-size: 18;");
            StackPane.setAlignment(sealLabel, Pos.BOTTOM_RIGHT);
            cell.getChildren().add(sealLabel);
        }

        return cell;
    }

    private String getBackgroundImagePath(int squareIndex) {
        // Special cases
        if (squareIndex == 0) {
            return "/assets/sprites/squares/background/Square-0.png";
        }
        if (squareIndex == Board.MAX_SQUARES - 1) {
            return "/assets/sprites/squares/background/Square-6.png";
        }

        int cols = Board.widthBoard;
        int row = squareIndex / cols;
        int indexInRow = squareIndex % cols;

        // Check if it's first or last column in the actual row direction
        boolean isFirstInRow = (indexInRow == 0);
        boolean isLastInRow = (indexInRow == cols - 1);
        boolean isEvenRow = (row % 2 == 0);

        // Lateral caselles (left and right columns)
        if (isFirstInRow) {
            // First in row is always left column
            return isEvenRow ? "/assets/sprites/squares/background/Square-5.png" : "/assets/sprites/squares/background/Square-4.png";
        }
        if (isLastInRow) {
            // Last in row is always right column
            return isEvenRow ? "/assets/sprites/squares/background/Square-3.png" : "/assets/sprites/squares/background/Square-2.png";
        }

        // Default: central caselles are always horizontal
        return "/assets/sprites/squares/background/Square-1.png";
    }

    private Image getForegroundImageForType(SquareType type) {
        if (type == null || type == SquareType.NORMAL) {
            return null;
        }

        String imageName = "/assets/sprites/squares/foreground/" + type.name() + ".png";
        return loadImage(imageName);
    }

    private void addPlayerSpritesToCell(StackPane cell, int squareIndex) {
        List<Player> playersHere = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && e.getSquareIndex() == squareIndex) {
                playersHere.add((Player) e);
            }
        }
        
        if (playersHere.isEmpty()) return;

        int playerCount = 0;
        for (Player player : playersHere) {
            StackPane playerToken = new StackPane();
            final int playerIndex = playerCount;

            if (baseImage != null && colorImage != null) {
                Canvas baseCanvas = new Canvas();
                GraphicsContext gcBase = baseCanvas.getGraphicsContext2D();
                gcBase.setImageSmoothing(false);
                
                Canvas colorCanvas = new Canvas();
                GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
                gcColor.setImageSmoothing(false);

                Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
                lighting.setSurfaceScale(0.0);
                colorCanvas.setEffect(lighting);

                DoubleBinding spriteSize = Bindings.createDoubleBinding(
                    () -> cell.getWidth() * 0.25,
                    cell.widthProperty()
                );

                baseCanvas.widthProperty().bind(spriteSize);
                baseCanvas.heightProperty().bind(spriteSize);
                colorCanvas.widthProperty().bind(spriteSize);
                colorCanvas.heightProperty().bind(spriteSize);

                spriteSize.addListener((obs, oldVal, newVal) -> {
                    double size = newVal.doubleValue();
                    gcBase.clearRect(0, 0, size, size);
                    gcBase.drawImage(baseImage, 0, 0, size, size);
                    gcColor.clearRect(0, 0, size, size);
                    gcColor.drawImage(colorImage, 0, 0, size, size);
                });

                playerToken.getChildren().addAll(baseCanvas, colorCanvas);
            } else {
                Circle fallback = new Circle();
                fallback.radiusProperty().bind(Bindings.createDoubleBinding(
                    () -> cell.getWidth() * 0.125,
                    cell.widthProperty()
                ));
                fallback.setFill(getColorFromHex(player.getColour()));
                fallback.setStroke(Color.WHITE);
                fallback.setStrokeWidth(2);
                playerToken.getChildren().add(fallback);
            }

            DoubleBinding spacing = Bindings.createDoubleBinding(
                () -> cell.getWidth() * 0.15,
                cell.widthProperty()
            );

            final int count = playersHere.size();
            playerToken.translateXProperty().bind(Bindings.createDoubleBinding(
                () -> (playerIndex - (count - 1) / 2.0) * spacing.get(),
                spacing
            ));

            cell.getChildren().add(playerToken);
            playerCount++;
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
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
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

    public void handleWin(Player winner) {
        gameOver = true;
        disableActions();
        model.game.SoundManager.getInstance().stopBackgroundMusic();
        logEvent("🏆 GAME OVER! " + winner.getName() + " HAS WON THE GAME! 🏆");
        drawBoard();
        showWinAnimation(winner);
        showAlert("🏆 Game Over 🏆", winner.getName() + " has reached the end and won the game!\n\nCongratulations!");
    }

    // ============================================
    //  CINEMATIC ANIMATIONS
    // ============================================

    private void showBearAnimation() {
        Label bearLabel = new Label("🐻");
        bearLabel.setFont(new Font("System", 150));
        animationOverlay.getChildren().add(bearLabel);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(300), bearLabel);
        st.setFromX(0); st.setFromY(0);
        st.setToX(2); st.setToY(2);
        
        FadeTransition ft = new FadeTransition(Duration.millis(500), bearLabel);
        ft.setDelay(Duration.millis(800));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        
        ft.setOnFinished(e -> animationOverlay.getChildren().remove(bearLabel));
        st.play();
        ft.play();
    }

    private void showSealAnimation() {
        Label sealLabel = new Label("🦭");
        sealLabel.setFont(new Font("System", 120));
        animationOverlay.getChildren().add(sealLabel);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), sealLabel);
        tt.setFromX(800);
        tt.setToX(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), sealLabel);
        ft.setDelay(Duration.millis(1000));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        
        ft.setOnFinished(e -> animationOverlay.getChildren().remove(sealLabel));
        tt.play();
        ft.play();
    }

    private void showWinAnimation(Player winner) {
        VBox winBox = new VBox(20);
        winBox.setAlignment(Pos.CENTER);
        winBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 50;");
        
        Label crown = new Label("👑");
        crown.setFont(new Font("System", 100));
        
        Label title = new Label(winner.getName() + " WINS!");
        title.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 60px; -fx-text-fill: gold; -fx-font-weight: bold;");
        
        winBox.getChildren().addAll(crown, title);
        animationOverlay.getChildren().add(winBox);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), winBox);
        st.setFromX(0.5); st.setFromY(0.5);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
    }

    public Board getCurrentGameBoard() {
        return this.gameBoard;
    }
}