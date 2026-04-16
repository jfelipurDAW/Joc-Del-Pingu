package view.font.generator;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class BitmapFontGenerator extends Application {
    
    private Image sourceImage;
    private List<DetectedChar> detectedChars = new ArrayList<>();
    private int currentCharIndex = 0;
    
    private Canvas previewCanvas;
    private TextField charInput;
    private Label statusLabel;
    private Label progressLabel;
    private Spinner<Integer> columnsSpinner;
    private Spinner<Integer> spacingXSpinner;
    private Spinner<Integer> spacingYSpinner;
    
    private static class DetectedChar {
        Image image;
        CharBounds bounds;
        String assignedChar;
        
        DetectedChar(Image img, CharBounds b) {
            this.image = img;
            this.bounds = b;
            this.assignedChar = null;
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bitmap Font Generator per CustomBitmapFont");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        
        HBox topControls = new HBox(10);
        topControls.setPadding(new Insets(0, 0, 15, 0));
        
        Button loadButton = new Button("Carregar Imatge");
        Button detectButton = new Button("Detectar Characters");
        Button generateButton = new Button("Generar Sprite Sheet");
        
        detectButton.setDisable(true);
        generateButton.setDisable(true);
        
        topControls.getChildren().addAll(loadButton, detectButton, generateButton);
        
        previewCanvas = new Canvas(500, 500);
        previewCanvas.setStyle("-fx-border-color: #333; -fx-border-width: 2;");
        
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setMinWidth(300);
        rightPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        progressLabel = new Label("Cap caracter detectat");
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label instructionLabel = new Label("Introdueix el caracter:");
        instructionLabel.setStyle("-fx-font-size: 12px;");
        
        charInput = new TextField();
        charInput.setMaxWidth(100);
        charInput.setPromptText("A, B, 1...");
        charInput.setDisable(true);
        charInput.setStyle("-fx-font-size: 18px; -fx-alignment: center;");
        
        Button nextButton = new Button("Seguent");
        Button prevButton = new Button("Anterior");
        nextButton.setDisable(true);
        prevButton.setDisable(true);
        
        HBox navButtons = new HBox(10, prevButton, nextButton);
        navButtons.setAlignment(Pos.CENTER);
        
        statusLabel = new Label("Carrega una imatge per començar");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        Separator sep1 = new Separator();
        Label configLabel = new Label("Configuracio Sprite Sheet");
        configLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        Label colLabel = new Label("Columnes:");
        columnsSpinner = new Spinner<>(5, 20, 16);
        columnsSpinner.setEditable(true);
        columnsSpinner.setMaxWidth(80);
        
        Label spXLabel = new Label("Spacing X:");
        spacingXSpinner = new Spinner<>(0, 10, 1);
        spacingXSpinner.setEditable(true);
        spacingXSpinner.setMaxWidth(80);
        
        Label spYLabel = new Label("Spacing Y:");
        spacingYSpinner = new Spinner<>(0, 10, 2);
        spacingYSpinner.setEditable(true);
        spacingYSpinner.setMaxWidth(80);
        
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(8);
        configGrid.add(colLabel, 0, 0);
        configGrid.add(columnsSpinner, 1, 0);
        configGrid.add(spXLabel, 0, 1);
        configGrid.add(spacingXSpinner, 1, 1);
        configGrid.add(spYLabel, 0, 2);
        configGrid.add(spacingYSpinner, 1, 2);
        
        rightPanel.getChildren().addAll(
            progressLabel,
            instructionLabel,
            charInput,
            navButtons,
            statusLabel,
            sep1,
            configLabel,
            configGrid
        );
        
        VBox leftPanel = new VBox(10, topControls, previewCanvas);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        
        root.setLeft(leftPanel);
        root.setRight(rightPanel);
        
        loadButton.setOnAction(e -> {
            loadImage(primaryStage);
            detectButton.setDisable(sourceImage == null);
        });
        
        detectButton.setOnAction(e -> {
            detectCharacters();
            if (!detectedChars.isEmpty()) {
                charInput.setDisable(false);
                nextButton.setDisable(false);
                prevButton.setDisable(false);
                generateButton.setDisable(false);
                showCurrentCharacter();
            }
        });
        
        charInput.setOnAction(e -> assignCharacter(nextButton));
        nextButton.setOnAction(e -> assignCharacter(nextButton));
        prevButton.setOnAction(e -> previousCharacter());
        
        generateButton.setOnAction(e -> generateSpriteSheet(primaryStage));
        
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        clearCanvas();
    }
    
    private void loadImage(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona la imatge amb characters");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imatges", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            try {
                sourceImage = new Image(new FileInputStream(file));
                statusLabel.setText("Imatge carregada: " + file.getName());
                drawSourceImage();
            } catch (Exception e) {
                showAlert("Error", "No s'ha pogut carregar: " + e.getMessage());
            }
        }
    }
    
    private void clearCanvas() {
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
    }
    
    private void drawSourceImage() {
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        
        double scale = Math.min(
            previewCanvas.getWidth() / sourceImage.getWidth(),
            previewCanvas.getHeight() / sourceImage.getHeight()
        ) * 0.95;
        
        double w = sourceImage.getWidth() * scale;
        double h = sourceImage.getHeight() * scale;
        double x = (previewCanvas.getWidth() - w) / 2;
        double y = (previewCanvas.getHeight() - h) / 2;
        
        gc.drawImage(sourceImage, x, y, w, h);
    }
    
    private void detectCharacters() {
        if (sourceImage == null) return;
        
        detectedChars.clear();
        currentCharIndex = 0;
        
        CharacterDetector detector = new CharacterDetector(sourceImage);
        List<ExtractedChar> extracted = detector.detectCharacters();
        
        for (ExtractedChar ec : extracted) {
            detectedChars.add(new DetectedChar(ec.image, ec.bounds));
        }
        
        if (detectedChars.isEmpty()) {
            showAlert("Atencio", "No s'han detectat characters. Comprova la imatge.");
            return;
        }
        
        statusLabel.setText("Detectats " + detectedChars.size() + " characters");
        progressLabel.setText("Caracter 1 de " + detectedChars.size());
    }
    
    private void showCurrentCharacter() {
        if (currentCharIndex < 0 || currentCharIndex >= detectedChars.size()) return;
        
        DetectedChar dc = detectedChars.get(currentCharIndex);
        
        clearCanvas();
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        
        double scale = Math.min(
            previewCanvas.getWidth() / dc.image.getWidth() * 0.7,
            previewCanvas.getHeight() / dc.image.getHeight() * 0.7
        );
        scale = Math.max(scale, 8);
        
        double w = dc.image.getWidth() * scale;
        double h = dc.image.getHeight() * scale;
        double x = (previewCanvas.getWidth() - w) / 2;
        double y = (previewCanvas.getHeight() - h) / 2;
        
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(x, y, w, h);
        gc.setFill(Color.WHITE);
        for (int i = 0; i < w / 10; i++) {
            for (int j = 0; j < h / 10; j++) {
                if ((i + j) % 2 == 0) {
                    gc.fillRect(x + i * 10, y + j * 10, 10, 10);
                }
            }
        }
        
        gc.drawImage(dc.image, x, y, w, h);
        
        progressLabel.setText(String.format("Caracter %d de %d", 
            currentCharIndex + 1, detectedChars.size()));
        
        if (dc.assignedChar != null) {
            charInput.setText(dc.assignedChar);
            statusLabel.setText("Assignat: '" + dc.assignedChar + "'");
        } else {
            charInput.setText("");
            statusLabel.setText("Esperant assignacio...");
        }
        
        charInput.requestFocus();
        charInput.selectAll();
    }
    
    private void assignCharacter(Button nextButton) {
        String input = charInput.getText().trim();
        if (input.isEmpty()) {
            showAlert("Error", "Has d'introduir un caracter!");
            return;
        }
        
        detectedChars.get(currentCharIndex).assignedChar = input;
        statusLabel.setText("Assignat: '" + input + "'");
        
        if (currentCharIndex < detectedChars.size() - 1) {
            currentCharIndex++;
            showCurrentCharacter();
        } else {
            showAlert("Completat!", 
                "Tots els characters assignats!\n\n" +
                "Ara pots generar el Sprite Sheet.");
        }
    }
    
    private void previousCharacter() {
        if (currentCharIndex > 0) {
            currentCharIndex--;
            showCurrentCharacter();
        }
    }
    
    private void generateSpriteSheet(Stage stage) {
        long unassigned = detectedChars.stream()
            .filter(dc -> dc.assignedChar == null || dc.assignedChar.isEmpty())
            .count();
        
        if (unassigned > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Characters sense assignar");
            alert.setHeaderText(unassigned + " caracter(s) sense assignar");
            alert.setContentText("Vols continuar igualment?");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        
        List<DetectedChar> assigned = new ArrayList<>();
        for (DetectedChar dc : detectedChars) {
            if (dc.assignedChar != null && !dc.assignedChar.isEmpty()) {
                assigned.add(dc);
            }
        }
        
        if (assigned.isEmpty()) {
            showAlert("Error", "No hi ha cap caracter assignat!");
            return;
        }
        
        int cols = columnsSpinner.getValue();
        int spacingX = spacingXSpinner.getValue();
        int spacingY = spacingYSpinner.getValue();
        
        SpriteSheetResult result = createSpriteSheet(assigned, cols, spacingX, spacingY);
        
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Sprite Sheet");
        fc.setInitialFileName("title.png");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            saveResult(result, file);
        }
    }
    
    private SpriteSheetResult createSpriteSheet(List<DetectedChar> chars, 
                                                 int cols, int spacingX, int spacingY) {
        int maxW = 0, maxH = 0;
        for (DetectedChar dc : chars) {
            maxW = Math.max(maxW, (int) dc.image.getWidth());
            maxH = Math.max(maxH, (int) dc.image.getHeight());
        }
        
        int rows = (int) Math.ceil((double) chars.size() / cols);
        
        int sheetWidth = cols * maxW + (cols - 1) * spacingX;
        int sheetHeight = rows * maxH + (rows - 1) * spacingY;
        
        WritableImage sheet = new WritableImage(sheetWidth, sheetHeight);
        
        StringBuilder charset = new StringBuilder();
        
        for (int i = 0; i < chars.size(); i++) {
            DetectedChar dc = chars.get(i);
            int col = i % cols;
            int row = i / cols;
            
            int x = col * (maxW + spacingX);
            int y = row * (maxH + spacingY);
            
            for (int py = 0; py < dc.image.getHeight(); py++) {
                for (int px = 0; px < dc.image.getWidth(); px++) {
                    Color color = dc.image.getPixelReader().getColor(px, py);
                    sheet.getPixelWriter().setColor(x + px, y + py, color);
                }
            }
            
            charset.append(dc.assignedChar);
        }
        
        SpriteSheetResult result = new SpriteSheetResult();
        result.image = sheet;
        result.charset = charset.toString();
        result.columns = cols;
        result.charWidth = maxW;
        result.charHeight = maxH;
        result.spacingX = spacingX;
        result.spacingY = spacingY;
        
        return result;
    }
    
    private void saveResult(SpriteSheetResult result, File imageFile) {
        try {
            BufferedImage bImage = SwingFXUtils.fromFXImage(result.image, null);
            ImageIO.write(bImage, "png", imageFile);
            
            File codeFile = new File(imageFile.getParent(), "FontCode.txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(codeFile))) {
                pw.println("// Copia aquest codi al teu projecte:");
                pw.println();
                pw.println("// 1. Col·loca 'title.png' a: src/assets/font/title.png");
                pw.println("// 2. Usa aquest codi per crear la font:");
                pw.println();
                pw.println("String charset = \"" + escapeJava(result.charset) + "\";");
                pw.println("int columns = " + result.columns + ";");
                pw.println("double charW = " + result.charWidth + ";");
                pw.println("double charH = " + result.charHeight + ";");
                pw.println("double spacingX = " + result.spacingX + ";");
                pw.println("double spacingY = " + result.spacingY + ";");
                pw.println();
                pw.println("CustomBitmapFont font = new CustomBitmapFont(");
                pw.println("    charset, columns, charW, charH, spacingX, spacingY");
                pw.println(");");
                pw.println();
                pw.println("// Exemple d'us:");
                pw.println("Group text = font.createText(\"HOLA MON\", 100, 100, 2.0);");
                pw.println("root.getChildren().add(text);");
            }
            
            showAlert("Exit!", 
                "Sprite Sheet guardat: " + imageFile.getName() + "\n" +
                "Codi generat: FontCode.txt\n\n" +
                "Charset: " + result.charset.length() + " characters\n" +
                "Mida: " + (int)result.image.getWidth() + "x" + (int)result.image.getHeight());
            
        } catch (Exception e) {
            showAlert("Error", "No s'ha pogut guardar: " + e.getMessage());
        }
    }
    
    private String escapeJava(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static class SpriteSheetResult {
        WritableImage image;
        String charset;
        int columns;
        int charWidth;
        int charHeight;
        int spacingX;
        int spacingY;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}