package CustomBitmapFont;


import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CustomBitmapFont {

    // ============= SINGLETON =============
    private static CustomBitmapFont instance;
    
    public static CustomBitmapFont getInstance() {
        if (instance == null) {
            instance = new CustomBitmapFont();
        }
        return instance;
    }
    // =====================================

    private final Image fontSheet;
    private final Map<Character, Rectangle2D> glyphViewports = new HashMap<>();
    private final double charWidth;
    private final double charHeight;
    private final int columns;
    private final String charset;
    private final double advanceX;

    /**
     * Constructor PRIVAT per Singleton
     */
    private CustomBitmapFont() {
        this(
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLM" +
            "NOPQRSTUVWXYZ[\\]^_`{|}~abcdefghijklmnopqrstuvwxyz",
            16,
            48.0,
            64.0,
            0.0,
            0.0
        );
    }

    /**
     * Constructor intern (cridat pel constructor privat)
     */
    private CustomBitmapFont(String charset, int columns,
                            double charW, double charH,
                            double spacingX, double spacingY) {

        String resourcePath = "/assets/font/title.png";

        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException(
                "No es troba la imatge: " + resourcePath + "\n" +
                "Comprova:\n" +
                "  • src/assets/font/title.png existeix\n" +
                "  • Nom exacte (majúscules/minúscules)\n" +
                "  • Projecte fet Refresh (F5 a Eclipse)"
            );
        }

        this.fontSheet = new Image(is);

        try {
            is.close();
        } catch (Exception ignored) {
        }

        if (fontSheet.isError() || fontSheet.getWidth() <= 0 || fontSheet.getHeight() <= 0) {
            throw new RuntimeException("Error greu carregant imatge: " + resourcePath);
        }

        this.charset    = charset;
        this.columns    = columns;
        this.charWidth  = charW;
        this.charHeight = charH;
        this.advanceX   = charW + spacingX;

        // Construcció dels viewports
        for (int i = 0; i < charset.length(); i++) {
            char c = charset.charAt(i);
            int col = i % columns;
            int row = i / columns;

            double x = col * (charW + spacingX);
            double y = row * (charH + spacingY);

            glyphViewports.put(c, new Rectangle2D(x, y, charW, charH));
        }

        // Glyph per defecte per caràcters desconeguts
        if (!glyphViewports.containsKey(' ')) {
            glyphViewports.put(' ', new Rectangle2D(0, 0, charW, charH));
        }
    }

    /**
     * Genera un Group amb el text dibuixat com a imatges
     */
    public Group createText(String text, double startX, double startY, double scale) {
        Group group = new Group();
        double currX = startX;
        double currY = startY;
        double scaledAdvance = advanceX * scale;
        double scaledHeight  = charHeight * scale;

        for (char ch : text.toCharArray()) {
            if (ch == '\n') {
                currX = startX;
                currY += scaledHeight * 1.1;
                continue;
            }

            Rectangle2D viewport = glyphViewports.getOrDefault(ch, glyphViewports.get(' '));
            if (viewport == null) {
                currX += scaledAdvance;
                continue;
            }

            ImageView iv = new ImageView(fontSheet);
            iv.setViewport(viewport);
            iv.setLayoutX(currX);
            iv.setLayoutY(currY);
            iv.setFitWidth(charWidth * scale);
            iv.setFitHeight(charHeight * scale);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);

            group.getChildren().add(iv);

            currX += scaledAdvance;
        }

        return group;
    }

    public Image getFontSheet() {
        return fontSheet;
    }
}