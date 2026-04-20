/**
 * 
 */
module JocDelPingu {
    requires org.yaml.snakeyaml;
    requires java.sql;
    requires java.desktop;

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
	requires javafx.base;

    opens controller.main to javafx.graphics, javafx.fxml;
    opens controller.ui to javafx.fxml, javafx.graphics;
    opens view.ui to javafx.fxml, javafx.graphics;
    opens view.fxml to javafx.fxml, javafx.graphics;
    opens assets.css to javafx.graphics, javafx.fxml;
    
    exports controller.main;
    exports controller.ui;
    exports view.ui;
    
    exports model.board;
    exports model.board.squares;
    exports model.config;
    exports model.db;
    exports model.entity;
    exports model.game;
    exports model.item;
    exports model.item.objects;
}