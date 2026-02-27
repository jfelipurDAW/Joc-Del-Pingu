package gamePanel;

import board.Board;
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
    	
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameBoard.fxml"));
        Parent root = loader.load();

        // Create the scene with the loaded FXML root node
        Scene scene = new Scene(root);

        // Set the scene on the primary stage
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(774);
        primaryStage.setMinHeight(546);
        primaryStage.setTitle((String) LangConfig.getLang(Lang.TEXT_GAME_TITLE));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
    	// Load the Lang file
    	LangConfig.loadLang();
    	
//    	Board board = new Board();
//    	board.createNewBoard();
        launch(args);
    }
    
}
