/**
 * 
 */
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

    opens gamePanel to javafx.fxml;
    opens com.fontgenerator to javafx.graphics, javafx.fxml;
    opens entity to javafx.fxml;
    opens board to javafx.fxml;
    opens ObjectManagers to javafx.fxml;

    exports gamePanel;
    exports com.fontgenerator;
    exports entity;
    exports board;
}