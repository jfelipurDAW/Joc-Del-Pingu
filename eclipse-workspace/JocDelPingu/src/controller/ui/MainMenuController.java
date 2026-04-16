package controller.ui;

import java.io.InputStream;

import view.font.CustomBitmapFont;
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
    private Text titleText;

    @FXML
    public void initialize() {
        

        addBackgroundImage();
        // Register listener for dynamic language updates
        LangConfig.addLanguageChangeListener(this::refreshTexts);
        
        // Removed addBitmapTitle() to use text instead of image-based font
        refreshTexts(); // use the central method to set initial texts

        // Changed: populate language dropdown with available languages
        setupLanguageDropdown();
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
            if (selectedDisplay == null) return;

            // Find the language code matching the selected display name
            for (String code : LangConfig.getAvailableLanguages()) {
                if (LangConfig.getDisplayName(code).equals(selectedDisplay)) {
                    LangConfig.loadLang(code);
                    System.out.println("Language changed to: " + code);
                    // UI refresh is handled automatically by the language listener
                    break;
                }
            }
        });
    }

    private void addBackgroundImage() {

        String path = "/assets/sprites/backgrounds/1.png";
        InputStream is = getClass().getResourceAsStream(path);

        if (is == null) {
            System.out.println("404: Image Not Found");
            return;
        }

        Image bgImage = new Image(is, 0, 0, true, true); // smooth scaling

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
    private void handleLoadGame() {
        System.out.println("Load Game clicked");
        boolean success = model.game.SaveLoadService.loadGame("SAVE_SLOT_1");
        if (success) {
            System.out.println("Load successful. Starting game...");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/gameBoard.fxml"));
                Parent gameBoardRoot = loader.load();

                Scene currentScene = rootPane.getScene();
                Stage stage = (Stage) currentScene.getWindow();

                Scene setupScene = new Scene(gameBoardRoot);
                stage.setScene(setupScene);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to load game. Start a new one instead.");
        }
    }

    /**
     * Changed: refreshes all translatable text after a language change.
     * Called from the ComboBox selection listener.
     */
    private void refreshTexts() {
        titleText.setText((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        newGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));
        loadGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_LOADGAME));

        // Update window title to match selected language
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        }
    }
}