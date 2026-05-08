package controller.ui;

import model.entity.Player;
import model.game.SaveLoadService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.config.GameSetupConfig;
import model.config.Lang;
import model.config.LangConfig;

public class PlayerSetupController {

    @FXML private StackPane rootPane;
    @FXML private ComboBox<Integer> numPlayersCombo;
    @FXML private CheckBox sealCheckBox;
    @FXML private VBox playersContainer;
    @FXML private Label player_number_select;
    @FXML private Label enable_seal_checkbox;
    @FXML private Text titleText;
    @FXML private Button backButton;
    @FXML private Button selectExistingButton;
    @FXML private Button startGameButton;

    private List<PlayerInput> playerInputs = new ArrayList<>();

    @FXML
    public void initialize() {
        LangConfig.addLanguageChangeListener(this::refreshTexts);
        refreshTexts();

        numPlayersCombo.getItems().addAll(1, 2, 3, 4);
        numPlayersCombo.setValue(2);
        sealCheckBox.setSelected(false);

        numPlayersCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updatePlayerFields(newVal);
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
        playerVBox.getStyleClass().add("setup-card");

        Label label = new Label(LangConfig.getLang(Lang.GAMESETUP_PLAYER) + playerNumber);
        label.getStyleClass().add("setup-card-title");

        TextField nameField = new TextField();
        nameField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PLAYERNAME));
        nameField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PASSWORD));
        passwordField.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(javafx.scene.paint.Color.RED);
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        colorPicker.setOnAction((javafx.event.ActionEvent event) -> {
            if (rootPane != null) rootPane.requestFocus();
        });
        colorPicker.setOnHiding((javafx.event.Event event) -> {
            if (rootPane != null) rootPane.requestFocus();
        });

        Button avatarBtn = new Button(LangConfig.getLang(Lang.GAMESETUP_CHOOSEAVATAR));
        avatarBtn.setMaxWidth(Double.MAX_VALUE);
        avatarBtn.getStyleClass().add("player-setup-field");
        final String[] avatarPath = new String[1];

        avatarBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(LangConfig.getLang(Lang.GAMESETUP_FILECHOOSER_TITLE));
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
            if (selectedFile != null) {
                avatarPath[0] = selectedFile.toURI().toString();
                avatarBtn.setText(LangConfig.getLang(Lang.GAMESETUP_AVATARSELECTED));
            }
        });

        playerVBox.getChildren().addAll(label, nameField, passwordField, colorPicker, avatarBtn);

        return new PlayerInput(playerNumber, nameField, passwordField, colorPicker, avatarPath, avatarBtn, playerVBox);
    }

    @FXML
    private void handleStartGame() {
        GameSetupConfig.setLoadedGame(false);
        List<Player> players = new ArrayList<>();

        for (PlayerInput input : playerInputs) {
            String name = input.nameField.getText().trim();
            String password = input.passwordField.getText();
            String color = input.colorPicker.getValue().toString().substring(2, 8).toUpperCase();

            if (!name.isEmpty() && !color.isEmpty()) {
                if (!SaveLoadService.verifyPassword(name, password)) {
                    mostrarAlerta(
                        LangConfig.getLang(Lang.ALERT_WRONGPASSWORD_TITLE),
                        String.format(LangConfig.getLang(Lang.ALERT_WRONGPASSWORD_MESSAGE), name)
                    );
                    return;
                }

                Player player = new Player(name, color);
                player.setPassword(password);
                if (input.avatarPath[0] != null) {
                    player.setAvatarPath(input.avatarPath[0]);
                }
                players.add(player);

                SaveLoadService.registerPlayer(name, password, color);
            }
        }

        if (players.isEmpty()) {
            return;
        }

        GameSetupConfig.setPlayers(players);
        GameSetupConfig.setSealEnabled(sealCheckBox.isSelected());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/gameBoard.fxml"));
            Parent gameBoardRoot = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(gameBoardRoot));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PlayerInput {
        int playerNumber;
        TextField nameField;
        PasswordField passwordField;
        javafx.scene.control.ColorPicker colorPicker;
        String[] avatarPath;
        Button avatarBtn;
        VBox vbox;

        PlayerInput(int playerNumber, TextField nameField, PasswordField passwordField,
                    javafx.scene.control.ColorPicker colorPicker, String[] avatarPath,
                    Button avatarBtn, VBox vbox) {
            this.playerNumber = playerNumber;
            this.nameField = nameField;
            this.passwordField = passwordField;
            this.colorPicker = colorPicker;
            this.avatarPath = avatarPath;
            this.avatarBtn = avatarBtn;
            this.vbox = vbox;
        }

        VBox getVBox() { return vbox; }
    }

    private void refreshTexts() {
        titleText.setText(LangConfig.getLang(Lang.TEXT_SETUP_TITLE));
        player_number_select.setText(LangConfig.getLang(Lang.GAMESETUP_TEXT_PLAYERNUMBER));
        enable_seal_checkbox.setText(LangConfig.getLang(Lang.GAMESETUP_TEXT_ENABLESEAL));

        if (backButton != null)
            backButton.setText(LangConfig.getLang(Lang.GAMESETUP_BUTTON_BACK));
        if (selectExistingButton != null)
            selectExistingButton.setText(LangConfig.getLang(Lang.GAMESETUP_BUTTON_SELECTPLAYER));
        if (startGameButton != null)
            startGameButton.setText(LangConfig.getLang(Lang.TEXT_GAME_STARTGAME));

        for (PlayerInput input : playerInputs) {
            if (!input.vbox.getChildren().isEmpty()) {
                javafx.scene.Node node = input.vbox.getChildren().get(0);
                if (node instanceof Label) {
                    ((Label) node).setText(LangConfig.getLang(Lang.GAMESETUP_PLAYER) + " " + input.playerNumber);
                }
            }
            input.nameField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PLAYERNAME));
            input.passwordField.setPromptText(LangConfig.getLang(Lang.GAMESETUP_PASSWORD));
            if (input.avatarPath[0] == null) {
                input.avatarBtn.setText(LangConfig.getLang(Lang.GAMESETUP_CHOOSEAVATAR));
            }
        }
    }

    @FXML
    private void handleSelectExistingPlayer() {
        List<Player> existentes = SaveLoadService.getRegisteredPlayers();

        if (existentes.isEmpty()) {
            mostrarAlerta(
                LangConfig.getLang(Lang.ALERT_NOPLAYERS_TITLE),
                LangConfig.getLang(Lang.ALERT_NOPLAYERS_MESSAGE)
            );
            return;
        }

        List<String> nombres = existentes.stream().map(Player::getName).collect(Collectors.toList());

        javafx.scene.control.ChoiceDialog<String> dialog =
            new javafx.scene.control.ChoiceDialog<>(nombres.get(0), nombres);
        dialog.setTitle(LangConfig.getLang(Lang.DIALOG_SELECTPLAYER_TITLE));
        dialog.setHeaderText(LangConfig.getLang(Lang.DIALOG_SELECTPLAYER_HEADER));

        java.util.Optional<String> result = dialog.showAndWait();

        result.ifPresent(nombreSeleccionado -> {
            for (Player p : existentes) {
                if (p.getName().equals(nombreSeleccionado)) {
                    if (!asignarJugadorAlPrimerInputVacio(p)) {
                        mostrarAlerta(
                            LangConfig.getLang(Lang.ALERT_FULL_TITLE),
                            LangConfig.getLang(Lang.ALERT_FULL_MESSAGE)
                        );
                    }
                }
            }
        });
    }

    /**
     * Fills the first empty PlayerInput row with this player's data.
     * Returns true if a slot was found, false if all rows are full.
     * Extracted so the search can return early without a loop break.
     */
    private boolean asignarJugadorAlPrimerInputVacio(Player p) {
        for (PlayerInput input : playerInputs) {
            if (input.nameField.getText().trim().isEmpty()) {
                input.nameField.setText(p.getName());
                if (p.getPassword() != null) {
                    input.passwordField.setText(p.getPassword());
                }
                try {
                    input.colorPicker.setValue(javafx.scene.paint.Color.web("#" + p.getColour()));
                } catch (Exception e) {
                    // ignore invalid stored color
                }
                return true;
            }
        }
        return false;
    }

    private void mostrarAlerta(String title, String message) {
        javafx.scene.control.Alert alert =
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        navigateToMainMenu();
    }

    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/mainMenu.fxml"));
            Parent mainMenuRoot = loader.load();
            Scene currentScene = rootPane.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            Scene menuScene = new Scene(mainMenuRoot);
            menuScene.getStylesheets().add(
                getClass().getResource("/assets/css/style.css").toExternalForm()
            );
            stage.setScene(menuScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
