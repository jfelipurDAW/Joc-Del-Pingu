package controller.main;

import model.config.Lang;
import model.config.LangConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load custom fonts
        try {
            Font pixelFont = Font.loadFont(getClass().getResource("/assets/font/pixel-game.regular.otf").toExternalForm(), 10);
            Font extrudeFont = Font.loadFont(getClass().getResource("/assets/font/pixel-game.extrude.otf").toExternalForm(), 10);
            Font unicodeFont = Font.loadFont(getClass().getResource("/assets/font/pixel-unicode-regular.ttf").toExternalForm(), 10);
            if (pixelFont == null) {
                System.out.println("Pixel font not loaded");
            } else {
                System.out.println("Pixel font loaded: " + pixelFont.getFamily());
            }
            if (extrudeFont == null) {
                System.out.println("Extrude font not loaded");
            } else {
                System.out.println("Extrude font loaded: " + extrudeFont.getFamily());
            }
            if (unicodeFont == null) {
                System.out.println("Unicode font not loaded");
            } else {
                System.out.println("Unicode font loaded: " + unicodeFont.getFamily());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/mainMenu.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/assets/css/style.css").toExternalForm()
        );

        primaryStage.setScene(scene);

        // Initial size — tall enough for the bigger pixel-art buttons
        primaryStage.setWidth(900);
        primaryStage.setHeight(720);

        //  Allow Rescale
        primaryStage.setResizable(true);

        final double[] savedDimensions = { 900, 720 };
        primaryStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (isFullScreen) {
                // Save current dimensions before going fullscreen
                savedDimensions[0] = primaryStage.getWidth();
                savedDimensions[1] = primaryStage.getHeight();
            } else {
                // Restore saved dimensions when exiting fullscreen
                primaryStage.setWidth(savedDimensions[0]);
                primaryStage.setHeight(savedDimensions[1]);
                primaryStage.centerOnScreen();
            }
        });

        primaryStage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    public static void main(String[] args) {


        LangConfig.loadLang();
        launch(args);
    }
    
}