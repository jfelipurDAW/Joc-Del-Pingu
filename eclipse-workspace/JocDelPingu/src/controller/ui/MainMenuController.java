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

public class MainMenuController {

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
    private Text titleText;

    @FXML
    private Text titleShadowText;

    @FXML
    public void initialize() {

        addBackgroundImage();
        // Register listener for dynamic language updates
        LangConfig.addLanguageChangeListener(this::refreshTexts);

        // Removed addBitmapTitle() to use text instead of image-based font
        refreshTexts(); // use the central method to set initial texts

        // Changed: populate language dropdown with available languages
        setupLanguageDropdown();

        // Background music starts as soon as the main menu opens (idempotent
        // call) and is restored to menu volume in case the user is returning
        // from a game where it had been ducked.
        model.game.SoundManager.getInstance().startBackgroundMusic();
        model.game.SoundManager.getInstance().restoreMenuVolume();
    }

    /**
     * Added: initializes the language ComboBox with display names
     * and wires the selection listener to switch languages.
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
//        System.out.println("Afegint text bitmap...");
//
//        Group title = CustomBitmapFont.getInstance()
//                .createText("JOC DEL PINGU", 180, 40, 4.0);
//
//        // Opcional: força posicions a píxels enters per evitar subpíxels
//        title.setTranslateX(Math.round(title.getTranslateX()));
//        title.setTranslateY(Math.round(title.getTranslateY()));
//
//        // Opcional: cache al grup del títol
//        // title.setCache(true);
//        // title.setCacheHint(CacheHint.SPEED);
//
//        System.out.println("Text creat amb " + title.getChildren().size() + " caràcters");
//
//        rootPane.getChildren().add(title);
//    }

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

    @FXML
    private void handleLoadGame() {
        // 1. Buscamos todas las partidas guardadas en Oracle
        List<String> partidas = model.game.SaveLoadService.getAllSavedGameIds();

        if (partidas.isEmpty()) {
            System.out.println("No hay partidas guardadas.");
        } else {
            promptLoadGameChoice(partidas);
        }
    }

    /**
     * Shows the saved-games picker dialog and loads the selected one.
     * Extracted so handleLoadGame() does not need an early empty return.
     */
    private void promptLoadGameChoice(List<String> partidas) {
        // 2. Abrimos el selector para que el usuario elija
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(partidas.get(0), partidas);
        dialog.setTitle("Cargar Partida");
        dialog.setHeaderText("Selecciona la partida que quieres jugar:");
        dialog.setContentText("ID de partida:");

        java.util.Optional<String> result = dialog.showAndWait();

        // 3. Si el usuario selecciona una...
        result.ifPresent(gameId -> {
            boolean success = model.game.SaveLoadService.loadGame(gameId);
            if (success) {
                System.out.println("Partida " + gameId + " cargada. Verificando jugadores...");
                if (authenticateLoadedPlayers()) {
                    enterLoadedGameScene();
                } else {
                    System.out.println("Autenticación cancelada o fallida — vuelve al menú.");
                    // Reset the loaded-game flag so future navigation doesn't
                    // accidentally re-enter the half-loaded state.
                    model.config.GameSetupConfig.setLoadedGame(false);
                }
            } else {
                System.out.println("Error al cargar la partida seleccionada.");
            }
        });
    }

    /**
     * For each player in the just-loaded game, asks for their password and
     * checks it against the encrypted DB value. Stops at the first failure.
     * Returns true only if every player authenticates successfully.
     */
    private boolean authenticateLoadedPlayers() {
        java.util.List<model.entity.Player> players = model.config.GameSetupConfig.getPlayers();
        if (players == null || players.isEmpty()) {
            return true; // nothing to authenticate; let the game proceed
        }
        for (model.entity.Player p : players) {
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

    /**
     * Changed: refreshes all translatable text after a language change.
     * Called from the ComboBox selection listener.
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