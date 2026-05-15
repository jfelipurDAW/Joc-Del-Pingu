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


/**
 * Controller for the gameplay screen ({@code gameBoard.fxml}).
 *
 * <p>This is by far the most complex screen in the game. It is responsible
 * for everything that happens once a match has started:</p>
 * <ul>
 *   <li>Building the snake-pattern board, cell by cell, with pixel-art
 *       backgrounds and foregrounds rendered onto {@link Canvas} nodes to
 *       avoid blurring.</li>
 *   <li>Driving the turn loop through {@code TurnController} and
 *       {@code GameManager}, with animations for dice rolls, player
 *       movement, snowball throws and bear / seal events.</li>
 *   <li>Managing the optional seal NPC: its dedicated turn, its tail-hit
 *       interactions and its eating cooldown.</li>
 *   <li>Drawing the live HUD: per-player portraits, inventory slots and
 *       the central game title.</li>
 *   <li>Persisting an in-memory event history that the user can review at
 *       any time via the "History" button.</li>
 *   <li>Offering a hidden developer "debug mode" (Ctrl+Shift+D) that lets
 *       the developer teleport pinguins by dragging, force the next dice
 *       roll, and tweak each player's inventory live.</li>
 *   <li>Handling save / exit flows and the cinematic win sequence.</li>
 * </ul>
 *
 * <p>The class keeps a strict no-bindings policy on the board grid: cell
 * sizes are snapshotted from the container size and used as fixed values,
 * because reactive bindings combined with the resize listener would cause
 * a redraw loop.</p>
 */
public class GameBoardController {


    /////////////////////////////
    ///   FXML INJECTIONS    ///
    /////////////////////////////

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


    /////////////////////////////
    ///    EVENT HISTORY     ///
    /////////////////////////////

    // --- Event history backing list (no on-screen ticker; the full
    //     log is reachable through the "📜 History" button on the action bar)
    // Every meaningful in-game event is appended here so the "History"
    // dialog can show the full chronological log even after many turns.
    private final java.util.List<String> eventHistoryFull = new ArrayList<>();


    /////////////////////////////
    ///   DEBUG-MODE FIELDS  ///
    /////////////////////////////

    // --- Debug mode (Ctrl+Shift+D) ---
    // Lets the developer drag a player token to any cell, pre-set the value
    // of the next dice roll, and tweak any player's inventory. Off by
    // default, no effect on normal play.
    private boolean debugMode = false;

    // When non-null, the next call to rollOrForce(...) returns this value
    // and clears the field, bypassing the actual dice randomness.
    private Integer debugForcedDice = null;

    // UI containers for the developer panel; built lazily the first time
    // debug mode is toggled on.
    private VBox debugPanel;
    private Label debugBannerLabel;
    private ComboBox<String> debugPlayerCombo;
    private Label debugSnowballLabel;
    private Label debugFishLabel;
    private Label debugFastDiceLabel;
    private Label debugSlowDiceLabel;

    // Toggled by the in-game console command "/view numbers". When true, each
    // square overlay is annotated with its linear index. Persisted through
    // DebugConsoleService so the setting survives between game sessions.
    private boolean viewSquareNumbers = false;


    /////////////////////////////
    ///     GAME STATE       ///
    /////////////////////////////

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


    /////////////////////////////
    ///       SPRITES        ///
    /////////////////////////////

    // --- Sprites ---
    // Player: 4 sprite layers (base/colour x left/right), plus damaged and frozen frames
    // The colour variant is layered on top of the base sprite and tinted at
    // runtime via a Lighting effect so the same PNG can represent every
    // player's chosen colour without needing per-colour assets.
    private final Image baseRightImage;
    private final Image colorRightImage;
    private final Image baseLeftImage;
    private final Image colorLeftImage;
    private final Image damagedRightImage;
    private final Image damagedLeftImage;
    private final Image iceRightImage;
    private final Image iceLeftImage;
    // Seal: idle facing left/right
    private final Image sealRightImage;
    private final Image sealLeftImage;

    // Image-cache keyed by resource path so we never decode the same PNG twice.
    private final Map<String, Image> resourceCache = new HashMap<>();

    // Guard against re-entrant drawBoard calls
    // (the boardContainer resize listener fires while drawBoard mutates the
    //  grid; without this flag the redraw would recurse and freeze the UI).
    private boolean isRedrawing = false;


    /////////////////////////////
    ///     CONSTRUCTOR      ///
    /////////////////////////////

