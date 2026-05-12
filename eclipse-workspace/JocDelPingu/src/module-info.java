/**
 * Java 9+ module descriptor for the "Joc del Pingu" board game.
 *
 * <p>This file declares the third-party libraries the game depends on,
 * the JavaFX modules required for the UI, and the packages that must be
 * either {@code exported} (so other modules can use them) or {@code opened}
 * (so JavaFX's FXML loader is allowed to access them via reflection).</p>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>{@code requires org.yaml.snakeyaml} - used to read the i18n YAML
 *       files that drive the in-game language switcher.</li>
 *   <li>{@code requires java.sql} - the Oracle JDBC driver lives in the
 *       classpath; the JDBC API itself comes from this module.</li>
 *   <li>The {@code opens} clauses must include {@code javafx.fxml} so that
 *       {@code @FXML} fields and event handlers can be wired through
 *       reflection - without it the controllers would fail to load.</li>
 * </ul>
 */
module JocDelPingu {

    // --- Third-party / JDK dependencies ---
    requires org.yaml.snakeyaml;
    requires java.sql;
    requires java.desktop;

    // --- JavaFX modules used across the UI layer ---
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.base;
    requires javafx.media;

    // --- Reflective access required by FXMLLoader ---
    // FXMLLoader instantiates controllers and injects @FXML fields using
    // reflection, so each package containing such classes must be opened
    // to the JavaFX modules that perform the access.
    opens controller.main to javafx.graphics, javafx.fxml;
    opens controller.ui to javafx.fxml, javafx.graphics;
    opens view.ui to javafx.fxml, javafx.graphics;
    opens view.fxml to javafx.fxml, javafx.graphics;
    opens assets.css to javafx.graphics, javafx.fxml;

    // --- Exported packages ---
    // Controllers and shared model packages are exported so they can be
    // referenced from FXML files and from other modules at compile time.
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
