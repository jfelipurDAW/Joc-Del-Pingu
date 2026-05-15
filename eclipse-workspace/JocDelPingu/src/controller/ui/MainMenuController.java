package controller.ui;

import java.io.InputStream;
import java.util.List;

import model.config.Lang;
import model.config.LangConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;


/**
 * Controller for the main menu screen ({@code mainMenu.fxml}).
 *
 * <p>The main menu offers four entry points:</p>
 * <ul>
 *   <li><b>New Game</b> - opens the player-setup screen.</li>
 *   <li><b>Load Game</b> - lists saved games, asks each loaded player for
 *       their password and, on success, jumps straight into the board.</li>
 *   <li><b>Stats</b> - shows the global leaderboard.</li>
 *   <li><b>Language</b> - dropdown that swaps the UI language at runtime,
 *       refreshing every text via the {@link LangConfig} listener.</li>
 * </ul>
 *
 * <p>The controller is also responsible for starting the menu music track
 * the moment the scene initialises.</p>
 */
public class MainMenuController {


    /////////////////////////////
    ///   FXML INJECTIONS    ///
    /////////////////////////////

    @FXML
    private StackPane rootPane;

    @FXML
    private Button newGame_button;

    @FXML
    private Button loadGame_button;

    // Changed: replaced Button with ComboBox for language dropdown
    @FXML
    private ComboBox<String> language_combobox;

    @FXML
    private Button stats_button;

    @FXML
    private Button console_button;

    // Held as static so the same Console window is reused if the user
    // clicks the button several times - prevents stacking multiple consoles.
    private static Stage consoleStage;

    @FXML
    private Text titleText;

    @FXML
    private Text titleShadowText;


    /////////////////////////////
    ///     INITIALIZATION    ///
    /////////////////////////////

    /**
     * FXML lifecycle hook. Called automatically by JavaFX after every
     * {@code @FXML} field has been injected. Sets up the background, wires
     * the language dropdown, registers a translation listener and starts
     * the title music.
     */
    @FXML
    public void initialize() {

        addBackgroundImage();

        // Register listener for dynamic language updates
        // Each translatable widget is re-read by refreshTexts() whenever the
        // user picks another language from the combobox.
        LangConfig.addLanguageChangeListener(this::refreshTexts);

        // Removed addBitmapTitle() to use text instead of image-based font
        refreshTexts(); // use the central method to set initial texts

        // Changed: populate language dropdown with available languages
        setupLanguageDropdown();

        // Title-screen music starts as soon as the main menu opens. The call
        // also restarts the track from zero so coming back from a game makes
        // the menu music begin again from the beginning.
        model.game.SoundManager.getInstance().playTitleMusic();
    }


    /////////////////////////////
    ///   LANGUAGE DROPDOWN   ///
    /////////////////////////////

    /**
     * Added: initializes the language ComboBox with display names
     * and wires the selection listener to switch languages.
     *
     * <p>The dropdown shows human-friendly names (e.g. "English") but
     * internally we work with the language codes returned by {@link LangConfig}.
     * {@link #applyLanguageByDisplayName(String)} translates back from the
     * picked label to the matching code.</p>
     */
    private void setupLanguageDropdown() {
        language_combobox.getItems().clear();
        for (String code : LangConfig.getAvailableLanguages()) {
            language_combobox.getItems().add(LangConfig.getDisplayName(code));
        }

        // Pre-select the currently active language
        language_combobox.setValue(LangConfig.getDisplayName(LangConfig.getCurrentLang()));

        // On selection change -> load the chosen language and refresh UI text
        language_combobox.setOnAction(event -> {
            String selectedDisplay = language_combobox.getValue();
            if (selectedDisplay != null) {
                applyLanguageByDisplayName(selectedDisplay);
            }
        });
    }


    /**
     * Loads the language whose display name matches the given value.
     * Uses an indexed loop with a found-flag so neither break nor an
     * empty return is needed.
     *
     * @param displayName the human-friendly language label picked in the combobox
     */
    private void applyLanguageByDisplayName(String displayName) {
        String[] codes = LangConfig.getAvailableLanguages();
        boolean done = false;
        for (int i = 0; i < codes.length && !done; i++) {
            String code = codes[i];
            if (LangConfig.getDisplayName(code).equals(displayName)) {
                LangConfig.loadLang(code);
                System.out.println("Language changed to: " + code);
                // UI refresh is handled automatically by the language listener
                done = true;
            }
        }
    }


    /////////////////////////////
    ///   BACKGROUND IMAGE    ///
    /////////////////////////////

