package view.font.generator;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;

public class CharacterDetector {
    
    private Image sourceImage;
    private boolean[][] visited;
    private int width;
    private int height;
    private PixelReader pixelReader;
    
    public CharacterDetector(Image image) {
        this.sourceImage = image;
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
        this.pixelReader = image.getPixelReader();
        this.visited = new boolean[width][height];
    }
    
    public List<ExtractedChar> detectCharacters() {
        List<ExtractedChar> characters = new ArrayList<>();
        List<CharBounds> bounds = new ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!visited[x][y] && isCharacterPixel(x, y)) {
                    CharBounds charBounds = floodFill(x, y);
                    if (isValidCharacter(charBounds)) {
                        bounds.add(charBounds);
                    }
                }
            }
        }
        
        bounds.sort((a, b) -> {
            int rowDiff = a.minY - b.minY;
            if (Math.abs(rowDiff) > 10) {
                return rowDiff;
            }
            return a.minX - b.minX;
        });
        
        for (CharBounds cb : bounds) {
            WritableImage charImage = extractCharacterImage(cb);
            ExtractedChar ec = new ExtractedChar();
            ec.image = charImage;
            ec.bounds = cb;
            characters.add(ec);
        }
        
        return characters;
    }
    
    private boolean isCharacterPixel(int x, int y) {
        Color color = pixelReader.getColor(x, y);
        double brightness = color.getBrightness();
        return color.getOpacity() > 0.5 && brightness < 0.9;
    }
    
    private CharBounds floodFill(int startX, int startY) {
        CharBounds bounds = new CharBounds();
        bounds.minX = startX;
        bounds.maxX = startX;
        bounds.minY = startY;
        bounds.maxY = startY;
        
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;
        
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            
            bounds.minX = Math.min(bounds.minX, p.x);
            bounds.maxX = Math.max(bounds.maxX, p.x);
            bounds.minY = Math.min(bounds.minY, p.y);
            bounds.maxY = Math.max(bounds.maxY, p.y);
            
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    int nx = p.x + dx;
                    int ny = p.y + dy;
                    
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                        !visited[nx][ny] && isCharacterPixel(nx, ny)) {
                        visited[nx][ny] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }
        
        return bounds;
    }
    
    private boolean isValidCharacter(CharBounds bounds) {
        int w = bounds.maxX - bounds.minX + 1;
        int h = bounds.maxY - bounds.minY + 1;
        
        return w >= 3 && h >= 3 && w <= width / 2 && h <= height / 2;
    }
    
    private WritableImage extractCharacterImage(CharBounds bounds) {
        int w = bounds.maxX - bounds.minX + 1;
        int h = bounds.maxY - bounds.minY + 1;
        
        int padding = 2;
        int newW = w + padding * 2;
        int newH = h + padding * 2;
        
        WritableImage charImage = new WritableImage(newW, newH);
        
        for (int y = 0; y < newH; y++) {
            for (int x = 0; x < newW; x++) {
                int srcX = bounds.minX + x - padding;
                int srcY = bounds.minY + y - padding;
                
                Color color;
                if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                    color = pixelReader.getColor(srcX, srcY);
                } else {
                    color = Color.TRANSPARENT;
                }
                
                charImage.getPixelWriter().setColor(x, y, color);
            }
        }
        
        return charImage;
    }
    
    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}