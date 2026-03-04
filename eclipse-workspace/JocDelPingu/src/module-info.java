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

    exports gamePanel;
    exports com.fontgenerator;
}