package controller.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import model.game.DebugConsoleService;


/**
 * Controller of the in-game debug console window (debugConsole.fxml).
 *
 * <p>This is a fully decoupled window: it can be opened from the main menu
 * before any game exists, kept open while several games come and go, or
 * closed without disturbing the running game. All it does is forward typed
 * lines to {@link DebugConsoleService} and print the response back.</p>
 */
public class DebugConsoleController {


    /////////////////////////////
    ///    FXML FIELDS        ///
    /////////////////////////////

    @FXML private TextArea  outputArea;
    @FXML private TextField inputField;
    @FXML private Button    sendButton;


    /////////////////////////////
    ///    INPUT HISTORY      ///
    /////////////////////////////

    private final List<String> history = new ArrayList<>();
    private int historyIndex = 0;


    /////////////////////////////
    ///    INITIALIZATION     ///
    /////////////////////////////

    @FXML
    public void initialize() {
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-control-inner-background: #0e1620; -fx-font-family: 'Consolas','Courier New',monospace; -fx-text-fill: #e0f0ff;");

        inputField.setStyle("-fx-font-family: 'Consolas','Courier New',monospace; -fx-background-color: #1a2330; -fx-text-fill: #e0f0ff;");
        inputField.setPromptText("Type a command, e.g. /help");

        // Plug ourselves in so background log() calls from the service or
        // the active GameBoardController land in this window.
        DebugConsoleService.getInstance().setOutputSink(this::appendLine);

        appendLine("=== Joc del Pingu - Debug Console ===");
        appendLine("Type /help for a list of commands. Closing this window does not affect the game.");
        appendLine("");

        // Up/Down navigates the command history.
        inputField.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.UP) {
                if (!history.isEmpty()) {
                    historyIndex = Math.max(0, historyIndex - 1);
                    inputField.setText(history.get(historyIndex));
                    inputField.positionCaret(inputField.getText().length());
                }
                ev.consume();
            } else if (ev.getCode() == KeyCode.DOWN) {
                if (!history.isEmpty()) {
                    historyIndex = Math.min(history.size(), historyIndex + 1);
                    inputField.setText(historyIndex == history.size() ? "" : history.get(historyIndex));
                    inputField.positionCaret(inputField.getText().length());
                }
                ev.consume();
            }
        });

        // Make sure that when the window is closed via the X, we detach
        // the output sink so we don't keep writing into a destroyed UI.
        // The Stage isn't known yet at initialize time; the helper below
        // wires it the first time the scene is shown.
        inputField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setOnCloseRequest(e -> {
                    DebugConsoleService.getInstance().setOutputSink(null);
                });
            }
        });
    }


    /////////////////////////////
    ///     EVENT HANDLERS    ///
    /////////////////////////////

    /** Triggered by the Send button OR by hitting Enter on the input field. */
    @FXML
    private void handleSend(ActionEvent event) {
        sendCurrentInput();
    }

    @FXML
    private void handleInputEnter(ActionEvent event) {
        sendCurrentInput();
    }


    /////////////////////////////
    ///        HELPERS        ///
    /////////////////////////////

    private void sendCurrentInput() {
        String text = inputField.getText();
        if (text == null) return;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return;

        appendLine("> " + trimmed);
        String response = DebugConsoleService.getInstance().executeCommand(trimmed);
        if (response != null && !response.isEmpty()) {
            appendLine(response);
        }
        appendLine("");

        // History bookkeeping: avoid storing consecutive duplicates.
        if (history.isEmpty() || !history.get(history.size() - 1).equals(trimmed)) {
            history.add(trimmed);
        }
        historyIndex = history.size();

        inputField.clear();
        inputField.requestFocus();
    }

    private void appendLine(String line) {
        outputArea.appendText(line + "\n");
    }
}
