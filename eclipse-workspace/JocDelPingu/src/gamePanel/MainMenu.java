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
        // Posem les propietats de renderitzat el més aviat possible
        // "t2k" → millor per text en alguns casos; prova també "d3d" o "sw" si estàs en Windows
    	System.setProperty("prism.order", "sw");
    	System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        // Opcional: si vols forçar renderitzat per software (més consistent per pixel art, però més lent)
        // System.setProperty("prism.order", "sw");

        // Carrega l'FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
        Parent root = loader.load();

        // Ara pots accedir al controlador si cal (per exemple per ajustar coses dinàmiques)
        // MainMenuController controller = loader.getController();

        // Crea l'escena
        Scene scene = new Scene(root);

        // Configuració de la finestra
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(774);
        primaryStage.setMinHeight(546);
        primaryStage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));

        // Opcional: centra la finestra o ajusta mida inicial
        primaryStage.centerOnScreen();

        primaryStage.show();
    }

    public static void main(String[] args) {
        // Carrega la llengua abans de llançar l'app
        LangConfig.loadLang();

        // Si vols provar el board sense menú, descomenta
        // Board board = new Board();
        // board.createNewBoard();

        launch(args);
    }
}