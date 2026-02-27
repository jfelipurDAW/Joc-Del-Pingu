package gamePanel;

import CustomBitmapFont.CustomBitmapFont;
import config.Lang;
import config.LangConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenuController {
	
	@FXML
//	private Button newGame_button;
	private StackPane rootPane;
	
	@FXML
    public void initialize() {		
//        newGame_button.setText(LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));
        
        addBitmapTitle();
	}
	
    @FXML
//    private void newGame_button() {
//        Stage stage = (Stage) newGame_button.getScene().getWindow();
//        stage.close();
//    }
    
    private void addBitmapTitle() {
        System.out.println("Afegint text bitmap...");
        
        Group title = CustomBitmapFont.getInstance()
            .createText("JOC DEL PINGU", 180, 40, 4);
        
        System.out.println("Text creat amb " + title.getChildren().size() + " caracters");
        
        rootPane.getChildren().add(title);
    }

}