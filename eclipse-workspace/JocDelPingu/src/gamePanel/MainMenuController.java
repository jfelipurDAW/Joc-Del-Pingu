package gamePanel;

import CustomBitmapFont.CustomBitmapFont;
import config.Lang;
import config.LangConfig;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

public class MainMenuController {

    @FXML
    private StackPane rootPane;

    @FXML
    public void initialize() {
        // Si tens botons o altres elements que necessitin textos traduïts:
        // newGame_button.setText(LangConfig.getLang(Lang.MENU_BUTTON_NEWGAME));

        addBitmapTitle();
    }

    private void addBitmapTitle() {
        System.out.println("Afegint text bitmap...");

        Group title = CustomBitmapFont.getInstance()
                .createText("JOC DEL PINGU", 180, 40, 4.0);

        // Opcional: força posicions a píxels enters per evitar subpíxels i borrositat addicional
        title.setTranslateX(Math.round(title.getTranslateX()));
        title.setTranslateY(Math.round(title.getTranslateY()));

        // Opcional: si vols aplicar cache al grup sencer (rendiment)
        // title.setCache(true);
        // title.setCacheHint(CacheHint.SPEED);

        System.out.println("Text creat amb " + title.getChildren().size() + " caràcters");

        rootPane.getChildren().add(title);
    }
}