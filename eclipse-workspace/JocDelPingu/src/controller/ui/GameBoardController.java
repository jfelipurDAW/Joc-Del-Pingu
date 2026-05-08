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
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
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

    @FXML private Button historyButton;

    // --- Event history backing list (no on-screen ticker; the full
    //     log is reachable through the "📜 History" button on the action bar)
    private final java.util.List<String> eventHistoryFull = new ArrayList<>();

    // --- Debug mode (Ctrl+Shift+D) ---
    // Lets the developer drag a player token to any cell and pre-set the
    // value of the next dice roll. Off by default, no effect on normal play.
    private boolean debugMode = false;
    private Integer debugForcedDice = null;
    private HBox debugPanel;
    private Label debugBannerLabel;

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
    private String winner;

    // --- Sprites ---
    // Player: 4 sprite layers (base/colour x left/right), plus damaged frames
    private final Image baseRightImage;
    private final Image colorRightImage;
    private final Image baseLeftImage;
    private final Image colorLeftImage;
    private final Image damagedRightImage;
    private final Image damagedLeftImage;
    // Seal: idle facing left/right
    private final Image sealRightImage;
    private final Image sealLeftImage;

    private final Map<String, Image> resourceCache = new HashMap<>();

    // Guard against re-entrant drawBoard calls
    private boolean isRedrawing = false;

    public GameBoardController() {
        baseRightImage    = loadImage("/assets/sprites/entities/player/player_idle_right.png");
        colorRightImage   = loadImage("/assets/sprites/entities/player/player_idle_colour_right.png");
        baseLeftImage     = loadImage("/assets/sprites/entities/player/player_idle_left.png");
        colorLeftImage    = loadImage("/assets/sprites/entities/player/player_idle_colour_left.png");
        damagedRightImage = loadImage("/assets/sprites/entities/player/player_damaged_right.png");
        damagedLeftImage  = loadImage("/assets/sprites/entities/player/player_damaged_left.png");
        sealRightImage    = loadImage("/assets/sprites/entities/seal/seal_idle_right.png");
        sealLeftImage     = loadImage("/assets/sprites/entities/seal/seal_idle_left.png");
        gameOver = false;
    }

    private Image loadImage(String path) {
        if (resourceCache.containsKey(path)) return resourceCache.get(path);
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) { System.err.println("Resource not found: " + path); return null; }
            Image img = new Image(is, 0, 0, true, false); // natural size, nearest-neighbour
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
                Map<String, Object> sealState = GameSetupConfig.getLoadedSealState();
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

        defaultDice = new Dice(ObjectType.DICE);
        fastDice    = new Dice(ObjectType.FASTDICE);
        slowDice    = new Dice(ObjectType.SLOWDICE);

        // Overlays must be created before any logEvent / drawBoard call —
        // both can be invoked from within initialize() (game-start log,
        // resize listener) and they rely on these containers existing.
        animationOverlay = new StackPane();
        animationOverlay.setMouseTransparent(true);
        animationOverlay.setStyle("-fx-background-color: transparent;");
        mainStack.getChildren().add(animationOverlay);

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

        // Music has likely been playing since the main menu — make sure it is
        // running. Volume stays at the menu level for the whole game so the
        // background track sounds the same as on the main menu.
        model.game.SoundManager.getInstance().startBackgroundMusic();
        model.game.SoundManager.getInstance().restoreMenuVolume();

        // Redraw the board once the container has its real layout size,
        // and again whenever the window is resized (width OR height) — no bindings, no loop.
        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0) drawBoard();
        };
        boardContainer.widthProperty().addListener(resizeListener);
        boardContainer.heightProperty().addListener(resizeListener);

        // Ctrl+Shift+D toggles a developer debug mode (drag pingu, force dice).
        // Registered on the scene as soon as the FXML is attached.
        mainStack.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
                    if (ev.isControlDown() && ev.isShiftDown() && ev.getCode() == KeyCode.D) {
                        toggleDebugMode();
                        ev.consume();
                    }
                });
            }
        });
    }

    // ============================================
    //  DEBUG MODE  (Ctrl+Shift+D)
    // ============================================

    private void toggleDebugMode() {
        debugMode = !debugMode;
        if (debugPanel == null) {
            buildDebugPanel();
        }
        debugPanel.setVisible(debugMode);
        debugPanel.setManaged(debugMode);
        if (!debugMode) {
            debugForcedDice = null;
            updateDebugBanner();
        }
        // Redraw so player tokens pick up (or drop) drag handlers/cursor.
        drawBoard();
    }

    private void buildDebugPanel() {
        debugBannerLabel = new Label("🛠 DEBUG ON  ");
        debugBannerLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: 900; -fx-font-size: 14px;");

        Label forceLbl = new Label("Force next roll:");
        forceLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        TextField diceField = new TextField();
        diceField.setPrefWidth(60);
        diceField.setPromptText("1-50");

        Button setBtn = new Button("Set");
        setBtn.setOnAction(e -> {
            try {
                int v = Integer.parseInt(diceField.getText().trim());
                if (v >= 1 && v <= 50) {
                    debugForcedDice = v;
                    updateDebugBanner();
                    diceField.clear();
                }
            } catch (NumberFormatException ignored) {
                // silently ignore invalid input — debug-only UI
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            debugForcedDice = null;
            updateDebugBanner();
        });

        Label hint = new Label("    Drag any 🐧 to teleport.");
        hint.setStyle("-fx-text-fill: #a0c4ff; -fx-font-size: 12px;");

        debugPanel = new HBox(8, debugBannerLabel, forceLbl, diceField, setBtn, clearBtn, hint);
        debugPanel.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 6 14;");
        debugPanel.setAlignment(Pos.CENTER_LEFT);
        debugPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        debugPanel.setMouseTransparent(false);
        debugPanel.setVisible(false);
        debugPanel.setManaged(false);

        StackPane.setAlignment(debugPanel, Pos.TOP_CENTER);
        StackPane.setMargin(debugPanel, new javafx.geometry.Insets(2, 0, 0, 0));
        mainStack.getChildren().add(debugPanel);
    }

    private void updateDebugBanner() {
        if (debugBannerLabel != null) {
            debugBannerLabel.setText(debugForcedDice != null
                ? "🛠 DEBUG ON — next: " + debugForcedDice + "  "
                : "🛠 DEBUG ON  ");
        }
    }

    /**
     * Returns either the forced debug value (and consumes it) or the dice's
     * normal random roll. Used by all three roll handlers.
     */
    private int rollOrForce(model.item.objects.Dice dice) {
        if (debugForcedDice != null) {
            int v = debugForcedDice;
            debugForcedDice = null;
            updateDebugBanner();
            return v;
        }
        return dice.roll();
    }

    /**
     * Attaches mouse-drag handlers to a player token while debug mode is on.
     * On release, the cell under the cursor is computed and the player is
     * teleported there. The board is then redrawn from scratch.
     */
    private void attachDebugDragHandlers(StackPane playerToken, Player player) {
        playerToken.setCursor(Cursor.OPEN_HAND);
        final double[] startTranslate = new double[2];
        final double[] startMouse = new double[2];

        playerToken.setOnMousePressed(e -> {
            if (debugMode) {
                startTranslate[0] = playerToken.getTranslateX();
                startTranslate[1] = playerToken.getTranslateY();
                startMouse[0] = e.getSceneX();
                startMouse[1] = e.getSceneY();
                playerToken.setCursor(Cursor.CLOSED_HAND);
                // Lift the parent cell to the front of the GridPane so the
                // dragged token isn't covered by later cells when moving
                // forward (GridPane renders children in insertion order, so
                // squares with a higher index were on top by default).
                Node parentCell = playerToken.getParent();
                if (parentCell != null) {
                    parentCell.toFront();
                }
                playerToken.toFront();
                e.consume();
            }
        });

        playerToken.setOnMouseDragged(e -> {
            if (debugMode) {
                double dx = e.getSceneX() - startMouse[0];
                double dy = e.getSceneY() - startMouse[1];
                playerToken.setTranslateX(startTranslate[0] + dx);
                playerToken.setTranslateY(startTranslate[1] + dy);
                e.consume();
            }
        });

        playerToken.setOnMouseReleased(e -> {
            if (debugMode) {
                int targetIndex = findCellIndexAt(e.getSceneX(), e.getSceneY());
                if (targetIndex >= 0) {
                    player.setSquare(targetIndex);
                    logEvent("🛠 DEBUG: " + player.getName() + " teleported to square " + targetIndex);
                }
                playerToken.setCursor(Cursor.OPEN_HAND);
                drawBoard();
                e.consume();
            }
        });
    }

    /**
     * Finds the snake-pattern square index of the cell under the given scene
     * coordinates, or -1 if no cell contains the point.
     */
    private int findCellIndexAt(double sceneX, double sceneY) {
        int cols = Board.widthBoard;
        for (Node child : grid.getChildren()) {
            Bounds b = child.localToScene(child.getBoundsInLocal());
            if (b.contains(sceneX, sceneY)) {
                Integer col = GridPane.getColumnIndex(child);
                Integer row = GridPane.getRowIndex(child);
                if (col != null && row != null) {
                    return row * cols + (row % 2 == 0 ? col : cols - 1 - col);
                }
            }
        }
        return -1;
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
            if (cssUrl != null) mainStack.getStylesheets().add(cssUrl.toExternalForm());
            else System.err.println("Could not load CSS: /assets/css/gameBoardStyle.css not found.");
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
    }

    // ============================================
    //  ACTION HANDLERS
    // ============================================

    @FXML
    private void rollDice() {
        if (!gameOver) {
            int result = rollOrForce(defaultDice);
            processDiceRoll(result, "Normal");
        }
    }

    @FXML
    private void rollFastDice() {
        if (!gameOver) {
            Player current = getCurrentPlayer();
            if (current.getInventory().getObjectQuantity(ObjectType.FASTDICE) > 0) {
                current.getInventory().useObject(ObjectType.FASTDICE, 1);
                int result = rollOrForce(fastDice);
                processDiceRoll(result, "Fast");
            } else {
                showAlert("No Fast Dice", "You don't have any fast dice! Land on event squares to find some.");
            }
        }
    }

    @FXML
    private void rollSlowDice() {
        if (!gameOver) {
            Player current = getCurrentPlayer();
            if (current.getInventory().getObjectQuantity(ObjectType.SLOWDICE) > 0) {
                current.getInventory().useObject(ObjectType.SLOWDICE, 1);
                int result = rollOrForce(slowDice);
                processDiceRoll(result, "Slow");
            } else {
                showAlert("No Slow Dice", "You don't have any slow dice in your inventory!");
            }
        }
    }

    @FXML
    private void throwSnowball() {
        if (!gameOver) {
            beginThrowSnowball();
        }
    }

    private void beginThrowSnowball() {
        Player current = getCurrentPlayer();

        if (current.getInventory().getObjectQuantity(ObjectType.SNOWBALL) <= 0) {
            showAlert("No Snowballs", "You don't have any snowballs! Land on event squares to find some.");
        } else {
            List<Player> targets = new ArrayList<>();
            for (Entity e : turnController.getAllPlayers()) {
                if (e instanceof Player && e != current) targets.add((Player) e);
            }

            if (targets.isEmpty()) {
                showAlert("No Targets", "There are no other players to throw snowballs at!");
            } else {
                promptSnowballTargetAndThrow(current, targets);
            }
        }
    }

    private void promptSnowballTargetAndThrow(Player current, List<Player> targets) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Throw Snowball ⛄");
        dialog.setHeaderText("Choose a target to hit with a snowball!");
        dialog.setContentText("Target:");

        List<String> targetNames = new ArrayList<>();
        for (Player p : targets) targetNames.add(p.getName() + " (Square " + p.getSquareIndex() + ")");
        dialog.getItems().addAll(targetNames);
        dialog.setSelectedItem(targetNames.get(0));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            int selectedIndex = targetNames.indexOf(result.get());
            Player target = targets.get(selectedIndex);

            model.game.ActionResult msg = gameManager.getPlayerManager().throwSnowball(current, target);
            String snowMsg = formatActionMessage(msg);
            logEvent(snowMsg);
            current.recordEvent(snowMsg);
            target.recordEvent(snowMsg);
            flashDamage(target);

            model.game.SoundManager.getInstance().playSnowballSound();
            animateSnowballThrow();
            drawBoard();
            updateHUD();
            endTurn();
        }
    }

    @FXML
    private void saveGame() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("MiPartida");
        dialog.setTitle("Guardar Partida");
        dialog.setHeaderText("Introduce un nombre para identificar tu partida:");
        dialog.setContentText("Nombre:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean ok = SaveLoadService.saveGame(name, this.gameBoard, this.turnController, this.seal, this.winner);
                if (ok) mostrarAlerta("Éxito", "Partida '" + name + "' guardada correctamente.");
                else     mostrarAlerta("Error", "No se pudo guardar la partida. Quizás el nombre ya existe.");
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
        Player current = getCurrentPlayer();
        int startSquare = current.getSquareIndex();

        logEvent("🎲 " + current.getName() + " rolled " + diceResult + " (" + diceType + " die)");

        // Delay the SFX so it lines up with the rolling-dice GIF instead of
        // firing the moment the button is clicked.
        PauseTransition diceSoundDelay = new PauseTransition(Duration.millis(1500));
        diceSoundDelay.setOnFinished(e -> model.game.SoundManager.getInstance().playDiceSound());
        diceSoundDelay.play();

        showDiceAnimation(() -> {
            showDiceResultBadge(diceResult, () ->
                runDiceMovement(current, startSquare, diceResult, diceType)
            );
        });
    }

    private void runDiceMovement(Player current, int startSquare, int diceResult, String diceType) {
        animatePlayerMovement(current, diceResult, () -> {
            current.setSquare(startSquare);
            model.game.ActionResult moveResult = gameManager.playTurn(diceResult);
            String moveMsg = formatActionMessage(moveResult);
            logEvent(moveMsg);
            // Persist this event in the player's history so it survives save/load
            current.recordEvent("🎲" + diceResult + " (" + diceType + ") → " + moveMsg);
            drawBoard();

            if (gameManager.isGameOver()) {
                handleWin(gameManager.getWinner());
            } else {
                if (moveResult != null) {
                    switch (moveResult.getType()) {
                        case BEAR_ATTACK:
                            model.game.SoundManager.getInstance().playBearSound();
                            showBearAnimation();
                            flashDamage(current);
                            break;
                        case ICE_HOLE:
                        case BROKEN_FLOOR_FALL:
                        case BROKEN_FLOOR_LOSE_ITEM:
                            flashDamage(current);
                            break;
                        case EVENT:
                            model.game.SoundManager.getInstance().playEventSound();
                            break;
                        default: break;
                    }
                }

                List<Player> collisions = turnController.getPlayersAtSquare(current.getSquareIndex(), current);
                if (!collisions.isEmpty()) {
                    for (Player other : collisions) handlePlayerWar(current, other);
                    drawBoard();
                }

                if (sealEnabled && seal != null && seal.getSquareIndex() == current.getSquareIndex()) {
                    model.game.SoundManager.getInstance().playSealSound();
                    showSealAnimation();
                    model.game.ActionResult sealResult = gameManager.getPlayerManager().handleSealInteraction(seal, current);
                    String sealMsg = formatActionMessage(sealResult);
                    logEvent(sealMsg);
                    current.recordEvent(sealMsg);
                    if (sealResult != null) {
                        switch (sealResult.getType()) {
                            case SEAL_HIT_HOLE:
                            case SEAL_HIT_START:
                            case SEAL_PASS:
                                flashDamage(current);
                                break;
                            default: break;
                        }
                    }
                    drawBoard();
                }

                updateHUD();
                endTurn();
            }
        });
    }

    /**
     * Briefly mark an entity as damaged → renders the damaged sprite for ~450ms
     * and then clears the flag so the idle sprite returns automatically.
     */
    private void flashDamage(Entity entity) {
        if (entity != null) {
            entity.setDamaged(true);
            drawBoard();
            Timeline t = new Timeline(new KeyFrame(Duration.millis(450), e -> {
                entity.setDamaged(false);
                drawBoard();
            }));
            t.play();
        }
    }

    /** Look up a Player in the current game by name (used for seal-turn log results). */
    private Player findPlayerByName(String name) {
        if (name == null) return null;
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && name.equals(e.getName())) return (Player) e;
        }
        return null;
    }

    private void animatePlayerMovement(Player player, int steps, Runnable onFinished) {
        int startPos = player.getSquareIndex();
        Timeline timeline = new Timeline();
        double stepDuration = 250;

        for (int i = 1; i <= steps; i++) {
            int nextStep = i;
            KeyFrame kf = new KeyFrame(Duration.millis(i * stepDuration), e -> {
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
        if (!gameOver) {
            turnController.nextTurn();
            if (sealEnabled && seal != null && turnController.getCurrentTurnIndex() == 0) {
                playSealTurnAnimated();
            } else {
                startNextPlayerTurn();
            }
        }
    }

    private void startNextPlayerTurn() {
        if (!gameOver) {
            updateHUD();
            enableActions();
            Player next = getCurrentPlayer();
            logEvent("──────────────────");
            logEvent("🎯 " + next.getName() + "'s turn!");
        }
    }

    private void playSealTurnAnimated() {
        disableActions();
        logEvent("──────────────────");
        logEvent("🦭 SEAL'S TURN!");

        List<Player> humanPlayers = turnController.getHumanPlayers();
        List<model.game.ActionResult> sealLog = seal.playTurn(humanPlayers);

        showSealAnimation();
        model.game.SoundManager.getInstance().playSealSound();

        Timeline sealTimeline = new Timeline();
        double delay = 600;

        for (int i = 0; i < sealLog.size(); i++) {
            final model.game.ActionResult msg = sealLog.get(i);
            KeyFrame kf = new KeyFrame(Duration.millis(delay + i * 700), e -> {
                logEvent(formatActionMessage(msg));
                // Flash any player hit by the seal during its turn
                switch (msg.getType()) {
                    case SEAL_HIT_HOLE:
                    case SEAL_HIT_START:
                    case SEAL_PASS:
                        flashDamage(findPlayerByName(msg.getPlayerName()));
                        break;
                    default: break;
                }
                drawBoard();
                updateSealStatus();
            });
            sealTimeline.getKeyFrames().add(kf);
        }

        KeyFrame finalFrame = new KeyFrame(Duration.millis(delay + sealLog.size() * 700 + 300), e -> {
            if (seal.getSquareIndex() >= Board.MAX_SQUARES - 1) {
                logEvent("🦭 THE SEAL REACHED THE END! ALL PLAYERS LOSE!");
                gameOver = true;
                this.winner = "Seal";
                disableActions();
                model.game.SoundManager.getInstance().restoreMenuVolume();
                SaveLoadService.recordGameResult(turnController.getAllPlayers(), null);
                drawBoard();
                showSealWinAnimation();
            } else {
                drawBoard();
                updateSealStatus();
                startNextPlayerTurn();
            }
        });
        sealTimeline.getKeyFrames().add(finalFrame);
        sealTimeline.play();
    }

    private void handlePlayerWar(Player attacker, Player defender) {
        int atkBalls = attacker.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int defBalls = defender.getInventory().getObjectQuantity(ObjectType.SNOWBALL);

        if (atkBalls == 0 && defBalls == 0) {
            logEvent("⚔️ " + attacker.getName() + " and " + defender.getName() + " meet on the same square, but neither has snowballs!");
        } else {
            executeSnowballWar(attacker, defender);
        }
    }

    private void executeSnowballWar(Player attacker, Player defender) {
        logEvent("⚔️ SNOWBALL WAR! " + attacker.getName() + " vs " + defender.getName() + "!");
        model.game.ActionResult warResult = gameManager.getPlayerManager().snowballWar(attacker, defender);
        String warMsg = formatActionMessage(warResult);
        logEvent(warMsg);
        attacker.recordEvent(warMsg);
        defender.recordEvent(warMsg);
        // Flash the loser (the one whose name does NOT match the winner in the result)
        if (warResult != null && warResult.getType() == model.game.ActionResult.ActionType.SNOWBALL_WAR_WIN) {
            Player loser = attacker.getName().equals(warResult.getPlayerName()) ? defender : attacker;
            flashDamage(loser);
        }
        model.game.SoundManager.getInstance().playSnowballSound();
        animateWarFlash();
    }

    private String formatActionMessage(model.game.ActionResult res) {
        if (res == null) return "";
        switch (res.getType()) {
            case ICE_HOLE:          return "🕳️ " + res.getPlayerName() + " fell into an ice hole! Sent back to square " + res.getValue();
            case SLED_FOUND:        return "🛷 " + res.getPlayerName() + " found a sled! Zooming forward to square " + res.getValue();
            case SLED_LAST:         return "🛷 " + res.getPlayerName() + " found the last sled. Nothing happens.";
            case BEAR_SAFE:         return "🐻 " + res.getPlayerName() + " bribed the bear with a fish! 🐟 Safe!";
            case BEAR_ATTACK:       return "🐻💥 " + res.getPlayerName() + " was attacked by the bear! No fish to bribe! Back to START!";
            case EVENT:             return "❓ " + res.getEventMessage();
            case BROKEN_FLOOR_FALL: return "💔 " + res.getPlayerName() + " was too heavy (" + res.getValue() + " items)! Fell through the broken floor! Back to START!";
            case BROKEN_FLOOR_CRACK:return "⚠️ " + res.getPlayerName() + " cracked the broken floor (" + res.getValue() + " items). Loses next turn!";
            case BROKEN_FLOOR_LOSE_ITEM: {
                String lostItem = res.getEventMessage();
                return "💨 " + res.getPlayerName() + " stumbled on the broken floor and dropped a "
                        + (lostItem != null ? lostItem.toLowerCase() : "item") + "!";
            }
            case BROKEN_FLOOR_SAFE: return "✅ " + res.getPlayerName() + " crosses the broken floor safely (no items)!";
            case WIN:               return "🎉 " + res.getPlayerName() + " reached the END! WINNER!";
            case START_SQUARE:      return res.getPlayerName() + " is at the start.";
            case END_SQUARE:        return res.getPlayerName() + " reached the END! 🎉";
            case NORMAL_SQUARE:     return (res.getEventMessage() != null) ? res.getEventMessage() : res.getPlayerName() + " landed on a normal square.";
            case SEAL_BRIBED:       return "🐟 " + res.getPlayerName() + " fed the seal a fish! It's blocked for 2 turns!";
            case SEAL_NO_FISH:      return "❌ " + res.getPlayerName() + " has no fish to feed the seal!";
            case SEAL_HIT_HOLE:     return "🦭💥 The seal hits " + res.getPlayerName() + " with its tail! Sent to ice hole at square " + res.getValue() + "!";
            case SEAL_HIT_START:    return "🦭💥 The seal hits " + res.getPlayerName() + "! Sent back to start!";
            case SEAL_PASS:         return "🦭 The seal passed through " + res.getPlayerName() + "'s square! Lost half inventory!";
            case SEAL_EATING:       return "🦭😴 The seal is eating a fish and can't move. (" + res.getValue() + " turns left)";
            case SEAL_ACTIVE:       return "🦭 The seal has finished eating and is dangerous again!";
            case SEAL_ROLL:         return "🦭 The seal rolls: " + res.getValue();
            case SEAL_MOVE:         return "🦭 The seal moves to square " + res.getValue();
            case SNOWBALL_WAR_EMPTY:return "⚔️ It's a tie! Both " + res.getPlayerName() + " and " + res.getTargetName() + " have no snowballs!";
            case SNOWBALL_WAR_WIN:  return "⚔️ " + res.getPlayerName() + " wins! (" + res.getValue() + " balls) " + res.getTargetName() + " retreats " + res.getValue2() + " squares!";
            case SNOWBALL_WAR_TIE:  return "⚔️ It's a tie! Both spend all snowballs. No one retreats.";
            case SNOWBALL_THROW:    return "⛄ " + res.getPlayerName() + " threw a snowball at " + res.getTargetName() + "! " + res.getTargetName() + " goes back " + res.getValue() + " squares to " + res.getValue2() + "!";
            default:                return "Activity from " + res.getPlayerName();
        }
    }

    // ============================================
    //  HUD UPDATES
    // ============================================

    private void updateHUD() {
        Player current = getCurrentPlayer();

        if (rightPanel != null) { rightPanel.setVisible(false); rightPanel.setManaged(false); }

        HBox hotbar = createHotbar(current);
        rootPane.setTop(hotbar);

        Inventory inv = current.getInventory();
        rollFastDiceButton.setDisable(inv.getFastdiceQuantity() <= 0);
        rollSlowDiceButton.setDisable(inv.getSlowdiceQuantity() <= 0);
        throwSnowballButton.setDisable(inv.getSnowballQuantity() <= 0);

        if (sealEnabled && seal != null) updateSealStatus();
    }

    private HBox createHotbar(Player player) {
        HBox hotbar = new HBox(18);
        hotbar.setAlignment(Pos.CENTER_LEFT);
        hotbar.getStyleClass().add("hotbar");

        VBox playerInfo = new VBox(6);
        playerInfo.setAlignment(Pos.CENTER);

        StackPane portrait = new StackPane();
        portrait.getStyleClass().add("hotbar-portrait");
        double targetSize = 48;

        if (player.getAvatarPath() != null) {
            try {
                javafx.scene.image.Image customAvatar = new javafx.scene.image.Image(player.getAvatarPath(), targetSize, targetSize, false, false);
                javafx.scene.image.ImageView avatarView = new javafx.scene.image.ImageView(customAvatar);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(targetSize, targetSize);
                clip.setArcWidth(10); clip.setArcHeight(10);
                avatarView.setClip(clip);
                portrait.getChildren().add(avatarView);
            } catch (Exception e) {
                System.out.println("Could not load custom avatar, falling back to default.");
            }
        }

        if (portrait.getChildren().isEmpty() && baseRightImage != null && colorRightImage != null) {
            // The HUD portrait is a static UI element → always face right (idle pose).
            double w = baseRightImage.getWidth(), h = baseRightImage.getHeight();
            double aspect = w / h;
            double dw, dh;
            if (aspect >= 1.0) { dw = targetSize; dh = targetSize / aspect; }
            else               { dh = targetSize; dw = targetSize * aspect; }
            double dx = (targetSize - dw) / 2.0;
            double dy = (targetSize - dh) / 2.0;

            Canvas baseCanvas = new Canvas(targetSize, targetSize);
            GraphicsContext gcBase = baseCanvas.getGraphicsContext2D();
            gcBase.setImageSmoothing(false);
            gcBase.drawImage(baseRightImage, 0, 0, w, h, dx, dy, dw, dh);

            Canvas colorCanvas = new Canvas(targetSize, targetSize);
            GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
            gcColor.setImageSmoothing(false);
            gcColor.drawImage(colorRightImage, 0, 0, w, h, dx, dy, dw, dh);

            Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
            lighting.setSurfaceScale(0.0);
            colorCanvas.setEffect(lighting);

            portrait.getChildren().addAll(baseCanvas, colorCanvas);
        }

        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("hotbar-player-name");
        playerInfo.getChildren().addAll(portrait, nameLabel);

        HBox slots = new HBox(8);
        slots.setAlignment(Pos.CENTER_LEFT);

        Inventory inv = player.getInventory();
        addSlotIfPresent(slots, inv, ObjectType.SNOWBALL, "/assets/sprites/objects/snowball.png");
        addSlotIfPresent(slots, inv, ObjectType.FISH,     "/assets/sprites/objects/fish.png");
        addSlotIfPresent(slots, inv, ObjectType.FASTDICE, "/assets/sprites/objects/fastdice.png");
        addSlotIfPresent(slots, inv, ObjectType.SLOWDICE, "/assets/sprites/objects/slowdice.png");

        // Centered game-title banner — same layered shadow + main Text technique
        // as the main menu, scaled down to fit the hotbar.
        StackPane titleStack = new StackPane();
        Text titleShadow = new Text("Pingu's Game");
        titleShadow.getStyleClass().add("hotbar-title-shadow");
        Text titleMain = new Text("Pingu's Game");
        titleMain.getStyleClass().add("hotbar-title");
        titleStack.getChildren().addAll(titleShadow, titleMain);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Inventory / dice slots are now anchored on the LEFT next to the
        // player sprite; spacers keep the game-title centered with empty
        // space on both sides.
        hotbar.getChildren().addAll(playerInfo, slots, leftSpacer, titleStack, rightSpacer);

        return hotbar;
    }

    private void addSlotIfPresent(HBox container, Inventory inv, ObjectType type, String path) {
        int qty = inv.getObjectQuantity(type);
        if (qty > 0) container.getChildren().add(createInventorySlot(loadImage(path), qty));
    }

    private StackPane createInventorySlot(Image icon, int quantity) {
        StackPane slot = new StackPane();
        slot.getStyleClass().add("inventory-slot");
        slot.setPrefSize(80, 80);
        slot.setMaxSize(80, 80);

        if (quantity > 0 && icon != null) {
            double iconSize = 55;
            Canvas iconCanvas = new Canvas(iconSize, iconSize);
            GraphicsContext gc = iconCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);

            double aspect = icon.getWidth() / icon.getHeight();
            double dw = iconSize, dh = iconSize;
            if (aspect > 1) dh = dw / aspect; else dw = dh * aspect;
            double dx = (iconSize - dw) / 2, dy = (iconSize - dh) / 2;
            gc.drawImage(icon, dx, dy, dw, dh);
            slot.getChildren().add(iconCanvas);

            if (quantity > 1) {
                Label qtyLabel = new Label(String.valueOf(quantity));
                qtyLabel.getStyleClass().add("inventory-quantity");
                StackPane.setAlignment(qtyLabel, Pos.BOTTOM_RIGHT);
                slot.getChildren().add(qtyLabel);
            }
        } else {
            slot.setOpacity(0.5);
        }
        return slot;
    }

    private void updateSealStatus() {
        if (seal != null) {
            sealPositionLabel.setText("📍 Position: Square " + seal.getSquareIndex());
            sealBlockedLabel.setText(seal.isBlocked()
                ? "😴 Eating fish (" + seal.getBlockedTurns() + " turns left)"
                : "⚡ Active & Dangerous!");
        }
    }

    // ============================================
    //  BOARD DRAWING — no reactive bindings
    // ============================================

    private void drawBoard() {
        // Re-entrancy guard: prevents the listener on boardContainer.widthProperty()
        // from triggering a second drawBoard() while we are still inside one.
        if (isRedrawing) {
            // already drawing — skip this call
        } else {
            redrawBoardOnce();
        }
    }

    private void redrawBoardOnce() {
        isRedrawing = true;
        try {
            int cols = Board.widthBoard;
            int rows = Board.heightBoard;

            grid.getColumnConstraints().clear();
            grid.getRowConstraints().clear();
            grid.getChildren().clear();

            grid.setHgap(0);
            grid.setVgap(0);
            grid.setPadding(new javafx.geometry.Insets(0));
            if (!grid.getStyleClass().contains("game-grid")) grid.getStyleClass().add("game-grid");

            // Snapshot the container size — no binding, so no feedback loop.
            double availW = boardContainer.getWidth();
            double availH = boardContainer.getHeight();
            double cellSize = (availW > 0 && availH > 0)
                ? Math.floor(Math.min(availW / cols, availH / rows))
                : 60.0;

            // Fixed-value constraints — not bound to any property.
            for (int i = 0; i < cols; i++) {
                ColumnConstraints cc = new ColumnConstraints(cellSize, cellSize, cellSize);
                cc.setHgrow(Priority.NEVER);
                grid.getColumnConstraints().add(cc);
            }
            for (int i = 0; i < rows; i++) {
                RowConstraints rc = new RowConstraints(cellSize, cellSize, cellSize);
                rc.setVgrow(Priority.NEVER);
                grid.getRowConstraints().add(rc);
            }

            grid.setPrefSize(cellSize * cols, cellSize * rows);
            grid.setMaxSize(cellSize * cols, cellSize * rows);
            grid.setAlignment(Pos.CENTER);

            for (int i = 0; i < Board.MAX_SQUARES; i++) {
                int row = i / cols;
                int col = (row % 2 == 0) ? (i % cols) : (cols - 1 - (i % cols));
                grid.add(createCell(i, cellSize), col, row);
            }
        } finally {
            isRedrawing = false;
        }
    }

    // All images are drawn onto a Canvas so we can force nearest-neighbour
    // (no blur) even at large zoom factors — ImageView.setSmooth(false) is
    // not reliable across all JavaFX versions/platforms.
    private StackPane createCell(int squareIndex, double cellSize) {
        StackPane cell = new StackPane();
        cell.setMinSize(cellSize, cellSize);
        cell.setPrefSize(cellSize, cellSize);
        cell.setMaxSize(cellSize, cellSize);
        cell.setSnapToPixel(true);
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

        // Background tile — Canvas with nearest-neighbour scaling
        Image cellBackground = loadImage(getBackgroundImagePath(squareIndex));
        if (cellBackground != null) {
            Canvas bgCanvas = new Canvas(cellSize, cellSize);
            GraphicsContext gc = bgCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);
            gc.drawImage(cellBackground, 0, 0, cellSize, cellSize);
            cell.getChildren().add(bgCanvas);
        }

        // Foreground overlay — Canvas with nearest-neighbour scaling
        Image overlayImage = getForegroundImageForType(type);
        if (overlayImage != null) {
            Canvas overlayCanvas = new Canvas(cellSize, cellSize);
            GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);
            gc.drawImage(overlayImage, 0, 0, cellSize, cellSize);
            cell.getChildren().add(overlayCanvas);
        }

        addPlayerSpritesToCell(cell, squareIndex, cellSize);

        if (sealEnabled && seal != null && seal.getSquareIndex() == squareIndex) {
            // Cap the seal to ~55% of the cell on its longest axis, then size the
            // canvas to match the sprite's actual aspect ratio. The seal is
            // centered so it shares the same horizontal "lane" as the player
            // tokens (which are also centered in the StackPane).
            double sealMax = Math.max(cellSize * 0.55, 1.0);
            // Direction follows the snake-row direction, like the players
            int sealRow = squareIndex / Board.widthBoard;
            boolean sealRowFacesRight = (sealRow % 2 == 0);
            Image sealSprite = sealRowFacesRight ? sealRightImage : sealLeftImage;
            if (sealSprite != null) {
                double sw = sealSprite.getWidth(), sh = sealSprite.getHeight();
                double aspect = sw / sh;
                double dw, dh;
                if (aspect >= 1.0) { dw = sealMax; dh = sealMax / aspect; }
                else               { dh = sealMax; dw = sealMax * aspect; }
                Canvas sealCanvas = new Canvas(dw, dh);
                GraphicsContext gc = sealCanvas.getGraphicsContext2D();
                gc.setImageSmoothing(false);
                gc.drawImage(sealSprite, 0, 0, sw, sh, 0, 0, dw, dh);
                StackPane.setAlignment(sealCanvas, Pos.CENTER);
                cell.getChildren().add(sealCanvas);
            } else {
                Label sealLabel = new Label("🦭");
                int fontSize = Math.max(8, (int)(cellSize * 0.3));
                sealLabel.setStyle("-fx-font-size: " + fontSize + ";");
                StackPane.setAlignment(sealLabel, Pos.CENTER);
                cell.getChildren().add(sealLabel);
            }
        }

        return cell;
    }

    private String getBackgroundImagePath(int squareIndex) {
        if (squareIndex == 0)                      return "/assets/sprites/squares/background/Square-0.png";
        if (squareIndex == Board.MAX_SQUARES - 1)  return "/assets/sprites/squares/background/Square-6.png";

        int cols = Board.widthBoard;
        int row = squareIndex / cols;
        int indexInRow = squareIndex % cols;
        boolean isEvenRow = (row % 2 == 0);

        if (indexInRow == 0)          return isEvenRow ? "/assets/sprites/squares/background/Square-5.png" : "/assets/sprites/squares/background/Square-4.png";
        if (indexInRow == cols - 1)   return isEvenRow ? "/assets/sprites/squares/background/Square-3.png" : "/assets/sprites/squares/background/Square-2.png";
        return "/assets/sprites/squares/background/Square-1.png";
    }

    private Image getForegroundImageForType(SquareType type) {
        if (type == null || type == SquareType.NORMAL) return null;
        return loadImage("/assets/sprites/squares/foreground/" + type.name() + ".png");
    }

    // Player sprites are also drawn onto Canvas for the same pixel-art reason.
    private void addPlayerSpritesToCell(StackPane cell, int squareIndex, double cellSize) {
        List<Player> playersHere = new ArrayList<>();
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && e.getSquareIndex() == squareIndex) playersHere.add((Player) e);
        }
        if (!playersHere.isEmpty()) {
            renderPlayersOnCell(cell, squareIndex, cellSize, playersHere);
        }
    }

    private void renderPlayersOnCell(StackPane cell, int squareIndex, double cellSize, List<Player> playersHere) {

        // Snake-pattern board: even rows go left→right (face right),
        // odd rows go right→left (face left). Direction follows the row,
        // not the player's last movement delta.
        int row = squareIndex / Board.widthBoard;
        boolean rowFacesRight = (row % 2 == 0);

        double spriteSize = Math.max(cellSize * 0.25, 1.0);
        double spacingVal = cellSize * 0.15;
        int count = playersHere.size();

        for (int idx = 0; idx < count; idx++) {
            Player player = playersHere.get(idx);
            StackPane playerToken = new StackPane();

            // Pick base / colour overlay according to row direction and damaged state.
            // Damaged frames have no colour-overlay variant → fall back to the idle colour.
            Image baseSprite;
            if (player.isDamaged()) {
                baseSprite = rowFacesRight ? damagedRightImage : damagedLeftImage;
            } else {
                baseSprite = rowFacesRight ? baseRightImage : baseLeftImage;
            }
            Image colorSprite = rowFacesRight ? colorRightImage : colorLeftImage;

            if (baseSprite != null && colorSprite != null) {
                // Preserve aspect ratio of the source sprite (17x19) inside the cell token area
                double sw = baseSprite.getWidth(), sh = baseSprite.getHeight();
                double aspect = sw / sh;
                double dw, dh;
                if (aspect >= 1.0) { dw = spriteSize; dh = spriteSize / aspect; }
                else               { dh = spriteSize; dw = spriteSize * aspect; }
                double dx = (spriteSize - dw) / 2.0;
                double dy = (spriteSize - dh) / 2.0;

                Canvas baseCanvas = new Canvas(spriteSize, spriteSize);
                GraphicsContext gcBase = baseCanvas.getGraphicsContext2D();
                gcBase.setImageSmoothing(false);
                gcBase.drawImage(baseSprite, 0, 0, sw, sh, dx, dy, dw, dh);

                Canvas colorCanvas = new Canvas(spriteSize, spriteSize);
                GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
                gcColor.setImageSmoothing(false);
                gcColor.drawImage(colorSprite, 0, 0, sw, sh, dx, dy, dw, dh);

                Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
                lighting.setSurfaceScale(0.0);
                colorCanvas.setEffect(lighting);

                playerToken.getChildren().addAll(baseCanvas, colorCanvas);
            } else {
                Circle fallback = new Circle(spriteSize / 2);
                fallback.setFill(getColorFromHex(player.getColour()));
                fallback.setStroke(Color.WHITE);
                fallback.setStrokeWidth(2);
                playerToken.getChildren().add(fallback);
            }

            // Centre the group of tokens horizontally within the cell
            playerToken.setTranslateX((idx - (count - 1) / 2.0) * spacingVal);
            // While debug mode is on, every player token can be dragged to
            // any cell of the board to teleport that player.
            if (debugMode) {
                attachDebugDragHandlers(playerToken, player);
            }
            cell.getChildren().add(playerToken);
        }
    }

    // ============================================
    //  ANIMATIONS
    // ============================================

    /**
     * Pixel-art overlay that flashes the dice result over the board for ~1s
     * after the rolling-dice GIF finishes and before the player walks. Gives
     * visible feedback of the rolled number (the original {@code diceResultLabel}
     * lives in an FXML HBox that gets replaced by the hotbar in updateHUD).
     */
    private void showDiceResultBadge(int diceResult, Runnable onFinished) {
        Label badge = new Label("🎲 " + diceResult);
        badge.getStyleClass().add("dice-result-badge");
        badge.setOpacity(0);
        StackPane.setAlignment(badge, Pos.CENTER);
        animationOverlay.getChildren().add(badge);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), badge);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(180), badge);
        pop.setFromX(0.6); pop.setFromY(0.6);
        pop.setToX(1.0);   pop.setToY(1.0);

        PauseTransition stay = new PauseTransition(Duration.millis(700));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), badge);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            animationOverlay.getChildren().remove(badge);
            if (onFinished != null) onFinished.run();
        });

        new ParallelTransition(fadeIn, pop).play();
        new SequentialTransition(new PauseTransition(Duration.millis(180)), stay, fadeOut).play();
    }

    private void showDiceAnimation(Runnable onFinished) {
        InputStream is = getClass().getResourceAsStream("/assets/dice/dados.gif");
        if (is == null) {
            System.err.println("Dice animation not found: /assets/dice/dados.gif");
            if (onFinished != null) onFinished.run();
        } else {
            playDiceGifOverlay(is, onFinished);
        }
    }

    private void playDiceGifOverlay(InputStream is, Runnable onFinished) {
        Image gif = new Image(is);
        ImageView view = new ImageView(gif);
        view.setPreserveRatio(true);
        view.setSmooth(false);
        view.setFitWidth(280);
        view.setOpacity(0);

        StackPane.setAlignment(view, Pos.CENTER);
        animationOverlay.getChildren().add(view);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), view);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.millis(1100));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), view);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            animationOverlay.getChildren().remove(view);
            if (onFinished != null) onFinished.run();
        });

        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }

    private void animateSnowballThrow() {
        FadeTransition flash = new FadeTransition(Duration.millis(100), grid);
        flash.setFromValue(1.0); flash.setToValue(0.7);
        flash.setCycleCount(4); flash.setAutoReverse(true);
        flash.play();
    }

    private void animateWarFlash() {
        FadeTransition flash = new FadeTransition(Duration.millis(150), grid);
        flash.setFromValue(1.0); flash.setToValue(0.5);
        flash.setCycleCount(6); flash.setAutoReverse(true);
        flash.play();
    }

    // ============================================
    //  UTILITIES
    // ============================================

    private Player getCurrentPlayer() { return (Player) turnController.getCurrentTurn(); }

    private void logEvent(String message) {
        if (message != null && !message.isBlank()) {
            eventHistoryFull.add(message);
        }
    }

    @FXML
    private void showEventHistory() {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("📜 Event History");
        dialog.setHeaderText("All events for this game");

        TextArea ta = new TextArea(String.join("\n", eventHistoryFull));
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(520, 400);

        dialog.getDialogPane().setContent(ta);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void disableActions() {
        rollDiceButton.setDisable(true);
        rollFastDiceButton.setDisable(true);
        rollSlowDiceButton.setDisable(true);
        throwSnowballButton.setDisable(true);
    }

    private void enableActions() {
        rollDiceButton.setDisable(false);
        // fast/slow/snowball re-enabled by updateHUD()
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
        if (hex.length() < 6) hex = "0".repeat(6 - hex.length()) + hex;
        return hex;
    }

    private Color getColorFromHex(String hex) {
        try { return Color.web("#" + padColor(hex)); }
        catch (Exception e) { return Color.GRAY; }
    }

    public void handleWin(Player winner) {
        gameOver = true;
        this.winner = winner.getName();
        disableActions();
        model.game.SoundManager.getInstance().restoreMenuVolume();
        logEvent("🏆 GAME OVER! " + winner.getName() + " HAS WON THE GAME! 🏆");
        SaveLoadService.recordGameResult(turnController.getAllPlayers(), winner.getName());
        drawBoard();
        showWinAnimation(winner);
    }

    // ============================================
    //  CINEMATIC ANIMATIONS
    // ============================================

    private void showBearAnimation() {
        Image bearSprite = loadImage("/assets/sprites/squares/foreground/BEAR.png");
        Node bearNode;
        if (bearSprite != null) {
            // Pixel-perfect render via Canvas (matches the rest of the board)
            double size = 220;
            Canvas bearCanvas = new Canvas(size, size);
            GraphicsContext gc = bearCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);
            gc.drawImage(bearSprite, 0, 0, size, size);
            bearNode = bearCanvas;
        } else {
            Label fallback = new Label("🐻");
            fallback.setFont(new Font("System", 150));
            bearNode = fallback;
        }
        animationOverlay.getChildren().add(bearNode);

        ScaleTransition st = new ScaleTransition(Duration.millis(300), bearNode);
        st.setFromX(0); st.setFromY(0); st.setToX(2); st.setToY(2);

        FadeTransition ft = new FadeTransition(Duration.millis(500), bearNode);
        ft.setDelay(Duration.millis(800)); ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> animationOverlay.getChildren().remove(bearNode));
        st.play(); ft.play();
    }

    private void showSealAnimation() {
        // Cinematic always slides in from the right → use the left-facing sprite
        // (the seal "looks toward" the centre as it enters).
        Image sprite = (sealLeftImage != null) ? sealLeftImage : sealRightImage;
        Node sealNode;
        if (sprite != null) {
            double size = 220;
            double sw = sprite.getWidth(), sh = sprite.getHeight();
            double aspect = sw / sh;
            double dw, dh;
            if (aspect >= 1.0) { dw = size; dh = size / aspect; }
            else               { dh = size; dw = size * aspect; }
            Canvas sealCanvas = new Canvas(size, size);
            GraphicsContext gc = sealCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);
            gc.drawImage(sprite, 0, 0, sw, sh, (size - dw) / 2.0, (size - dh) / 2.0, dw, dh);
            sealNode = sealCanvas;
        } else {
            Label fallback = new Label("🦭");
            fallback.setFont(new Font("System", 120));
            sealNode = fallback;
        }
        animationOverlay.getChildren().add(sealNode);

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), sealNode);
        tt.setFromX(800); tt.setToX(0);

        FadeTransition ft = new FadeTransition(Duration.millis(300), sealNode);
        ft.setDelay(Duration.millis(1000)); ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> animationOverlay.getChildren().remove(sealNode));
        tt.play(); ft.play();
    }

    private void showWinAnimation(Player winner) {
        showWinAnimation(winner.getName(), buildPlayerCharacterNode(winner, 200));
    }

    private void showSealWinAnimation() {
        showWinAnimation("THE SEAL", buildSealCharacterNode(200));
    }

    /**
     * Win screen: dark backdrop with the winner's crown, sprite, name and
     * a back-to-menu button. The sprite gently bobs up and down to give
     * the scene some life.
     */
    private void showWinAnimation(String winnerName, Node characterNode) {
        animationOverlay.setMouseTransparent(false);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(8, 18, 35, 0.92);");
        root.prefWidthProperty().bind(animationOverlay.widthProperty());
        root.prefHeightProperty().bind(animationOverlay.heightProperty());

        // Build winner content: 👑 crown + sprite + name + back button
        VBox winBox = new VBox(18);
        winBox.setAlignment(Pos.CENTER);
        winBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Label crown = new Label("👑");
        crown.setStyle("-fx-font-size: 110px;");
        // Tint the emoji to bright yellow using the same Lighting trick that
        // tints player sprites in the hotbar — surfaceScale 0 makes the light
        // colour fully replace the underlying pixels' colour.
        Lighting crownTint = new Lighting(new Light.Distant(45, 90, Color.web("#ffeb3b")));
        crownTint.setSurfaceScale(0.0);
        crown.setEffect(crownTint);

        Label title = new Label(winnerName + " WINS!");
        title.getStyleClass().add("win-title");

        Button backBtn = new Button(model.config.LangConfig.getLang(model.config.Lang.GAME_BACK_TO_MENU));
        backBtn.getStyleClass().add("nav-btn-home");
        backBtn.setStyle("-fx-font-size: 22px; -fx-padding: 14 40;");
        backBtn.setOnAction(e -> {
            model.game.SoundManager.getInstance().restoreMenuVolume();
            navigateTo("/view/fxml/mainMenu.fxml", "/assets/css/style.css");
        });

        if (characterNode != null) {
            winBox.getChildren().addAll(crown, characterNode, title, backBtn);
            // Subtle floating idle: ±10 px on Y, ~1.3s per cycle, looping.
            TranslateTransition bob = new TranslateTransition(Duration.millis(1300), characterNode);
            bob.setFromY(-10);
            bob.setToY(10);
            bob.setAutoReverse(true);
            bob.setCycleCount(Animation.INDEFINITE);
            bob.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            bob.play();
        } else {
            winBox.getChildren().addAll(crown, title, backBtn);
        }
        winBox.setOpacity(0);

        root.getChildren().add(winBox);
        animationOverlay.getChildren().add(root);

        FadeTransition winFade = new FadeTransition(Duration.millis(500), winBox);
        winFade.setFromValue(0); winFade.setToValue(1);

        ScaleTransition winScale = new ScaleTransition(Duration.millis(620), winBox);
        winScale.setFromX(0.5); winScale.setFromY(0.5);
        winScale.setToX(1.0);   winScale.setToY(1.0);
        winScale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        new ParallelTransition(winFade, winScale).play();
    }

    /** Builds a large pixel-art canvas of the player's tinted sprite. */
    private Node buildPlayerCharacterNode(Player player, double size) {
        if (baseRightImage == null || colorRightImage == null) {
            Label fallback = new Label("🐧");
            fallback.setStyle("-fx-font-size: " + (int) size + "px;");
            return fallback;
        }
        double w = baseRightImage.getWidth(), h = baseRightImage.getHeight();
        double aspect = w / h;
        double dw, dh;
        if (aspect >= 1.0) { dw = size; dh = size / aspect; }
        else               { dh = size; dw = size * aspect; }
        double dx = (size - dw) / 2.0;
        double dy = (size - dh) / 2.0;

        Canvas baseCanvas = new Canvas(size, size);
        baseCanvas.getGraphicsContext2D().setImageSmoothing(false);
        baseCanvas.getGraphicsContext2D().drawImage(baseRightImage, 0, 0, w, h, dx, dy, dw, dh);

        Canvas colorCanvas = new Canvas(size, size);
        colorCanvas.getGraphicsContext2D().setImageSmoothing(false);
        colorCanvas.getGraphicsContext2D().drawImage(colorRightImage, 0, 0, w, h, dx, dy, dw, dh);

        Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
        lighting.setSurfaceScale(0.0);
        colorCanvas.setEffect(lighting);

        StackPane sp = new StackPane(baseCanvas, colorCanvas);
        sp.setMinSize(size, size);
        sp.setPrefSize(size, size);
        return sp;
    }

    /** Builds a large pixel-art canvas of the seal sprite. */
    private Node buildSealCharacterNode(double size) {
        Image sprite = (sealRightImage != null) ? sealRightImage : sealLeftImage;
        if (sprite == null) {
            Label fallback = new Label("🦭");
            fallback.setStyle("-fx-font-size: " + (int) size + "px;");
            return fallback;
        }
        double w = sprite.getWidth(), h = sprite.getHeight();
        double aspect = w / h;
        double dw, dh;
        if (aspect >= 1.0) { dw = size; dh = size / aspect; }
        else               { dh = size; dw = size * aspect; }
        Canvas c = new Canvas(size, size);
        c.getGraphicsContext2D().setImageSmoothing(false);
        c.getGraphicsContext2D().drawImage(sprite, 0, 0, w, h, (size - dw) / 2.0, (size - dh) / 2.0, dw, dh);
        StackPane sp = new StackPane(c);
        sp.setMinSize(size, size);
        sp.setPrefSize(size, size);
        return sp;
    }

    public Board getCurrentGameBoard() { return this.gameBoard; }

    // ===== NAVIGATION =====

    @FXML
    private void handleBack() {
        if (confirmLeaveIfNeeded("Leave Game?", "You will lose your current game progress!", "Go back to Player Setup?")) {
            model.game.SoundManager.getInstance().restoreMenuVolume();
            navigateTo("/view/fxml/playerSetup.fxml", "/assets/css/style.css");
        }
    }

    @FXML
    private void handleReturnToMenu() {
        if (confirmLeaveIfNeeded("Return to Menu?", "You will lose your current game progress!", "Return to Main Menu?")) {
            model.game.SoundManager.getInstance().restoreMenuVolume();
            navigateTo("/view/fxml/mainMenu.fxml", "/assets/css/style.css");
        }
    }

    /**
     * If the game is still in progress, asks the user to confirm leaving;
     * otherwise the navigation is silently allowed. Returns true when the
     * caller may proceed with the navigation.
     */
    private boolean confirmLeaveIfNeeded(String title, String header, String content) {
        if (gameOver) {
            return true;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void navigateTo(String fxmlPath, String cssPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene currentScene = mainStack.getScene();
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            javafx.scene.Scene newScene = new javafx.scene.Scene(root);
            if (cssPath != null) {
                java.net.URL css = getClass().getResource(cssPath);
                if (css != null) newScene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
