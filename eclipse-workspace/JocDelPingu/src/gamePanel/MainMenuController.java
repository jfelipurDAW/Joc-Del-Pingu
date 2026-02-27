package gamePanel;

import CustomBitmapFont.CustomBitmapFont;
import config.Lang;
import config.LangConfig;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.CacheHint;
import java.io.InputStream;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class MainMenuController {

    @FXML
    private StackPane rootPane;

    @FXML
    public void initialize() {
        

        addBackgroundImage();   // ← Afegit: fons pixel art
        addBitmapTitle();
    }

    private void addBackgroundImage() {
        String backgroundPath = "/assets/sprites/backgrounds/menu_background.png";

        try (InputStream is = getClass().getResourceAsStream(backgroundPath)) {
            if (is == null) {
                System.err.println("No trobat: " + backgroundPath);
                return;
            }

            Image bgImage = new Image(is);
            if (bgImage.isError() || bgImage.getWidth() <= 0) {
                System.err.println("Error: " + backgroundPath);
                return;
            }

            Canvas bgCanvas = new Canvas();
            bgCanvas.widthProperty().bind(rootPane.widthProperty());
            bgCanvas.heightProperty().bind(rootPane.heightProperty());

            GraphicsContext gc = bgCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);

            // Factor enter més proper (prova 3 o 4 segons com quedi)
            double scale = 4.0;  // ← canvia a 3.0 o 4.0 per provar
            double scaledW = bgImage.getWidth() * scale;
            double scaledH = bgImage.getHeight() * scale;

            // Centra la imatge escalada al Canvas
            double offsetX = (bgCanvas.getWidth() - scaledW) / 2;
            double offsetY = (bgCanvas.getHeight() - scaledH) / 2;

            // Arrodoneix offsets per evitar subpíxels (borrositat)
            offsetX = Math.round(offsetX);
            offsetY = Math.round(offsetY);

            gc.drawImage(bgImage,
                         0, 0, bgImage.getWidth(), bgImage.getHeight(),
                         offsetX, offsetY, scaledW, scaledH);

            bgCanvas.setCache(true);
            bgCanvas.setCacheHint(CacheHint.SPEED);

            rootPane.getChildren().add(0, bgCanvas);

            System.out.println("Fons amb upscale enter ×" + scale + " carregat");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBitmapTitle() {
        System.out.println("Afegint text bitmap...");

        Group title = CustomBitmapFont.getInstance()
                .createText("JOC DEL PINGU", 180, 40, 4.0);

        // Opcional: força posicions a píxels enters per evitar subpíxels
        title.setTranslateX(Math.round(title.getTranslateX()));
        title.setTranslateY(Math.round(title.getTranslateY()));

        // Opcional: cache al grup del títol
        // title.setCache(true);
        // title.setCacheHint(CacheHint.SPEED);

        System.out.println("Text creat amb " + title.getChildren().size() + " caràcters");

        rootPane.getChildren().add(title);
    }
}