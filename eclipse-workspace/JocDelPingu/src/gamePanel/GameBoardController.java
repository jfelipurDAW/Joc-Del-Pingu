package gamePanel;

import board.Board;
import board.SquareType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public class GameBoardController {

    @FXML
    private GridPane grid;

    private Board gameBoard;

    @FXML
    public void initialize() {
    	gameBoard = new Board();
    	gameBoard.createNewBoard();

        drawBoard();
    }

    private void drawBoard() {
        int cols = Board.widthBoard;
        int rows = Board.heightBoard;

        // ==========================================
        // PART 1: RESPONSIVE AND SQUARED BOARD (1:1)
        // ==========================================
        
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        DoubleBinding midaCella = Bindings.createDoubleBinding(
            () -> Math.min(grid.getWidth() / cols, grid.getHeight() / rows),
            grid.widthProperty(), 
            grid.heightProperty()
        );

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.prefWidthProperty().bind(midaCella);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.prefHeightProperty().bind(midaCella);
            grid.getRowConstraints().add(row);
        }

        grid.setAlignment(javafx.geometry.Pos.CENTER);


        // ==========================================
        // PART 2: FOLLOW THE "S" PATTERN ON DRAW
        // ==========================================
        
        for (int i = 0; i < Board.MAX_SQUARES; i++) {
            
            int row = i / cols; 
            int col;
            
            if (row % 2 == 0) {
                col = i % cols; // Even Row: left to right
            } else {
                col = (cols - 1) - (i % cols); // Odd Row: right to left
            }

            StackPane cell = new StackPane();
            cell.getStyleClass().add("square");

            switch (gameBoard.getSquareType(i)) {
                case NORMAL: cell.getStyleClass().add("square-normal"); break;
                case ICE_HOLE: cell.getStyleClass().add("square-ice-hole"); break;
                case SLED: cell.getStyleClass().add("square-sled"); break;
                case BEAR: cell.getStyleClass().add("square-bear"); break;
                case EVENT: cell.getStyleClass().add("square-event"); break;
                case BROKEN_FLOOR: cell.getStyleClass().add("square-broken-floor"); break;
                case START: cell.getStyleClass().add("square-start"); break;
                case END: cell.getStyleClass().add("square-end");
            }

            boolean isLeft  = (col == 0);
            boolean isRight = (col == cols - 1);
            boolean isEven  = (row % 2 == 0);
            boolean isOdd   = (row % 2 != 0);
            
            if (gameBoard.getSquareType(i) != SquareType.START && gameBoard.getSquareType(i) != SquareType.END) {
            	if (isLeft) {
            		if (isEven) {
            			cell.getStyleClass().add("square-bottom-left");
            		}   
            		else if (isOdd) {
            			cell.getStyleClass().add("square-top-left");
            		} 
            	}
            	else if (isRight) {
            		if (isOdd) {
            			cell.getStyleClass().add("square-bottom-right");
            		}   
            		else if (isEven) {
            			cell.getStyleClass().add("square-top-right");
            		} 
            	}
            }

            grid.add(cell, col, row);
        }
    }
}