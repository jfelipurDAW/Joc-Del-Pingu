package gamePanel;

import config.Lang;
import config.LangConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class GamePanelController {
	
	@FXML
	private Button newGame_button;
	private Button loadGame_button;
	private Button language_button;
	
	@FXML
    public void initialize() {
		LangConfig.loadLang();
		
        newGame_button.setText(LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));
        loadGame_button.setText(LangConfig.getLang(Lang.MENU_BUTTON_LOADGAME));
        language_button.setText(LangConfig.getLang(Lang.MENU_BUTTON_LANGUAGE));
        
     
	}
	
    @FXML
    private void newGame_button() {
        Stage stage = (Stage) newGame_button.getScene().getWindow();
        stage.close();
    }

}