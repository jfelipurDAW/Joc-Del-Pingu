package gamePanel;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GamePanelController {
	
	@FXML
	private Button exit;
	
    @FXML
    private void exit() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

}