    /**
     * Pre-loads every static sprite used by the game board (players in
     * various states + seal). The actual gameplay state is built later, in
     * {@link #initialize()}, once the FXML injections are available.
     */
    public GameBoardController() {
        baseRightImage    = loadImage("/assets/sprites/entities/player/player_idle_right.png");
        colorRightImage   = loadImage("/assets/sprites/entities/player/player_idle_colour_right.png");
        baseLeftImage     = loadImage("/assets/sprites/entities/player/player_idle_left.png");
        colorLeftImage    = loadImage("/assets/sprites/entities/player/player_idle_colour_left.png");
        damagedRightImage = loadImage("/assets/sprites/entities/player/player_damaged_right.png");
        damagedLeftImage  = loadImage("/assets/sprites/entities/player/player_damaged_left.png");
        iceRightImage     = loadImage("/assets/sprites/entities/player/ice_player_right.png");
        iceLeftImage      = loadImage("/assets/sprites/entities/player/ice_player_left.png");
        sealRightImage    = loadImage("/assets/sprites/entities/seal/seal_idle_right.png");
        sealLeftImage     = loadImage("/assets/sprites/entities/seal/seal_idle_left.png");
        gameOver = false;
    }


    /**
     * Loads a PNG / GIF from the classpath, caching by path so repeated
     * lookups (e.g. one per cell of the board) are essentially free.
     *
     * <p>The {@code Image} is created with smoothing disabled and at the
     * source PNG's natural size; downstream code rescales onto a Canvas with
     * nearest-neighbour interpolation, preserving the pixel-art look.</p>
     *
     * @param path classpath-style resource path (must start with {@code /})
     * @return the loaded image, or {@code null} if the resource is missing
     */
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


    /////////////////////////////
    ///  SCENE INITIALIZATION ///
    /////////////////////////////

    /**
     * FXML lifecycle hook. Builds the {@code GameManager}, restores the
     * board from a save (if applicable) or creates a fresh one, prepares
     * the dice instances, wires up the resize listener and the Ctrl+Shift+D
     * debug hotkey, and finally hands control to {@code drawBoard()}.
     */
    @FXML
    public void initialize() {
        gameManager = new model.game.GameManager("LOCAL_MATCH", 0);
        gameBoard = new Board();
        gameManager.setBoard(gameBoard);

        // Two distinct paths: restoring a saved game versus starting fresh.
        // The "loaded" path also restores the seal's current square and how
        // many turns it has left of its eating-fish cooldown.
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

        // The three dice variants are created once and reused for every roll;
        // their internal state is just min/max values, so a single instance
        // per variant is enough.
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

        // Switch the soundtrack: the menu's title music stops, the in-game
        // bg_music starts from the beginning. Leaving the game (handleWin,
        // handleBack, handleReturnToMenu) brings the title track back.
        model.game.SoundManager.getInstance().playGameMusic();

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

        // Register with the debug console service so /tp, /give, /view
        // numbers, /setdice ... can all talk to this board controller. The
        // service re-applies any persistent flags (e.g. viewNumbers) here.
        model.game.DebugConsoleService.getInstance().setActiveBoardController(this);
    }


    /////////////////////////////
    /// DEBUG MODE (CTRL+SHIFT+D) ///
    /////////////////////////////

    // Everything below is wired only when the developer hits Ctrl+Shift+D.
    // The toggle builds (lazily) a black panel at the top of the scene with
    // a "Force next roll" field, drag-to-teleport handlers on every pingu,
    // and live inventory editing for any player.

    /**
     * Flips the debug-mode flag and rebuilds the developer panel and the
     * player drag handlers accordingly. Triggered by Ctrl+Shift+D.
     */
    private void toggleDebugMode() {
        debugMode = !debugMode;
        if (debugPanel == null) {
            buildDebugPanel();
        }
        debugPanel.setVisible(debugMode);
        debugPanel.setManaged(debugMode);
        if (debugMode) {
            populateDebugPlayerCombo();
        } else {
            debugForcedDice = null;
            updateDebugBanner();
        }
        // Redraw so player tokens pick up (or drop) drag handlers/cursor.
        drawBoard();
    }


    /**
     * Builds the developer panel once and stashes it inside {@code mainStack}.
     * The panel has two rows: forced-dice / drag hint, and per-player
     * inventory editor.
     */
    private void buildDebugPanel() {
        // ---------- Row 1: status + forced dice + drag hint ----------
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

        HBox row1 = new HBox(8, debugBannerLabel, forceLbl, diceField, setBtn, clearBtn, hint);
        row1.setAlignment(Pos.CENTER_LEFT);

        // ---------- Row 2: inventory editor (player + per-item +/-) ----------
        Label playerLbl = new Label("Inventory of:");
        playerLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        debugPlayerCombo = new ComboBox<>();
        debugPlayerCombo.setPrefWidth(140);
        debugPlayerCombo.setOnAction(e -> refreshDebugInventoryLabels());

        debugSnowballLabel  = buildInventoryDisplay("⛄ -");
        debugFishLabel      = buildInventoryDisplay("🐟 -");
        debugFastDiceLabel  = buildInventoryDisplay("🎲✨ -");
        debugSlowDiceLabel  = buildInventoryDisplay("🎲 -");

        HBox row2 = new HBox(6,
            playerLbl, debugPlayerCombo,
            debugSnowballLabel,  invMinus(ObjectType.SNOWBALL), invPlus(ObjectType.SNOWBALL),
            debugFishLabel,      invMinus(ObjectType.FISH),     invPlus(ObjectType.FISH),
            debugFastDiceLabel,  invMinus(ObjectType.FASTDICE), invPlus(ObjectType.FASTDICE),
            debugSlowDiceLabel,  invMinus(ObjectType.SLOWDICE), invPlus(ObjectType.SLOWDICE)
        );
        row2.setAlignment(Pos.CENTER_LEFT);

        // ---------- Container ----------
        debugPanel = new VBox(4, row1, row2);
        debugPanel.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 6 14;");
        debugPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        debugPanel.setMouseTransparent(false);
        debugPanel.setVisible(false);
        debugPanel.setManaged(false);

        StackPane.setAlignment(debugPanel, Pos.TOP_CENTER);
        StackPane.setMargin(debugPanel, new javafx.geometry.Insets(2, 0, 0, 0));
        mainStack.getChildren().add(debugPanel);
    }

