package view.font.generator;

public class CharBounds {
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    
    public int getWidth() {
        return maxX - minX + 1;
    }
    
    public int getHeight() {
        return maxY - minY + 1;
    }
}