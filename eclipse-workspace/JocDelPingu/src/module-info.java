/**
 * 
 */
/**
 * 
 */
module JocDelPingu {
	requires org.yaml.snakeyaml;
	requires java.sql;

	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;

	opens gamePanel to javafx.fxml;

	exports gamePanel;
}