    private Label buildInventoryDisplay(String initial) {
        Label l = new Label(initial);
        l.setStyle("-fx-text-fill: #ffeb3b; -fx-font-weight: 900; -fx-font-size: 13px; -fx-padding: 0 0 0 8;");
        return l;
    }

    private Button invMinus(ObjectType type) {
        Button b = new Button("−");
        b.setStyle("-fx-padding: 2 8; -fx-font-weight: 900;");
        b.setOnAction(e -> tweakInventory(type, -1));
        return b;
    }

    private Button invPlus(ObjectType type) {
        Button b = new Button("+");
        b.setStyle("-fx-padding: 2 8; -fx-font-weight: 900;");
        b.setOnAction(e -> tweakInventory(type, +1));
        return b;
    }

    private void populateDebugPlayerCombo() {
        if (debugPlayerCombo != null) {
            String previous = debugPlayerCombo.getValue();
            debugPlayerCombo.getItems().clear();
            for (Entity e : turnController.getAllPlayers()) {
                if (e instanceof Player) {
                    debugPlayerCombo.getItems().add(e.getName());
                }
            }
            if (previous != null && debugPlayerCombo.getItems().contains(previous)) {
                debugPlayerCombo.setValue(previous);
            } else if (!debugPlayerCombo.getItems().isEmpty()) {
                debugPlayerCombo.setValue(debugPlayerCombo.getItems().get(0));
            }
            refreshDebugInventoryLabels();
        }
    }