    /**
     * Adds the menu background as a separate node behind the rest of the UI.
     *
     * <p>We deliberately do not assign the background directly to {@code rootPane}
     * because the CSS class {@code mainmenu_bg} on that pane would override
     * it. Instead a {@link javafx.scene.layout.Region} is inserted at index 0
     * (bottom-most) and its size is bound to the parent so it tracks resizing.</p>
     */
    private void addBackgroundImage() {

        String path = "/assets/sprites/backgrounds/1.png";
        InputStream is = getClass().getResourceAsStream(path);

        if (is == null) {
            System.out.println("404: Image Not Found");
        } else {
            Image bgImage = new Image(is, 0, 0, true, false); // pixel-perfect scaling

            javafx.scene.layout.BackgroundImage backgroundImage = new javafx.scene.layout.BackgroundImage(
                    bgImage,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundPosition.CENTER,
                    new javafx.scene.layout.BackgroundSize(
                            javafx.scene.layout.BackgroundSize.AUTO,
                            javafx.scene.layout.BackgroundSize.AUTO,
                            false, false, false, true // cover = true
                    )
            );

            // Fix: Do NOT assign it to rootPane directly because mainmenu_bg CSS overwrites it.
            // Instead, retain the background as a Node in the layout graph (just like the old Canvas was).
            javafx.scene.layout.Region bgNode = new javafx.scene.layout.Region();
            bgNode.setBackground(new javafx.scene.layout.Background(backgroundImage));

            // Bind the sizing accurately to the parent container
            bgNode.prefWidthProperty().bind(rootPane.widthProperty());
            bgNode.prefHeightProperty().bind(rootPane.heightProperty());

            // Add to the bottom-most layer
            rootPane.getChildren().add(0, bgNode);
        }
    }

//    private void addBitmapTitle() {
//        System.out.println("Adding bitmap text...");
//
//        Group title = CustomBitmapFont.getInstance()
//                .createText("JOC DEL PINGU", 180, 40, 4.0);
//
//        // Optional: snap positions to integer pixels to avoid subpixel rendering
//        title.setTranslateX(Math.round(title.getTranslateX()));
//        title.setTranslateY(Math.round(title.getTranslateY()));
//
//        // Optional: cache on the title group
//        // title.setCache(true);
//        // title.setCacheHint(CacheHint.SPEED);
//
//        System.out.println("Text created with " + title.getChildren().size() + " characters");
//
//        rootPane.getChildren().add(title);
//    }


    /////////////////////////////
    ///   BUTTON HANDLERS    ///
    /////////////////////////////

