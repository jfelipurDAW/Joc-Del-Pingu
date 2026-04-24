package controller.ui;

import model.entity.Player;
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
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.config.GameSetupConfig;
import model.config.Lang;
import model.config.LangConfig;

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
        LangConfig.addLanguageChangeListener(this::refreshTexts);
        refreshTexts();
        
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

        Label label = new Label(LangConfig.getLang(Lang.GAMESETUP_PLAYER) + playerNumber);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PLAYERNAME));
        nameField.setPrefWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PASSWORD));
        passwordField.setPrefWidth(200);

        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(javafx.scene.paint.Color.RED);
        colorPicker.setPrefWidth(200);

        // Workaround for color picker minimizing issue
        colorPicker.setOnAction((javafx.event.ActionEvent event) -> {
            if (rootPane != null) rootPane.requestFocus();
        });
        colorPicker.setOnHiding((javafx.event.Event event) -> {
            if (rootPane != null) rootPane.requestFocus();
        });

        // Avatar Selection
        Button avatarBtn = new Button("Choose Avatar Image");
        avatarBtn.setPrefWidth(200);
        avatarBtn.getStyleClass().add("player-setup-field");
        final String[] avatarPath = new String[1];
        
        avatarBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Avatar Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
            if (selectedFile != null) {
                avatarPath[0] = selectedFile.toURI().toString();
                avatarBtn.setText("Avatar Selected");
            }
        });

        playerVBox.getChildren().addAll(label, nameField, passwordField, colorPicker, avatarBtn);

        return new PlayerInput(playerNumber, nameField, passwordField, colorPicker, avatarPath, playerVBox);
    }

    @FXML
    private void handleStartGame() {
        List<Player> players = new ArrayList<>();

        for (PlayerInput input : playerInputs) {
            String name = input.nameField.getText().trim();
            String password = input.passwordField.getText();
            String color = input.colorPicker.getValue().toString().substring(2, 8).toUpperCase();

            if (!name.isEmpty() && !color.isEmpty()) {
                Player player = new Player(name, color);
                player.setPassword(password);
                if (input.avatarPath[0] != null) {
                    player.setAvatarPath(input.avatarPath[0]);
                }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/gameBoard.fxml"));
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
        javafx.scene.control.ColorPicker colorPicker;
        String[] avatarPath;
        VBox vbox;

        PlayerInput(int playerNumber, TextField nameField, PasswordField passwordField, javafx.scene.control.ColorPicker colorPicker, String[] avatarPath, VBox vbox) {
            this.playerNumber = playerNumber;
            this.nameField = nameField;
            this.passwordField = passwordField;
            this.colorPicker = colorPicker;
            this.avatarPath = avatarPath;
            this.vbox = vbox;
        }

        VBox getVBox() {
            return vbox;
        }
    }

    /**
     * Refreshes all texts dynamically when the language changes.
     */
    private void refreshTexts() {
        titleText.setText((String) LangConfig.getLang(Lang.TEXT_SETUP_TITLE));
        player_number_select.setText((String) LangConfig.getLang(Lang.GAMESETUP_TEXT_PLAYERNUMBER));
        enable_seal_checkbox.setText((String) LangConfig.getLang(Lang.GAMESETUP_TEXT_ENABLESEAL));
        
        for (PlayerInput input : playerInputs) {
            if (input.vbox.getChildren().size() > 0) {
                javafx.scene.Node node = input.vbox.getChildren().get(0);
                if (node instanceof Label) {
                    ((Label) node).setText(LangConfig.getLang(Lang.GAMESETUP_PLAYER) + " " + input.playerNumber);
                }
                input.nameField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PLAYERNAME));
                input.passwordField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PASSWORD));
            }
        }
    }
}