    private Player findDebugSelectedPlayer() {
        if (debugPlayerCombo == null) {
            return null;
        }
        String name = debugPlayerCombo.getValue();
        if (name == null) {
            return null;
        }
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && name.equals(e.getName())) {
                return (Player) e;
            }
        }
        return null;
    }

    private void refreshDebugInventoryLabels() {
        Player p = findDebugSelectedPlayer();
        if (p == null) {
            debugSnowballLabel.setText("⛄ -");
            debugFishLabel.setText("🐟 -");
            debugFastDiceLabel.setText("🎲✨ -");
            debugSlowDiceLabel.setText("🎲 -");
        } else {
            Inventory inv = p.getInventory();
            debugSnowballLabel.setText("⛄ "   + inv.getSnowballQuantity() + "/" + Inventory.MAX_SNOWBALLS);
            debugFishLabel.setText("🐟 "       + inv.getFishQuantity()     + "/" + Inventory.MAX_FISH);
            debugFastDiceLabel.setText("🎲✨ " + inv.getFastdiceQuantity());
            debugSlowDiceLabel.setText("🎲 "   + inv.getSlowdiceQuantity());
        }
    }

    /**
     * Bumps the selected player's count for the given object up or down by 1,
     * respecting the per-item caps in Inventory (snowballs/fish/total dice).
     * Negative deltas only act when there is at least one to remove.
     */
    private void tweakInventory(ObjectType type, int delta) {
        Player p = findDebugSelectedPlayer();
        if (p != null) {
            Inventory inv = p.getInventory();
            if (delta > 0) {
                switch (type) {
                    case SNOWBALL: inv.addSnowballs(1); break;
                    case FISH:     inv.addFish();      break;
                    case FASTDICE: inv.addDice(ObjectType.FASTDICE); break;
                    case SLOWDICE: inv.addDice(ObjectType.SLOWDICE); break;
                    default: break;
                }
            } else if (inv.getObjectQuantity(type) > 0) {
                inv.useObject(type, 1);
            }
            refreshDebugInventoryLabels();
            updateHUD();
            drawBoard();
        }
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


    /**
     * Pulls the player list out of {@link GameSetupConfig} (filled by the
     * player-setup screen or by a loaded save) and adds each one to the
     * turn controller. When no list is configured a default two-player
     * match is created so the developer can always launch the screen
     * directly from the IDE.
     */
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


    /**
     * Loads the dedicated stylesheet for the game-board scene. The main-menu
     * stylesheet is cleared first so its rules cannot leak in and affect the
     * game-board look.
     */
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


    /////////////////////////////
    ///   ACTION HANDLERS    ///
    /////////////////////////////

    /**
     * "Roll" button handler (default 1-6 die). Skips the dice randomness
     * entirely when debug mode has forced a value via the developer panel.
     */
    @FXML
    private void rollDice() {
        if (!gameOver) {
            int result = rollOrForce(defaultDice);
            processDiceRoll(result, "Normal");
        }
    }


    /**
     * "Roll Fast Dice" handler. Consumes one fast-dice item from the
     * current player's inventory and triggers a 5-10 roll, or warns the
     * user when they have none.
     */
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


    /**
     * "Roll Slow Dice" handler. Consumes one slow-dice item and triggers a
     * 1-3 roll; useful when the player wants to avoid overshooting a trap.
     */
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


    /**
     * "Throw Snowball" handler. Defers to {@link #beginThrowSnowball()}
     * which checks inventory, lists targets and asks the user to pick one.
     */
    @FXML
    private void throwSnowball() {
        if (!gameOver) {
            beginThrowSnowball();
        }
    }


    /**
     * Verifies the current player actually has snowballs and other players
     * to target; otherwise it shows a friendly alert. Splitting this out
     * keeps the {@code @FXML} handler tiny.
     */
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


    /**
     * Shows a chooser dialog listing every other player, then resolves the
     * snowball action via {@code GameManager}: logs the event, flashes the
     * target's damage frame, plays the SFX and ends the turn.
     *
     * @param current the attacker (current turn)
     * @param targets every other player still on the board
     */
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


    /////////////////////////////
    ///      SAVE / EXIT     ///
    /////////////////////////////

    /**
     * "Save Game" handler. Prompts for a save name and persists the board,
     * the turn controller, the seal state and the winner (when applicable)
     * through {@link SaveLoadService}. Duplicate names are rejected by the
     * DB layer and reported back as an error alert.
     */
    @FXML
    private void saveGame() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("MyGame");
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter a name to identify your saved game:");
        dialog.setContentText("Name:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean ok = SaveLoadService.saveGame(name, this.gameBoard, this.turnController, this.seal, this.winner);
                if (ok) showAlert("Success", "Game '" + name + "' saved successfully.");
                else     showAlert("Error", "Could not save the game. The name may already exist.");
            }
        });
    }


    /////////////////////////////
    ///   TURN / DICE LOGIC  ///
    /////////////////////////////

    /**
     * Master routine for "the player just rolled X". Disables the action
     * buttons while animations play, thaws the player if they were frozen,
     * fires the dice GIF, then the result badge, then walks the pingu to
     * its new square via {@link #runDiceMovement}.
     *
     * @param diceResult the value rolled (or forced by debug mode)
     * @param diceType   "Normal" / "Fast" / "Slow" - used purely for logging
     */
    private void processDiceRoll(int diceResult, String diceType) {
        disableActions();
        Player current = getCurrentPlayer();
        int startSquare = current.getSquareIndex();

        // The player is rolling now → if they were frozen from a previous
        // ice-hole landing, thaw them so the sprite reverts on the next draw.
        if (current.isFrozen()) {
            current.setFrozen(false);
            drawBoard();
        }

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


    /**
     * Plays the per-step movement animation and resolves the consequences
     * of the landing square: bear attack, ice hole, broken floor, event
     * pick-up, player-collision war, seal encounter, and the win check.
     *
     * <p>The method is dense because it co-ordinates animation, sound and
     * game-state mutation. Each {@code case} of the switch corresponds to a
     * different square type defined by the game-logic layer.</p>
     */
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
                            // Player fell into an ice hole → freeze sprite
                            // until their next turn's move.
                            current.setFrozen(true);
                            flashDamage(current);
                            break;
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
                                current.setFrozen(true);
                                flashDamage(current);
                                break;
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


    /**
     * Look up a Player in the current game by name (used for seal-turn log results).
     *
     * @param name the player's display name (case-sensitive)
     * @return the matching player, or {@code null} when no such player is in the match
     */
    private Player findPlayerByName(String name) {
        if (name == null) return null;
        for (Entity e : turnController.getAllPlayers()) {
            if (e instanceof Player && name.equals(e.getName())) return (Player) e;
        }
        return null;
    }


    /////////////////////////////
    ///   PLAYER MOVEMENT    ///
    /////////////////////////////

    /**
     * Walks the player one square at a time, ~250 ms per step, by setting
     * the visual square index on each {@link KeyFrame} and triggering a
     * {@code drawBoard()}. After the last frame the {@code onFinished}
     * callback is invoked so the caller can resolve the landing square.
     *
     * <p>The visual position is capped at {@code MAX_SQUARES - 1} so the
     * animation never walks past the final square; the actual game logic
     * runs afterwards in {@link #runDiceMovement}.</p>
     */
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


    /**
     * Advances the turn pointer. When the seal is enabled and the loop has
     * wrapped back to the first player, the seal gets to play its own
     * animated turn before the next human plays.
     */
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


    /**
     * Refreshes the HUD, re-enables the action buttons and logs the new
     * player's turn header into the event history.
     */
    private void startNextPlayerTurn() {
        if (!gameOver) {
            updateHUD();
            enableActions();
            Player next = getCurrentPlayer();
            logEvent("──────────────────");
            logEvent("🎯 " + next.getName() + "'s turn!");
        }
    }


    /////////////////////////////
    ///     SEAL LOGIC       ///
    /////////////////////////////

    /**
     * Runs the seal's animated turn. The {@code seal.playTurn(...)} call
     * returns a list of {@link model.game.ActionResult} entries describing
     * everything that happened (rolls, moves, hits). The method then
     * staggers their UI feedback over a {@link Timeline} so the user can
     * actually follow the sequence visually.
     *
     * <p>When the seal reaches the final square every player has lost and
     * the win screen for the seal is shown instead of the normal one.</p>
     */
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
                // Flash any player hit by the seal during its turn; if the
                // hit lands them in an ice hole, also freeze their sprite.
                Player hitPlayer = findPlayerByName(msg.getPlayerName());
                switch (msg.getType()) {
                    case SEAL_HIT_HOLE:
                        if (hitPlayer != null) hitPlayer.setFrozen(true);
                        flashDamage(hitPlayer);
                        break;
                    case SEAL_HIT_START:
                    case SEAL_PASS:
                        flashDamage(hitPlayer);
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
                model.game.SoundManager.getInstance().playTitleMusic();
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


    /////////////////////////////
    ///   SNOWBALL COMBAT    ///
    /////////////////////////////

    /**
     * Resolves a same-square encounter between two players. If neither has
     * snowballs we just log a friendly "draw" message; otherwise the proper
     * {@link #executeSnowballWar} routine kicks in.
     */
    private void handlePlayerWar(Player attacker, Player defender) {
        int atkBalls = attacker.getInventory().getObjectQuantity(ObjectType.SNOWBALL);
        int defBalls = defender.getInventory().getObjectQuantity(ObjectType.SNOWBALL);

        if (atkBalls == 0 && defBalls == 0) {
            logEvent("⚔️ " + attacker.getName() + " and " + defender.getName() + " meet on the same square, but neither has snowballs!");
        } else {
            executeSnowballWar(attacker, defender);
        }
    }


    /**
     * Delegates the actual rules of the snowball war to {@code GameManager},
     * then flashes the loser's damaged sprite, plays the SFX and starts the
     * grid-shake flash animation.
     */
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


    /////////////////////////////
    ///   EVENT FEEDBACK     ///
    /////////////////////////////

    /**
     * Converts an {@link model.game.ActionResult} returned by the game
     * logic into a human-readable, emoji-prefixed string that fits in the
     * event log and the per-player history. Every possible action type is
     * mapped here so the UI side never has to know about the result codes.
     *
     * @param res the action result from {@code GameManager}
     * @return a single, log-friendly message (empty when {@code res} is null)
     */
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


    /////////////////////////////
    ///     INVENTORY UI     ///
    /////////////////////////////

    /**
     * Re-renders the top-of-screen hotbar for the player whose turn it is.
     * Also enables / disables the fast / slow / snowball buttons based on
     * the player's current item counts.
     */
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


    /**
     * Builds the per-turn hotbar: portrait + name on the left, inventory
     * slots next to it, and the centred game-title banner with empty
     * spacers around it.
     *
     * @param player the player whose turn it is
     * @return a new {@link HBox} ready to be set as the top of the layout
     */
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


    /**
     * Adds an inventory slot for a given item only when the player actually
     * has at least one of it; this avoids cluttering the hotbar with empty
     * placeholders for stuff the player has not picked up yet.
     */
    private void addSlotIfPresent(HBox container, Inventory inv, ObjectType type, String path) {
        int qty = inv.getObjectQuantity(type);
        if (qty > 0) container.getChildren().add(createInventorySlot(loadImage(path), qty));
    }


    /**
     * Renders one inventory slot (icon + quantity badge). Always uses
     * nearest-neighbour scaling on a {@link Canvas} so the pixel-art icons
     * stay crisp at any size.
     */
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


    /**
     * Refreshes the seal status box in the right panel: current square and
     * whether it is currently eating (blocked for N turns) or back to
     * dangerous mode.
     */
    private void updateSealStatus() {
        if (seal != null) {
            sealPositionLabel.setText("📍 Position: Square " + seal.getSquareIndex());
            sealBlockedLabel.setText(seal.isBlocked()
                ? "😴 Eating fish (" + seal.getBlockedTurns() + " turns left)"
                : "⚡ Active & Dangerous!");
        }
    }


    /////////////////////////////
    ///   BOARD RENDERING    ///
    /////////////////////////////

    // The drawing pipeline deliberately avoids reactive bindings on width
    // and height because the board container is itself listened to for
    // resizes - a binding would cause infinite redraw loops.

    /**
     * Entry point of the redraw pipeline. The {@link #isRedrawing} flag
     * stops the resize listener from firing a second redraw while one is
     * already in flight, which would otherwise spin until the stack
     * overflowed.
     */
    private void drawBoard() {
        // Re-entrancy guard: prevents the listener on boardContainer.widthProperty()
        // from triggering a second drawBoard() while we are still inside one.
        if (isRedrawing) {
            // already drawing — skip this call
        } else {
            redrawBoardOnce();
        }
    }


    /**
     * Single redraw pass. Computes the best cell size that fits the current
     * container, sets fixed-size column/row constraints to that value, and
     * rebuilds the cells in snake order (even rows go left-to-right, odd
     * rows go right-to-left, just like a real Snakes-and-Ladders board).
     */
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

            // Snake-pattern coordinate maths: every other row reverses the
            // column index so the visual "path" makes a zig-zag from the
            // start cell (0) all the way to the final square.
            for (int i = 0; i < Board.MAX_SQUARES; i++) {
                int row = i / cols;
                int col = (row % 2 == 0) ? (i % cols) : (cols - 1 - (i % cols));
                grid.add(createCell(i, cellSize), col, row);
            }
        } finally {
            isRedrawing = false;
        }
    }


    /**
     * Builds one cell of the board. The cell stacks:
     * <ol>
     *   <li>a background tile drawn on a {@link Canvas} so it stays crisp,</li>
     *   <li>an optional foreground overlay for special square types,</li>
     *   <li>every player currently on that square,</li>
     *   <li>the seal sprite when the seal is on this square.</li>
     * </ol>
     *
     * <p>All images go through a Canvas because {@code ImageView.setSmooth(false)}
     * is unreliable across JavaFX versions, while explicit
     * {@code setImageSmoothing(false)} on a Canvas is consistent.</p>
     */
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

        // /view numbers overlay - tiny label in the corner with the cell
        // index. Useful for crafting /tp commands.
        if (viewSquareNumbers) {
            Label idxLabel = new Label(String.valueOf(squareIndex));
            int fontPx = Math.max(8, (int) (cellSize * 0.22));
            idxLabel.setStyle(
                "-fx-text-fill: white;"
                + "-fx-background-color: rgba(0,0,0,0.65);"
                + "-fx-padding: 1 4 1 4;"
                + "-fx-font-family: 'Consolas','Courier New',monospace;"
                + "-fx-font-weight: 900;"
                + "-fx-font-size: " + fontPx + "px;"
            );
            StackPane.setAlignment(idxLabel, Pos.TOP_LEFT);
            cell.getChildren().add(idxLabel);
        }

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


    /**
     * Picks the correct background-tile PNG for a given square. The start
     * (0) and end ({@code MAX_SQUARES - 1}) cells have dedicated artwork.
     * The remaining cells use one of five edge / interior variants based on
     * where the cell sits in its row, so the visual joins line up.
     *
     * @param squareIndex linear index of the cell in snake order
     * @return classpath path to the PNG to use
     */
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


    /**
     * Returns the foreground overlay sprite for a given square type (bear,
     * ice hole, sled...), or {@code null} for normal squares which have no
     * overlay. The PNG name on disk matches the enum constant.
     */
    private Image getForegroundImageForType(SquareType type) {
        if (type == null || type == SquareType.NORMAL) return null;
        return loadImage("/assets/sprites/squares/foreground/" + type.name() + ".png");
    }


    /**
     * Adds every player currently standing on this square to the cell. The
     * heavy lifting (sprite picking, tinting, centering) is delegated to
     * {@link #renderPlayersOnCell}.
     */
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


    /**
     * Renders one or more player sprites inside a single cell.
     *
     * <p>Each player token is a {@link StackPane} with two canvases: the
     * base silhouette and a colour overlay tinted via {@link Lighting} to
     * the player's chosen hue. Damaged / frozen states swap the base sprite
     * out for the matching frame; frozen also skips the colour overlay
     * because the ice sprite already includes its own colour.</p>
     *
     * <p>Tokens are spaced horizontally so they remain visible when several
     * players share a square. While debug mode is on, drag handlers are
     * attached so the developer can teleport any pingu by dragging it.</p>
     */
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

            // Sprite priority: damaged (short flash) > frozen (persistent) > idle.
            // Damaged/frozen frames have no colour-overlay variant → reuse the idle
            // colour overlay so the player's tint still shows.
            Image baseSprite;
            if (player.isDamaged()) {
                baseSprite = rowFacesRight ? damagedRightImage : damagedLeftImage;
            } else if (player.isFrozen()) {
                baseSprite = rowFacesRight ? iceRightImage : iceLeftImage;
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

                // The ice sprite already carries its own colour and replaces
                // the regular pingu silhouette, so the tintable colour overlay
                // would either be invisible or paint over the ice. Skip it.
                if (player.isFrozen()) {
                    playerToken.getChildren().add(baseCanvas);
                } else {
                    Canvas colorCanvas = new Canvas(spriteSize, spriteSize);
                    GraphicsContext gcColor = colorCanvas.getGraphicsContext2D();
                    gcColor.setImageSmoothing(false);
                    gcColor.drawImage(colorSprite, 0, 0, sw, sh, dx, dy, dw, dh);

                    Lighting lighting = new Lighting(new Light.Distant(45, 90, getColorFromHex(player.getColour())));
                    lighting.setSurfaceScale(0.0);
                    colorCanvas.setEffect(lighting);

                    playerToken.getChildren().addAll(baseCanvas, colorCanvas);
                }
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


    /////////////////////////////
    ///   DICE ANIMATION     ///
    /////////////////////////////

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


    /**
     * Plays the rolling-dice GIF in the centre of the board as an overlay.
     * Falls through to {@code onFinished} immediately when the GIF resource
     * cannot be located so the game still progresses.
     */
    private void showDiceAnimation(Runnable onFinished) {
        InputStream is = getClass().getResourceAsStream("/assets/dice/dados.gif");
        if (is == null) {
            System.err.println("Dice animation not found: /assets/dice/dados.gif");
            if (onFinished != null) onFinished.run();
        } else {
            playDiceGifOverlay(is, onFinished);
        }
    }


    /**
     * Sub-routine of {@link #showDiceAnimation} that builds and times the
     * fade-in / hold / fade-out lifecycle of the dice GIF. Once the fade-out
     * ends the overlay is removed and the supplied callback is invoked.
     */
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


    /**
     * Quick double-blink of the whole board to emphasise that a snowball
     * has just been thrown. Auto-reversing the fade does the blink without
     * a separate "fade back" transition.
     */
    private void animateSnowballThrow() {
        FadeTransition flash = new FadeTransition(Duration.millis(100), grid);
        flash.setFromValue(1.0); flash.setToValue(0.7);
        flash.setCycleCount(4); flash.setAutoReverse(true);
        flash.play();
    }


    /**
     * Stronger, longer flash than {@link #animateSnowballThrow}, used when
     * two players battle on the same square (snowball war).
     */
    private void animateWarFlash() {
        FadeTransition flash = new FadeTransition(Duration.millis(150), grid);
        flash.setFromValue(1.0); flash.setToValue(0.5);
        flash.setCycleCount(6); flash.setAutoReverse(true);
        flash.play();
    }


    /////////////////////////////
    ///       HELPERS        ///
    /////////////////////////////

    /**
     * Convenience accessor that casts the {@code TurnController}'s current
     * entity back to {@link Player}. Safe because the controller only ever
     * adds Players (and the seal, which takes its own turn through a
     * separate code path).
     */
    private Player getCurrentPlayer() { return (Player) turnController.getCurrentTurn(); }


    /**
     * Appends a single line to the global event history. Empty / null lines
     * are dropped so the {@code History} dialog stays readable.
     */
    private void logEvent(String message) {
        if (message != null && !message.isBlank()) {
            eventHistoryFull.add(message);
        }
    }


    /**
     * "History" button handler. Opens a modal dialog with a non-editable
     * text area containing every event logged so far, line by line.
     */
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


    /**
     * Disables every action button. Called for the duration of animations
     * and during the seal's turn so the player can't click on anything
     * while the screen is busy.
     */
    private void disableActions() {
        rollDiceButton.setDisable(true);
        rollFastDiceButton.setDisable(true);
        rollSlowDiceButton.setDisable(true);
        throwSnowballButton.setDisable(true);
    }


    /**
     * Re-enables only the default-dice button. The fast / slow / snowball
     * buttons are toggled separately in {@link #updateHUD} based on the
     * current player's inventory, so a player without items doesn't see
     * misleadingly active buttons.
     */
    private void enableActions() {
        rollDiceButton.setDisable(false);
        // fast/slow/snowball re-enabled by updateHUD()
    }


    /**
     * Shows a non-blocking information alert. Wrapped in
     * {@code Platform.runLater(...)} so it is always run on the JavaFX
     * thread, regardless of where the call originated.
     */
    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }


    /**
     * Pads a hex string out to six characters by prepending zeroes; required
     * because the colour-picker sometimes returns a shorter representation
     * (e.g. "FF00") that JavaFX's {@code Color.web} cannot parse.
     */
    private String padColor(String hex) {
        if (hex == null) return "FFFFFF";
        hex = hex.trim();
        if (hex.length() < 6) hex = "0".repeat(6 - hex.length()) + hex;
        return hex;
    }


    /**
     * Parses a hex string (3 or 6 digits, no "#") to a JavaFX {@link Color},
     * defaulting to grey if the conversion fails so the sprite tinting code
     * never throws on bad input.
     */
    private Color getColorFromHex(String hex) {
        try { return Color.web("#" + padColor(hex)); }
        catch (Exception e) { return Color.GRAY; }
    }


    /////////////////////////////
    ///     WIN ANIMATION    ///
    /////////////////////////////

    /**
     * Called by the game-logic layer when a player has reached the final
     * square. Marks the game as over, disables actions, switches the music
     * back to the title track, persists the result to the leaderboard and
     * shows the cinematic win overlay.
     *
     * @param winner the {@link Player} who just won
     */
    public void handleWin(Player winner) {
        gameOver = true;
        this.winner = winner.getName();
        disableActions();
        model.game.SoundManager.getInstance().playTitleMusic();
        logEvent("🏆 GAME OVER! " + winner.getName() + " HAS WON THE GAME! 🏆");
        SaveLoadService.recordGameResult(turnController.getAllPlayers(), winner.getName());
        drawBoard();
        showWinAnimation(winner);
    }


    /////////////////////////////
    ///  CINEMATIC ANIMATIONS ///
    /////////////////////////////

    /**
     * Pops a giant bear sprite in the middle of the screen, scales it from
     * 0 to 2x in 300 ms and then fades it out. Used when a player lands on
     * a BEAR square with no fish to bribe.
     */
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


    /**
     * Slides a large seal sprite in from the right edge of the screen and
     * fades it out after a short hold. Plays whenever a seal interaction
     * happens (player landing on the seal, or seal hitting a player).
     */
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


    /**
     * Convenience overload that builds the winner's character sprite and
     * forwards to the generic {@link #showWinAnimation(String, Node)}.
     */
    private void showWinAnimation(Player winner) {
        showWinAnimation(winner.getName(), buildPlayerCharacterNode(winner, 200));
    }


    /**
     * Variant of the win animation used when the seal reaches the final
     * square and beats every human player.
     */
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
            model.game.SoundManager.getInstance().playTitleMusic();
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


    /**
     * @return the in-memory board currently being played; mostly useful for
     *         tests or save/load integration that needs to peek at the state
     */
    public Board getCurrentGameBoard() { return this.gameBoard; }


    /////////////////////////////
    ///     NAVIGATION       ///
    /////////////////////////////

    /**
     * "Back" handler. Bounces to the player-setup screen, with a confirm
     * dialog if there is still a game in progress that would be lost.
     */
    @FXML
    private void handleBack() {
        if (confirmLeaveIfNeeded("Leave Game?", "You will lose your current game progress!", "Go back to Player Setup?")) {
            model.game.SoundManager.getInstance().playTitleMusic();
            navigateTo("/view/fxml/playerSetup.fxml", "/assets/css/style.css");
        }
    }


    /**
     * "Return to Menu" handler. Same as {@link #handleBack()} but jumps all
     * the way back to the main menu instead of the player-setup screen.
     */
    @FXML
    private void handleReturnToMenu() {
        if (confirmLeaveIfNeeded("Return to Menu?", "You will lose your current game progress!", "Return to Main Menu?")) {
            model.game.SoundManager.getInstance().playTitleMusic();
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


    /**
     * Generic helper that loads the FXML at {@code fxmlPath}, optionally
     * attaches a stylesheet, and swaps the result into the current
     * {@link Stage}. Used by both the back / menu navigation and the win
     * screen's "back to menu" button.
     */
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
            // Leaving the game scene - unregister from the debug console so
            // its commands stop targeting a board that is no longer visible.
            model.game.DebugConsoleService.getInstance().setActiveBoardController(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /////////////////////////////
    ///  DEBUG-CONSOLE API    ///
    /////////////////////////////

    /** Exposed for {@code model.game.DebugConsoleService} command implementations. */
    public TurnController getTurnController() { return turnController; }

    /** Exposed for the debug console; {@code null} if the seal is disabled. */
    public Seal getSeal() { return seal; }

    public boolean isSealEnabled() { return sealEnabled; }

    /** Public redraw entry point used by the debug console after mutating game state. */
    public void requestRedraw() {
        javafx.application.Platform.runLater(() -> {
            drawBoard();
            updateHUD();
        });
    }

    /**
     * Sets (or clears with {@code null}) the value that {@code rollOrForce}
     * will return on the next dice roll. Called by the {@code /setdice} and
     * {@code /reset} console commands.
     */
    public void setDebugForcedDice(Integer value) {
        this.debugForcedDice = value;
        javafx.application.Platform.runLater(this::updateDebugBanner);
    }

    /**
     * Applies the global "show square numbers" flag and triggers a redraw so
     * the change is visible immediately.
     */
    public void applyDebugViewNumbers(boolean enabled) {
        this.viewSquareNumbers = enabled;
        javafx.application.Platform.runLater(this::drawBoard);
    }
}
