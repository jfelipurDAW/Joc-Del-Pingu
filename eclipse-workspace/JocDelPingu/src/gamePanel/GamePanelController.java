package gamePanel;

import config.Lang;
import config.LangConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GamePanelController {
	
	@FXML
	private Button exit;
	
	@FXML
    public void initialize() {
//	    LangConfig.loadLang(); 

		LangConfig.loadLang();
        exit.setText(LangConfig.getLang(Lang.ENTITY_SEAL));
     
	}
	
    @FXML
    private void exit() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

}