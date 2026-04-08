package gamePanel;

import java.io.InputStream;

import CustomBitmapFont.CustomBitmapFont;
import config.Lang;
import config.LangConfig;
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
        // Removed addBitmapTitle() to use text instead of image-based font
        titleText.setText((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        newGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));
        loadGame_button.setText((String) LangConfig.getLang(Lang.MENU_BUTTON_LOADGAME));

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
                    refreshTexts();
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

        Image bgImage = new Image(is);

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(rootPane.widthProperty());
        canvas.heightProperty().bind(rootPane.heightProperty());

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        rootPane.getChildren().add(0, canvas);

        // Redraw on resize
        rootPane.widthProperty().addListener((obs, o, n) ->
                drawPixelPerfect(gc, canvas, bgImage)
        );

        rootPane.heightProperty().addListener((obs, o, n) ->
                drawPixelPerfect(gc, canvas, bgImage)
        );

        drawPixelPerfect(gc, canvas, bgImage);
    }
    private void updateScale(ImageView bgView, Image bgImage) {

        double paneWidth = rootPane.getWidth();
        double paneHeight = rootPane.getHeight();

        double imgWidth = bgImage.getWidth();
        double imgHeight = bgImage.getHeight();

        // Automatic scale
        double scale = Math.floor(Math.min(
                paneWidth / imgWidth,
                paneHeight / imgHeight
        ));

        if (scale < 1) scale = 1;

        bgView.setFitWidth(imgWidth * scale);
        bgView.setFitHeight(imgHeight * scale);
    }

    private void drawPixelPerfect(GraphicsContext gc, Canvas canvas, Image img) {

        double paneW = canvas.getWidth();
        double paneH = canvas.getHeight();

        double imgW = img.getWidth();
        double imgH = img.getHeight();

        //  Escala ENTERA que cubra TODA la pantalla
        double scale = Math.ceil(Math.max(paneW / imgW, paneH / imgH));

        if (scale < 1) scale = 1;

        double drawW = imgW * scale;
        double drawH = imgH * scale;

        //  Centrado horizontalmente, pegado al bottom
        double x = Math.floor((paneW - drawW) / 2);
        double y = paneH - drawH;

        gc.setImageSmoothing(false);

        // Limpia
        gc.clearRect(0, 0, paneW, paneH);

        //  Dibujar ocupando todo
        gc.drawImage(img, x, y, drawW, drawH);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playerSetup.fxml"));
            Parent playerSetupRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene setupScene = new Scene(playerSetupRoot);
            stage.setScene(setupScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadGame() {
        System.out.println("Load Game clicked");
        boolean success = main.SaveLoadService.loadGame("SAVE_SLOT_1");
        if (success) {
            System.out.println("Load successful. Starting game...");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("gameBoard.fxml"));
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
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
    }
}