    /**
     * "New Game" button handler. Loads the player-setup FXML and replaces
     * the current scene without recreating the {@link Stage}.
     */
    @FXML
    private void handleNewGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/playerSetup.fxml"));
            Parent playerSetupRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene setupScene = new Scene(playerSetupRoot);
            setupScene.getStylesheets().add(
                getClass().getResource("/assets/css/style.css").toExternalForm()
            );
            stage.setScene(setupScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * "Console" button handler. Opens (or focuses) a separate modeless
     * {@link Stage} that holds the debug console. The console window is
     * completely independent of the game window: closing it never disturbs
     * the running game, and the player can leave it open across screens.
     */
    @FXML
    private void handleOpenConsole() {
        try {
            // If a previous console window is still alive, just bring it to
            // front and focus the input - no need to create another one.
            if (consoleStage != null && consoleStage.isShowing()) {
                consoleStage.toFront();
                consoleStage.requestFocus();
                return;
            }

            // Try multiple lookup strategies so this works whether the JAR is
            // on the classpath, in a named module, or behind the jar-in-jar
            // loader. In named modules a leading-slash path can fail when the
            // FXML lives in a package that has no Java classes; using the
            // ClassLoader fallback or loading via a stream sidesteps that.
            java.net.URL fxmlUrl = MainMenuController.class.getResource("/view/fxml/debugConsole.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = MainMenuController.class.getClassLoader().getResource("view/fxml/debugConsole.fxml");
            }

            FXMLLoader loader = new FXMLLoader();
            Parent root;
            if (fxmlUrl != null) {
                loader.setLocation(fxmlUrl);
                root = loader.load();
            } else {
                // Last-resort fallback: load directly from a stream. This
                // bypasses the URL plumbing entirely and works as long as
                // the JAR contains the resource at all.
                java.io.InputStream is = MainMenuController.class.getResourceAsStream("/view/fxml/debugConsole.fxml");
                if (is == null) {
                    is = MainMenuController.class.getClassLoader().getResourceAsStream("view/fxml/debugConsole.fxml");
                }
                if (is == null) {
                    System.err.println("debugConsole.fxml not found on classpath.");
                    return;
                }
                root = loader.load(is);
                is.close();
            }

            Scene scene = new Scene(root);
            try {
                java.net.URL css = MainMenuController.class.getResource("/assets/css/style.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception cssIgnored) {
                // optional - the console has its own inline styles
            }
            consoleStage = new Stage();
            consoleStage.setTitle("Joc del Pingu - Debug Console");
            consoleStage.setScene(scene);
            // No modality: the console floats over (or beside) the game window
            // without blocking input on either side.
            consoleStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * "Stats" button handler. Loads the player-statistics FXML and swaps
     * the current scene for the leaderboard view.
     */
    @FXML
    private void handleStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/playerStats.fxml"));
            Parent statsRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene statsScene = new Scene(statsRoot);
            statsScene.getStylesheets().add(
                getClass().getResource("/assets/css/style.css").toExternalForm()
            );
            stage.setScene(statsScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * "Load Game" button handler. Queries the database for every saved-game
     * id; if any exist, opens a chooser dialog to let the user pick one.
     */
    @FXML
    private void handleLoadGame() {
        // 1. Fetch every saved-game id from Oracle
        List<String> savedGames = model.game.SaveLoadService.getAllSavedGameIds();

        if (savedGames.isEmpty()) {
            System.out.println("No saved games found.");
        } else {
            promptLoadGameChoice(savedGames);
        }
    }


    /**
     * Shows the saved-games picker dialog and loads the selected one.
     * Extracted so handleLoadGame() does not need an early empty return.
     *
     * <p>The flow is: pick id -> load board/players from DB -> ask each
     * player for their password -> jump to the gameBoard scene on success.</p>
     *
     * @param savedGames list of saved-game ids returned by the DB layer
     */
    private void promptLoadGameChoice(List<String> savedGames) {
        // 2. Open the picker so the user can choose one
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(savedGames.get(0), savedGames);
        dialog.setTitle("Load Game");
        dialog.setHeaderText("Select the saved game you want to play:");
        dialog.setContentText("Game id:");

        java.util.Optional<String> result = dialog.showAndWait();

        // 3. If the user selects one...
        result.ifPresent(gameId -> {
            boolean success = model.game.SaveLoadService.loadGame(gameId);
            if (success) {
                System.out.println("Game " + gameId + " loaded. Verifying players...");
                if (authenticateLoadedPlayers()) {
                    enterLoadedGameScene();
                } else {
                    System.out.println("Authentication cancelled or failed — back to menu.");
                    // Reset the loaded-game flag so future navigation doesn't
                    // accidentally re-enter the half-loaded state.
                    model.config.GameSetupConfig.setLoadedGame(false);
                }
            } else {
                System.out.println("Error loading the selected saved game.");
            }
        });
    }


    /**
     * For each player in the just-loaded game, asks for their password and
     * checks it against the encrypted DB value. Stops at the first failure.
     * Returns true only if every player authenticates successfully.
     *
     * @return {@code true} when every player's password matched, {@code false} otherwise
     */
    private boolean authenticateLoadedPlayers() {
        java.util.List<model.entity.Player> players = model.config.GameSetupConfig.getPlayers();
        if (players == null || players.isEmpty()) {
            return true; // nothing to authenticate; let the game proceed
        }
        for (model.entity.Player p : players) {

            // Custom Dialog with a single PasswordField so the typed password
            // is masked while the user types it.
            javafx.scene.control.Dialog<String> pwDialog = new javafx.scene.control.Dialog<>();
            pwDialog.setTitle(LangConfig.getLang(Lang.DIALOG_SELECTPLAYER_TITLE));
            pwDialog.setHeaderText("Enter password for " + p.getName());

            javafx.scene.control.PasswordField pwField = new javafx.scene.control.PasswordField();
            pwField.setPromptText("Password");
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(8, new javafx.scene.control.Label("Player: " + p.getName()), pwField);
            box.setPadding(new javafx.geometry.Insets(10));
            pwDialog.getDialogPane().setContent(box);

            javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            pwDialog.getDialogPane().getButtonTypes().addAll(okBtn, javafx.scene.control.ButtonType.CANCEL);
            pwDialog.setResultConverter(bt -> bt == okBtn ? pwField.getText() : null);

            java.util.Optional<String> entered = pwDialog.showAndWait();
            if (entered.isEmpty()) {
                return false; // user cancelled — abort load
            }
            if (!model.game.SaveLoadService.verifyPassword(p.getName(), entered.get())) {
                javafx.scene.control.Alert wrong = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                wrong.setTitle(LangConfig.getLang(Lang.ALERT_WRONGPASSWORD_TITLE));
                wrong.setHeaderText(null);
                wrong.setContentText(String.format(LangConfig.getLang(Lang.ALERT_WRONGPASSWORD_MESSAGE), p.getName()));
                wrong.showAndWait();
                return false;
            }
        }
        return true;
    }


    /** Switches the scene to the loaded gameBoard FXML. */
    private void enterLoadedGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/gameBoard.fxml"));
            Parent gameBoardRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene setupScene = new Scene(gameBoardRoot);
            setupScene.getStylesheets().add(
                getClass().getResource("/assets/css/style.css").toExternalForm()
            );
            stage.setScene(setupScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /////////////////////////////
    ///    LANGUAGE REFRESH   ///
    /////////////////////////////

    /**
     * Changed: refreshes all translatable text after a language change.
     * Called from the ComboBox selection listener.
     *
     * <p>Includes a small extra step that re-applies the (now translated)
     * title to the {@link Stage} so the OS-level window title also tracks
     * the chosen language.</p>
     */
    private void refreshTexts() {
        String title = (String) LangConfig.getLang(Lang.TEXT_GAME_TITLE);
        titleText.setText(title);
        titleShadowText.setText(title);
        newGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));
        loadGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_LOADGAME));
        stats_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_STATS));

        // Update window title to match selected language
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle(title);
        }
    }
}
