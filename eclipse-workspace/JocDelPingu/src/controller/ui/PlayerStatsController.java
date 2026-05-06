package controller.ui;

import model.config.Lang;
import model.config.LangConfig;
import model.game.SaveLoadService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PlayerStatsController {

    @FXML private StackPane rootPane;
    @FXML private Text titleText;
    @FXML private VBox statsContainer;
    @FXML private Label noDataLabel;
    @FXML private Label headerPlayer;
    @FXML private Label headerColour;
    @FXML private Label headerPlayed;
    @FXML private Label headerWon;

    @FXML
    public void initialize() {
        LangConfig.addLanguageChangeListener(this::refreshTexts);
        refreshTexts();
        loadStats();
    }

    private void refreshTexts() {
        titleText.setText(LangConfig.getLang(Lang.STATS_TITLE));
        headerPlayer.setText(LangConfig.getLang(Lang.STATS_PLAYER));
        headerColour.setText(LangConfig.getLang(Lang.STATS_COLOUR));
        headerPlayed.setText(LangConfig.getLang(Lang.STATS_GAMES_PLAYED));
        headerWon.setText(LangConfig.getLang(Lang.STATS_GAMES_WON));
        noDataLabel.setText(LangConfig.getLang(Lang.STATS_NO_DATA));
    }

    private void loadStats() {
        statsContainer.getChildren().clear();

        ArrayList<LinkedHashMap<String, String>> stats = SaveLoadService.getPlayerStats();

        if (stats.isEmpty()) {
            noDataLabel.setVisible(true);
            noDataLabel.setManaged(true);
            return;
        }

        noDataLabel.setVisible(false);
        noDataLabel.setManaged(false);

        int rank = 1;
        for (LinkedHashMap<String, String> row : stats) {
            String name = row.get("PLAYERNAME");
            String colour = row.get("COLOUR");
            String played = row.get("GAMES_PLAYED");
            String won = row.get("GAMES_WON");

            HBox rowBox = new HBox(10);
            rowBox.setAlignment(Pos.CENTER);
            rowBox.setStyle(
                "-fx-padding: 12; " +
                "-fx-background-color: rgba(255,255,255,0.15); " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-border-color: rgba(255,255,255,0.2); " +
                "-fx-border-width: 1;"
            );

            // Rank medal
            String medal = rank == 1 ? "🥇 " : rank == 2 ? "🥈 " : rank == 3 ? "🥉 " : rank + ". ";

            Label nameLabel = new Label(medal + (name != null ? name : "?"));
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15; -fx-min-width: 180;");

            // Colour swatch
            String safeColour = (colour != null && colour.length() >= 6) ? colour : "FFFFFF";
            Label colourLabel = new Label("■ " + safeColour);
            colourLabel.setStyle("-fx-text-fill: #" + safeColour + "; -fx-font-size: 15; -fx-min-width: 100;");

            Label playedLabel = new Label(played != null ? played : "0");
            playedLabel.setStyle("-fx-text-fill: #a0d8ef; -fx-font-size: 15; -fx-min-width: 140; -fx-alignment: center;");

            Label wonLabel = new Label(won != null ? won : "0");
            wonLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 15; -fx-min-width: 120; -fx-alignment: center;");

            rowBox.getChildren().addAll(nameLabel, colourLabel, playedLabel, wonLabel);
            statsContainer.getChildren().add(rowBox);
            rank++;
        }
    }

    @FXML
    private void handleBack() {
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
