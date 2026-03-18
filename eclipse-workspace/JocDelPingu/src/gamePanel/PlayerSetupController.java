package gamePanel;

import entity.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

import config.Lang;
import config.LangConfig;

public class PlayerSetupController {

    @FXML
    private StackPane rootPane;

    @FXML
    private ComboBox<Integer> numPlayersCombo;

    @FXML
    private CheckBox sealCheckBox;

    @FXML
    private VBox playersContainer;
    
    @FXML
    private Label player_number_select;

    @FXML
    private Label enable_seal_checkbox;

    @FXML
    private Text titleText;

    private List<PlayerInput> playerInputs = new ArrayList<>();

    @FXML
    public void initialize() {
    	titleText.setText((String) LangConfig.getLang(Lang.TEXT_SETUP_TITLE));
    	player_number_select.setText((String) LangConfig.getLang(Lang.GAMESETUP_TEXT_PLAYERNUMBER));
    	enable_seal_checkbox.setText((String) LangConfig.getLang(Lang.GAMESETUP_TEXT_ENABLESEAL));
    	
        numPlayersCombo.getItems().addAll(1, 2, 3, 4);
        numPlayersCombo.setValue(2); // Default to 2 players
        sealCheckBox.setSelected(false); // Default seal unchecked

        numPlayersCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePlayerFields(newVal);
            }
        });

        updatePlayerFields(2);
    }

    private void updatePlayerFields(int numPlayers) {
        playersContainer.getChildren().clear();
        playerInputs.clear();

        for (int i = 1; i <= numPlayers; i++) {
            PlayerInput playerInput = createPlayerInput(i);
            playerInputs.add(playerInput);
            playersContainer.getChildren().add(playerInput.getVBox());
        }
    }

    private PlayerInput createPlayerInput(int playerNumber) {
        VBox playerVBox = new VBox();
        playerVBox.setSpacing(8);
        playerVBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");

        Label label = new Label("Player " + playerNumber);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Player name");
        nameField.setPrefWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);

        TextField colorField = new TextField();
        colorField.setPromptText("Hex color (e.g., FF0000)");
        colorField.setPrefWidth(200);

        playerVBox.getChildren().addAll(label, nameField, passwordField, colorField);

        return new PlayerInput(playerNumber, nameField, passwordField, colorField, playerVBox);
    }

    @FXML
    private void handleStartGame() {
        List<Player> players = new ArrayList<>();

        for (PlayerInput input : playerInputs) {
            String name = input.nameField.getText().trim();
            String password = input.passwordField.getText();
            String color = input.colorField.getText().trim();

            if (!name.isEmpty() && !color.isEmpty()) {
                Player player = new Player(name, color);
                player.setPassword(password);
                players.add(player);
            }
        }

        if (players.isEmpty()) {
            System.out.println("No valid players entered!");
            return;
        }

        // Store configuration
        GameSetupConfig.setPlayers(players);
        GameSetupConfig.setSealEnabled(sealCheckBox.isSelected());

        // Load gameBoard.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameBoard.fxml"));
            Parent gameBoardRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene gameScene = new Scene(gameBoardRoot);
            stage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class for player input management
    private static class PlayerInput {
        int playerNumber;
        TextField nameField;
        PasswordField passwordField;
        TextField colorField;
        VBox vbox;

        PlayerInput(int playerNumber, TextField nameField, PasswordField passwordField, TextField colorField, VBox vbox) {
            this.playerNumber = playerNumber;
            this.nameField = nameField;
            this.passwordField = passwordField;
            this.colorField = colorField;
            this.vbox = vbox;
        }

        VBox getVBox() {
            return vbox;
        }
    }
}
