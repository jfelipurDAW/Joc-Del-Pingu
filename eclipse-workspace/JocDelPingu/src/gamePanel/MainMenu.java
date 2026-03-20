package gamePanel;

import config.Lang;
import config.LangConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        // Initial size
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        //  Allow Rescale
        primaryStage.setResizable(true);

        primaryStage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    public static void main(String[] args) {

        // Solo esto si tienes problema con DPI en Windows
        // System.setProperty("prism.allowhidpi", "false");

        LangConfig.loadLang();
        launch(args);
    }
    
}