package view.font;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;

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
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 !?", // charset
                16,     // columns
                8.0,    // charW
                12.0,   // charH
                1.0,    // spacingX
                2.0     // spacingY
        );
    }

    /**
     * Constructor intern
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
                    " • src/assets/font/title.png existeix\n" +
                    " • Nom exacte (majúscules/minúscules)\n" +
                    " • Projecte fet Refresh (F5 a Eclipse)"
            );
        }
        this.fontSheet = new Image(is);
        try {
            is.close();
        } catch (Exception ignored) {
        }
        if (fontSheet.isError() || fontSheet.getWidth() <= 0 || fontSheet.getHeight() <= 0) {
            throw new RuntimeException("WARNING: " + resourcePath);
        }

        this.charset = charset;
        this.columns = columns;
        this.charWidth = charW;
        this.charHeight = charH;
        this.advanceX = charW + spacingX;

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
     * Genera un Group amb el text dibuixat com a imatges en un Canvas (pixel-perfect amb nearest neighbor)
     */
    public Group createText(String text, double startX, double startY, double scale) {
        int len = text.length();
        double scaledCharW = charWidth * scale;
        double scaledCharH = charHeight * scale;
        double scaledAdvance = advanceX * scale;
        double totalWidth = len * scaledAdvance;
        double totalHeight = scaledCharH;

        Canvas canvas = new Canvas(totalWidth, totalHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Desactiva interpolació → nearest neighbor real
        gc.setImageSmoothing(false);

        double currX = 0;
        for (char ch : text.toCharArray()) {
            if (ch == '\n') {
                currX = 0;
                // Per multi-línia: afegeix currY += scaledCharH * 1.1; aquí si cal
                continue;
            }

            Rectangle2D viewport = glyphViewports.getOrDefault(ch, glyphViewports.get(' '));
            if (viewport == null) {
                currX += scaledAdvance;
                continue;
            }

            gc.drawImage(
                    fontSheet,
                    viewport.getMinX(), viewport.getMinY(), viewport.getWidth(), viewport.getHeight(),
                    currX, 0, scaledCharW, scaledCharH
            );

            currX += scaledAdvance;
        }

        // Embolica en Group per posicionar
        Group group = new Group(canvas);
        canvas.setTranslateX(startX);
        canvas.setTranslateY(startY);

        // Cache per rendiment
        canvas.setCache(true);
        canvas.setCacheHint(CacheHint.SPEED);

        return group;
    }

    public Image getFontSheet() {
        return fontSheet;
    }
}