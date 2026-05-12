package controller.main;

import model.config.Lang;
import model.config.LangConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * JavaFX entry point for the whole "Joc del Pingu" game.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Pre-loads the custom pixel-art fonts used by every screen so they
 *       are available the moment any FXML is parsed.</li>
 *   <li>Loads the main menu FXML and applies the global stylesheet.</li>
 *   <li>Configures the primary {@link Stage}: starting size, fullscreen
 *       handling, window title and centring.</li>
 *   <li>In {@link #main(String[])}, loads the language YAML before
 *       launching the JavaFX runtime so the first scene already shows
 *       the right strings.</li>
 * </ul>
 */
public class MainMenu extends Application {


    /////////////////////////////
    ///   APPLICATION ENTRY   ///
    /////////////////////////////

    /**
     * Called by the JavaFX runtime once it has finished initializing.
     * Builds the main menu scene and shows the primary window.
     *
     * @param primaryStage the top-level window provided by JavaFX
     * @throws Exception if the FXML or stylesheet cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load custom fonts
        // These pixel-art fonts must be registered before any FXML parses
        // styles that reference them; otherwise JavaFX silently falls back
        // to the default font and the menu looks wrong on first paint.
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
            // Font loading failure is non-fatal - log it and keep going.
            e.printStackTrace();
        }


        /////////////////////////////
        ///   SCENE CONSTRUCTION  ///
        /////////////////////////////

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/mainMenu.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/assets/css/style.css").toExternalForm()
        );

        primaryStage.setScene(scene);


        /////////////////////////////
        ///    STAGE BEHAVIOUR    ///
        /////////////////////////////

        // Initial size — tall enough for the bigger pixel-art buttons
        primaryStage.setWidth(900);
        primaryStage.setHeight(720);

        //  Allow Rescale
        primaryStage.setResizable(true);

        // Remember the windowed dimensions before switching to fullscreen so
        // we can restore the exact same size when the user comes back out
        // (JavaFX otherwise keeps the fullscreen dimensions on the Stage).
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

        // Window title is translated through LangConfig so it follows the
        // selected language at launch time.
        primaryStage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }


    /////////////////////////////
    ///       MAIN(args)      ///
    /////////////////////////////

    /**
     * Program entry point. Loads the language strings before delegating to
     * {@link Application#launch(String...)} so the very first frame of the
     * UI is already localised.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {

        // Language must be loaded before launch() because the FXML controllers
        // call LangConfig.getLang(...) in their initialize() methods.
        LangConfig.loadLang();
        launch(args);
    }

}
